package com.godson.discoin4j.exceptions;

public class TransactionNotFoundException extends Exception {

    public TransactionNotFoundException() {
        super("The transaction could not be found, either because it was incorrectly typed, or the transaction does not exist.");
    }
}
