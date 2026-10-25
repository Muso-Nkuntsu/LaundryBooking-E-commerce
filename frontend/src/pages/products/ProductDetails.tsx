import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState, Loading } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { useCart } from "../../context/useCart";
import { getProductById } from "../../services/productService";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

function ProductDetails() {
  const { productId } = useParams();
  const toast = useToast();
  const cart = useCart();
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);

  const { data: product, loading, error, reload } = useFetch(async () => {
    if (!productId) throw new Error("Product not found.");
    try {
      return await getProductById(productId);
    } catch (err) {
      throw new Error(friendlyError(err, "We couldn't load this product."));
    }
  }, productId ?? "");

  const back = (
    <Link className="back" to="/products"><Icon name="arrowLeft" size={18} /> Shop</Link>
  );

  if (loading) return <div className="page"><Loading message="Loading product..." /></div>;
  if (error || !product) {
    return (
      <div className="page">
        {back}
        <ErrorState message={error ?? "Product not found."} onRetry={reload} />
      </div>
    );
  }

  const stock = product.inventoryQuantity;
  const inCart = cart.quantityOf(product.productId);
  const canAdd = Math.max(stock - inCart, 0);
  const inStock = canAdd > 0;

  const handleAdd = () => {
    if (!inStock) return;
    const added = cart.add(product, quantity);
    setQuantity(1);
    toast.success(`${added} × ${product.name} added to your cart.`);
  };

  return (
    <div className="page">
      {back}
      <section className="card grid grid-2" style={{ gap: 28 }}>
        <div className="product-art big" aria-hidden="true"><Icon name="bag" size={80} /></div>
        <div className="stack">
          <span className="muted small">{product.category || "Laundry"}</span>
          <h1>{product.name}</h1>
          <p className="muted">{product.description || "Laundry product for student use."}</p>
          <div className="row" style={{ justifyContent: "flex-start", gap: 16 }}>
            <span className="price" style={{ fontSize: "2rem" }}>{formatCurrency(product.price)}</span>
            <span className={`pill ${stock > 0 ? "pill-ok" : "pill-bad"}`}>{stock <= 0 ? "Out of stock" : `${stock} available`}</span>
          </div>

          <div>
            <span id="quantity-label" style={{ fontWeight: 600, display: "block", marginBottom: 8 }}>Quantity</span>
            <div className="stepper" role="group" aria-labelledby="quantity-label">
              <button type="button" aria-label="One fewer" disabled={!inStock || quantity <= 1} onClick={() => setQuantity((q) => Math.max(q - 1, 1))}>−</button>
              <output aria-live="polite">{quantity}</output>
              <button type="button" aria-label="One more" disabled={!inStock || quantity >= canAdd} onClick={() => setQuantity((q) => Math.min(q + 1, canAdd))}>+</button>
            </div>
          </div>

          <div className="btn-row">
            <button type="button" className="btn btn-primary" disabled={!inStock} onClick={handleAdd}>
              {stock > 0 && !inStock ? "All stock is in your cart" : `Add to cart, ${formatCurrency(product.price * quantity)}`}
            </button>
            {inCart > 0 && (
              <button type="button" className="btn btn-ghost" onClick={() => navigate("/cart")}>
                View cart ({inCart} of this)
              </button>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

export default ProductDetails;
