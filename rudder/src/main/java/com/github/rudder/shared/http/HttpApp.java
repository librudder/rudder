package com.github.rudder.shared.http;

import io.javalin.Javalin;
import io.javalin.http.Handler;

import java.util.ArrayList;
import java.util.List;

public class HttpApp {

    /**
     * Application port (must be this inside container, can be different on a host)
     */
    public static final int COORDINATOR_CONTROL_PORT = 9513;

    /**
     * HTTP app handlers
     */
    private final List<HandlerDefinition> handlers = new ArrayList<>();

    /**
     * App port
     */
    private int port;

    /**
     * Javalin HTTP server
     */
    private Javalin app;

    public HttpApp(final int port) {
        this.port = port;
    }

    public HttpApp() {
        this.port = 0;
    }

    /**
     * Add new HTTP handler
     *
     * @param handlerDefinition HTTP handler definition
     */
    public void add(final HandlerDefinition handlerDefinition) {
        this.handlers.add(handlerDefinition);
    }

    /**
     * Start HTTP server
     */
    public void start() {
        this.app = Javalin.create().start(port);

        this.port = this.app.port();

        handlers.forEach(handlerDefinition -> app.post(handlerDefinition.path(), handlerDefinition.handler()));
    }

    /**
     * Stop HTTP server
     */
    public void stop() {
        if (this.app != null) {
            this.app.stop();
        }
    }

    public int getPort() {
        return this.port;
    }

    /**
     * Definition of the HTTP handler
     */
    public interface HandlerDefinition {

        /**
         * Path that this handler should use
         *
         * @return http path
         */
        String path();

        /**
         * Handler object that should be created
         *
         * @return handler object
         */
        Handler handler();

    }

}
