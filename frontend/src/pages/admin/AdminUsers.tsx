import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { ErrorState, Loading } from "../../components/common/States";
import TextField from "../../components/common/TextField";
import { useToast } from "../../context/useToast";
import { useFetch } from "../../hooks/useFetch";
import { adminApi } from "../../services/adminApi";
import type { AdminUser, Role, UserInput } from "../../services/adminApi";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { Modal, PageHead, Segmented, Status, money, shortDate, shortTime } from "./adminUi";

type StatusFilter = "" | "active" | "inactive" | "unverified";

// Students and administrators: search, view, edit, activate/deactivate and change roles.
function AdminUsers() {
  const [params] = useSearchParams();
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [role, setRole] = useState<"" | Role>("");
  const [status, setStatus] = useState<StatusFilter>((params.get("status") as StatusFilter) ?? "");
  const [selected, setSelected] = useState<number | null>(null);

  const key = `${q}|${role}|${status}`;
  const { data, loading, error, reload } = useFetch(() => adminApi.users({ q, role, status }), key);
  const users = data ?? [];

  return (
    <>
      <PageHead title="Students" intro="Everyone with an account. Click a row to see details or make changes." />

      <form className="adm-tools" onSubmit={(e) => { e.preventDefault(); setQ(search.trim()); }}>
        <input className="input grow" type="search" placeholder="Search name, email or phone" value={search}
               onChange={(e) => setSearch(e.target.value)} aria-label="Search students" />
        <select className="input" value={role} onChange={(e) => setRole(e.target.value as "" | Role)} aria-label="Role">
          <option value="">All roles</option>
          <option value="USER">Students</option>
          <option value="ADMIN">Administrators</option>
        </select>
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
      </form>
      <div className="adm-tools">
        <Segmented label="Account status" value={status} onChange={setStatus} options={[
          { value: "", label: "All" },
          { value: "active", label: "Active" },
          { value: "inactive", label: "Deactivated" },
          { value: "unverified", label: "Email not verified" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : (
        <section className="card">
          <p className="adm-count">{users.length} {users.length === 1 ? "account" : "accounts"}</p>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr><th>Name</th><th>Phone</th><th>Role</th><th>Account</th><th>Joined</th></tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.studentId} className="clickable" onClick={() => setSelected(u.studentId)}>
                    <td>
                      <button type="button" className="link-btn" onClick={(e) => { e.stopPropagation(); setSelected(u.studentId); }}>
                        {u.firstName} {u.lastName}
                      </button>
                      <span className="sub">{u.email}</span>
                    </td>
                    <td>{u.phoneNumber}</td>
                    <td><span className={`pill ${u.role === "ADMIN" ? "pill-info" : "pill-off"}`}>{u.role === "ADMIN" ? "Admin" : "Student"}</span></td>
                    <td>
                      {u.disabled ? <span className="pill pill-bad">Deactivated</span>
                        : u.emailVerified ? <span className="pill pill-ok">Active</span>
                        : <span className="pill pill-warn">Email not verified</span>}
                    </td>
                    <td>{shortDate(u.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {users.length === 0 && <p className="adm-empty">No accounts match.</p>}
          </div>
        </section>
      )}

      {selected !== null && <UserDetails id={selected} onClose={() => setSelected(null)} onChanged={reload} />}
    </>
  );
}

type Confirm = { title: string; message: string; label: string; run: () => Promise<unknown> } | null;

function UserDetails({ id, onClose, onChanged }: { id: number; onClose: () => void; onChanged: () => void }) {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(() => adminApi.user(id), id);
  const [form, setForm] = useState<UserInput | null>(null);
  const [busy, setBusy] = useState(false);
  const [confirm, setConfirm] = useState<Confirm>(null);
  const isMe = id === getStudentId();

  useEffect(() => {
    if (data) {
      const u = data.user;
      setForm({ firstName: u.firstName, lastName: u.lastName, email: u.email, phoneNumber: u.phoneNumber });
    }
  }, [data]);

  const run = async (action: () => Promise<unknown>, success: string) => {
    setBusy(true);
    try {
      await action();
      toast.success(success);
      reload();
      onChanged();
    } catch (err) {
      toast.error(friendlyError(err, "That didn't work. Try again."));
    } finally {
      setBusy(false);
      setConfirm(null);
    }
  };

  const save = (event: FormEvent) => {
    event.preventDefault();
    if (form) run(() => adminApi.updateUser(id, form), "Details saved.");
  };

  const field = (name: keyof UserInput, label: string, type = "text") => form && (
    <TextField id={`user-${name}`} label={label} type={type} value={form[name]}
               onChange={(value) => setForm({ ...form, [name]: value })} />
  );

  const u: AdminUser | undefined = data?.user;

  return (
    <Modal title="Student details" onClose={onClose}>
      {loading && !data ? <Loading /> : error || !data || !u || !form ? <ErrorState message={error ?? "Not found"} onRetry={reload} /> : (
        <>
          <dl className="adm-facts">
            <div><dt>Role</dt><dd>{u.role === "ADMIN" ? "Administrator" : "Student"}</dd></div>
            <div><dt>Account</dt><dd>{u.disabled ? "Deactivated" : "Active"}</dd></div>
            <div><dt>Email</dt><dd>{u.emailVerified ? "Verified" : "Not verified"}</dd></div>
            <div><dt>Joined</dt><dd>{shortDate(u.createdAt)}</dd></div>
            <div><dt>Bookings</dt><dd>{data.bookingCount}</dd></div>
            <div><dt>Orders</dt><dd>{data.orderCount}</dd></div>
            <div><dt>Total paid</dt><dd>{money(data.totalPaid)}</dd></div>
          </dl>

          <form className="adm-section" onSubmit={save}>
            <h3>Edit details</h3>
            <div className="form-grid">
              {field("firstName", "First name")}
              {field("lastName", "Last name")}
              {field("email", "Email", "email")}
              {field("phoneNumber", "Phone number", "tel")}
            </div>
            <p className="muted small">If you change the email address, the student must verify the new one before logging in again.</p>
            <div><button type="submit" className="btn btn-primary btn-sm" disabled={busy}>Save details</button></div>
          </form>

          <div className="adm-section">
            <h3>Account access</h3>
            {isMe && <p className="muted small">This is your own account, so you can't deactivate it or change its role.</p>}
            <div className="btn-row">
              {!u.emailVerified && (
                <button type="button" className="btn btn-ghost btn-sm" disabled={busy}
                        onClick={() => run(() => adminApi.verifyUserEmail(id), "Email marked as verified.")}>
                  Mark email as verified
                </button>
              )}
              {u.disabled ? (
                <button type="button" className="btn btn-primary btn-sm" disabled={busy}
                        onClick={() => run(() => adminApi.setUserEnabled(id, true), "Account activated.")}>
                  Activate account
                </button>
              ) : (
                <button type="button" className="btn btn-danger btn-sm" disabled={busy || isMe}
                        onClick={() => setConfirm({
                          title: "Deactivate this account?",
                          message: `${u.firstName} will be logged out straight away and won't be able to log in until you activate the account again.`,
                          label: "Deactivate",
                          run: () => adminApi.setUserEnabled(id, false),
                        })}>
                  Deactivate account
                </button>
              )}
              {u.role === "USER" ? (
                <button type="button" className="btn btn-ghost btn-sm" disabled={busy || isMe}
                        onClick={() => setConfirm({
                          title: "Make this student an administrator?",
                          message: `${u.firstName} will be able to open this dashboard and change students, bookings, products, orders and payments.`,
                          label: "Make administrator",
                          run: () => adminApi.setUserRole(id, "ADMIN"),
                        })}>
                  Make administrator
                </button>
              ) : (
                <button type="button" className="btn btn-danger btn-sm" disabled={busy || isMe}
                        onClick={() => setConfirm({
                          title: "Remove administrator access?",
                          message: `${u.firstName} will become a normal student account.`,
                          label: "Remove access",
                          run: () => adminApi.setUserRole(id, "USER"),
                        })}>
                  Remove administrator access
                </button>
              )}
            </div>
          </div>

          <div className="adm-section">
            <h3>Latest bookings</h3>
            {data.recentBookings.length === 0 ? <p className="muted">No bookings.</p> : (
              <ul className="list">
                {data.recentBookings.map((b) => (
                  <li key={b.bookingId}>
                    <span>#{b.bookingId} · {shortDate(b.date)} {shortTime(b.startTime)} · {b.machineNumber}</span>
                    <Status kind="booking" value={b.status} />
                  </li>
                ))}
              </ul>
            )}
            <h3>Latest orders</h3>
            {data.recentOrders.length === 0 ? <p className="muted">No orders.</p> : (
              <ul className="list">
                {data.recentOrders.map((o) => (
                  <li key={o.orderId}>
                    <span>#{o.orderId} · {shortDate(o.orderDate)} · {money(o.totalAmount)}</span>
                    <Status kind="order" value={o.status} />
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}

      {confirm && (
        <ConfirmDialog title={confirm.title} message={confirm.message} confirmLabel={confirm.label} cancelLabel="Go back"
                       busy={busy} onCancel={() => setConfirm(null)} onConfirm={() => run(confirm.run, "Done.")} />
      )}
    </Modal>
  );
}

export default AdminUsers;
