// src/pages/notifications/NotificationsPage.tsx

import React, { useState } from 'react';
import { NotificationList } from '../../components/NotificationList';
import { NotificationBadge } from '../../components/NotificationBadge';

export const NotificationsPage: React.FC = () => {
  const [unreadCount, setUnreadCount] = useState<number>(3);

  const handleHeaderBadgeClick = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      {/* Top Header Section with Badge Indicator */}
      <div className="max-w-2xl mx-auto flex items-center justify-between mb-6 pb-4 border-b border-gray-200">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          <p className="text-sm text-gray-500">
            View system alerts, booking confirmations, and order updates
          </p>
        </div>

        {/* Navigation Indicator / Badge */}
        <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-full shadow-sm border border-gray-100">
          <span className="text-xs font-medium text-gray-600">Unread:</span>
          <NotificationBadge unreadCount={unreadCount} onClick={handleHeaderBadgeClick} />
        </div>
      </div>

      {/* Main Notifications List */}
      <main className="max-w-2xl mx-auto">
        <NotificationList />
      </main>
    </div>
  );
};

export default NotificationsPage;