import { NotificationItem } from '../types/notification';

const BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
  }
  return response.json();
}

export const notificationService = {
  async fetchNotifications(): Promise<NotificationItem[]> {
    const res = await fetch(`${BASE_URL}/notifications`);
    return handleResponse<NotificationItem[]>(res);
  },

  async markAsRead(id: string): Promise<void> {
    const res = await fetch(`${BASE_URL}/notifications/${id}/read`, { method: 'PATCH' });
    return handleResponse<void>(res);
  },

  async markAllAsRead(): Promise<void> {
    const res = await fetch(`${BASE_URL}/notifications/read-all`, { method: 'PATCH' });
    return handleResponse<void>(res);
  }
};