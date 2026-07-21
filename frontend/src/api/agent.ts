import request from '@/utils/request';

export function sendChatMessage(message: string) {
  return request.post('/agent/chat', { message });
}

export function getChatHistory() {
  return request.get('/agent/history');
}

export function clearChatHistory() {
  return request.delete('/agent/history');
}

export function confirmOrder(token: string, edits: Record<string, any>) {
  return request.post('/agent/order/confirm', { confirmToken: token, edits });
}
