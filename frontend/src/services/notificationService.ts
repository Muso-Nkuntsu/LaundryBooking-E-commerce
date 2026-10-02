import { apiGet, apiPut } from './Api';
import type { NotificationItem, NotificationType } from '../types/notification';

// Shape sent by the backend.
interface RawNotification {
  id: number;
  type: NotificationType;
  message: string;
  dateSent: string;
  read: boolean;
}

const TITLES: Record<NotificationType, string> = {
  BOOKING_CONFIRMATION: 'Booking confirmed',
  BOOKING_CANCELLED: 'Booking cancelled',
  ORDER_COMPLETE: 'Order complete',
  PAYMENT_CONFIRMATION: 'Payment confirmed',
};

const toNotification = (raw: RawNotification): NotificationItem => ({
  id: raw.id,
  type: raw.type,
  title: TITLES[raw.type] ?? 'Notice',
  message: raw.message,
  isRead: raw.read,
  createdAt: raw.dateSent,
});

export const notificationService = {
  /** Notifications for one student, newest first. */
  async fetchNotifications(studentId: number): Promise<NotificationItem[]> {
    return (await apiGet<RawNotification[]>(`/notification/student/${studentId}`)).map(toNotification);
  },

  async markAsRead(id: number): Promise<void> {
    await apiPut(`/notification/${id}/read`);
  },

  async markAllAsRead(studentId: number): Promise<void> {
    await apiPut(`/notification/student/${studentId}/read-all`);
  },
};
