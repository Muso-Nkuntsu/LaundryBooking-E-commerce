import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../context/useAuth";
import { adminCss } from "./adminStyles";

const NAV = [
    { to: "/admin", label: "Overview", end: true },
    { to: "/admin/users", label: "Users" },
    { to: "/admin/bookings", label: "Bookings & Machines" },
    { to: "/admin/products", label: "Products" },
    { to: "/admin/orders", label: "Orders & Payments" },
    { to: "/admin/reports", label: "Reports" },
];

// Shell for every admin page. Access is limited to ADMIN by <ProtectedRoute role="ADMIN"> in the router
// AND by the server, which rejects every /api/admin/** call from a non-admin.
function AdminLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [open, setOpen] = useState(false);

    const current = NAV.find((n) => (n.end ? location.pathname === n.to : location.pathname.startsWith(n.to)));

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    return (
        <div className="adm">
            <style>{adminCss}</style>
            {open && <div className="adm-scrim" onClick={() => setOpen(false)} />}
            <nav className={`adm-side ${open ? "open" : ""}`} aria-label="Admin navigation">
                <div className="adm-brand">Laundry Admin</div>
                {NAV.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        end={item.end}
                        className={({ isActive }) => `adm-link ${isActive ? "active" : ""}`}
                        onClick={() => setOpen(false)}
                    >
                        {item.label}
                    </NavLink>
                ))}
                <NavLink to="/dashboard" className="adm-link" onClick={() => setOpen(false)}>
                    Student view
                </NavLink>
            </nav>

            <div className="adm-main">
                <header className="adm-top">
                    <button className="adm-burger" onClick={() => setOpen(true)} aria-label="Open menu">
                        Menu
                    </button>
                    <h1>{current?.label ?? "Admin"}</h1>
                    <span className="adm-muted">{user?.firstName} {user?.lastName}</span>
                    <button onClick={handleLogout}>Log out</button>
                </header>
                <main className="adm-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default AdminLayout;