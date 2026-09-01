import React, { useEffect, useState } from 'react';
import { NotificationItem } from '../types/notification';
import { notificationService } from '../services/notificationService';

export const NotificationList: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await notificationService.fetchNotifications();
      setNotifications(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (err: any) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err: any) {
      console.error('Failed to mark all as read:', err);
    }
  };

  if (loading) return <div className="p-6 text-center text-gray-500">Loading notifications...</div>;
  if (error) return <div className="p-6 text-center text-red-600">{error}</div>;

  return (
    <div className="max-w-2xl mx-auto p-4 bg-white shadow rounded-lg">
      <div className="flex justify-between items-center pb-4 mb-4 border-b">
        <h2 className="text-xl font-bold text-gray-800">Notifications</h2>
        {notifications.some((n) => !n.isRead) && (
          <button
            onClick={handleMarkAllAsRead}
            className="text-sm text-blue-600 hover:text-blue-800 font-medium"
          >
            Mark all as read
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <p className="text-center text-gray-500 py-8">No notifications found.</p>
      ) : (
        <ul className="divide-y divide-gray-100">
          {notifications.map((item) => (
            <li
              key={item.id}
              className={`p-4 rounded-md transition-colors ${
                item.isRead ? 'bg-white' : 'bg-blue-50/50 font-medium'
              }`}
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    {!item.isRead && (
                      <span className="w-2 h-2 rounded-full bg-blue-600 inline-block" />
                    )}
                    <h3 className="text-sm font-semibold text-gray-900">{item.title}</h3>
                  </div>
                  <p className="text-sm text-gray-600 mt-1">{item.message}</p>
                  <span className="text-xs text-gray-400 mt-2 block">
                    {new Date(item.createdAt).toLocaleString()}
                  </span>
                </div>
                {!item.isRead && (
                  <button
                    onClick={() => handleMarkAsRead(item.id)}
                    className="ml-4 text-xs text-gray-500 hover:text-gray-700 whitespace-nowrap"
                  >
                    Mark as read
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};