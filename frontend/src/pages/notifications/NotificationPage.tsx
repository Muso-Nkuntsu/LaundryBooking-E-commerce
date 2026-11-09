import React from "react";
import { NotificationList } from "../../components/notification/NotificationList";

export const NotificationsPage: React.FC = () => {
  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Notifications</h1>
          <p>Updates about your bookings, orders and payments.</p>
        </div>
      </header>
      <NotificationList />
    </div>
  );
};

export default NotificationsPage;
