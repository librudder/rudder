package com.github.rudder.host;

import com.github.rudder.shared.HttpApp;
import io.javalin.http.Handler;

import java.util.function.Consumer;

public class HelloController implements HttpApp.HandlerDefinition {

    private final String mainObjectId;

    private final Consumer<Integer> portConsumer;

    public HelloController(final String mainObjectId, final Consumer<Integer> portConsumer) {
        this.mainObjectId = mainObjectId;
        this.portConsumer = portConsumer;
    }

    @Override
    public String path() {
        return "/hello";
    }

    @Override
    public Handler handler() {
        return context -> {
            final String port = context.req.getParameter("port");
            portConsumer.accept(Integer.parseInt(port));
            context.result(mainObjectId);
        };
    }
}
