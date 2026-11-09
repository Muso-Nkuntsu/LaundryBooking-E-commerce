import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";

import { useAuth } from "../../context/useAuth";
import { getApiErrorMessage } from "../../services/Api";
import { adminService } from "../../services/adminService";
import { Badge, LoadState, Notice, fmtDateTime, useLoad } from "../../components/admin/ui";
import type { AdminUser } from "../../types/admin";

const loadUsers = () => adminService.getUsers();

function AdminUsers() {
    const { user: me } = useAuth();
    const { data, loading, error, reload } = useLoad(loadUsers);
    const [search, setSearch] = useState("");
    const [selectedId, setSelectedId] = useState<number | null>(null);
    const [form, setForm] = useState({ firstName: "", lastName: "", email: "", phoneNumber: "" });
    const [msg, setMsg] = useState<{ kind: "err" | "okk"; text: string } | null>(null);
    const [busy, setBusy] = useState(false);

    const selected = useMemo(() => data?.find((u) => u.studentId === selectedId) ?? null, [data, selectedId]);

    useEffect(() => {
        if (selected) {
            setForm({
                firstName: selected.firstName,
                lastName: selected.lastName,
                email: selected.email,
                phoneNumber: selected.phoneNumber,
            });
        }
    }, [selected]);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return (data ?? []).filter(
            (u) => !q || `${u.firstName} ${u.lastName} ${u.email} ${u.phoneNumber}`.toLowerCase().includes(q)
        );
    }, [data, search]);

    const run = async (action: () => Promise<AdminUser>, success: string) => {
        setBusy(true);
        setMsg(null);
        try {
            await action();
            setMsg({ kind: "okk", text: success });
            await reload();
        } catch (err) {
            setMsg({ kind: "err", text: getApiErrorMessage(err) });
        } finally {
            setBusy(false);
        }
    };

    const save = (event: FormEvent) => {
        event.preventDefault();
        if (selected) void run(() => adminService.updateUser(selected.studentId, form), "User updated.");
    };

    const toggleActive = () => {
        if (!selected) return;
        const next = !selected.active;
        if (!next && !window.confirm(`Deactivate ${selected.firstName}? They will be signed out and unable to log in.`)) return;
        void run(() => adminService.setUserActive(selected.studentId, next), next ? "Account activated." : "Account deactivated.");
    };

    const changeRole = (role: "USER" | "ADMIN") => {
        if (!selected || role === selected.role) return;
        if (!window.confirm(`Change ${selected.firstName}'s role to ${role}? They will have to sign in again.`)) return;
        void run(() => adminService.setUserRole(selected.studentId, role), `Role changed to ${role}.`);
    };

    const isSelf = selected?.studentId === me?.studentId;

    return (
        <div>
            <LoadState loading={loading} error={error} onRetry={reload} />
            {msg && <Notice kind={msg.kind}>{msg.text}</Notice>}

            <div className="adm-two">
                <section className="adm-card">
                    <div className="adm-toolbar">
                        <input
                            type="search"
                            placeholder="Search name, email or phone"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            aria-label="Search users"
                        />
                        <span className="adm-muted">{filtered.length} user(s)</span>
                    </div>
                    <div className="adm-table-wrap">
                        <table>
                            <thead>
                            <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Bookings</th><th>Orders</th></tr>
                            </thead>
                            <tbody>
                            {filtered.map((u) => (
                                <tr
                                    key={u.studentId}
                                    className={`clickable ${u.studentId === selectedId ? "sel" : ""}`}
                                    onClick={() => { setSelectedId(u.studentId); setMsg(null); }}
                                >
                                    <td>{u.firstName} {u.lastName}</td>
                                    <td>{u.email}</td>
                                    <td><Badge text={u.role} /></td>
                                    <td><Badge text={u.active ? "Active" : "Inactive"} /></td>
                                    <td>{u.bookingCount}</td>
                                    <td>{u.orderCount}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </section>

                <section className="adm-card">
                    <h2>User details</h2>
                    {!selected && <p className="adm-muted">Select a user from the list.</p>}
                    {selected && (
                        <>
                            <p className="adm-muted">
                                ID {selected.studentId} · joined {fmtDateTime(selected.createdAt)}
                            </p>
                            <form className="adm-form" onSubmit={save}>
                                <div>
                                    <label htmlFor="u-first">First name</label>
                                    <input id="u-first" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
                                </div>
                                <div>
                                    <label htmlFor="u-last">Last name</label>
                                    <input id="u-last" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
                                </div>
                                <div className="wide">
                                    <label htmlFor="u-email">Email</label>
                                    <input id="u-email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
                                </div>
                                <div className="wide">
                                    <label htmlFor="u-phone">Phone</label>
                                    <input id="u-phone" value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} required />
                                </div>
                                <div className="wide adm-row">
                                    <button className="primary" type="submit" disabled={busy}>Save changes</button>
                                </div>
                            </form>

                            <hr style={{ margin: "16px 0", border: 0, borderTop: "1px solid #e4e8f0" }} />

                            <div className="adm-row">
                                <label htmlFor="u-role" style={{ margin: 0 }}>Role</label>
                                <select id="u-role" value={selected.role} disabled={busy || isSelf} onChange={(e) => changeRole(e.target.value as "USER" | "ADMIN")}>
                                    <option value="USER">USER</option>
                                    <option value="ADMIN">ADMIN</option>
                                </select>
                                <button className={selected.active ? "danger" : "primary"} disabled={busy || isSelf} onClick={toggleActive}>
                                    {selected.active ? "Deactivate account" : "Activate account"}
                                </button>
                            </div>
                            {isSelf && <p className="adm-muted">You cannot change your own role or deactivate yourself.</p>}
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}

export default AdminUsers;