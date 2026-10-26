import { Link, useNavigate } from "react-router-dom";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { orderService } from "../../services/orderService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";
import type { Order } from "../../types/Order";

function OrderHistory() {
    const navigate = useNavigate();
    const studentId = getStudentId();

    const { data, loading, error, reload } = useFetch<Order[]>(
        () =>
            orderService.getOrdersByStudent(studentId).catch((err: unknown) => {
                throw new Error(friendlyError(err, "We couldn't load your orders."));
            }),
        studentId
    );
    const orders = data ?? [];

    return (
        <div className="page">
            <header className="page-head">
                <div>
                    <h1>My orders</h1>
                    <p>Orders placed through the shop.</p>
                </div>
                <Link to="/products" className="btn btn-ghost">Go to the shop</Link>
            </header>

            {loading && <Loading message="Loading your orders..." />}
            {!loading && error && <ErrorState message={error} onRetry={reload} />}
            {!loading && !error && orders.length === 0 && (
                <EmptyState
                    title="No orders yet"
                    message="Your past orders will be listed here once you place one."
                    action={<Link to="/products" className="btn btn-primary">Browse the shop</Link>}
                />
            )}

            {orders.length > 0 && (
                <section className="card table-wrap">
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Order</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th className="num">Total</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        {orders.map((order) => (
                            <tr key={order.orderId}>
                                <td>#{order.orderId}</td>
                                <td>{new Date(order.orderDate).toLocaleDateString()}</td>
                                <td>{order.status}</td>
                                <td className="num">{formatCurrency(order.totalAmount)}</td>
                                <td>
                                    <button
                                        type="button"
                                        className="btn btn-ghost"
                                        onClick={() => navigate(`/orders/${order.orderId}`)}
                                    >
                                        View Details
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </section>
            )}
        </div>
    );
}

export default OrderHistory;