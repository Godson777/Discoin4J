package com.godson.discoin4j.exceptions;

import com.godson.discoin4j.Discoin4J;

public class RejectedException extends Exception {

    public RejectedException(Discoin4J.Status status) {
        super(status.getReason());
    }
}
