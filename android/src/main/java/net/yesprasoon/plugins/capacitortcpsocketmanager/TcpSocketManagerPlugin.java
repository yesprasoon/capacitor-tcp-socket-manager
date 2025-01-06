package com.tastyapps.plugins.capacitortcpsocketmanager;

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

@CapacitorPlugin(name = "TcpSocketManager")
public class TcpSocketManagerPlugin extends Plugin {

    private static final String TAG = "TcpSocketManager";

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private List<Socket> clientConnections = new ArrayList<>();  // List to track client connections
    private static final int MAX_CLIENTS = 10;

    @PluginMethod
    public void getDeviceIpAddress(PluginCall call) {
        try {
            String ipAddress = getLocalIpAddress();
            JSObject result = new JSObject();
            result.put("ipAddress", ipAddress);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Error fetching device IP address", e);
        }
    }

    private String getLocalIpAddress() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                // Filter out the loopback addresses
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        throw new Exception("Unable to get the device IP address.");
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
        try {
            if (port < 1 || port > 65535) {
                call.reject("Invalid port number. Port must be between 1 and 65535.");
                return;
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                call.resolve(new JSObject().put("success", true));
                return;
            }
            serverSocket = new ServerSocket(port);
            // serverSocket.setSoTimeout(30000); // Timeout after 30 seconds
            new Thread(() -> {
                try {
                    while (!serverSocket.isClosed()) {
                        Socket client = serverSocket.accept();
                        if (clientConnections.size() >= MAX_CLIENTS) {
                            client.close();
                            Log.w(TAG, "Connection rejected: Maximum client limit reached.");
                            continue;
                        }
                        clientConnections.add(client);
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        String message;
                        while ((message = in.readLine()) != null) {
                            Log.i(TAG, "Received message: " + message);

                            // Notify the frontend with the received message
                            JSObject eventData = new JSObject();
                            eventData.put("message", message);
                            notifyListeners("receiveMessage", eventData);  // Notify frontend
                        }
                    }
                } catch (IOException e) {
                    call.reject("Error starting server", e);
                }
            }).start();

            call.resolve(new JSObject().put("success", true));
        } catch (IOException e) {
            call.reject("Error starting server", e);
        }
    }

    @PluginMethod
    public void stopServer(PluginCall call) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                call.resolve(new JSObject().put("success", true));
            } catch (IOException e) {
                call.reject("Error stopping the server", e);
            }
        } else {
            call.reject("Server is not running");
        }
    }

    // Connect to Server (Client)
    @PluginMethod
    public void connectToServer(PluginCall call) {
        String ipAddress = call.getString("ipAddress", "");
        if (!isValidIpAddress(ipAddress)) {
            call.reject("Invalid IP address format.");
            return;
        }
        int port = call.getInt("port", 8080); // Default port

        if (ipAddress.isEmpty()) {
            call.reject("IP address is required");
            return;
        }

        // Add logic to connect to the server
        try {
            clientSocket = new Socket(ipAddress, port);  // Connect to server
            // clientSocket.setSoTimeout(30000); // Timeout after 30 seconds for reading data
            // isClientConnected = true;
            Log.i(TAG, "Connected to server at " + ipAddress + ":" + port);

            // Notify the JavaScript side that the connection is successful
            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);

        } catch (IOException e) {
            Log.e(TAG, "Error connecting to server: " + e.getMessage());
            // isClientConnected = false;
            call.reject("Failed to connect to server", e);
        }
    }

    // Disconnect from Server (Client)
    @PluginMethod
    public void disconnectFromServer(PluginCall call) {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
                // isClientConnected = false;
                JSObject result = new JSObject();
                result.put("success", true);
                call.resolve(result);  // Notify frontend about the disconnection

            } catch (IOException e) {
                call.reject("Error disconnecting from server", e);
            }
        } else {
            call.reject("Client is not connected");
        }
    }

    // Send Message to Server (Client)
    @PluginMethod
    public void sendMessageToServer(PluginCall call) {
        if (clientSocket == null || clientSocket.isClosed()) {
            call.reject("Not connected to server");
            return;
        }

        String message = call.getString("message", "");
        if (message == null || message.trim().isEmpty()) {
            call.reject("Message cannot be empty.");
            return;
        }

        try {
             // Check if the message is valid (not empty)
            if (message == null || message.isEmpty()) {
                call.reject("Message cannot be empty");
                return;
            }

            // Send message to the server (if connected)
            clientSocket.getOutputStream().write((message + "\n").getBytes());
            Log.i(TAG, "Message sent to server: " + message);
            call.resolve(new JSObject().put("success", true));
        } catch (IOException e) {
            Log.e(TAG, "Error sending message: " + e.getMessage());
            call.reject("Failed to send message", e);
        }
    }

    @PluginMethod
    public void disconnectAllClients(PluginCall call) {
        try {
                synchronized (clientConnections) {
                    for (Socket client : clientConnections) {
                        if (client != null && !client.isClosed()) {
                            client.close(); // Close the client socket
                            Log.i(TAG, "Client connection closed: " + client.getInetAddress().getHostAddress());
                        }
                    }
                    clientConnections.clear(); // Clear the list of client connections
                }
                call.resolve(new JSObject().put("success", true));
            } catch (IOException e) {
                call.reject("Error disconnecting clients", e);
            }
    }

    @PluginMethod
    public void getClientCount(PluginCall call) {
        call.resolve(new JSObject().put("count", clientConnections.size()));
    }

}
