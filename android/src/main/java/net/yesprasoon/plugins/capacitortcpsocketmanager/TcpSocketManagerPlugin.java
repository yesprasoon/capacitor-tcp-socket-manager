package net.yesprasoon.plugins.capacitortcpsocketmanager;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.Collections;


@CapacitorPlugin(name = "TcpSocketManager")
public class TcpSocketManagerPlugin extends Plugin {

    private static final String TAG = "TcpSocketManager";

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final List<Socket> clientConnections = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_CLIENTS = 10;
    private Thread heartbeatThread;
    private volatile boolean isHeartbeatRunning = false;
    private String serverIpAddress;
    private Integer serverPort;
    private volatile boolean isServerRunning = false; // Volatile ensures thread-safety


    private String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown IP";
    }

    private boolean isValidIpAddress(String ipAddress) {
        try {
            InetAddress.getByName(ipAddress);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    // Start Server
    @PluginMethod
    public void startServer(PluginCall call) {
        int port = call.getInt("port", 8080);
        Log.d(TAG, "startServer called with port: " + port);
        try {
            if (port < 1 || port > 65535) {
                call.reject("Invalid port number. Port must be between 1 and 65535.");
                return;
            }

            // Check if the server is already running
            if (serverSocket != null && !serverSocket.isClosed()) {
                Log.w(TAG, "Server is already running on port: " + serverSocket.getLocalPort());
                String ipAddress = getDeviceIpAddress(); // Get the device's actual IP
                call.resolve(new JSObject()
                        .put("success", true)
                        .put("message", "Server already running")
                        .put("ipAddress", ipAddress)
                        .put("port", serverSocket.getLocalPort()));
                return;
            }

            // Reset client socket if previously connected
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                    clientSocket = null;
                    Log.i(TAG, "Resetting client socket before starting the server.");
                } catch (IOException e) {
                    Log.e(TAG, "Error closing client socket before restarting server: " + e.getMessage());
                }
            }

            // Initialize the server
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(30000); // Optional: set timeout
            isServerRunning = true; // Set flag to true

            new Thread(() -> listenForClients()).start();
            String ipAddress = getDeviceIpAddress(); // Get the device's actual IP

            call.resolve(new JSObject()
                .put("success", true)
                .put("ipAddress", ipAddress)
                .put("port", port));
            Log.i(TAG, "Server started successfully on IP " + ipAddress + " and port " + port);
        } catch (IOException e) {
             Log.e(TAG, "Failed to start server on port " + port + ": " + e.getMessage(), e);
            call.reject("Error starting server. Please check logs for more details.", e);
        }
    }

    private void listenForClients() {
        while (isServerRunning && !serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                if (client == null || client.isClosed()) {
                    Log.w(TAG, "Connection attempt failed.");
                    continue;
                }
                handleClient(client); // Delegate each client connection
            } catch (SocketTimeoutException e) {
                Log.w(TAG, "Socket accept timed out, continuing...");
            } catch (IOException e) {
                Log.e(TAG, "Error accepting client connection", e);
            }
        }
    }


    private void handleClient(Socket client) {
        new Thread(() -> {
            try {
                if (clientConnections.size() >= MAX_CLIENTS) {
                    cleanupClient(client); // Reject the client and clean up
                    Log.w(TAG, "Connection rejected: Maximum client limit reached.");
                    return;
                }

                synchronized (clientConnections) {
                    clientConnections.add(client);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    Log.i(TAG, "Received message: " + message);

                    // Notify the frontend with the received message
                    JSObject eventData = new JSObject();
                    eventData.put("message", message);
                    notifyListeners("receiveMessage", eventData);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error in client handling", e);
            } finally {
                cleanupClient(client);
            }
        }).start();
    }

    private void cleanupClient(Socket client) {
        try {
            synchronized (clientConnections) {
                clientConnections.remove(client);
            }
            client.close();
            Log.i(TAG, "Client disconnected and cleaned up.");
        } catch (IOException e) {
            Log.e(TAG, "Error closing client socket", e);
        }
    }

    @PluginMethod
    public void stopServer(PluginCall call) {
        try {
            isServerRunning = false; // Set flag to false
            if (serverSocket != null && !serverSocket.isClosed()) {
                synchronized (clientConnections) {
                    for (Socket client : clientConnections) {
                        cleanupClient(client); // Ensure each client is properly cleaned up
                    }
                    clientConnections.clear();
                }
                serverSocket.close();
                Log.i(TAG, "Server stopped successfully.");
            }
            if (call != null) {
                call.resolve(new JSObject().put("success", true));
            }
        } catch (IOException e) {
            if (call != null) {
                call.reject("Error stopping server", e);
            }
        }
    }

    // Connect to Server (Client)
    @PluginMethod
    public void connectToServer(PluginCall call) {
        String ipAddress = call.getString("ipAddress", "");
        if (!isValidIpAddress(ipAddress)) {
            Log.w(TAG, "Invalid IP address: " + ipAddress);
            call.reject("Invalid IP address format.");
            return;
        }

        int port = call.getInt("port", 8080);
        if (port < 1 || port > 65535) {
            call.reject("Invalid port number. Port must be between 1 and 65535.");
            return;
        }

        if (ipAddress.isEmpty()) {
            call.reject("IP address is required.");
            return;
        }

        synchronized (this) {
            // Close and clean up if a previous connection exists
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                    clientSocket = null;
                } catch (IOException e) {
                    Log.e(TAG, "Error closing previous socket before reconnecting", e);
                }
            }
        }

        try {
            this.serverIpAddress = ipAddress;
            this.serverPort = port;

            clientSocket = new Socket(ipAddress, port);

            // Enable keep-alive
            clientSocket.setKeepAlive(true);

            // Set socket timeout (optional)
            clientSocket.setSoTimeout(30000);

            Log.i(TAG, "Connected to server at " + ipAddress + ":" + port);

            // Start the heartbeat after connection
            startHeartbeat(ipAddress, port);

            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to server: " + e.getMessage());
            call.reject("Failed to connect to server", e);
        }
    }

    // Disconnect from Server (Client)
    @PluginMethod
    public void disconnectFromServer(PluginCall call) {
        stopHeartbeat(); // Stop heartbeat

        synchronized (this) {
            if (clientSocket == null || clientSocket.isClosed()) {
                this.serverIpAddress = null;
                this.serverPort = null;
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "Already disconnected.");
                call.resolve(result);
                return;
            }

            try {
                clientSocket.close();
                clientSocket = null;
                Log.i(TAG, "Disconnected from server successfully.");

                this.serverIpAddress = null;
                this.serverPort = null;
                JSObject result = new JSObject();
                result.put("success", true);
                call.resolve(result);
            } catch (IOException e) {
                Log.e(TAG, "Error disconnecting from server: " + e.getMessage());
                call.reject("Failed to disconnect from server", e);
            }
        }
    }


    // Send Message to Server (Client)
    @PluginMethod
    public void sendMessageToServer(PluginCall call) {
        synchronized (this) {
            // Validate message input
            String message = call.getString("message", "").trim();
            if (message.isEmpty()) {
                call.reject("Message cannot be empty.");
                return;
            }
            // Check message size
            // if (message.length() > 1024) { // Example max size
            //     call.reject("Message exceeds maximum allowed length (1024 characters).");
            //     return;
            // }
            try {
                // Check if client is connected
                if (clientSocket == null || clientSocket.isClosed() || !clientSocket.isConnected()) {
                    Log.w(TAG, "Socket not connected. Attempting to reconnect...");
                    
                    // Get IP and port from parameters or fallback to stored values
                    String ipAddress = call.getString("ipAddress", this.serverIpAddress);
                    int port = call.getInt("port", this.serverPort);

                    // Validate the fallback values
                    if (ipAddress == null || ipAddress.isEmpty() || (port < 1 || port > 65535)) {
                        call.reject("Invalid IP address or port. Ensure valid connection details are set.");
                        return;
                    }

                    // Attempt reconnection
                    if (!reconnectToServer(ipAddress, port)) {
                        call.reject("Failed to reconnect to server.");
                        return;
                    }
                }

                // Send the message
                sendMessage(message);
                call.resolve(new JSObject().put("success", true));

            } catch (IOException e) {
                Log.e(TAG, "Error sending message: " + e.getMessage(), e);

                // Handle "broken pipe" or disconnection specifically
                if (e.getMessage() != null && (e.getMessage().contains("Broken pipe") || e.getMessage().contains("Not connected"))) {
                    Log.w(TAG, "Connection lost. Attempting to reconnect...");

                    // Use fallback values if not explicitly passed
                    String ipAddress = call.getString("ipAddress", this.serverIpAddress);
                    int port = call.getInt("port", this.serverPort);

                    // Validate the fallback values
                    if (ipAddress == null || ipAddress.isEmpty() || port <= 0) {
                        call.reject("Reconnection failed due to missing IP or port.");
                        return;
                    }

                    if (reconnectToServer(ipAddress, port)) {
                        Log.i(TAG, "Reconnected successfully. Retrying message send...");
                        try {
                            sendMessage(message);
                            call.resolve(new JSObject().put("success", true));
                        } catch (IOException retryException) {
                            Log.e(TAG, "Failed to send message after reconnection: " + retryException.getMessage(), retryException);
                            call.reject("Failed to send message after reconnection.", retryException);
                        }
                    } else {
                        call.reject("Reconnection failed. Cannot send message.");
                    }
                } else {
                    call.reject("Failed to send message to server.", e);
                }
            }
        }
    }


    private void sendMessage(String message) throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {
            throw new IOException("Not connected to server.");
        }
        clientSocket.getOutputStream().write((message + "\n").getBytes());
        clientSocket.getOutputStream().flush(); // Ensure message is sent immediately
        Log.i(TAG, "Message sent to server: " + message);
    }

    private boolean reconnectToServer(String ipAddress, int port) {
        try {
            // Validate IP address and port
            if (!isValidIpAddress(ipAddress)) {
                Log.w(TAG, "Invalid IP address during reconnection: " + ipAddress);
                return false;
            }
            if (port < 1 || port > 65535) {
                Log.w(TAG, "Invalid port during reconnection: " + port);
                return false;
            }

            // Attempt reconnection
            clientSocket = new Socket(ipAddress, port);
            clientSocket.setSoTimeout(30000); // Optional: Timeout for reading data
            Log.i(TAG, "Reconnected to server at " + ipAddress + ":" + port);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Reconnection failed: " + e.getMessage(), e);
            return false;
        }
    }


    @PluginMethod
    public void getClientCount(PluginCall call) {
        call.resolve(new JSObject().put("count", clientConnections.size()));
    }

    @Override
    public void handleOnDestroy() {
        super.handleOnDestroy();

        // Clean up the client socket if it exists
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
                clientSocket = null;
                Log.i(TAG, "Client socket closed during app destruction.");
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket on app destroy: " + e.getMessage());
            }
        }

        // Stop the server if running
        stopServer(null);
    }

    private void startHeartbeat(String ipAddress, int port) {
        new Thread(() -> {
            while (true) {
                try {
                    // Pause for 5 seconds between heartbeats
                    Thread.sleep(5000);

                    synchronized (this) {
                        // Check if the client socket is connected and operational
                        if (clientSocket == null || clientSocket.isClosed() || !clientSocket.isConnected()) {
                            Log.w(TAG, "Connection lost. Attempting to reconnect...");
                            if (reconnectToServer(ipAddress, port)) {
                                Log.i(TAG, "Reconnected successfully during heartbeat.");
                            } else {
                                Log.e(TAG, "Heartbeat reconnection failed.");
                            }
                        } else {
                            // Send a lightweight heartbeat message
                            clientSocket.getOutputStream().write(("\n").getBytes());
                            clientSocket.getOutputStream().flush();
                            Log.i(TAG, "Heartbeat sent to server.");
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Heartbeat thread interrupted", e);
                    break; // Exit the loop if the thread is interrupted
                } catch (IOException e) {
                    Log.e(TAG, "Error during heartbeat operation", e);
                }
            }
        }).start();
    }


    private void stopHeartbeat() {
        isHeartbeatRunning = false;
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
            heartbeatThread = null;
        }
        Log.i(TAG, "Heartbeat stopped.");
    }


}
