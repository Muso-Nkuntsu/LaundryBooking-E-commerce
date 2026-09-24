export type NotificationType = 
  | 'BOOKING_CONFIRMATION'
  | 'BOOKING_REMINDER'
  | 'BOOKING_CANCELLATION'
  | 'ORDER_UPDATE'
  | 'PAYMENT_STATUS'
  | 'SYSTEM_ALERT';

export interface NotificationItem {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  actionUrl?: string;
}

export interface NotificationState {
  notifications: NotificationItem[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
}