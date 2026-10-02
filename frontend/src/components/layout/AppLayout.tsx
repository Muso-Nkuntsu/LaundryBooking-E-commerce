import type { ReactNode } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import Drum from "../common/Drum";
import { APP_NAME } from "../../config";
import Icon from "../common/Icon";
import type { IconName } from "../common/Icon";
import { useFetch } from "../../hooks/useFetch";
import { notificationService } from "../../services/notificationService";
import { clearSession, getInitials, getStudentId } from "../../services/session";

const NAV: { to: string; label: string }[] = [
  { to: "/dashboard", label: "Home" },
  { to: "/make-booking", label: "Book" },
  { to: "/my-bookings", label: "My bookings" },
  { to: "/laundry-rooms", label: "Rooms" },
  { to: "/laundry", label: "Services" },
  { to: "/products", label: "Shop" },
];

const TABS: { to: string; label: string; icon: IconName }[] = [
  { to: "/dashboard", label: "Home", icon: "home" },
  { to: "/make-booking", label: "Book", icon: "calendar" },
  { to: "/my-bookings", label: "Bookings", icon: "list" },
  { to: "/products", label: "Shop", icon: "bag" },
  { to: "/profile", label: "Profile", icon: "user" },
];

const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? "active" : undefined);

function AppLayout({ children }: { children: ReactNode }) {
  const navigate = useNavigate();

  // The bell is a nice-to-have: if notifications can't load, it just shows no count.
  const { data: notifications } = useFetch(() => notificationService.fetchNotifications(getStudentId()));
  const unread = Array.isArray(notifications) ? notifications.filter((item) => !item.isRead).length : 0;

  const handleLogout = () => {
    clearSession();
    navigate("/login");
  };

  return (
    <div className="shell">
      <header className="topbar">
        <Link to="/dashboard" className="brand">
          <Drum tone="dark" size={30} />
          {APP_NAME}
        </Link>

        <nav className="topnav" aria-label="Main">
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} className={navClass}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="topbar-actions">
          <Link to="/notifications" className="icon-btn" aria-label={unread > 0 ? `Notifications, ${unread} unread` : "Notifications"}>
            <Icon name="bell" />
            {unread > 0 && <span className="count">{unread > 99 ? "99+" : unread}</span>}
          </Link>
          <Link to="/profile" className="avatar" aria-label="Your profile">
            {getInitials()}
          </Link>
          <button type="button" className="icon-btn" onClick={handleLogout} aria-label="Log out" title="Log out">
            <Icon name="logout" />
          </button>
        </div>
      </header>

      <main className="shell-main">{children}</main>

      <nav className="tabbar" aria-label="Main">
        {TABS.map((tab) => (
          <NavLink key={tab.to} to={tab.to} className={navClass}>
            <Icon name={tab.icon} />
            {tab.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}

export default AppLayout;
