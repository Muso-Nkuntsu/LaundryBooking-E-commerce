import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { ErrorState, Loading } from "../../components/common/States";
import { useToast } from "../../context/useToast";
import { useFetch } from "../../hooks/useFetch";
import { BOOKING_NEXT, adminApi } from "../../services/adminApi";
import type { AdminBooking, BookingStatus, Schedule } from "../../services/adminApi";
import { friendlyError } from "../../utilis/errorMessage";
import { ACTION_LABEL, Modal, PageHead, Segmented, Status, dateTime, money, shortDate, shortTime, statusLabel, todayIso } from "./adminUi";

type Tab = "list" | "schedule";

// Every laundry booking: filter, view details, change status, and see a day's schedule.
function AdminBookings() {
  const [params] = useSearchParams();
  const [tab, setTab] = useState<Tab>("list");

  return (
    <>
      <PageHead title="Bookings" intro="Two students can never book the same machine in the same slot; the system blocks it." />
      <div className="adm-tabs">
        <Segmented label="View" value={tab} onChange={setTab} options={[
          { value: "list", label: "All bookings" },
          { value: "schedule", label: "Day schedule" },
        ]} />
      </div>
      {tab === "list" ? <BookingList initialStatus={(params.get("status") as BookingStatus) ?? ""} /> : <DaySchedule />}
    </>
  );
}

