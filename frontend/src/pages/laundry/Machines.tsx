import { Link, useNavigate, useParams } from "react-router-dom";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { machineService } from "../../services/machineService";
import { friendlyError } from "../../utilis/errorMessage";
import type { LaundryMachine } from "../../types/LaundryMachine";

function Machines() {
    const navigate = useNavigate();
    const { roomId } = useParams();

    const { data, loading, error, reload } = useFetch<LaundryMachine[]>(
        () =>
            machineService.getMachinesByRoom(Number(roomId)).catch((err: unknown) => {
                throw new Error(friendlyError(err, "We couldn't load the machines for this room."));
            }),
        roomId ?? ""
    );
    const machines = data ?? [];

    return (
        <div className="page-shell">
            <div className="page-heading">
                <div>
                    <p className="eyebrow">Laundry room</p>
                    <h1>Machines</h1>
                </div>
            </div>

            <Link to="/laundry-rooms" className="back-link">← Back to laundry rooms</Link>

            {loading && <Loading message="Loading machines..." />}
            {!loading && error && <ErrorState message={error} onRetry={reload} />}
            {!loading && !error && machines.length === 0 && (
                <EmptyState title="No machines found" message="This laundry room has no machines listed yet." />
            )}

            {!loading && !error && machines.length > 0 && (
                <div className="panel">
                    <div className="machine-list">
                        {machines.map((machine) => {
                            const isAvailable = machine.status === "AVAILABLE";
                            return (
                                <div key={machine.machineId} className="machine-row">
                                    <div>
                                        <strong>Machine {machine.machineNumber}</strong>
                                        <span>Type: {machine.type}</span>
                                    </div>
                                    <span className={`status-pill ${isAvailable ? "available" : "unavailable"}`}>
                    {machine.status}
                  </span>
                                    <button
                                        type="button"
                                        className="secondary-button"
                                        onClick={() => navigate(`/laundry-rooms/${roomId}/machines/${machine.machineId}`)}
                                    >
                                        View Details
                                    </button>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
}

export default Machines;