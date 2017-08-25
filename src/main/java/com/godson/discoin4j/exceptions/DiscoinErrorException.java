package com.godson.discoin4j.exceptions;

import com.godson.discoin4j.Discoin4J;

public class DiscoinErrorException extends Exception {

    public DiscoinErrorException(String reason) {
        super(reason);
    }
}