function BookingList({ initialStatus }: { initialStatus: "" | BookingStatus }) {
  const toast = useToast();
  const [status, setStatus] = useState<"" | BookingStatus>(initialStatus);
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [selected, setSelected] = useState<AdminBooking | null>(null);
  const [cancelling, setCancelling] = useState<AdminBooking | null>(null);
  const [busy, setBusy] = useState(false);

  const key = `${status}|${from}|${to}|${q}`;
  const { data, loading, error, reload } = useFetch(() => adminApi.bookings({ status, from, to, q }), key);
  const bookings = data ?? [];

  const change = async (booking: AdminBooking, next: BookingStatus) => {
    setBusy(true);
    try {
      const updated = await adminApi.setBookingStatus(booking.bookingId, next);
      toast.success(`Booking #${booking.bookingId}: ${statusLabel("booking", updated.status)}.`);
      if (selected?.bookingId === booking.bookingId) setSelected(updated);
      reload();
    } catch (err) {
      toast.error(friendlyError(err, "The status couldn't be changed."));
    } finally {
      setBusy(false);
      setCancelling(null);
    }
  };

  const ask = (booking: AdminBooking, next: BookingStatus) => {
    if (next === "CANCELLED") setCancelling(booking);
    else change(booking, next);
  };

  return (
    <>
      <form className="adm-tools" onSubmit={(e) => { e.preventDefault(); setQ(search.trim()); }}>
        <input className="input grow" type="search" placeholder="Search student, email, machine or #number" value={search}
               onChange={(e) => setSearch(e.target.value)} aria-label="Search bookings" />
        <label className="small muted" htmlFor="b-from">From</label>
        <input id="b-from" className="input" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        <label className="small muted" htmlFor="b-to">To</label>
        <input id="b-to" className="input" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
      </form>
      <div className="adm-tools">
        <Segmented label="Status" value={status} onChange={setStatus} options={[
          { value: "", label: "All" },
          { value: "PENDING", label: "Awaiting payment" },
          { value: "CONFIRMED", label: "Confirmed" },
          { value: "COMPLETED", label: "Completed" },
          { value: "CANCELLED", label: "Cancelled" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : (
        <section className="card">
          <p className="adm-count">{bookings.length} {bookings.length === 1 ? "booking" : "bookings"}</p>
          <div className="table-wrap">
            <table className="table" style={{ minWidth: 760 }}>
              <thead>
                <tr><th>#</th><th>Wash</th><th>Machine</th><th>Student</th><th className="num">Amount</th><th>Status</th><th /></tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.bookingId} className="clickable" onClick={() => setSelected(b)}>
                    <td>{b.bookingId}</td>
                    <td>{shortDate(b.date)}<span className="sub">{shortTime(b.startTime)}–{shortTime(b.endTime)}</span></td>
                    <td>{b.machineNumber}<span className="sub">Room {b.roomNumber}</span></td>
                    <td>{b.studentName}<span className="sub">{b.studentEmail}</span></td>
                    <td className="num">{money(b.totalAmount)}</td>
                    <td><Status kind="booking" value={b.status} /></td>
                    <td onClick={(e) => e.stopPropagation()}>
                      <div className="actions-cell">
                        {BOOKING_NEXT[b.status].map((next) => (
                          <button key={next} type="button" disabled={busy}
                                  className={`btn btn-sm ${next === "CANCELLED" ? "btn-danger" : "btn-ghost"}`}
                                  onClick={() => ask(b, next)}>
                            {ACTION_LABEL[next]}
                          </button>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {bookings.length === 0 && <p className="adm-empty">No bookings match.</p>}
          </div>
        </section>
      )}

      {selected && (
        <Modal title={`Booking #${selected.bookingId}`} onClose={() => setSelected(null)}>
          <div className="row"><Status kind="booking" value={selected.status} /></div>
          <dl className="adm-facts">
            <div><dt>Student</dt><dd>{selected.studentName}</dd></div>
            <div><dt>Email</dt><dd>{selected.studentEmail}</dd></div>
            <div><dt>Date</dt><dd>{shortDate(selected.date)}</dd></div>
            <div><dt>Time</dt><dd>{shortTime(selected.startTime)}–{shortTime(selected.endTime)}</dd></div>
            <div><dt>Machine</dt><dd>{selected.machineNumber} ({selected.machineType})</dd></div>
            <div><dt>Room</dt><dd>{selected.roomNumber}</dd></div>
            <div><dt>Service</dt><dd>{selected.serviceName ?? "None"}</dd></div>
            <div><dt>Amount</dt><dd>{money(selected.totalAmount)}</dd></div>
            <div><dt>Booked on</dt><dd>{dateTime(selected.bookedAt)}</dd></div>
          </dl>
          <div className="adm-section">
            <h3>Change status</h3>
            {BOOKING_NEXT[selected.status].length === 0 ? (
              <p className="muted">This booking is finished, so its status can't change any more.</p>
            ) : (
              <>
                {selected.status === "PENDING" && (
                  <p className="muted small">Confirming without a payment is for cash paid at the office. EFTs are confirmed automatically
                    when you approve the payment on the Orders &amp; payments page.</p>
                )}
                <div className="btn-row">
                  {BOOKING_NEXT[selected.status].map((next) => (
                    <button key={next} type="button" disabled={busy}
                            className={`btn btn-sm ${next === "CANCELLED" ? "btn-danger" : "btn-primary"}`}
                            onClick={() => ask(selected, next)}>
                      {ACTION_LABEL[next]}
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>
        </Modal>
      )}

      {cancelling && (
        <ConfirmDialog title={`Cancel booking #${cancelling.bookingId}?`}
                       message={`${cancelling.studentName}'s slot on ${shortDate(cancelling.date)} at ${shortTime(cancelling.startTime)} will be freed and they'll get a notification.`}
                       confirmLabel="Cancel booking" cancelLabel="Keep it" busy={busy}
                       onCancel={() => setCancelling(null)} onConfirm={() => change(cancelling, "CANCELLED")} />
      )}
    </>
  );
}

/** Every machine against every time slot for one day. */
function DaySchedule() {
  const [date, setDate] = useState(todayIso());
  const { data, loading, error, reload } = useFetch(() => adminApi.schedule(date), date);

  return (
    <>
      <div className="adm-tools">
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setDate(shift(date, -1))}>← Previous day</button>
        <input className="input" type="date" value={date} onChange={(e) => e.target.value && setDate(e.target.value)} aria-label="Date" />
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setDate(shift(date, 1))}>Next day →</button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setDate(todayIso())}>Today</button>
      </div>
      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : data && <ScheduleGrid schedule={data} />}
    </>
  );
}

function ScheduleGrid({ schedule }: { schedule: Schedule }) {
  if (schedule.slots.length === 0) {
    return <div className="card adm-empty">There are no time slots on {shortDate(schedule.date)}. Add some under Machines &amp; slots → Time slots.</div>;
  }
  if (schedule.machines.length === 0) {
    return <div className="card adm-empty">There are no machines yet. Add some under Machines &amp; slots.</div>;
  }
  const bookingAt = (machineId: number, slotId: number) =>
    schedule.bookings.find((b) => b.machineId === machineId && b.timeSlotId === slotId);

  return (
    <>
      <p className="adm-count">
        {schedule.bookings.length} booked of {schedule.slots.filter((s) => s.available).length * schedule.machines.length} machine-slots on {shortDate(schedule.date)}
      </p>
      <div className="adm-grid-wrap">
        <table className="adm-grid">
          <thead>
            <tr>
              <th className="time">Time</th>
              {schedule.machines.map((m) => (
                <th key={m.machineId} title={`Room ${m.roomNumber}`}>{m.machineNumber}<br /><span className="muted small">{m.type}</span></th>
              ))}
            </tr>
          </thead>
          <tbody>
            {schedule.slots.map((slot) => (
              <tr key={slot.timeSlotId}>
                <th className="time">{shortTime(slot.startTime)}–{shortTime(slot.endTime)}</th>
                {schedule.machines.map((m) => {
                  const booking = bookingAt(m.machineId, slot.timeSlotId);
                  if (booking) {
                    return (
                      <td key={m.machineId} className={booking.status === "PENDING" ? "pending" : "booked"}
                          title={`#${booking.bookingId} ${booking.studentName} (${booking.status.toLowerCase()})`}>
                        {booking.studentName.split(" ")[0]}
                      </td>
                    );
                  }
                  if (!slot.available) return <td key={m.machineId} className="closed">Closed</td>;
                  if (m.status === "OUT_OF_ORDER") return <td key={m.machineId} className="broken">Out of order</td>;
                  return <td key={m.machineId} className="free">Free</td>;
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="muted small" style={{ marginTop: 10 }}>Blue: confirmed or completed · Yellow: awaiting payment · Striped: slot closed. Hover a booking to see who it is.</p>
    </>
  );
}

function shift(iso: string, days: number): string {
  const d = new Date(`${iso}T00:00:00`);
  d.setDate(d.getDate() + days);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export default AdminBookings;
