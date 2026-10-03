import { Link, useParams } from "react-router-dom";
import { ErrorState, Loading } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import MachineDoor from "../../components/booking/MachineDoor";
import { isMachineAvailable } from "../../utilis/machine";
import { useFetch } from "../../hooks/useFetch";
import { getLaundryRoomById } from "../../services/laundryRoomService";
import { getAllLaundryMachines } from "../../services/laundryMachineService";
import { friendlyError } from "../../utilis/errorMessage";

function LaundryRoomDetails() {
  const { roomId } = useParams();
  const id = Number(roomId);

  const { data, loading, error, reload } = useFetch(async () => {
    if (!Number.isInteger(id)) throw new Error("That laundry room doesn't exist.");
    try {
      const [room, allMachines] = await Promise.all([getLaundryRoomById(id), getAllLaundryMachines()]);
      return { room, machines: allMachines.filter((machine) => machine.laundryRoom?.roomId === id) };
    } catch (err) {
      throw new Error(friendlyError(err, "We couldn't load this laundry room."));
    }
  }, id);

  if (loading) return <div className="page"><Loading message="Loading room..." /></div>;
  if (error || !data) {
    return (
      <div className="page">
        <Link className="back" to="/laundry-rooms"><Icon name="arrowLeft" size={18} /> Laundry rooms</Link>
        <ErrorState message={error ?? "Room not found."} onRetry={reload} />
      </div>
    );
  }

  const { room, machines } = data;
  const available = machines.filter(isMachineAvailable).length;

  return (
    <div className="page">
      <Link className="back" to="/laundry-rooms"><Icon name="arrowLeft" size={18} /> Laundry rooms</Link>

      <header className="page-head">
        <div>
          <h1>{room.roomNumber}</h1>
          <p>{room.location}</p>
        </div>
        <span className={`pill ${room.isActive ? "pill-ok" : "pill-off"}`}>{room.isActive ? "Open" : "Closed"}</span>
      </header>

      <section className="grid grid-3" style={{ marginBottom: 16 }}>
        <div className="card"><div className="price">{machines.length}</div><span className="muted">Machines</span></div>
        <div className="card"><div className="price" style={{ color: "var(--leaf)" }}>{available}</div><span className="muted">Free now</span></div>
        <div className="card"><div className="price">{machines.length - available}</div><span className="muted">In use or unavailable</span></div>
      </section>

      <section className="card">
        <div className="row" style={{ marginBottom: 16 }}>
          <h2>Machines</h2>
          <Link className="btn btn-primary btn-sm" to={`/make-booking?roomId=${room.roomId}`}>
            Book in this room
          </Link>
        </div>
        {machines.length === 0 ? (
          <p className="muted">No machines are assigned to this room yet.</p>
        ) : (
          <div className="machines">
            {machines.map((machine) => (
              <MachineDoor key={machine.machineId} machine={machine} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default LaundryRoomDetails;
