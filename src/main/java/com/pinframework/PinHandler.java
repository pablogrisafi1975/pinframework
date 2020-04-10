package com.pinframework;

@FunctionalInterface
public interface PinHandler {
    PinResponse handle(PinExchange pinExchange) throws Exception;
}
