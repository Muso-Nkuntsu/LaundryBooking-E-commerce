import type { ReactNode } from "react";
import Drum from "../common/Drum";
import { APP_NAME } from "../../config";

function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="auth">
      <aside className="auth-art">
        <span className="brand">
          <Drum tone="dark" size={30} />
          {APP_NAME}
        </span>
        <div>
          <h1>Laundry day, booked.</h1>
          <p>Pick a machine and a time before you carry the basket downstairs.</p>
        </div>
        <div className="porthole" aria-hidden="true">
          <Drum tone="dark" state="spinning" slow size="100%" />
        </div>
      </aside>
      <section className="auth-form">
        <div>{children}</div>
      </section>
    </div>
  );
}

export default AuthLayout;
