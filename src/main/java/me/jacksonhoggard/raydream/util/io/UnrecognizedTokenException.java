package me.jacksonhoggard.raydream.util.io;

public class UnrecognizedTokenException extends Exception {
    public UnrecognizedTokenException(String token) {
        super("Unrecognized token: \"" + token + "\"");
    }
}
