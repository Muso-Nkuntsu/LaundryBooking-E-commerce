import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { EmptyState } from "../../components/common/States";
import { useCart } from "../../context/useCart";
import { useToast } from "../../context/useToast";
import { orderService } from "../../services/orderService";
import { getStudentId } from "../../services/session";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

function Cart() {
  const cart = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const handleCheckout = async () => {
    try {
      setSubmitting(true);
      // The backend sets the prices, checks stock and holds it for this order.
      const order = await orderService.checkout(
        getStudentId(),
        cart.items.map((item) => ({ productId: item.productId, quantity: item.quantity })),
      );
      cart.clear();
      navigate("/payment", {
        state: { orderId: order.orderId, amount: order.totalAmount, description: `Shop order #${order.orderId}` },
      });
    } catch (error) {
      toast.error(friendlyError(error, "We couldn't place your order. Try again."));
    } finally {
      setSubmitting(false);
    }
  };

  if (cart.items.length === 0) {
    return (
      <div className="page narrow">
        <header className="page-head">
          <div>
            <h1>Your cart</h1>
          </div>
        </header>
        <EmptyState
          title="Your cart is empty"
          message="Add detergent, softener or other supplies from the shop."
          action={<Link to="/products" className="btn btn-primary">Go to the shop</Link>}
        />
      </div>
    );
  }

  return (
    <div className="page narrow">
      <header className="page-head">
        <div>
          <h1>Your cart</h1>
          <p>{cart.count} {cart.count === 1 ? "item" : "items"}. You'll collect them from the laundry room.</p>
        </div>
        <Link to="/products" className="btn btn-ghost">Keep shopping</Link>
      </header>

      <section className="card">
        <ul className="list">
          {cart.items.map((item) => (
            <li key={item.productId} style={{ gap: 16, flexWrap: "wrap" }}>
              <span style={{ flex: "1 1 180px" }}>
                <strong>{item.name}</strong>
                <br />
                <span className="muted small">{formatCurrency(item.price)} each</span>
              </span>
              <div className="stepper" role="group" aria-label={`Quantity of ${item.name}`}>
                <button type="button" aria-label="One fewer" onClick={() => cart.setQuantity(item.productId, item.quantity - 1)}>−</button>
                <output aria-live="polite">{item.quantity}</output>
                <button type="button" aria-label="One more" disabled={item.quantity >= item.stock} onClick={() => cart.setQuantity(item.productId, item.quantity + 1)}>+</button>
              </div>
              <strong style={{ minWidth: 90, textAlign: "right" }}>{formatCurrency(item.price * item.quantity)}</strong>
              <button type="button" className="link-btn small" onClick={() => cart.remove(item.productId)}>
                Remove
              </button>
            </li>
          ))}
        </ul>
      </section>

      <section className="card stack">
        <div className="row">
          <span className="muted">Total</span>
          <span className="price">{formatCurrency(cart.total)}</span>
        </div>
        <p className="muted small" style={{ margin: 0 }}>
          The final price and stock are confirmed when you check out. You pay on the next step, by card or EFT.
        </p>
        <button type="button" className="btn btn-primary btn-block" disabled={submitting} onClick={handleCheckout}>
          {submitting ? "Placing order..." : `Check out, ${formatCurrency(cart.total)}`}
        </button>
      </section>
    </div>
  );
}

export default Cart;
