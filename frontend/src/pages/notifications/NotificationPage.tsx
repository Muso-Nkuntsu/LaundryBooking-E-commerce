import React from "react";
import { NotificationList } from "../../components/notification/NotificationList";

export const NotificationsPage: React.FC = () => {
  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Notifications</h1>
          <p>Booking confirmations, reminders and order updates.</p>
        </div>
      </header>
      <NotificationList />
    </div>
  );
};

export default NotificationsPage;
