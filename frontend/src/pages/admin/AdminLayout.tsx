import type { ReactNode } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import Drum from "../../components/common/Drum";
import Icon from "../../components/common/Icon";
import type { IconName } from "../../components/common/Icon";
import { APP_NAME } from "../../config";
import { useCart } from "../../context/useCart";
import { clearSession, getSession } from "../../services/session";
import "./admin.css";

const NAV: { to: string; label: string; icon: IconName; end?: boolean }[] = [
  { to: "/admin", label: "Dashboard", icon: "home", end: true },
  { to: "/admin/users", label: "Students", icon: "users" },
  { to: "/admin/bookings", label: "Bookings", icon: "calendar" },
  { to: "/admin/facilities", label: "Machines & slots", icon: "washer" },
  { to: "/admin/products", label: "Products", icon: "box" },
  { to: "/admin/orders", label: "Orders & payments", icon: "receipt" },
  { to: "/admin/reports", label: "Reports", icon: "chart" },
];

const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? "active" : undefined);

// The frame around every admin page: a sidebar on computers, a scrolling bar on phones.
function AdminLayout({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const cart = useCart();
  const session = getSession();

  const handleLogout = () => {
    clearSession();
    cart.clear();
    navigate("/login");
  };

  return (
    <div className="adm">
      <aside className="adm-side">
        <Link to="/admin" className="brand">
          <Drum tone="dark" size={28} />
          <span className="adm-label">{APP_NAME}</span>
          <span className="adm-badge">ADMIN</span>
        </Link>

        <nav className="adm-nav" aria-label="Admin">
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className={navClass}>
              <span className="adm-icon"><Icon name={item.icon} size={20} /></span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="adm-side-foot">
          {session && <div className="adm-who">{session.firstName} {session.lastName}</div>}
          <Link to="/dashboard" title="Student site">
            <Icon name="arrowLeft" size={20} />
            <span className="adm-text">Student site</span>
          </Link>
          <button type="button" onClick={handleLogout} title="Log out">
            <Icon name="logout" size={20} />
            <span className="adm-text">Log out</span>
          </button>
        </div>
      </aside>

      <main className="adm-main">
        <div className="adm-page">{children}</div>
      </main>
    </div>
  );
}

export default AdminLayout;
