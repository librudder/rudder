package com.github.rudder.shared;

import io.javalin.Javalin;
import io.javalin.http.Handler;

import java.util.ArrayList;
import java.util.List;

public class HttpApp {

    private int port;

    private final List<HandlerDefinition> handlers = new ArrayList<>();

    private Javalin app;

    public HttpApp(final int port) {
        this.port = port;
    }

    public HttpApp() {
        this.port = 0;
    }

    public void add(final HandlerDefinition handlerDefinition) {
        this.handlers.add(handlerDefinition);
    }

    public void start() {
        this.app = Javalin.create().start(port);

        this.port = this.app.port();

        handlers.forEach(handlerDefinition -> app.post(handlerDefinition.path(), handlerDefinition.handler()));
    }

    public int getPort() {
        return this.port;
    }

    public void stop() {
        if (this.app != null) {
            this.app.stop();
        }
    }

    public interface HandlerDefinition {

        String path();

        Handler handler();

    }

}
