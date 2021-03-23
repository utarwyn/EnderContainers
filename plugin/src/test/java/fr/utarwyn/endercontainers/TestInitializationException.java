package fr.utarwyn.endercontainers;

public class TestInitializationException extends Exception {

    TestInitializationException(Throwable cause) {
        super("cannot initialize test environment", cause);
    }

}
