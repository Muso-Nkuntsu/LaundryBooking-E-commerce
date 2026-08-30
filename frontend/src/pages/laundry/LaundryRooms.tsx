import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { LaundryRoom } from "../../types/LaundryRoom";
import { getActiveLaundryRooms } from "../../services/laundryRoomService";

function LaundryRooms() {
  const [rooms, setRooms] = useState<LaundryRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getActiveLaundryRooms()
      .then(setRooms)
      .catch(() => setError("Unable to load laundry rooms. Please try again."))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="page-shell">
      <div className="page-heading">
        <div>
          <p className="eyebrow">LAUNDRY FACILITIES</p>
          <h1>Laundry Rooms</h1>
          <p>Select a laundry room to view its machines.</p>
        </div>
        <Link className="secondary-button" to="/dashboard">Dashboard</Link>
      </div>

      {loading && <div className="state-card">Loading laundry rooms...</div>}
      {error && <div className="state-card error">{error}</div>}
      {!loading && !error && rooms.length === 0 && (
        <div className="state-card">No active laundry rooms are available.</div>
      )}

      <section className="card-grid">
        {rooms.map((room) => (
          <article className="feature-card" key={room.roomId}>
            <div className="card-icon">⌂</div>
            <h2>{room.roomNumber}</h2>
            <p className="muted">{room.location}</p>
            <p>{room.description || "Laundry facility available for students."}</p>
            <div className="info-row">
              <span>Capacity</span>
              <strong>{room.capacity}</strong>
            </div>
            <Link className="primary-button full-width" to={`/laundry-rooms/${room.roomId}`}>
              View Room
            </Link>
          </article>
        ))}
      </section>
    </main>
  );
}

export default LaundryRooms;
