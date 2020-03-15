package com.github.rudder.client;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.rudder.host.Coordinator;
import com.github.rudder.shared.HttpApp;
import com.github.rudder.shared.InvocationController;
import com.github.rudder.shared.ObjectStorage;
import com.github.rudder.shared.Util;
import io.github.classgraph.ClassGraph;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
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

    public static final String RUDDER_HOST = "rudder.host";
    /**
     * Name of the container holding application
     */
    private final String containerName;

    /**
     * Name of the docker container image
     */
    private final String imageName;

    /**
     * Class to be run inside container
     */
    private final Class<T> clazz;

    /**
     * Arguments to pass to application
     */
    private final List<String> args;

    /**
     * HTTP application that is handling RPCs
     */
    private HttpApp httpApp;

    /**
     * Docker container
     */
    private GenericContainer container;

    public ContaineredApplication(final String containerName, final String imageName, final Class<T> clazz, final List<String> args) {
        this.containerName = containerName;
        this.imageName = imageName;
        this.clazz = clazz;
        this.args = args;

//        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    /**
     * Start container with application
     *
     * @throws Exception
     */
    public void start() throws Exception {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String networkName;
        if (osName.startsWith("mac os x")) {
            networkName = "en0";
        } else {
            networkName = "docker0";
        }

        final NetworkInterface docker0 = NetworkInterface.getByName(networkName);

        final String localhostAddressForDocker = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(docker0.getInetAddresses().asIterator(), Spliterator.ORDERED),
                false
        ).filter(addr -> addr instanceof Inet4Address).findFirst().map(InetAddress::getHostAddress).orElse("");

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

        String classPathString = String.join(":", allPaths);

        final ExposedPort coordinatorControlPort = ExposedPort.tcp(9513);

        Ports portBindings = new Ports();
        portBindings.bind(coordinatorControlPort, Ports.Binding.bindPort(9513));

        final List<String> cmd = new ArrayList<>(Arrays.asList("java", "-classpath", classPathString, Coordinator.class.getName(), clazz.getName()));

        cmd.addAll(args);

        GenericContainer container = new GenericContainer<>(imageName);
        container.withExposedPorts(COORDINATOR_CONTROL_PORT);
        paths.forEach(path -> container.withFileSystemBind(path, path, BindMode.READ_ONLY));
        container.withCommand(cmd.toArray(new String[0]));
        container.withExtraHost(RUDDER_HOST, localhostAddressForDocker);
        container.start();
        this.container = container;
    }

    /**
     * Stop container and HTTP RPC app
     */
    public void stop() {
        container.stop();

        if (this.httpApp != null) {
            this.httpApp.stop();
        }
    }

    /**
     * Get application object connected to application inside container
     *
     * @return application object
     * @throws Exception
     */
    public T getApplication() throws Exception {
        Retrofit retrofit = createRetrofit("localhost", container.getMappedPort(COORDINATOR_CONTROL_PORT));

        final CoordinatorClient coordinatorClient = retrofit.create(CoordinatorClient.class);

        final ObjectStorage objectStorage = new ObjectStorage();
        this.httpApp = new HttpApp();
        httpApp.add(new InvocationController(objectStorage, coordinatorClient));
        httpApp.start();
        Testcontainers.exposeHostPorts(httpApp.getPort());


        String uid = null;
        while (Util.isEmpty(uid)) {
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

}
