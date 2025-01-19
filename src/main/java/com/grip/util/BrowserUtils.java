package com.grip.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class that provides functionality for browser interaction and server management.
 * This includes functions to check if a server is running on a specific host/port,
 * wait for server availability, and handle browser launching.
 */
public class BrowserUtils {
    
    /**
     * Checks whether a server is currently listening on the specified host and port.
     *
     * @param host The host address to check
     * @param port The port number to check
     * @return true if a server is running at the specified host and port, false otherwise
     */
    public static boolean isServerRunning(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Blocks until a local server is listening on the specified host and port.
     * The wait can be cancelled using the cancelEvent parameter.
     *
     * @param host The host address to check
     * @param port The port number to check
     * @param cancelEvent AtomicBoolean that can be set to true to cancel the wait
     * @return true if server is running, false if wait was cancelled
     */
    public static boolean waitForServer(String host, int port) {
        return waitForServer(host, port, null);
    }

    public static boolean waitForServer(String host, int port, AtomicBoolean cancelEvent) {
        while (!isServerRunning(host, port)) {
            if (cancelEvent != null && cancelEvent.get()) {
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    /**
     * Opens the specified URL in a new browser window.
     *
     * @param url The URL to open in the browser
     */
    public static void startBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (IOException | URISyntaxException e) {
            // Silently ignore exceptions as per original Python implementation
        }
    }

    /**
     * Waits for the server to run and then opens the specified address in the browser.
     * The wait can be cancelled using the cancelEvent parameter.
     *
     * @param host The host address to check
     * @param port The port number to check (if null, defaults to 80)
     * @param cancelEvent AtomicBoolean that can be set to true to cancel the wait
     */
    public static void waitAndStartBrowser(String host) {
        waitAndStartBrowser(host, null, null);
    }

    public static void waitAndStartBrowser(String host, Integer port, AtomicBoolean cancelEvent) {
        if ("0.0.0.0".equals(host)) {
            host = "localhost";
        }
        if (port == null) {
            port = 80;
        }

        if (waitForServer(host, port, cancelEvent)) {
            startBrowser(String.format("http://%s:%d/", host, port));
        }
    }

    /**
     * Starts a thread that waits for the server then opens the specified address in the browser.
     * The wait can be cancelled using the cancelEvent parameter.
     *
     * @param host The host address to check
     * @param port The port number to check (if null, defaults to 80)
     * @param cancelEvent AtomicBoolean that can be set to true to cancel the wait
     * @return The started Thread object
     */
    public static Thread startBrowserWhenReady(String host) {
        return startBrowserWhenReady(host, null, null);
    }

    public static Thread startBrowserWhenReady(String host, Integer port, AtomicBoolean cancelEvent) {
        Thread browserThread = new Thread(() -> waitAndStartBrowser(host, port, cancelEvent));
        browserThread.setDaemon(true);
        browserThread.start();
        return browserThread;
    }
}