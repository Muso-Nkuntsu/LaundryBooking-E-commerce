import { useState } from "react";
import type { FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { ErrorState, Loading } from "../../components/common/States";
import TextField from "../../components/common/TextField";
import { useToast } from "../../context/useToast";
import { useFetch } from "../../hooks/useFetch";
import { MACHINE_TYPES, adminApi } from "../../services/adminApi";
import type { Machine, MachineStatus, Room, TimeSlot } from "../../services/adminApi";
import { friendlyError } from "../../utilis/errorMessage";
import { Modal, PageHead, Segmented, Status, shortDate, shortTime, statusLabel, todayIso } from "./adminUi";

type Tab = "rooms" | "machines" | "slots";
type Confirm = { title: string; message: string; label: string; run: () => Promise<unknown>; done: string } | null;

/** Runs an admin action, shows a message, and reloads. */
function useRunner(reload: () => void) {
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const run = async (action: () => Promise<unknown>, success: string): Promise<boolean> => {
    setBusy(true);
    try {
      await action();
      toast.success(success);
      reload();
      return true;
    } catch (err) {
      toast.error(friendlyError(err, "That didn't work. Try again."));
      return false;
    } finally {
      setBusy(false);
    }
  };
  return { busy, run };
}

// Laundry rooms, machines and the time slots students can book.
function AdminFacilities() {
  const [params] = useSearchParams();
  const [tab, setTab] = useState<Tab>((params.get("tab") as Tab) || "machines");

  return (
    <>
      <PageHead title="Machines & slots" intro="Rooms hold machines. Students book a machine for a time slot." />
      <div className="adm-tabs">
        <Segmented label="Section" value={tab} onChange={setTab} options={[
          { value: "rooms", label: "Laundry rooms" },
          { value: "machines", label: "Machines" },
          { value: "slots", label: "Time slots" },
        ]} />
      </div>
      {tab === "rooms" && <Rooms />}
      {tab === "machines" && <Machines />}
      {tab === "slots" && <Slots />}
    </>
  );
}

// =====================================================================
// Rooms
// =====================================================================

function Rooms() {
  const { data, loading, error, reload } = useFetch(() => adminApi.rooms());
  const { busy, run } = useRunner(reload);
  const [editing, setEditing] = useState<Room | "new" | null>(null);
  const [confirm, setConfirm] = useState<Confirm>(null);
  const rooms = data ?? [];

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading && !data) return <Loading />;

  return (
    <section className="card">
      <div className="row" style={{ marginBottom: 12 }}>
        <p className="adm-count" style={{ margin: 0 }}>{rooms.length} {rooms.length === 1 ? "room" : "rooms"}</p>
        <button type="button" className="btn btn-primary btn-sm" onClick={() => setEditing("new")}>Add room</button>
      </div>
      <div className="table-wrap">
        <table className="table">
          <thead><tr><th>Room</th><th>Location</th><th className="num">Machines</th><th>Status</th><th /></tr></thead>
          <tbody>
            {rooms.map((r) => (
              <tr key={r.roomId}>
                <td><strong>{r.roomNumber}</strong>{r.description && <span className="sub">{r.description}</span>}</td>
                <td>{r.location}</td>
                <td className="num">{r.machineCount} / {r.capacity}</td>
                <td>{r.active ? <span className="pill pill-ok">Open</span> : <span className="pill pill-off">Closed</span>}</td>
                <td>
                  <div className="actions-cell">
                    <button type="button" className="btn btn-ghost btn-sm" onClick={() => setEditing(r)}>Edit</button>
                    <button type="button" className="btn btn-ghost btn-sm" disabled={busy}
                            onClick={() => run(() => adminApi.setRoomActive(r.roomId, !r.active), r.active ? "Room closed." : "Room opened.")}>
                      {r.active ? "Close" : "Open"}
                    </button>
                    <button type="button" className="btn btn-danger btn-sm" disabled={busy}
                            onClick={() => setConfirm({ title: `Delete room ${r.roomNumber}?`, label: "Delete room", done: "Room deleted.",
                              message: "This can't be undone. A room that still has machines can't be deleted.",
                              run: () => adminApi.deleteRoom(r.roomId) })}>
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {rooms.length === 0 && <p className="adm-empty">No rooms yet. Add the first laundry room.</p>}
      </div>

      {editing && <RoomForm room={editing === "new" ? null : editing} onClose={() => setEditing(null)}
                            onSaved={() => { setEditing(null); reload(); }} />}
      {confirm && <ConfirmDialog title={confirm.title} message={confirm.message} confirmLabel={confirm.label} cancelLabel="Go back"
                                 busy={busy} onCancel={() => setConfirm(null)}
                                 onConfirm={async () => { await run(confirm.run, confirm.done); setConfirm(null); }} />}
    </section>
  );
}

function RoomForm({ room, onClose, onSaved }: { room: Room | null; onClose: () => void; onSaved: () => void }) {
  const [roomNumber, setRoomNumber] = useState(room?.roomNumber ?? "");
  const [location, setLocation] = useState(room?.location ?? "");
  const [capacity, setCapacity] = useState(String(room?.capacity ?? 6));
  const [description, setDescription] = useState(room?.description ?? "");
  const { busy, run } = useRunner(() => undefined);

  const save = async (event: FormEvent) => {
    event.preventDefault();
    const body = { roomNumber: roomNumber.trim(), location: location.trim(), capacity: Number(capacity), description: description.trim() };
    const ok = await run(() => (room ? adminApi.updateRoom(room.roomId, body) : adminApi.createRoom(body)),
      room ? "Room saved." : "Room added.");
    if (ok) onSaved();
  };

  return (
    <Modal title={room ? `Edit room ${room.roomNumber}` : "Add a laundry room"} onClose={onClose} narrow>
      <form className="stack" onSubmit={save}>
        <TextField id="room-number" label="Room number" value={roomNumber} onChange={setRoomNumber} required
                   disabled={!!room} hint={room ? "The room number can't be changed." : "For example A or Block B"} />
        <TextField id="room-location" label="Location" value={location} onChange={setLocation} required placeholder="Block A, ground floor" />
        <TextField id="room-capacity" label="Number of machines it can hold" type="number" min={1} max={200} value={capacity} onChange={setCapacity} required />
        <TextField id="room-description" label="Description (optional)" value={description} onChange={setDescription} />
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Saving..." : "Save"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

// =====================================================================
// Machines
// =====================================================================

const MACHINE_STATUSES: MachineStatus[] = ["AVAILABLE", "IN_USE", "OUT_OF_ORDER"];

function Machines() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(() => Promise.all([adminApi.machines(), adminApi.rooms()]));
  const { busy, run } = useRunner(reload);
  const [editing, setEditing] = useState<Machine | "new" | null>(null);
  const [confirm, setConfirm] = useState<Confirm>(null);

  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (loading && !data) return <Loading />;
  const [machines, rooms] = data ?? [[], []];

  const changeStatus = (m: Machine, status: MachineStatus) =>
    run(async () => {
      const result = await adminApi.setMachineStatus(m.machineId, status);
      if (result.upcomingBookingsAffected > 0) {
        toast.error(`${m.machineNumber} has ${result.upcomingBookingsAffected} upcoming booking(s). Check them on the Bookings page.`);
      }
    }, `${m.machineNumber}: ${statusLabel("machine", status)}.`);

  return (
    <section className="card">
      <div className="row" style={{ marginBottom: 12 }}>
        <p className="adm-count" style={{ margin: 0 }}>{machines.length} {machines.length === 1 ? "machine" : "machines"}</p>
        <button type="button" className="btn btn-primary btn-sm" onClick={() => setEditing("new")} disabled={rooms.length === 0}
                title={rooms.length === 0 ? "Add a laundry room first" : undefined}>Add machine</button>
      </div>
      <div className="table-wrap">
        <table className="table">
          <thead><tr><th>Machine</th><th>Room</th><th className="num">Upcoming bookings</th><th>Status</th><th /></tr></thead>
          <tbody>
            {machines.map((m) => (
              <tr key={m.machineId}>
                <td><strong>{m.machineNumber}</strong><span className="sub">{m.type}</span></td>
                <td>{m.roomNumber}</td>
                <td className="num">{m.upcomingBookings}</td>
                <td>
                  <select className="input" style={{ minHeight: 38, padding: "6px 10px", width: "auto" }} value={m.status}
                          disabled={busy} aria-label={`Status of ${m.machineNumber}`}
                          onChange={(e) => changeStatus(m, e.target.value as MachineStatus)}>
                    {MACHINE_STATUSES.map((s) => <option key={s} value={s}>{statusLabel("machine", s)}</option>)}
                  </select>
                </td>
                <td>
                  <div className="actions-cell">
                    <button type="button" className="btn btn-ghost btn-sm" onClick={() => setEditing(m)}>Edit</button>
                    <button type="button" className="btn btn-danger btn-sm" disabled={busy}
                            onClick={() => setConfirm({ title: `Delete ${m.machineNumber}?`, label: "Delete machine", done: "Machine deleted.",
                              message: "This can't be undone. A machine that has ever been booked can't be deleted; set it to Out of order instead.",
                              run: () => adminApi.deleteMachine(m.machineId) })}>
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {machines.length === 0 && <p className="adm-empty">No machines yet.</p>}
      </div>
      <p className="muted small" style={{ marginTop: 10 }}>
        <Status kind="machine" value="OUT_OF_ORDER" /> machines can't be booked. Setting a machine out of order doesn't cancel bookings that are already made.
      </p>

      {editing && <MachineForm machine={editing === "new" ? null : editing} rooms={rooms} onClose={() => setEditing(null)}
                               onSaved={() => { setEditing(null); reload(); }} />}
      {confirm && <ConfirmDialog title={confirm.title} message={confirm.message} confirmLabel={confirm.label} cancelLabel="Go back"
                                 busy={busy} onCancel={() => setConfirm(null)}
                                 onConfirm={async () => { await run(confirm.run, confirm.done); setConfirm(null); }} />}
    </section>
  );
}

function MachineForm({ machine, rooms, onClose, onSaved }: {
  machine: Machine | null; rooms: Room[]; onClose: () => void; onSaved: () => void;
}) {
  const [machineNumber, setMachineNumber] = useState(machine?.machineNumber ?? "");
  const [type, setType] = useState(machine?.type ?? MACHINE_TYPES[0]);
  const [roomId, setRoomId] = useState(String(machine?.roomId ?? rooms[0]?.roomId ?? ""));
  const [status, setStatus] = useState<MachineStatus>(machine?.status ?? "AVAILABLE");
  const { busy, run } = useRunner(() => undefined);
  const types = MACHINE_TYPES.includes(type) ? MACHINE_TYPES : [type, ...MACHINE_TYPES];

  const save = async (event: FormEvent) => {
    event.preventDefault();
    const body = { machineNumber: machineNumber.trim(), type, roomId: Number(roomId), status };
    const ok = await run(() => (machine ? adminApi.updateMachine(machine.machineId, body) : adminApi.createMachine(body)),
      machine ? "Machine saved." : "Machine added.");
    if (ok) onSaved();
  };

  return (
    <Modal title={machine ? `Edit ${machine.machineNumber}` : "Add a machine"} onClose={onClose} narrow>
      <form className="stack" onSubmit={save}>
        <TextField id="m-number" label="Machine number" value={machineNumber} onChange={setMachineNumber} required placeholder="A-W5" />
        <div className="field">
          <label htmlFor="m-type">Type</label>
          <select id="m-type" className="input" value={type} onChange={(e) => setType(e.target.value)}>
            {types.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="m-room">Laundry room</label>
          <select id="m-room" className="input" value={roomId} onChange={(e) => setRoomId(e.target.value)} required>
            {rooms.map((r) => <option key={r.roomId} value={r.roomId}>{r.roomNumber} – {r.location} ({r.machineCount}/{r.capacity})</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="m-status">Status</label>
          <select id="m-status" className="input" value={status} onChange={(e) => setStatus(e.target.value as MachineStatus)}>
            {MACHINE_STATUSES.map((s) => <option key={s} value={s}>{statusLabel("machine", s)}</option>)}
          </select>
        </div>
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Saving..." : "Save"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

// =====================================================================
// Time slots
// =====================================================================

function Slots() {
  const [from, setFrom] = useState(todayIso());
  const [to, setTo] = useState(todayIso(6));
  const { data, loading, error, reload } = useFetch(() => adminApi.timeSlots(from, to), `${from}|${to}`);
  const { busy, run } = useRunner(reload);
  const [adding, setAdding] = useState<"one" | "many" | null>(null);
  const [confirm, setConfirm] = useState<Confirm>(null);
  const slots = data ?? [];

  const byDate = slots.reduce<Record<string, TimeSlot[]>>((groups, slot) => {
    (groups[slot.date] ??= []).push(slot);
    return groups;
  }, {});

  return (
    <>
      <div className="adm-tools">
        <label className="small muted" htmlFor="s-from">From</label>
        <input id="s-from" className="input" type="date" value={from} onChange={(e) => e.target.value && setFrom(e.target.value)} />
        <label className="small muted" htmlFor="s-to">To</label>
        <input id="s-to" className="input" type="date" value={to} onChange={(e) => e.target.value && setTo(e.target.value)} />
        <span style={{ flex: 1 }} />
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setAdding("one")}>Add one slot</button>
        <button type="button" className="btn btn-primary btn-sm" onClick={() => setAdding("many")}>Create slots for several days</button>
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : slots.length === 0 ? (
        <div className="card adm-empty">No time slots in these dates. Use "Create slots for several days" to add them.</div>
      ) : (
        Object.entries(byDate).map(([date, daySlots]) => (
          <section className="card" key={date}>
            <h3 style={{ marginBottom: 10 }}>{shortDate(date)}</h3>
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Time</th><th className="num">Bookings</th><th>Status</th><th /></tr></thead>
                <tbody>
                  {daySlots.map((s) => (
                    <tr key={s.timeSlotId}>
                      <td>{shortTime(s.startTime)}–{shortTime(s.endTime)}</td>
                      <td className="num">{s.bookings}</td>
                      <td>{s.available ? <span className="pill pill-ok">Open</span> : <span className="pill pill-off">Closed</span>}</td>
                      <td>
                        <div className="actions-cell">
                          <button type="button" className="btn btn-ghost btn-sm" disabled={busy}
                                  onClick={() => run(() => adminApi.setSlotAvailable(s.timeSlotId, !s.available),
                                    s.available ? "Slot closed. It can't be booked now." : "Slot opened for booking.")}>
                            {s.available ? "Close" : "Open"}
                          </button>
                          <button type="button" className="btn btn-danger btn-sm" disabled={busy || s.bookings > 0}
                                  title={s.bookings > 0 ? "This slot has bookings. Close it instead." : undefined}
                                  onClick={() => setConfirm({ title: "Delete this slot?", label: "Delete slot", done: "Slot deleted.",
                                    message: `${shortDate(s.date)}, ${shortTime(s.startTime)}–${shortTime(s.endTime)}. This can't be undone.`,
                                    run: () => adminApi.deleteSlot(s.timeSlotId) })}>
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        ))
      )}

      {adding === "one" && <OneSlotForm onClose={() => setAdding(null)} onSaved={() => { setAdding(null); reload(); }} />}
      {adding === "many" && <GenerateSlotsForm onClose={() => setAdding(null)} onSaved={() => { setAdding(null); reload(); }} />}
      {confirm && <ConfirmDialog title={confirm.title} message={confirm.message} confirmLabel={confirm.label} cancelLabel="Go back"
                                 busy={busy} onCancel={() => setConfirm(null)}
                                 onConfirm={async () => { await run(confirm.run, confirm.done); setConfirm(null); }} />}
    </>
  );
}

function OneSlotForm({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const [date, setDate] = useState(todayIso(1));
  const [startTime, setStartTime] = useState("08:00");
  const [endTime, setEndTime] = useState("09:00");
  const { busy, run } = useRunner(() => undefined);

  const save = async (event: FormEvent) => {
    event.preventDefault();
    if (await run(() => adminApi.createSlot({ date, startTime, endTime }), "Slot added.")) onSaved();
  };

  return (
    <Modal title="Add one time slot" onClose={onClose} narrow>
      <form className="stack" onSubmit={save}>
        <TextField id="one-date" label="Date" type="date" min={todayIso()} value={date} onChange={setDate} required />
        <div className="form-grid">
          <TextField id="one-start" label="Starts" type="time" value={startTime} onChange={setStartTime} required />
          <TextField id="one-end" label="Ends" type="time" value={endTime} onChange={setEndTime} required />
        </div>
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Saving..." : "Add slot"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

function GenerateSlotsForm({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const toast = useToast();
  const [fromDate, setFromDate] = useState(todayIso(1));
  const [toDate, setToDate] = useState(todayIso(7));
  const [dayStart, setDayStart] = useState("06:00");
  const [dayEnd, setDayEnd] = useState("22:00");
  const [slotMinutes, setSlotMinutes] = useState("60");
  const [busy, setBusy] = useState(false);

  const save = async (event: FormEvent) => {
    event.preventDefault();
    setBusy(true);
    try {
      const result = await adminApi.generateSlots({ fromDate, toDate, dayStart, dayEnd, slotMinutes: Number(slotMinutes) });
      toast.success(`${result.created} slot(s) created${result.skipped > 0 ? `, ${result.skipped} already existed` : ""}.`);
      onSaved();
    } catch (err) {
      toast.error(friendlyError(err, "The slots couldn't be created."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="Create slots for several days" onClose={onClose} narrow>
      <form className="stack" onSubmit={save}>
        <p className="muted small">Back-to-back slots are made for every day in the range. Slots that already exist are skipped. Up to 31 days at a time.</p>
        <div className="form-grid">
          <TextField id="g-from" label="First day" type="date" min={todayIso()} value={fromDate} onChange={setFromDate} required />
          <TextField id="g-to" label="Last day" type="date" min={fromDate} value={toDate} onChange={setToDate} required />
          <TextField id="g-start" label="Laundry opens" type="time" value={dayStart} onChange={setDayStart} required />
          <TextField id="g-end" label="Laundry closes" type="time" value={dayEnd} onChange={setDayEnd} required />
        </div>
        <div className="field">
          <label htmlFor="g-len">Length of each slot</label>
          <select id="g-len" className="input" value={slotMinutes} onChange={(e) => setSlotMinutes(e.target.value)}>
            <option value="30">30 minutes</option>
            <option value="45">45 minutes</option>
            <option value="60">1 hour</option>
            <option value="90">1 hour 30 minutes</option>
            <option value="120">2 hours</option>
          </select>
        </div>
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Creating..." : "Create slots"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

export default AdminFacilities;
