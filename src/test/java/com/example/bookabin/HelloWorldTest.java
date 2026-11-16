// src/test/java/com/example/bookabin/HelloWorldTest.java
package com.example.bookabin;

import org.junit.jupiter.api.Test;

import com.example.HelloWorld;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {
    @Test
    public void testHelloWorld() {
        assertEquals("Hello, World!", new HelloWorld().greet());
    }
}