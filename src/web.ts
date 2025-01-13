import { WebPlugin } from '@capacitor/core';

import type { TcpSocketManagerPlugin } from './definitions';

export class TcpSocketManagerWeb extends WebPlugin implements TcpSocketManagerPlugin {

  constructor() {
    super({
      name: 'TcpSocketManager',
      platforms: ['web'],
    });
  }

  // Server
  async startServer(): Promise<{ success: boolean, message?: string, ipAddress: string, port: number }> {
    console.warn('Web does not support starting a TCP server.');
    return { success: false, message: 'Web platform does not support this functionality.', ipAddress: '192.168.0.100', port: 8080 };
  }

  async stopServer(): Promise<{ success: boolean }> {
    console.warn('Web does not support stopping a TCP server.');
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


  // Client
  async connectToServer(): Promise<{ success: boolean }> {
    console.warn('Web does not support TCP socket connections.');
    return { success: false };
  }

  async disconnectFromServer(): Promise<{ success: boolean }> {
    console.warn('Web does not support disconnecting from a TCP server.');
    return { success: false };
  }

  async sendMessageToServer(): Promise<{ success: boolean }> {
    console.warn('Web does not support sending messages to a TCP server.');
    return { success: false };
  }

  addListener(eventName: 'receiveMessage', listenerFunc: (data: { message: string }) => void): any {
    console.log(`Listener added for event: ${eventName}`);
    return super.addListener(eventName, listenerFunc);
  }

}

const TcpSocketManager = new TcpSocketManagerWeb();
export { TcpSocketManager };
