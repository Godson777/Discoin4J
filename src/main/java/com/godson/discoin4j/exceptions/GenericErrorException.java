package com.godson.discoin4j.exceptions;

public class GenericErrorException extends Exception {

    public GenericErrorException(String error) {
        super("An unknown error has occurred! This is likely due to the Discoin API returning an error that is not recognized by Discoin4J. (" + error + ")");
    }
}
