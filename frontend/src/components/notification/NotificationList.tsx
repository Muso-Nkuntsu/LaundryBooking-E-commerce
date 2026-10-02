import React from "react";
import type { NotificationItem, NotificationType } from "../../types/notification";
import { EmptyState, ErrorState, Loading } from "../common/States";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { notificationService } from "../../services/notificationService";
import { friendlyError } from "../../utilis/errorMessage";

const TYPE_LABEL: Record<NotificationType, string> = {
  BOOKING_CONFIRMATION: "Booking confirmed",
  BOOKING_REMINDER: "Reminder",
  BOOKING_CANCELLATION: "Booking cancelled",
  ORDER_UPDATE: "Order update",
  PAYMENT_STATUS: "Payment",
  SYSTEM_ALERT: "Notice",
};

export const NotificationList: React.FC = () => {
  const toast = useToast();
  const { data, loading, error, reload, setData } = useFetch<NotificationItem[]>(() =>
    notificationService.fetchNotifications().catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load your notifications."));
    }),
  );
  const notifications = Array.isArray(data) ? data : [];
  const unreadCount = notifications.filter((item) => !item.isRead).length;

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationService.markAsRead(id);
      setData((prev) => (prev ?? []).map((item) => (item.id === id ? { ...item, isRead: true } : item)));
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't mark that as read."));
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setData((prev) => (prev ?? []).map((item) => ({ ...item, isRead: true })));
      toast.success("All notifications marked as read.");
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't mark them as read."));
    }
  };

  if (loading) return <Loading message="Loading notifications..." />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (notifications.length === 0) {
    return <EmptyState title="You're all caught up" message="Booking confirmations, reminders and order updates will appear here." />;
  }

  return (
    <div className="stack">
      <div className="row">
        <span className="muted">{unreadCount > 0 ? `${unreadCount} unread` : "Nothing unread"}</span>
        {unreadCount > 0 && (
          <button type="button" className="link-btn" onClick={handleMarkAllAsRead}>
            Mark all as read
          </button>
        )}
      </div>

      {notifications.map((item) => (
        <article key={item.id} className={`notif ${item.isRead ? "read" : "unread"}`}>
          <span className="dot" aria-hidden="true" />
          <div style={{ flex: 1 }}>
            <span className="small muted">{TYPE_LABEL[item.type] ?? "Notice"}</span>
            <h3>{item.title}</h3>
            <p>{item.message}</p>
            <span className="small muted">{new Date(item.createdAt).toLocaleString("en-ZA", { dateStyle: "medium", timeStyle: "short" })}</span>
          </div>
          {!item.isRead && (
            <button type="button" className="link-btn small" onClick={() => handleMarkAsRead(item.id)}>
              Mark as read
            </button>
          )}
        </article>
      ))}
    </div>
  );
};
