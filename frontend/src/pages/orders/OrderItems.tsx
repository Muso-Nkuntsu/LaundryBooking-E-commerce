import { useState } from "react";
import { Link } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { useAwaitingEft } from "../../hooks/useAwaitingEft";
import { useToast } from "../../context/useToast";
import { orderItemService } from "../../services/orderItemService";
import { orderService } from "../../services/orderService";
import { getStudentId } from "../../services/session";
import type { OrderItem } from "../../types/orderItem";
import { ORDER_STATUS_LABEL, ORDER_STATUS_PILL } from "../../types/order";
import type { OrderStatus } from "../../types/order";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";
import { formatFullDate } from "../../utilis/FormatDate";

interface OrderGroup {
  orderId: number;
  orderDate?: string;
  status: OrderStatus;
  total: number;
  items: OrderItem[];
}

/** The backend sends one row per item; show them together under their order, newest first. */
function groupByOrder(items: OrderItem[]): OrderGroup[] {
  const groups = new Map<number, OrderGroup>();
  items.forEach((item) => {
    const orderId = item.order?.orderId ?? 0;
    const group = groups.get(orderId) ?? {
      orderId,
      orderDate: item.order?.orderDate,
      status: item.order?.status ?? "PENDING",
      total: item.order?.totalAmount ?? 0,
      items: [],
    };
    group.items.push(item);
    groups.set(orderId, group);
  });
  return Array.from(groups.values())
    .map((group) => ({ ...group, total: group.total || group.items.reduce((sum, item) => sum + (item.subtotal ?? 0), 0) }))
    .sort((a, b) => b.orderId - a.orderId);
}

function OrderItems() {
  const toast = useToast();
  const studentId = getStudentId();
  const { data, loading, error, reload } = useFetch(
    () =>
      orderItemService.getOrderItemsByStudent(studentId).catch((err: unknown) => {
        throw new Error(friendlyError(err, "We couldn't load your orders."));
      }),
    studentId,
  );
  const orders = groupByOrder(data ?? []);
  const awaitingEft = useAwaitingEft("order", orders.filter((order) => order.status === "PENDING").map((order) => order.orderId));

  const [toCancel, setToCancel] = useState<OrderGroup | null>(null);
  const [cancelling, setCancelling] = useState(false);

  const handleCancel = async () => {
    if (!toCancel) return;
    try {
      setCancelling(true);
      await orderService.cancelOrder(toCancel.orderId);
      toast.success(`Order #${toCancel.orderId} cancelled.`);
      setToCancel(null);
      reload();
    } catch (err) {
      toast.error(friendlyError(err, "We couldn't cancel that order. Try again."));
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>My orders</h1>
          <p>Products you've ordered from the shop. Paid orders are collected from the laundry room.</p>
        </div>
        <Link to="/products" className="btn btn-ghost">Go to the shop</Link>
      </header>

      {loading && <Loading message="Loading your orders..." />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && orders.length === 0 && (
        <EmptyState
          title="No orders yet"
          message="Add products to your cart and check out, and your orders will be listed here."
          action={<Link to="/products" className="btn btn-primary">Browse the shop</Link>}
        />
      )}

      {orders.map((order) => (
        <section className="card table-wrap" key={order.orderId}>
          <div className="row" style={{ flexWrap: "wrap", gap: 12, marginBottom: 12 }}>
            <div>
              <h2 style={{ display: "inline", marginRight: 10 }}>Order #{order.orderId}</h2>
              <span className={ORDER_STATUS_PILL[order.status] ?? "pill pill-off"}>{ORDER_STATUS_LABEL[order.status] ?? order.status}</span>
              {order.orderDate && <p className="muted small" style={{ margin: "4px 0 0" }}>{formatFullDate(order.orderDate.slice(0, 10))}</p>}
            </div>
            {order.status === "PENDING" && (
              <div className="btn-row">
                {awaitingEft.has(order.orderId) ? (
                  <span className="pill pill-info">EFT being verified</span>
                ) : (
                  <Link
                    to="/payment"
                    state={{ orderId: order.orderId, amount: order.total, description: `Shop order #${order.orderId}` }}
                    className="btn btn-primary btn-sm"
                  >
                    Pay now
                  </Link>
                )}
                <button type="button" className="btn btn-danger btn-sm" onClick={() => setToCancel(order)}>
                  Cancel order
                </button>
              </div>
            )}
          </div>

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
              {order.items.map((item) => (
                <tr key={item.orderItemId}>
                  <td><strong>{item.product?.name || item.product?.productName || "Unknown product"}</strong></td>
                  <td className="num">{item.quantity}</td>
                  <td className="num">{formatCurrency(item.unitPrice ?? 0)}</td>
                  <td className="num">{formatCurrency(item.subtotal ?? 0)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan={3} className="num">Total</td>
                <td className="num price" style={{ fontSize: "1.2rem" }}>{formatCurrency(order.total)}</td>
              </tr>
            </tfoot>
          </table>
        </section>
      ))}

      {toCancel && (
        <ConfirmDialog
          title="Cancel this order?"
          message={`Order #${toCancel.orderId}, ${formatCurrency(toCancel.total)}. The items go back on the shelf.`}
          confirmLabel="Cancel order"
          busy={cancelling}
          onConfirm={handleCancel}
          onCancel={() => setToCancel(null)}
        />
      )}
    </div>
  );
}

export default OrderItems;
