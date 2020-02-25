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

    public void add(final String path, final Handler handler) {
        this.handlers.add(new HandlerDefinition(path, handler));
    }

    public void start() {
        this.app = Javalin.create().start(port);

        this.port = this.app.port();

        handlers.forEach(handlerDefinition -> app.post(handlerDefinition.getPath(), handlerDefinition.getHandler()));
    }

    public int getPort() {
        return this.port;
    }

    public void stop() {
        if (this.app != null) {
            this.app.stop();
        }
    }

    public static class HandlerDefinition {

        private final String path;

        private final Handler handler;

        public HandlerDefinition(final String path, final Handler handler) {
            this.path = path;
            this.handler = handler;
        }

        public String getPath() {
            return path;
        }

        public Handler getHandler() {
            return handler;
        }
    }

}
