package com.github.rudder.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.rudder.host.Coordinator;
import com.github.rudder.shared.HttpApp;
import com.github.rudder.shared.InvocationController;
import com.github.rudder.shared.ObjectStorage;
import io.github.classgraph.ClassGraph;
import io.netty.util.internal.SocketUtils;
import org.apache.http.util.TextUtils;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.rudder.client.Runner.createProxy;
import static com.github.rudder.client.Runner.createRetrofit;
import static com.github.rudder.host.Coordinator.COORDINATOR_CONTROL_PORT;

public class ContaineredApplication<T> {

    private final String containerName;

    private final String container;

    private final Class<T> clazz;

    private final List<String> args;

    private final DockerClient dockerClient;

    private String containerId;

    private InspectContainerResponse containerInfo;

    private HttpApp httpApp;

    public ContaineredApplication(final String containerName, final String container, final Class<T> clazz, final List<String> args) {
        this.containerName = containerName;
        this.container = container;
        this.clazz = clazz;
        this.args = args;

        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    public void start() throws Exception {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String networkName;
        if (osName.startsWith("mac os x")) {
            networkName = "en0";
        } else {
            networkName = "docker0";
        }

        final NetworkInterface docker0 = NetworkInterface.getByName(networkName);

        final String[] extraHosts = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(docker0.getInetAddresses().asIterator(), Spliterator.ORDERED),
                false
        ).filter(addr -> addr instanceof Inet4Address).findFirst()
                .map(addr -> new String[]{"jainer.host:" + addr.getHostAddress()})
                .orElse(new String[0]);

        List<URI> classpath = new ClassGraph().getClasspathURIs();

        final List<String> allPaths = classpath.stream().map(URI::getPath)
                .collect(Collectors.toList());

        final List<String> paths = new ArrayList<>(allPaths);

        final String mavenFolderPrefix = ".m2";

        paths.removeIf(path -> path.contains(mavenFolderPrefix));

        // add only single maven folder
        allPaths.stream()
                .filter(path -> path.contains(mavenFolderPrefix))
                .findFirst()
                .map(s -> {
                    Path current = Path.of(s);
                    while (!current.getFileName().toString().equals(mavenFolderPrefix)) {
                        current = current.getParent();
                    }
                    return current.toString();
                })
                .ifPresent(paths::add);

        final List<Bind> binds = paths.stream().map(path -> new Bind(path, new Volume(path), AccessMode.ro)).collect(Collectors.toList());

        String classPathString = String.join(":", allPaths);

        final ExposedPort coordinatorControlPort = ExposedPort.tcp(9513);

        Ports portBindings = new Ports();
        portBindings.bind(coordinatorControlPort, Ports.Binding.bindPort(9513));

        final List<String> cmd = new ArrayList<>(Arrays.asList("java", "-classpath", classPathString, Coordinator.class.getName(), clazz.getName()));

        cmd.addAll(args);

        CreateContainerResponse createdContainer = dockerClient
                .createContainerCmd(container)
                .withEnv()
                .withPublishAllPorts(true)
                .withCmd(cmd)
                .withName(containerName)
                .withBinds(binds)
                .withExtraHosts(extraHosts)
                .withExposedPorts(coordinatorControlPort)
                .exec();

        this.containerId = createdContainer.getId();
        dockerClient.startContainerCmd(containerId).exec();

        this.containerInfo = this.dockerClient.inspectContainerCmd(this.containerId).exec();
    }

    public void stop() {
        try {
            dockerClient.stopContainerCmd(containerName).exec();
        } catch (Exception e) {
        }

        try {
            dockerClient.removeContainerCmd(containerName).exec();
        } catch (Exception e) {
        }

        if (this.httpApp != null) {
            this.httpApp.stop();
        }
    }

    public T getApplication() throws Exception {
        Retrofit retrofit = createRetrofit("localhost", getExposedPort(COORDINATOR_CONTROL_PORT));

        final CoordinatorClient coordinatorClient = retrofit.create(CoordinatorClient.class);

        final ObjectStorage objectStorage = new ObjectStorage();
        this.httpApp = new HttpApp();
        httpApp.add("/invoke", new InvocationController(objectStorage, coordinatorClient));
        httpApp.start();


        String uid = null;
        while (TextUtils.isEmpty(uid)) {
            try {
                uid = coordinatorClient.hello(httpApp.getPort()).execute().body();
            } catch (IOException e) {
                // just wait a little bit more
                // TODO: add timeout
                Thread.sleep(200);
            }
        }
        return createProxy(coordinatorClient, objectStorage, uid, clazz);
    }



    public T getApplicationTest() throws Exception {
        Retrofit retrofit = createRetrofit("localhost", COORDINATOR_CONTROL_PORT);

        final CoordinatorClient coordinatorClient = retrofit.create(CoordinatorClient.class);

        final ObjectStorage objectStorage = new ObjectStorage();
        this.httpApp = new HttpApp();
        httpApp.add("/invoke", new InvocationController(objectStorage, coordinatorClient));
        httpApp.start();


        String uid = null;
        while (TextUtils.isEmpty(uid)) {
            try {
                uid = coordinatorClient.hello(httpApp.getPort()).execute().body();
            } catch (IOException e) {
                // just wait a little bit more
                // TODO: add timeout
                Thread.sleep(200);
            }
        }
        return createProxy(coordinatorClient, objectStorage, uid, clazz);
    }

    public Integer getExposedPort(final int originalPort) {
        Ports.Binding[] binding = new Ports.Binding[0];
        if (containerInfo != null) {
            binding = containerInfo.getNetworkSettings().getPorts().getBindings().get(new ExposedPort(originalPort));
        }

        if (binding != null && binding.length > 0 && binding[0] != null) {
            return Integer.valueOf(binding[0].getHostPortSpec());
        } else {
            throw new IllegalArgumentException("Requested port (" + originalPort + ") is not mapped");
        }
    }

}
