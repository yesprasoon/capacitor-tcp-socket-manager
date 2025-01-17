import { PluginListenerHandle } from '@capacitor/core';

export interface TcpSocketManagerPlugin {
  // Server-related functions

  /**
   * Starts a TCP server on the specified port.
   * @param options - Options for starting the server.
   *  - `port` (optional): The port number to start the server on. Defaults to 8080 if not provided.
   * @returns A promise that resolves with the server's IP address and port.
   */
  startServer(options: { port?: number }): Promise<{ success: boolean, message?: string, ipAddress: string, port: number }>;

  /**
   * Stops the TCP server if it is running.
   * @returns A promise that resolves with the success status of the operation.
   */
  stopServer(): Promise<{ success: boolean }>;

  /**
   * Listens for and retrieves a message from the server.
   * @returns A promise that resolves with the received message.
   */
  receiveMessage(): Promise<{ message: string }>;

  /**
   * Gets the current number of clients connected to the server.
   * @returns A promise that resolves with the count of connected clients.
   */
  getClientCount(): Promise<{ count: number }>;

  // Client-related functions
  /**
   * Connects to a TCP server.
   * @param options - Options for connecting to the server.
   *  - `ipAddress`: The IP address of the server.
   *  - `port`: The port number of the server.
   * @returns A promise that resolves with the success status of the connection.
   */
  connectToServer(options: { ipAddress: string; port: number }): Promise<{ success: boolean }>;

  /**
   * Disconnects from the currently connected server.
   * @returns A promise that resolves with the success status of the disconnection.
   */
  disconnectFromServer(): Promise<{ success: boolean }>;

  /**
   * Sends a message to the connected server.
   * @param options - Options for sending the message.
   *  - `ipAddress` (optional): The IP address of the server. Uses the stored IP address if not provided.
   *  - `port` (optional): The port number of the server. Uses the stored port if not provided.
   *  - `message`: The message string to send.
   * @returns A promise that resolves with the success status of the operation.
   */
  sendMessageToServer(options: { ipAddress?: string; port?: number; message: string }): Promise<{ success: boolean }>;

  // Listener definitions

  /**
   * Adds a listener for the specified event.
   * @param eventName - The name of the event to listen for.
   *  - `receiveMessage`: Event triggered when a message is received on the server.
   * @param listenerFunc - The function to execute when the event is triggered. Receives data containing the message.
   * @returns A handle that can be used to remove the listener.
   */
  addListener(
    eventName: 'receiveMessage',
    listenerFunc: (data: { message: string }) => void
  ): PluginListenerHandle;
}
