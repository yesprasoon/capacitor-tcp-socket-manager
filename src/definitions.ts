import { PluginListenerHandle } from '@capacitor/core';

export interface TcpSocketManagerPlugin {
  getDeviceIpAddress(): Promise<{ ipAddress: string }>;
  // Server-related functions
  startServer(options: { port: number }): Promise<{ success: boolean, message?: string }>;
  stopServer(): Promise<{ success: boolean }>;
  receiveMessage(): Promise<{ message: string }>;
  getClientCount(): Promise<{ count: number }>;

  // Client-related functions
  connectToServer(options: { ipAddress: string; port: number }): Promise<{ success: boolean }>;
  disconnectFromServer(): Promise<{ success: boolean }>;
  sendMessageToServer(options: { ipAddress: string; port: number; message: string }): Promise<{ success: boolean }>;

  // Add listener definitions
  addListener(
    eventName: 'receiveMessage',
    listenerFunc: (data: { message: string }) => void
  ): PluginListenerHandle;
}
