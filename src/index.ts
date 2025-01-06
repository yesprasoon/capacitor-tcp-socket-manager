import { registerPlugin } from '@capacitor/core';

import type { TcpSocketManagerPlugin } from './definitions';

const TcpSocketManager = registerPlugin<TcpSocketManagerPlugin>('TcpSocketManager', {
  web: () => import('./web').then((m) => new m.TcpSocketManagerWeb()),
});

export * from './definitions';
export { TcpSocketManager };
