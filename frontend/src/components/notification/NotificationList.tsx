import React from "react";
import type { NotificationItem } from "../../types/notification";
import { EmptyState, ErrorState, Loading } from "../common/States";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { notificationService } from "../../services/notificationService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";

export const NotificationList: React.FC = () => {
  const toast = useToast();
  const studentId = getStudentId();
  const { data, loading, error, reload, setData } = useFetch<NotificationItem[]>(() =>
    notificationService.fetchNotifications(studentId).catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load your notifications."));
    }),
    studentId,
  );
  const notifications = Array.isArray(data) ? data : [];
  const unreadCount = notifications.filter((item) => !item.isRead).length;

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      setData((prev) => (prev ?? []).map((item) => (item.id === id ? { ...item, isRead: true } : item)));
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't mark that as read."));
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead(studentId);
      setData((prev) => (prev ?? []).map((item) => ({ ...item, isRead: true })));
      toast.success("All notifications marked as read.");
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't mark them as read."));
    }
  };

  if (loading) return <Loading message="Loading notifications..." />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (notifications.length === 0) {
    return <EmptyState title="You're all caught up" message="Booking confirmations and cancellations will appear here." />;
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
