package com.example.bookabin.config;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Plain Java utility to choose an available port without requiring Spring on the classpath.
 */
public class PortCustomizer {

    public static int choosePort() {
        int preferred = 8080;
        if (!isPortAvailable(preferred)) {
            System.out.println("Port 8080 in use — switching server port to 8081");
            return 8081;
        } else {
            return preferred;
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
