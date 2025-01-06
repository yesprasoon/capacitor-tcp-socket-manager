# Capacitor TCP Socket Manager

`capacitor-tcp-socket-manager` is a Capacitor plugin that enables seamless TCP socket communication for your hybrid mobile apps. It allows you to create a TCP server, manage client connections, send and receive messages, and handle various socket operations efficiently.

---

## Features

- Start and stop a TCP server.
- Connect to a TCP server as a client.
- Send and receive messages.
- Manage multiple client connections.
- Get the local device's IP address.
- Limit maximum client connections.
- Disconnect clients or the server gracefully.

---

## Installation

```bash
npm install capacitor-tcp-socket-manager
npx cap sync
```

---

## API Methods

### Server-Side Methods

#### `startServer`
Starts a TCP server on the specified port.

**Parameters:**
- `port`: (optional, default: 8080) Port number to start the server on.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.startServer({ port: 12345 })
  .then(() => console.log('Server started'))
  .catch(error => console.error(error));
```

#### `stopServer`
Stops the TCP server if running.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.stopServer()
  .then(() => console.log('Server stopped'))
  .catch(error => console.error(error));
```

#### `disconnectAllClients`
Disconnects all connected clients from the server.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.disconnectAllClients()
  .then(() => console.log('All clients disconnected'))
  .catch(error => console.error(error));
```

#### `getClientCount`
Gets the number of currently connected clients.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.getClientCount()
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
Capacitor.Plugins.CapacitorTcpSocketManager.connectToServer({ ipAddress: '192.168.1.1', port: 12345 })
  .then(() => console.log('Connected to server'))
  .catch(error => console.error(error));
```

#### `disconnectFromServer`
Disconnects from the connected server.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.disconnectFromServer()
  .then(() => console.log('Disconnected from server'))
  .catch(error => console.error(error));
```

#### `sendMessageToServer`
Sends a message to the connected server.

**Parameters:**
- `message`: The message string to send.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.sendMessageToServer({ message: 'Hello Server!' })
  .then(() => console.log('Message sent'))
  .catch(error => console.error(error));
```

### Utility Methods

#### `getDeviceIpAddress`
Gets the local device's IP address.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.getDeviceIpAddress()
  .then(result => console.log(`Device IP Address: ${result.ipAddress}`))
  .catch(error => console.error(error));
```

---

## Events

### `receiveMessage`
Listens for incoming messages on the server.

**Usage:**
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.addListener('receiveMessage', data => {
  console.log(`Message received: ${data.message}`);
});
```

---

## Example Usage

### Starting a Server
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.startServer({ port: 8080 })
  .then(() => console.log('Server is running on port 8080'))
  .catch(error => console.error(error));
```

### Sending a Message from Client to Server
```javascript
Capacitor.Plugins.CapacitorTcpSocketManager.connectToServer({ ipAddress: '192.168.0.100', port: 8080 })
  .then(() => {
    return Capacitor.Plugins.CapacitorTcpSocketManager.sendMessageToServer({ message: 'Hello, Server!' });
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
