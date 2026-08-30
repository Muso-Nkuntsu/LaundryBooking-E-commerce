import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import type { LaundryRoom } from "../../types/LaundryRoom";
import { getLaundryRoomById } from "../../services/laundryRoomService";
import { getAllLaundryMachines, type LaundryMachine } from "../../services/laundryMachineService";

function LaundryRoomDetails() {
  const { roomId } = useParams();
  const [room, setRoom] = useState<LaundryRoom | null>(null);
  const [machines, setMachines] = useState<LaundryMachine[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const id = Number(roomId);
    if (!Number.isInteger(id)) {
      setError("Invalid laundry room.");
      setLoading(false);
      return;
    }

    Promise.all([getLaundryRoomById(id), getAllLaundryMachines()])
      .then(([loadedRoom, allMachines]) => {
        setRoom(loadedRoom);
        setMachines(allMachines.filter((machine) => machine.laundryRoom?.roomId === id));
      })
      .catch(() => setError("Unable to load this laundry room."))
      .finally(() => setLoading(false));
  }, [roomId]);

  const available = useMemo(
    () => machines.filter((machine) => machine.status === "AVAILABLE").length,
    [machines],
  );

  if (loading) return <main className="page-shell"><div className="state-card">Loading room...</div></main>;
  if (error || !room) return <main className="page-shell"><div className="state-card error">{error || "Room not found."}</div></main>;

  return (
    <main className="page-shell">
      <Link className="back-link" to="/laundry-rooms">← Back to laundry rooms</Link>
      <div className="detail-header">
        <div>
          <p className="eyebrow">LAUNDRY ROOM</p>
          <h1>{room.roomNumber}</h1>
          <p>{room.location}</p>
        </div>
        <span className={`status-pill ${room.isActive ? "available" : "unavailable"}`}>
          {room.isActive ? "ACTIVE" : "INACTIVE"}
        </span>
      </div>

      <section className="summary-grid">
        <div className="summary-card"><strong>{machines.length}</strong><span>Total machines</span></div>
        <div className="summary-card"><strong>{available}</strong><span>Available now</span></div>
        <div className="summary-card"><strong>{Math.max(machines.length - available, 0)}</strong><span>Currently unavailable</span></div>
      </section>

      <section className="panel">
        <h2>Machines</h2>
        {machines.length === 0 ? (
          <p className="muted">No machines are currently assigned to this room.</p>
        ) : (
          <div className="machine-list">
            {machines.map((machine) => (
              <div className="machine-row" key={machine.machineId}>
                <div>
                  <strong>{machine.machineNumber}</strong>
                  <span>{machine.type}</span>
                </div>
                <span className={`status-pill ${machine.status === "AVAILABLE" ? "available" : "unavailable"}`}>
                  {machine.status}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>

      <Link className="primary-button" to={`/laundry-machines?roomId=${room.roomId}`}>
        Browse Machines
      </Link>
    </main>
  );
}

export default LaundryRoomDetails;
