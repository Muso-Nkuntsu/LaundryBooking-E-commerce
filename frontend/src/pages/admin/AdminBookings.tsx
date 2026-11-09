import { Fragment, useMemo, useState } from "react";
import type { FormEvent } from "react";

import { getApiErrorMessage } from "../../services/Api";
import { adminService } from "../../services/adminService";
import { Badge, LoadState, Notice, money, useLoad } from "../../components/admin/ui";
import type { BookingStatus, MachineStatus } from "../../types/admin";

const BOOKING_STATUSES: BookingStatus[] = ["PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"];
const MACHINE_STATUSES: MachineStatus[] = ["AVAILABLE", "IN_USE", "OUT_OF_ORDER"];

const loadBookings = () => adminService.getBookings();
const loadMachines = () => adminService.getMachines();
const loadRooms = () => adminService.getRooms();

function AdminBookings() {
    const [tab, setTab] = useState<"bookings" | "machines">("bookings");
    const bookings = useLoad(loadBookings);
    const machines = useLoad(loadMachines);
    const rooms = useLoad(loadRooms);
    const [filter, setFilter] = useState<"ALL" | BookingStatus>("ALL");
    const [openId, setOpenId] = useState<number | null>(null);
    const [msg, setMsg] = useState<{ kind: "err" | "okk"; text: string } | null>(null);
    const [busy, setBusy] = useState(false);
    const [newMachine, setNewMachine] = useState({ number: "", type: "", status: "AVAILABLE" as MachineStatus, roomId: "" });

    const shown = useMemo(
        () => (bookings.data ?? []).filter((b) => filter === "ALL" || b.status === filter),
        [bookings.data, filter]
    );

    const run = async (action: () => Promise<unknown>, success: string, after: () => Promise<void>) => {
        setBusy(true);
        setMsg(null);
        try {
            await action();
            setMsg({ kind: "okk", text: success });
            await after();
        } catch (err) {
            setMsg({ kind: "err", text: getApiErrorMessage(err) });
        } finally {
            setBusy(false);
        }
    };

    const addMachine = (event: FormEvent) => {
        event.preventDefault();
        if (!newMachine.roomId) {
            setMsg({ kind: "err", text: "Choose a laundry room for the machine." });
            return;
        }
        void run(
            () => adminService.createMachine(newMachine.number.trim(), newMachine.type.trim(), newMachine.status, Number(newMachine.roomId)),
            "Machine added.",
            async () => {
                setNewMachine({ number: "", type: "", status: "AVAILABLE", roomId: "" });
                await machines.reload();
            }
        );
    };

    return (
        <div>
            <div className="adm-tabs">
                <button className={tab === "bookings" ? "on" : ""} onClick={() => setTab("bookings")}>Bookings</button>
                <button className={tab === "machines" ? "on" : ""} onClick={() => setTab("machines")}>Machines & availability</button>
            </div>
            {msg && <Notice kind={msg.kind}>{msg.text}</Notice>}

            {tab === "bookings" && (
                <section className="adm-card">
                    <div className="adm-toolbar">
                        <label htmlFor="b-filter" style={{ margin: 0 }}>Status</label>
                        <select id="b-filter" value={filter} onChange={(e) => setFilter(e.target.value as "ALL" | BookingStatus)}>
                            <option value="ALL">All</option>
                            {BOOKING_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                        </select>
                        <span className="adm-muted">{shown.length} booking(s)</span>
                        <button onClick={bookings.reload}>Refresh</button>
                    </div>
                    <LoadState loading={bookings.loading} error={bookings.error} onRetry={bookings.reload} />
                    <div className="adm-table-wrap">
                        <table>
                            <thead>
                            <tr><th>#</th><th>Student</th><th>Room / machine</th><th>Date</th><th>Time</th><th>Total</th><th>Status</th><th>Update</th></tr>
                            </thead>
                            <tbody>
                            {shown.map((b) => (
                                <Fragment key={b.id}>
                                    <tr className="clickable" onClick={() => setOpenId(openId === b.id ? null : b.id)}>
                                        <td>{b.id}</td>
                                        <td>{b.student?.name ?? "-"}</td>
                                        <td>{b.roomNumber ?? "-"} / {b.machineNumber ?? "-"}</td>
                                        <td>{b.slotDate ?? "-"}</td>
                                        <td>{b.startTime?.slice(0, 5)}-{b.endTime?.slice(0, 5)}</td>
                                        <td>{money(b.totalAmount)}</td>
                                        <td><Badge text={b.status} /></td>
                                        <td onClick={(e) => e.stopPropagation()}>
                                            <select
                                                aria-label={`Status for booking ${b.id}`}
                                                value={b.status}
                                                disabled={busy}
                                                onChange={(e) =>
                                                    void run(
                                                        () => adminService.updateBookingStatus(b.id, e.target.value as BookingStatus),
                                                        `Booking #${b.id} updated.`,
                                                        async () => { await bookings.reload(); }
                                                    )
                                                }
                                            >
                                                {BOOKING_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                                            </select>
                                        </td>
                                    </tr>
                                    {openId === b.id && (
                                        <tr>
                                            <td colSpan={8}>
                                                <strong>Booking #{b.id}</strong> · {b.student?.name} ({b.student?.email}) · Service: {b.serviceName ?? "none"} ·
                                                Booked on {b.bookingDate?.replace("T", " ").slice(0, 16)} · Machine status: {b.machineStatus ?? "-"}
                                            </td>
                                        </tr>
                                    )}
                                </Fragment>
                            ))}
                            </tbody>
                        </table>
                    </div>
                    <p className="adm-muted">
                        The server blocks double bookings: a machine cannot be booked twice for the same or an overlapping time slot,
                        and a student cannot hold two overlapping bookings. Cancelled bookings free the slot.
                    </p>
                </section>
            )}

            {tab === "machines" && (
                <>
                    <section className="adm-card">
                        <h2>Add a machine</h2>
                        <form className="adm-form" onSubmit={addMachine}>
                            <div>
                                <label htmlFor="m-num">Machine number</label>
                                <input id="m-num" value={newMachine.number} onChange={(e) => setNewMachine({ ...newMachine, number: e.target.value })} required />
                            </div>
                            <div>
                                <label htmlFor="m-type">Type</label>
                                <input id="m-type" placeholder="Washer / Dryer" value={newMachine.type} onChange={(e) => setNewMachine({ ...newMachine, type: e.target.value })} required />
                            </div>
                            <div>
                                <label htmlFor="m-room">Laundry room</label>
                                <select id="m-room" value={newMachine.roomId} onChange={(e) => setNewMachine({ ...newMachine, roomId: e.target.value })} required>
                                    <option value="">Select room</option>
                                    {(rooms.data ?? []).map((r) => <option key={r.roomId} value={r.roomId}>{r.roomNumber}</option>)}
                                </select>
                            </div>
                            <div>
                                <label htmlFor="m-status">Status</label>
                                <select id="m-status" value={newMachine.status} onChange={(e) => setNewMachine({ ...newMachine, status: e.target.value as MachineStatus })}>
                                    {MACHINE_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                                </select>
                            </div>
                            <div className="wide"><button className="primary" type="submit" disabled={busy}>Add machine</button></div>
                        </form>
                    </section>

                    <section className="adm-card">
                        <h2>Machines</h2>
                        <LoadState loading={machines.loading} error={machines.error} onRetry={machines.reload} />
                        <div className="adm-table-wrap">
                            <table>
                                <thead><tr><th>Number</th><th>Type</th><th>Room</th><th>Availability</th><th></th></tr></thead>
                                <tbody>
                                {(machines.data ?? []).map((m) => (
                                    <tr key={m.machineId}>
                                        <td>{m.machineNumber}</td>
                                        <td>{m.type}</td>
                                        <td>{m.laundryRoom?.roomNumber ?? "-"}</td>
                                        <td>
                                            <select
                                                aria-label={`Status for machine ${m.machineNumber}`}
                                                value={m.status}
                                                disabled={busy}
                                                onChange={(e) =>
                                                    void run(
                                                        () => adminService.updateMachineStatus(m.machineId, e.target.value as MachineStatus),
                                                        `Machine ${m.machineNumber} set to ${e.target.value}.`,
                                                        async () => { await machines.reload(); }
                                                    )
                                                }
                                            >
                                                {MACHINE_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                                            </select>{" "}
                                            <Badge text={m.status} />
                                        </td>
                                        <td>
                                            <button
                                                className="danger"
                                                disabled={busy}
                                                onClick={() => {
                                                    if (!window.confirm(`Delete machine ${m.machineNumber}?`)) return;
                                                    void run(() => adminService.deleteMachine(m.machineId), "Machine deleted.", async () => { await machines.reload(); });
                                                }}
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </section>
                </>
            )}
        </div>
    );
}

export default AdminBookings;