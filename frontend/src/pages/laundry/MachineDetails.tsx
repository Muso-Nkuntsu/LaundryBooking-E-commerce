import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { machineService } from "../../services/machineService";
import { friendlyError } from "../../utilis/errorMessage";
import type { LaundryMachine } from "../../types/LaundryMachine";

function MachineDetails() {
    const navigate = useNavigate();
    const { roomId, machineId } = useParams();

    const { data: machine, loading, error, reload } = useFetch<LaundryMachine>(
        () =>
            machineService.getMachineById(Number(machineId)).catch((err: unknown) => {
                throw new Error(friendlyError(err, "We couldn't load this machine."));
            }),
        machineId ?? ""
    );

    const goToBooking = () => {
        if (!machine) return;
        navigate("/bookings/create", {
            state: { machineId: machine.machineId, machineNumber: machine.machineNumber },
        });
    };

    return (
        <div className="page-shell">
            <div className="detail-header">
                <div>
                    <p className="eyebrow">Machine details</p>
                    <h1>{machine ? `Machine ${machine.machineNumber}` : "Machine"}</h1>
                </div>
            </div>

            <Link to={`/laundry-rooms/${roomId}/machines`} className="back-link">← Back to machines</Link>

            {loading && <Loading message="Loading machine details..." />}
            {!loading && error && <ErrorState message={error} onRetry={reload} />}

            {!loading && !error && machine && (
                <div className="detail-card">
          <span className={`status-pill ${machine.status === "AVAILABLE" ? "available" : "unavailable"}`}>
            {machine.status}
          </span>

                    <p className="machine-type">Type: {machine.type}</p>

                    <div className="detail-fields">
                        <div>
                            <span>Status</span>
                            {machine.status}
                        </div>
                        <div>
                            <span>Laundry room</span>
                            Room #{machine.laundryRoomId}
                        </div>
                    </div>

                    {machine.status === "AVAILABLE" ? (
                        <div className="button-row">
                            <button type="button" className="primary-button full-width" onClick={goToBooking}>
                                View Available Time Slots
                            </button>
                        </div>
                    ) : (
                        <p className="notice">This machine is currently not available for booking.</p>
                    )}
                </div>
            )}
        </div>
    );
}

export default MachineDetails;