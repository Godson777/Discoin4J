package com.godson.discoin4j.exceptions;

public class UnknownErrorException extends Exception {

    public UnknownErrorException() {
        super("An unknown error has occurred! This is likely due to the Discoin API returning an error that is not recognized by Discoin4J.");
    }
}
