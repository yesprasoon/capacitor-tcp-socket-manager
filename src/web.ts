import { WebPlugin } from '@capacitor/core';

import type { TcpSocketManagerPlugin } from './definitions';

export class TcpSocketManagerWeb extends WebPlugin implements TcpSocketManagerPlugin {

  private connected = false;

  constructor() {
    super({
      name: 'CapacitorSocketServer',
      platforms: ['web'],
    });
  }

  async getDeviceIpAddress(): Promise<any> {
    console.log('ECHO', 'getDeviceIpAddress');
    return true;
  }

  // Server
  async startServer(options: { port: number }): Promise<{ success: boolean }> {
    console.log(`Starting server on port: ${options.port}`);

    // Simulate message reception for testing
    setTimeout(() => {
      this.notifyListeners('receiveMessage', { message: 'Simulated message from server' });
    }, 5000);

    return { success: true };
  }

  async stopServer(): Promise<{ success: boolean }> {
    console.log('stopServer called (Web)');
    return { success: false };
  }

  async receiveMessage(): Promise<{ message: string }> {
    console.log('receiveMessage called (Web)');
    return { message: 'Not available on Web' };
  }

  getClientCount(): Promise<{ count: number }> {
    console.warn('getClientCount is not implemented on the web platform');
    return Promise.resolve({ count: 0 });
  }

  async disconnectAllClients(): Promise<{ success: boolean }> {
    console.log('disconnectAllClients called (Web)');
    return { success: false };
  }

  // Client
  async connectToServer(options: { ipAddress: string; port: number }): Promise<{ success: boolean }> {
    console.log(`Connecting to server at ${options.ipAddress}:${options.port}`);
    this.connected = true;
    return { success: true };
  }

  async disconnectFromServer(): Promise<{ success: boolean }> {
    console.log('Disconnecting from server');
    this.connected = false;
    return { success: true };
  }

  async sendMessageToServer(options: { message: string }): Promise<{ success: boolean }> {
    if (!this.connected) {
      throw new Error('Not connected to a server');
    }
    console.log(`Sending message: ${options.message}`);
    return { success: true };
  }

  addListener(eventName: 'receiveMessage', listenerFunc: (data: { message: string }) => void): any {
    console.log(`Listener added for event: ${eventName}`);
    return super.addListener(eventName, listenerFunc);
  }

}

const TcpSocketManager = new TcpSocketManagerWeb();
export { TcpSocketManager };
