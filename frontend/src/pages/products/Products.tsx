import { useState } from "react";
import { Link } from "react-router-dom";
import type { Product } from "../../types/Product";
import { EmptyState, ErrorState, SkeletonGrid } from "../../components/common/States";
import Icon from "../../components/common/Icon";
import { useFetch } from "../../hooks/useFetch";
import { useToast } from "../../context/useToast";
import { getAllProducts } from "../../services/productService";
import { friendlyError } from "../../utilis/errorMessage";
import { formatCurrency } from "../../utilis/FormatCurrency";

function Products() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(() =>
    getAllProducts().catch((err: unknown) => {
      throw new Error(friendlyError(err, "We couldn't load the products."));
    }),
  );
  const products = data ?? [];

  const [category, setCategory] = useState("All");
  const categories = ["All", ...Array.from(new Set(products.map((product) => product.category || "Laundry")))];
  const visible = category === "All" ? products : products.filter((product) => (product.category || "Laundry") === category);

  const handleAdd = (product: Product) => {
    if (product.inventoryQuantity <= 0) return;
    // The cart isn't connected to the backend yet; this only confirms the click.
    toast.success(`${product.name} added to your cart.`);
  };

  return (
    <div className="page">
      <header className="page-head">
        <div>
          <h1>Shop</h1>
          <p>Detergent, softener and other laundry supplies to collect with your wash.</p>
        </div>
        <Link to="/order-items" className="btn btn-ghost">My orders</Link>
      </header>

      {loading && <SkeletonGrid count={6} height={300} />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && products.length === 0 && (
        <EmptyState title="Nothing in the shop yet" message="Products will appear here once they are added." />
      )}

      {categories.length > 2 && (
        <div className="segmented" role="group" aria-label="Filter by category" style={{ marginBottom: 18 }}>
          {categories.map((name) => (
            <button key={name} type="button" aria-pressed={category === name} onClick={() => setCategory(name)}>
              {name}
            </button>
          ))}
        </div>
      )}

      <section className="grid grid-3">
        {visible.map((product) => {
          const inStock = product.inventoryQuantity > 0;
          return (
            <article className="card tile hoverable" key={product.productId}>
              <div className="product-art" aria-hidden="true"><Icon name="bag" size={44} /></div>
              <span className="muted small">{product.category || "Laundry"}</span>
              <h2>{product.name}</h2>
              <p className="muted">{product.description || "Laundry product for student use."}</p>
              <div className="row">
                <span className="price">{formatCurrency(product.price)}</span>
                <span className={`pill ${inStock ? "pill-ok" : "pill-bad"}`}>{inStock ? `${product.inventoryQuantity} in stock` : "Out of stock"}</span>
              </div>
              <div className="btn-row push">
                <Link className="btn btn-ghost btn-sm" to={`/products/${encodeURIComponent(product.productId)}`}>
                  Details
                </Link>
                <button type="button" className="btn btn-primary btn-sm" style={{ flex: 1 }} disabled={!inStock} onClick={() => handleAdd(product)}>
                  Add to cart
                </button>
              </div>
            </article>
          );
        })}
      </section>
    </div>
  );
}

export default Products;
