import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { Product } from "../../types/Product";
import { getAllProducts } from "../../services/productService";
import { addToCart } from "../../services/cartService";

function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    getAllProducts()
      .then(setProducts)
      .catch(() => setError("Unable to load products. Please try again."))
      .finally(() => setLoading(false));
  }, []);

  const handleAdd = (product: Product) => {
    if (product.inventoryQuantity <= 0) return;
    addToCart(product);
    setMessage(`${product.name} added to your cart.`);
    window.setTimeout(() => setMessage(""), 2500);
  };

  return (
    <main className="page-shell">
      <div className="page-heading">
        <div>
          <p className="eyebrow">LAUNDRY SHOP</p>
          <h1>Products</h1>
          <p>Browse approved laundry products and add them to your cart.</p>
        </div>
        <Link className="secondary-button" to="/dashboard">Dashboard</Link>
      </div>

      {message && <div className="toast">{message}</div>}
      {loading && <div className="state-card">Loading products...</div>}
      {error && <div className="state-card error">{error}</div>}
      {!loading && !error && products.length === 0 && <div className="state-card">No products are available.</div>}

      <section className="card-grid product-grid">
        {products.map((product) => (
          <article className="feature-card product-card" key={product.productId}>
            <div className="product-image">Laundry Product</div>
            <span className="category-label">{product.category || "Laundry"}</span>
            <h2>{product.name}</h2>
            <p>{product.description || "Laundry product for student use."}</p>
            <div className="price">R{product.price.toFixed(2)}</div>
            <p className={product.inventoryQuantity > 0 ? "stock available-text" : "stock unavailable-text"}>
              {product.inventoryQuantity > 0 ? `${product.inventoryQuantity} in stock` : "Out of stock"}
            </p>
            <div className="button-row">
              <Link className="secondary-button" to={`/products/${encodeURIComponent(product.productId)}`}>
                Details
              </Link>
              <button className="primary-button" disabled={product.inventoryQuantity <= 0} onClick={() => handleAdd(product)}>
                Add to Cart
              </button>
            </div>
          </article>
        ))}
      </section>
    </main>
  );
}

export default Products;
