# Capacitor TCP Socket Manager

`capacitor-tcp-socket-manager` is an **Android-specific** Capacitor plugin that enables seamless TCP socket communication for your hybrid mobile apps. It allows you to create a TCP server, manage client connections, send and receive messages, and handle various socket operations efficiently. **Note: This plugin currently supports Android only.**

---

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
  - [Utility Methods](#utility-methods)
    - [getDeviceIpAddress](#getdeviceipaddress)
- [Events](#events)
  - [receiveMessage](#receivemessage)
- [Example Usage](#example-usage)
  - [Starting a Server](#starting-a-server)
  - [Sending a Message from Client to Server](#sending-a-message-from-client-to-server)
- [Notes](#notes)
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

To install the plugin, run the following commands:

```bash
npm install @yesprasoon/capacitor-tcp-socket-manager
npx cap sync

```

---

## Imports

### Importing the Plugin
Before using the capacitor-tcp-socket-manager plugin in your frontend, you need to import it in your TypeScript files as follows:
```javascript
import { TcpSocketManager } from 'capacitor-tcp-socket-manager';
```
## API Methods

### Server-Side Methods

#### `startServer`
Starts a TCP server on the specified port.

**Parameters:**
- `port`: (optional, default: 8080) Port number to start the server on.

**Usage:**
```javascript
TcpSocketManager.startServer({ port: 8080 })
  .then(() => console.log('Server started'))
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
- `ipAddress`: The IP address of the server.
- `port`: (optional, default: 8080) Port number of the server.
- `message`: The message string to send.

**Usage:**
```javascript
TcpSocketManager.sendMessageToServer({ ipAddress: '192.168.0.100', port: 8080, message: 'Hello Server!' })
  .then(() => console.log('Message sent'))
  .catch(error => console.error(error));
```

### Utility Methods

#### `getDeviceIpAddress`
Gets the local device's IP address.

**Usage:**
```javascript
TcpSocketManager.getDeviceIpAddress()
  .then(result => console.log(`Device IP Address: ${result.ipAddress}`))
  .catch(error => console.error(error));
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
  .then(() => console.log('Server is running on port 8080'))
  .catch(error => console.error(error));
```

### Sending a Message from Client to Server
```javascript
TcpSocketManager.connectToServer({ ipAddress: '192.168.0.100', port: 8080 })
  .then(() => {
    return TcpSocketManager.sendMessageToServer({ ipAddress: '192.168.0.100', port: 8080, message: 'Hello, Server!' });
  })
  .then(() => console.log('Message sent to server'))
  .catch(error => console.error(error));
```

---

## Notes

- The maximum number of client connections is limited to 10 by default. You can modify this limit in the Java source code if needed.
- Ensure proper error handling for connection and message-related operations.
- Server and client timeouts can be configured using `setSoTimeout` in the Java code.

---

## License

This project is licensed under the MIT License. See the LICENSE file for more details.
