package com.github.rudder.client;

import com.github.rudder.host.Coordinator;
import com.github.rudder.shared.http.HttpApp;
import com.github.rudder.shared.http.InvocationController;
import com.github.rudder.shared.ObjectStorage;
import com.github.rudder.shared.Util;
import io.github.classgraph.ClassGraph;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import retrofit2.Retrofit;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.rudder.client.Runner.createProxy;
import static com.github.rudder.client.Runner.createRetrofit;
import static com.github.rudder.shared.http.HttpApp.COORDINATOR_CONTROL_PORT;

public class ContaineredApplication<T> {

    public static final String RUDDER_HOST = "rudder.host";

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

    /**
     * Operator for container changes before it was started
     */
    @Nullable
    private Consumer<GenericContainer> containerChanger;

    public ContaineredApplication(final String imageName, final Class<T> clazz, final List<String> args) {
        this.imageName = imageName;
        this.clazz = clazz;
        this.args = args;
    }

    /**
     * Start container with application
     *
     */
    public void start() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String networkName;
        if (osName.startsWith("mac os x")) {
            networkName = "en0";
        } else {
            networkName = "docker0";
        }

        final NetworkInterface docker0;
        try {
            docker0 = NetworkInterface.getByName(networkName);
        } catch (SocketException e) {
            throw new RuntimeException(String.format("Can't find docker network interface [%s]", networkName));
        }

        final String localhostAddressForDocker = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(docker0.getInetAddresses().asIterator(), Spliterator.ORDERED),
                false
        ).filter(addr -> addr instanceof Inet4Address).findFirst().map(InetAddress::getHostAddress).orElse("");

        List<URI> classpath = new ClassGraph().getClasspathURIs();

        final List<String> allPaths = classpath.stream().map(URI::getPath)
                .collect(Collectors.toList());

        String classPathString = String.join(":", allPaths);

        final List<String> paths = getVolumes(allPaths);

        final List<String> cmd = new ArrayList<>(Arrays.asList("java", "-classpath", classPathString, Coordinator.class.getName(), clazz.getName()));
        cmd.addAll(args);

        GenericContainer container = prepareContainer(localhostAddressForDocker, paths, cmd);

        container.start();
        this.container = container;
    }

    private GenericContainer prepareContainer(final String localhostAddressForDocker, final List<String> paths, final List<String> cmd) {
        GenericContainer container = new GenericContainer<>(imageName)
                .withExposedPorts(COORDINATOR_CONTROL_PORT)
                .withCommand(cmd.toArray(new String[0]))
                .withExtraHost(RUDDER_HOST, localhostAddressForDocker);

        paths.forEach(path -> container.withFileSystemBind(path, path, BindMode.READ_ONLY));

        if (containerChanger != null) {
            containerChanger.accept(container);
        }
        return container;
    }

    @NotNull
    private List<String> getVolumes(final List<String> allPaths) {
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
        return paths;
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

    public GenericContainer getContainer() {
        return container;
    }

    public void setContainerChanger(@Nullable final Consumer<GenericContainer> containerChanger) {
        this.containerChanger = containerChanger;
    }
}
