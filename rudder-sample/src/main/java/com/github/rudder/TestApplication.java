package com.github.rudder;

import com.github.rudder.RudderApplication;

import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestApplication implements RudderApplication<TestApplication> {

    private static Consumer<TestApplication> callback;

    public static void setReadyCallback(final Consumer<TestApplication> callback) {
        TestApplication.callback = callback;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Arrays.toString(args));
        TestApplication.callback.accept(new TestApplication());
        System.out.println("This is Major Tom to Ground Control!");
    }

    public TestApplication() {

    }

    public Pupa getPupa() {
        return new Pupa();
    }

    public static class Pupa {

        public String exec() {
            return "I'm floating in the most peculiar way";
        }

        public Lupa methodWithPrimitive() {
            return new Lupa(this, "damn");
        }

    }

    public static class Lupa {

        private final String zipper;

        private final Pupa pupa;

        private Function<String, String> holler;

        private String value;

        public Lupa(final Pupa pupa, final String zipper) {
            this.zipper = zipper;
            this.pupa = pupa;
        }

        public boolean isSamePupa(final Pupa pupa) {
            return this.pupa == pupa;
        }

        public File getSomeFile(final String path) {
            return new File(path);
        }

        public String foobar(final String jeppa) {
            return jeppa + " Ooooh " + zipper;
        }

        public void setHoller(final Function<String, String> holler) {
            this.holler = holler;
        }

        public void call(final String holla) {
            this.value = this.holler.apply(holla);
        }

        public String getValue() {
            return value;
        }
    }

}
