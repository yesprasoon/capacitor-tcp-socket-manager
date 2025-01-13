# Capacitor TCP Socket Manager

`capacitor-tcp-socket-manager` is an **Android-specific** Capacitor plugin that enables seamless TCP socket communication for your hybrid mobile apps. It allows you to create a TCP server, manage client connections, send and receive messages, and handle various socket operations efficiently. **Note: This plugin currently supports Android only.**

---

![npm](https://img.shields.io/npm/v/@yesprasoon/capacitor-tcp-socket-manager)
![npm downloads](https://img.shields.io/npm/dw/@yesprasoon/capacitor-tcp-socket-manager)
![license](https://img.shields.io/github/license/yesprasoon/capacitor-tcp-socket-manager)

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [API Methods](#api-methods)
  - [Server-Side Methods](#server-side-methods)
    - [startServer](#startserver)
    - [stopServer](#stopserver)
    - [getClientCount](#getclientcount)
  - [Client-Side Methods](#client-side-methods)
    - [connectToServer](#connecttoserver)
    - [disconnectFromServer](#disconnectfromserver)
    - [sendMessageToServer](#sendmessagetoserver)
- [Events](#events)
  - [receiveMessage](#receivemessage)
- [Example Usage](#example-usage)
  - [Starting a Server](#starting-a-server)
  - [Sending a Message from Client to Server](#sending-a-message-from-client-to-server)
- [Planned Features](#planned-features)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

- Start and stop a TCP server.
- Connect to a TCP server as a client.
- Send and receive messages.
- Manage multiple client connections.
- Get the local device's IP address.
- Limit maximum client connections (default is 10).
- Disconnect individual or all clients gracefully.

---

## Installation

### Prerequisites
- Capacitor 4 or later
- Android Studio installed and configured
- Node.js and npm installed

### Steps
1. Install the plugin:
   ```bash
   npm install @yesprasoon/capacitor-tcp-socket-manager
   ```
2. Sync with Capacitor:
   ```bash
   npx cap sync
   ```
3. Rebuild the Android project:
   ```bash
   npx cap open android
   ```

---

## API Methods

### Server-Side Methods

#### `startServer`
Starts a TCP server on the specified port.

**Parameters:**
- `port`: (optional, default: 8080) Port number to start the server on.

**Returns:**
- `ipAddress`: The IP address of the server.
- `port`: The port on which the server is running.

**Usage:**
```javascript
TcpSocketManager.startServer({ port: 8080 })
  .then(response => console.log(`Server started on ${response.ipAddress}:${response.port}`))
  .catch(error => console.error(error));
```

#### `stopServer`
Stops the TCP server if running.

**Usage:**
```javascript
TcpSocketManager.stopServer()
  .then(() => console.log('Server stopped'))
  .catch(error => console.error(error));
```

#### `getClientCount`
Gets the number of currently connected clients.

**Returns:**
- `count`: The number of connected clients.

**Usage:**
```javascript
TcpSocketManager.getClientCount()
  .then(result => console.log(`Connected clients: ${result.count}`))
  .catch(error => console.error(error));
```

### Client-Side Methods

#### `connectToServer`
Connects to a TCP server.

**Parameters:**
- `ipAddress`: The IP address of the server.
- `port`: (optional, default: 8080) Port number of the server.

**Usage:**
```javascript
TcpSocketManager.connectToServer({ ipAddress: '192.168.0.100', port: 8080 })
  .then(() => console.log('Connected to server'))
  .catch(error => console.error(error));
```

#### `disconnectFromServer`
Disconnects from the connected server.

**Usage:**
```javascript
TcpSocketManager.disconnectFromServer()
  .then(() => console.log('Disconnected from server'))
  .catch(error => console.error(error));
```

#### `sendMessageToServer`
Sends a message to the connected server.

**Parameters:**
- `message`: The message string to send.
- `ipAddress`: (optional): The IP address of the server. If not provided, the stored IP address (from the connection step) will be used.
- `port`: (optional, default: 8080): Port number of the server. If not provided, the stored port (from the connection step) will be used.

**Usage:**
```javascript
TcpSocketManager.sendMessageToServer({ message: 'Hello Server!' })
  .then(() => console.log('Message sent successfully'))
  .catch(error => console.error('Failed to send message:', error));
```

---

## Events

### `receiveMessage`
Listens for incoming messages on the server.

**Usage:**
```javascript
TcpSocketManager.addListener('receiveMessage', data => {
  console.log(`Message received: ${data.message}`);
});
```

---

## Example Usage

### Starting a Server
```javascript
TcpSocketManager.startServer({ port: 8080 })
  .then(response => console.log(`Server is running on ${response.ipAddress}:${response.port}`))
  .catch(error => console.error(error));
```

### Sending a Message from Client to Server
```javascript
TcpSocketManager.connectToServer({ ipAddress: '192.168.0.100', port: 8080 })
  .then(() => {
    return TcpSocketManager.sendMessageToServer({ message: 'Hello, Server!' });
  })
  .then(() => console.log('Message sent to server'))
  .catch(error => console.error(error));
```

---

## Planned Features

We are continuously improving this plugin. Future updates may include:
- TLS/SSL communication for secure connections.
- IP whitelisting for enhanced access control.
- Client authentication mechanisms.
- Message acknowledgment for reliable communication.
- Automatic client reconnection.

All suggestions are welcome!

---

## Troubleshooting

### Problem: Server does not start
- Ensure the port is not already in use by another application.
- Check for sufficient permissions in your app's AndroidManifest.xml.

### Problem: Unable to connect to the server
- Verify the IP address and port are correct.
- Ensure the device is on the same network as the server.

---

## License

This project is licensed under the MIT License. See the LICENSE file for more details.

