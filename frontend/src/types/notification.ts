// The four kinds of notification the backend sends.
export type NotificationType =
  | 'BOOKING_CONFIRMATION'
  | 'BOOKING_CANCELLED'
  | 'ORDER_COMPLETE'
  | 'PAYMENT_CONFIRMATION';

export interface NotificationItem {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}
