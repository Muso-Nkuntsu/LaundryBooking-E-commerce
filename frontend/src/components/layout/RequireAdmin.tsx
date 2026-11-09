import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import AdminLayout from "../../pages/admin/AdminLayout";
import { isAdmin, isLoggedIn } from "../../services/session";

/**
 * Shows an admin page inside the admin layout. Visitors who aren't logged in go to the login page,
 * and students go to their own dashboard.
 * This only hides the pages: the backend refuses every /api/admin request from a non-admin anyway.
 */
function RequireAdmin({ children }: { children: ReactNode }) {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin()) {
    return <Navigate to="/dashboard" replace />;
  }
  return <AdminLayout>{children}</AdminLayout>;
}

export default RequireAdmin;
