import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import type { Product } from "../../types/Product";
import { getProductById } from "../../services/productService";
import { addToCart } from "../../services/cartService";

function ProductDetails() {
  const { productId } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!productId) {
      setError("Product not found.");
      setLoading(false);
      return;
    }
    getProductById(productId)
      .then(setProduct)
      .catch(() => setError("Unable to load this product."))
      .finally(() => setLoading(false));
  }, [productId]);

  const handleAdd = () => {
    if (!product || product.inventoryQuantity <= 0) return;
    addToCart(product, quantity);
    setMessage(`${quantity} × ${product.name} added to your cart.`);
  };

  if (loading) return <main className="page-shell"><div className="state-card">Loading product...</div></main>;
  if (error || !product) return <main className="page-shell"><div className="state-card error">{error || "Product not found."}</div></main>;

  return (
    <main className="page-shell">
      <Link className="back-link" to="/products">← Back to products</Link>
      <section className="product-detail">
        <div className="product-image large">Laundry Product</div>
        <div>
          <span className="category-label">{product.category || "Laundry"}</span>
          <h1>{product.name}</h1>
          <p className="product-description">{product.description || "Laundry product for student use."}</p>
          <div className="price large-price">R{product.price.toFixed(2)}</div>
          <p className={product.inventoryQuantity > 0 ? "available-text" : "unavailable-text"}>
            {product.inventoryQuantity > 0 ? `${product.inventoryQuantity} available` : "Out of stock"}
          </p>
          <label className="quantity-label" htmlFor="quantity">Quantity</label>
          <input
            id="quantity"
            className="quantity-input"
            type="number"
            min="1"
            max={Math.max(product.inventoryQuantity, 1)}
            disabled={product.inventoryQuantity <= 0}
            value={quantity}
            onChange={(event) => setQuantity(Math.min(Math.max(Number(event.target.value) || 1, 1), product.inventoryQuantity))}
          />
          <button className="primary-button" disabled={product.inventoryQuantity <= 0} onClick={handleAdd}>
            Add to Cart
          </button>
          {message && <p className="success-message">{message}</p>}
        </div>
      </section>
    </main>
  );
}

export default ProductDetails;
