import { useState } from "react";
import { Link } from "react-router-dom";
import type { Booking } from "../../types/booking";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { bookingService } from "../../services/bookingService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { formatDayNumber, formatMonth } from "../../utilis/FormatDate";
import { STATUS_LABEL, STATUS_PILL, bookingDate, bookingPlace, bookingTime, byDateAscending, isUpcoming } from "../../utilis/bookingDisplay";

type Filter = "upcoming" | "past" | "cancelled" | "all";

const FILTERS: { id: Filter; label: string }[] = [
  { id: "upcoming", label: "Upcoming" },
  { id: "past", label: "Past" },
  { id: "cancelled", label: "Cancelled" },
  { id: "all", label: "All" },
];

function matches(booking: Booking, filter: Filter): boolean {
  if (filter === "all") return true;
  if (filter === "cancelled") return booking.status === "CANCELLED";
  if (filter === "upcoming") return isUpcoming(booking);
  return !isUpcoming(booking) && booking.status !== "CANCELLED";
}

function MyBookings() {
  const toast = useToast();
  const studentId = getStudentId();

  const { data, loading, error, reload } = useFetch(
    () =>
      bookingService.getBookingsByStudent(studentId).catch((err: unknown) => {
        throw new Error(friendlyError(err, "We couldn't load your bookings."));
      }),
    studentId,
  );

  const [filter, setFilter] = useState<Filter>("upcoming");
  const [toCancel, setToCancel] = useState<Booking | null>(null);
  const [cancelling, setCancelling] = useState(false);

  const bookings = (data ?? []).slice().sort(byDateAscending);
  const visible = bookings.filter((booking) => matches(booking, filter));

  const handleCancel = async () => {
    if (!toCancel) return;
    try {
      setCancelling(true);
      await bookingService.cancelBooking(toCancel.id);
      toast.success(`Booking #${toCancel.id} cancelled.`);
      setToCancel(null);
      reload();
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't cancel that booking. Try again."));
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>My bookings</h1>
          <p>Everything you've booked, with the option to cancel what's still ahead.</p>
        </div>
        <Link to="/make-booking" className="btn btn-primary">Book a machine</Link>
      </header>

      {loading && <Loading message="Loading your bookings..." />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}

      {!loading && !error && bookings.length === 0 && (
        <EmptyState
          title="No bookings yet"
          message="Book a machine and it will show up here."
          action={<Link to="/make-booking" className="btn btn-primary">Book a machine</Link>}
        />
      )}

      {!loading && !error && bookings.length > 0 && (
        <>
          <div className="segmented" role="group" aria-label="Filter bookings" style={{ marginBottom: 18 }}>
            {FILTERS.map((option) => (
              <button key={option.id} type="button" aria-pressed={filter === option.id} onClick={() => setFilter(option.id)}>
                {option.label} ({bookings.filter((booking) => matches(booking, option.id)).length})
              </button>
            ))}
          </div>

          {visible.length === 0 ? (
            <p className="muted">No bookings in this view.</p>
          ) : (
            <div className="stack">
              {visible.map((booking) => {
                const date = booking.timeSlot?.date;
                return (
                  <article key={booking.id} className="card booking" style={{ margin: 0 }}>
                    <div className="datechip" aria-hidden="true">
                      {date ? (
                        <>
                          {formatMonth(date)}
                          <b>{formatDayNumber(date)}</b>
                        </>
                      ) : (
                        <b>?</b>
                      )}
                    </div>
                    <div>
                      <div className="row" style={{ justifyContent: "flex-start", flexWrap: "wrap" }}>
                        <h3>{bookingDate(booking)}</h3>
                        <span className={STATUS_PILL[booking.status] ?? "pill pill-off"}>{STATUS_LABEL[booking.status] ?? booking.status}</span>
                      </div>
                      <p className="muted">{bookingTime(booking)}, {bookingPlace(booking)}</p>
                      <p className="small muted">Booking #{booking.id}, {formatCurrency(booking.totalAmount ?? 0)}</p>
                    </div>
                    <div className="booking-actions">
                      {/* Only allow cancellation if confirmed */}
                      {booking.status === "CONFIRMED" && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => setToCancel(booking)}>
                          Cancel booking
                        </button>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </>
      )}

      {toCancel && (
        <ConfirmDialog
          title="Cancel this booking?"
          message={`${bookingDate(toCancel)}, ${bookingTime(toCancel)}. The slot goes back to other students.`}
          confirmLabel="Cancel booking"
          busy={cancelling}
          onConfirm={handleCancel}
          onCancel={() => setToCancel(null)}
        />
      )}
    </div>
  );
}

export default MyBookings;
