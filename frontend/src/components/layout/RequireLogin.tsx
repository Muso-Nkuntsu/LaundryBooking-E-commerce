import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import AppLayout from "./AppLayout";
import { isLoggedIn } from "../../services/session";

/** Shows the page inside the navigation bar, or sends visitors who are not logged in to the login page. */
function RequireLogin({ children }: { children: ReactNode }) {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return <AppLayout>{children}</AppLayout>;
}

export default RequireLogin;
