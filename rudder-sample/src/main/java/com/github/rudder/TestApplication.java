package com.github.rudder;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestApplication implements RudderApplication<TestApplication> {

    private static Consumer<TestApplication> callback;

    public static void setReadyCallback(final Consumer<TestApplication> callback) {
        TestApplication.callback = callback;
    }

    public static void main(String[] args) {
        TestApplication.callback.accept(new TestApplication());
    }

    public TestApplication() {

    }

    public ClassOne getClassOneObject() {
        return new ClassOne();
    }

    public static class ClassOne {

        public String exec() {
            return "I'm floating in the most peculiar way";
        }

        public ClassTwo methodWithPrimitive() {
            return new ClassTwo(this, "damn");
        }

    }

    public static class ClassTwo {

        private final String someValue;

        private final ClassOne classOne;

        private Function<String, String> someFunc;

        private String value;

        public ClassTwo(final ClassOne classOne, final String someValue) {
            this.someValue = someValue;
            this.classOne = classOne;
        }

        public boolean isSamePupa(final ClassOne classOne) {
            return this.classOne == classOne;
        }

        public File getSomeFile(final String path) {
            return new File(path);
        }

        public String foobar(final String foobarValue) {
            return foobarValue + " Ooooh " + someValue;
        }

        public void setSomeFunc(final Function<String, String> someFunc) {
            this.someFunc = someFunc;
        }

        public void call(final String callParameter) {
            this.value = this.someFunc.apply(callParameter);
        }

        public String getValue() {
            return value;
        }
    }

}
