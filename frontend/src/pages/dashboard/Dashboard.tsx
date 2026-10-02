import { Link } from "react-router-dom";
import Drum from "../../components/common/Drum";
import Icon from "../../components/common/Icon";
import type { IconName } from "../../components/common/Icon";
import { useFetch } from "../../hooks/useFetch";
import { bookingService } from "../../services/bookingService";
import { getDisplayName, getStudentId } from "../../services/session";
import { STATUS_LABEL, STATUS_PILL, bookingDate, bookingPlace, bookingTime, byDateAscending, isUpcoming } from "../../utilis/bookingDisplay";

const ACTIONS: { to: string; icon: IconName; label: string; note: string }[] = [
  { to: "/laundry-rooms", icon: "door", label: "Laundry rooms", note: "See which machines are free" },
  { to: "/laundry", icon: "spark", label: "Services", note: "Extras for your wash" },
  { to: "/products", icon: "bag", label: "Shop", note: "Detergent and softener" },
  { to: "/order-items", icon: "receipt", label: "My orders", note: "What you've bought" },
  { to: "/payment", icon: "card", label: "Payment", note: "Pay for a booking" },
  { to: "/reviews", icon: "star", label: "Reviews", note: "Rate the laundry service" },
];

function Dashboard() {
  const studentId = getStudentId();
  const { data, loading, error } = useFetch(() => bookingService.getBookingsByStudent(studentId), studentId);

  const bookings = Array.isArray(data) ? data : [];
  const upcoming = bookings.filter(isUpcoming).sort(byDateAscending);
  const next = upcoming[0];
  const recent = bookings.filter((booking) => !isUpcoming(booking)).slice(-3).reverse();

  return (
    <div className="page">
      <section className="hero">
        <div>
          <h1>Hi {getDisplayName()}</h1>
          {loading ? (
            <p>Checking your bookings...</p>
          ) : next ? (
            <>
              <p>Your next wash is booked.</p>
              <div className="next">
                <strong>{bookingDate(next)}</strong>
                {bookingTime(next)}, {bookingPlace(next)}
              </div>
            </>
          ) : (
            <p>{error ? "We couldn't load your bookings right now." : "You have no wash booked yet."}</p>
          )}
          <div className="btn-row" style={{ marginTop: 20 }}>
            <Link to="/make-booking" className="btn btn-sun">
              Book a machine
            </Link>
            {upcoming.length > 0 && (
              <Link to="/my-bookings" className="btn btn-ghost" style={{ color: "#fff", borderColor: "rgba(255,255,255,0.4)" }}>
                View my bookings
              </Link>
            )}
          </div>
        </div>
        <div className="hero-drum" aria-hidden="true">
          <Drum tone="dark" state={next ? "spinning" : "idle"} slow size="100%" />
        </div>
      </section>

      <nav className="actions" aria-label="Shortcuts">
        {ACTIONS.map((action) => (
          <Link key={action.to} to={action.to} className="action">
            <span className="ic">
              <Icon name={action.icon} />
            </span>
            <span>
              {action.label}
              <br />
              <small>{action.note}</small>
            </span>
          </Link>
        ))}
      </nav>

      <div className="grid grid-2">
        <section className="card">
          <div className="row">
            <h2>Coming up</h2>
            <Link to="/my-bookings" className="link-btn small">See all</Link>
          </div>
          {upcoming.length === 0 ? (
            <p className="muted" style={{ marginTop: 12 }}>Nothing booked yet.</p>
          ) : (
            <ul className="list" style={{ marginTop: 6 }}>
              {upcoming.slice(0, 3).map((booking) => (
                <li key={booking.id}>
                  <span>
                    <strong>{bookingDate(booking)}</strong>
                    <br />
                    <span className="muted small">{bookingTime(booking)}, {bookingPlace(booking)}</span>
                  </span>
                  <span className={STATUS_PILL[booking.status]}>{STATUS_LABEL[booking.status]}</span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card">
          <h2>Recent washes</h2>
          {recent.length === 0 ? (
            <p className="muted" style={{ marginTop: 12 }}>Your finished and cancelled bookings will show here.</p>
          ) : (
            <ul className="list" style={{ marginTop: 6 }}>
              {recent.map((booking) => (
                <li key={booking.id}>
                  <span>
                    <strong>{bookingDate(booking)}</strong>
                    <br />
                    <span className="muted small">{bookingPlace(booking)}</span>
                  </span>
                  <span className={STATUS_PILL[booking.status]}>{STATUS_LABEL[booking.status]}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}

export default Dashboard;
