package com.godson.discoin4j.exceptions;

public class UnauthorizedException extends Exception {

    public UnauthorizedException() {
        super("Unauthorized. You've either entered the token incorrectly, or don't have one.");
    }
}
