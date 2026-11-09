import { Link } from "react-router-dom";
import { EmptyState, ErrorState, SkeletonGrid } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import { useFetch } from "../../hooks/useFetch";
import { getActiveLaundryRooms } from "../../services/laundryRoomService";
import { friendlyError } from "../../utilis/errorMessage";

function LaundryRooms() {
  const { data, loading, error, reload } = useFetch(() =>
    getActiveLaundryRooms().catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load the laundry rooms."));
    }),
  );
  const rooms = data ?? [];

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>Laundry rooms</h1>
          <p>Open a room to see its machines and which ones are free right now.</p>
        </div>
      </header>

      {loading && <SkeletonGrid count={3} height={210} />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && rooms.length === 0 && (
        <EmptyState title="No rooms open" message="No laundry rooms are active at the moment. Check back later." />
      )}

      <section className="grid grid-3">
        {rooms.map((room) => (
          <Link className="card tile" key={room.roomId} to={`/laundry-rooms/${room.roomId}`}>
            <span className="action" style={{ padding: 0, border: 0, background: "none", boxShadow: "none", transform: "none" }}>
              <span className="ic"><Icon name="door" /></span>
            </span>
            <h2>{room.roomNumber}</h2>
            <p className="muted">{room.location}</p>
            <p>{room.description || "Laundry room for residents."}</p>
            <div className="row push">
              <span className="muted small">Capacity {room.capacity}</span>
              <span className="link-btn">View machines</span>
            </div>
          </Link>
        ))}
      </section>
    </div>
  );
}

export default LaundryRooms;
