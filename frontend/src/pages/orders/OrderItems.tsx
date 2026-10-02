import { Link } from "react-router-dom";
import { EmptyState, ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { orderItemService } from "../../services/orderItemService";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

function OrderItems() {
  const { data, loading, error, reload } = useFetch(() =>
    orderItemService.getAllOrderItems().catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load your order items."));
    }),
  );
  const orderItems = data ?? [];

  // Calculate total for all displayed items
  const total = orderItems.reduce((sum, item) => sum + (item.subtotal ?? 0), 0);

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>My orders</h1>
          <p>Products you've ordered from the shop.</p>
        </div>
        <Link to="/products" className="btn btn-ghost">Go to the shop</Link>
      </header>

      {loading && <Loading message="Loading your orders..." />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && orderItems.length === 0 && (
        <EmptyState
          title="No orders yet"
          message="When you buy something from the shop it will be listed here."
          action={<Link to="/products" className="btn btn-primary">Browse the shop</Link>}
        />
      )}

      {orderItems.length > 0 && (
        <section className="card table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Item</th>
                <th className="num">Quantity</th>
                <th className="num">Unit price</th>
                <th className="num">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {orderItems.map((item) => (
                <tr key={item.orderItemId}>
                  <td>#{item.order?.orderId || item.order?.id || "N/A"}</td>
                  <td><strong>{item.product?.productName || item.product?.name || "Unknown product"}</strong></td>
                  <td className="num">{item.quantity}</td>
                  <td className="num">{formatCurrency(item.unitPrice ?? 0)}</td>
                  <td className="num">{formatCurrency(item.subtotal ?? 0)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan={4} className="num">Total</td>
                <td className="num price" style={{ fontSize: "1.2rem" }}>{formatCurrency(total)}</td>
              </tr>
            </tfoot>
          </table>
        </section>
      )}
    </div>
  );
}

export default OrderItems;
