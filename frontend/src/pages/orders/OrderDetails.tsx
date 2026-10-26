import { useParams } from "react-router-dom";
import { ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { orderService } from "../../services/orderService";
import { orderItemService } from "../../services/orderItemService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";
import type { Order } from "../../types/Order";
import type { OrderItem } from "../../types/orderItem";

interface OrderDetailsData {
    order: Order;
    items: OrderItem[];
}

function OrderDetails() {
    const { orderId } = useParams();
    const studentId = getStudentId();

    const { data, loading, error, reload } = useFetch<OrderDetailsData>(
        () =>
            Promise.all([
                orderService.getOrderById(Number(orderId)),
                orderItemService.getOrderItemsByStudent(studentId),
            ])
                .then(([order, allItems]) => ({
                    order,
                    items: allItems.filter(
                        (item) =>
                            String(item.order?.orderId) === orderId || String(item.order?.id) === orderId
                    ),
                }))
                .catch((err: unknown) => {
                    throw new Error(friendlyError(err, "We couldn't load this order."));
                }),
        orderId ?? ""
    );

    return (
        <div className="page">
            <header className="page-head">
                <div>
                    <h1>Order #{orderId}</h1>
                </div>
            </header>

            {loading && <Loading message="Loading order details..." />}
            {!loading && error && <ErrorState message={error} onRetry={reload} />}

            {!loading && !error && data && (
                <>
                    <section className="card detail-card">
                        <div className="info-row">
                            <span>Status</span>
                            <strong>{data.order.status}</strong>
                        </div>
                        <div className="info-row">
                            <span>Order date</span>
                            <span>{new Date(data.order.orderDate).toLocaleDateString()}</span>
                        </div>
                        {data.order.paymentStatus && (
                            <div className="info-row">
                                <span>Payment status</span>
                                <span>{data.order.paymentStatus}</span>
                            </div>
                        )}
                    </section>

                    <section className="card table-wrap">
                        <table className="table">
                            <thead>
                            <tr>
                                <th>Item</th>
                                <th className="num">Quantity</th>
                                <th className="num">Unit price</th>
                                <th className="num">Subtotal</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.items.map((item) => (
                                <tr key={item.orderItemId}>
                                    <td>{item.product?.productName || item.product?.name || "Unknown product"}</td>
                                    <td className="num">{item.quantity}</td>
                                    <td className="num">{formatCurrency(item.unitPrice ?? 0)}</td>
                                    <td className="num">{formatCurrency(item.subtotal ?? 0)}</td>
                                </tr>
                            ))}
                            </tbody>
                            <tfoot>
                            <tr>
                                <td colSpan={3} className="num">Total</td>
                                <td className="num price">{formatCurrency(data.order.totalAmount)}</td>
                            </tr>
                            </tfoot>
                        </table>
                    </section>
                </>
            )}
        </div>
    );
}

export default OrderDetails;