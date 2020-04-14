package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PinUtilsTest {

    @ParameterizedTest
    @MethodSource
    public void removeTrailingSlash(String string, String expected) {
        assertEquals(expected, PinUtils.removeTrailingSlash(string));
    }

    static Stream<Arguments> removeTrailingSlash() {
        return Stream.of(
                arguments(null, null),
                arguments("", ""),
                arguments("users/", "users"),
                arguments("users", "users"),
                arguments("/u/users/", "/u/users")
        );
    }

}