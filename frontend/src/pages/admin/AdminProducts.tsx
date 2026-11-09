import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import ProductImage from "../../components/common/ProductImage";
import { ErrorState, Loading } from "../../components/common/States";
import TextField from "../../components/common/TextField";
import { useToast } from "../../context/useToast";
import { useFetch } from "../../hooks/useFetch";
import { assetUrl } from "../../services/Api";
import { adminApi } from "../../services/adminApi";
import type { AdminProduct } from "../../services/adminApi";
import { friendlyError } from "../../utilis/errorMessage";
import { Modal, PageHead, Segmented, money } from "./adminUi";

type StatusFilter = "" | "active" | "inactive" | "low";
const MAX_IMAGE_BYTES = 2 * 1024 * 1024;

// Shop products: add, edit, price, stock, pictures, and hide or delete.
function AdminProducts() {
  const toast = useToast();
  const [params] = useSearchParams();
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [category, setCategory] = useState("");
  const [status, setStatus] = useState<StatusFilter>((params.get("status") as StatusFilter) ?? "");
  const [editing, setEditing] = useState<AdminProduct | "new" | null>(null);
  const [stockFor, setStockFor] = useState<AdminProduct | null>(null);
  const [deleting, setDeleting] = useState<AdminProduct | null>(null);
  const [busy, setBusy] = useState(false);

  const { data, loading, error, reload } = useFetch(() => adminApi.products({ q, category, status }), `${q}|${category}|${status}`);
  const categories = useFetch(() => adminApi.categories());
  const products = data ?? [];

  const run = async (action: () => Promise<unknown>, success: string) => {
    setBusy(true);
    try {
      await action();
      toast.success(success);
      reload();
    } catch (err) {
      toast.error(friendlyError(err, "That didn't work. Try again."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHead title="Products" intro="What students can buy in the shop. Deactivated products are hidden from the shop.">
        <button type="button" className="btn btn-primary" onClick={() => setEditing("new")}>Add product</button>
      </PageHead>

      <form className="adm-tools" onSubmit={(e) => { e.preventDefault(); setQ(search.trim()); }}>
        <input className="input grow" type="search" placeholder="Search products" value={search}
               onChange={(e) => setSearch(e.target.value)} aria-label="Search products" />
        <select className="input" value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Category">
          <option value="">All categories</option>
          {(categories.data ?? []).map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
      </form>
      <div className="adm-tools">
        <Segmented label="Show" value={status} onChange={setStatus} options={[
          { value: "", label: "All" },
          { value: "active", label: "In the shop" },
          { value: "low", label: "Low stock" },
          { value: "inactive", label: "Deactivated" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : (
        <section className="card">
          <p className="adm-count">{products.length} {products.length === 1 ? "product" : "products"}</p>
          <div className="table-wrap">
            <table className="table" style={{ minWidth: 760 }}>
              <thead>
                <tr><th /><th>Product</th><th className="num">Price</th><th className="num">In stock</th><th className="num">Sold</th><th>Status</th><th /></tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr key={p.productId}>
                    <td><ProductImage name={p.name} imageUrl={p.imageUrl} size="thumb" /></td>
                    <td><strong>{p.name}</strong><span className="sub">{p.category || "No category"}</span></td>
                    <td className="num">{money(p.price)}</td>
                    <td className="num">
                      {p.inventoryQuantity}{" "}
                      {p.inventoryQuantity === 0 ? <span className="pill pill-bad">Sold out</span> : p.lowStock && <span className="pill pill-warn">Low</span>}
                    </td>
                    <td className="num">{p.unitsSold}</td>
                    <td>{p.active ? <span className="pill pill-ok">In the shop</span> : <span className="pill pill-off">Deactivated</span>}</td>
                    <td>
                      <div className="actions-cell">
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setEditing(p)}>Edit</button>
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setStockFor(p)}>Stock</button>
                        <button type="button" className="btn btn-ghost btn-sm" disabled={busy}
                                onClick={() => run(() => adminApi.setProductActive(p.productId, !p.active),
                                  p.active ? `${p.name} is hidden from the shop.` : `${p.name} is back in the shop.`)}>
                          {p.active ? "Deactivate" : "Activate"}
                        </button>
                        <button type="button" className="btn btn-danger btn-sm" disabled={busy} onClick={() => setDeleting(p)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {products.length === 0 && <p className="adm-empty">No products match.</p>}
          </div>
        </section>
      )}

      {editing && (
        <ProductForm product={editing === "new" ? null : editing} categories={categories.data ?? []}
                     onClose={() => { setEditing(null); reload(); categories.reload(); }} />
      )}
      {stockFor && <StockForm product={stockFor} onClose={() => setStockFor(null)} onSaved={() => { setStockFor(null); reload(); }} />}
      {deleting && (
        <ConfirmDialog title={`Delete ${deleting.name}?`} confirmLabel="Delete product" cancelLabel="Go back" busy={busy}
                       message="This can't be undone. A product that's in past orders can't be deleted; deactivate it instead."
                       onCancel={() => setDeleting(null)}
                       onConfirm={async () => { await run(() => adminApi.deleteProduct(deleting.productId), "Product deleted."); setDeleting(null); }} />
      )}
    </>
  );
}

/** Add or edit a product. After adding, the window stays open so a picture can be uploaded. */
function ProductForm({ product, categories, onClose }: { product: AdminProduct | null; categories: string[]; onClose: () => void }) {
  const toast = useToast();
  const [current, setCurrent] = useState<AdminProduct | null>(product);
  const [name, setName] = useState(product?.name ?? "");
  const [category, setCategory] = useState(product?.category ?? "");
  const [price, setPrice] = useState(product ? String(product.price) : "");
  const [stock, setStock] = useState("0");
  const [description, setDescription] = useState(product?.description ?? "");
  const [busy, setBusy] = useState(false);

  const act = async (action: () => Promise<AdminProduct | void>, success: string) => {
    setBusy(true);
    try {
      const result = await action();
      if (result) setCurrent(result);
      toast.success(success);
    } catch (err) {
      toast.error(friendlyError(err, "That didn't work. Try again."));
    } finally {
      setBusy(false);
    }
  };

  const save = (event: FormEvent) => {
    event.preventDefault();
    const body = { name: name.trim(), category: category.trim(), description: description.trim(), price: Number(price) };
    if (current) {
      act(() => adminApi.updateProduct(current.productId, body), "Product saved.");
    } else {
      act(() => adminApi.createProduct({ ...body, inventoryQuantity: Number(stock) }), "Product added. You can add a picture now.");
    }
  };

  const onPicture = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = ""; // so the same file can be chosen again
    if (!file || !current) return;
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      toast.error("Choose a JPEG, PNG or WEBP picture.");
      return;
    }
    if (file.size > MAX_IMAGE_BYTES) {
      toast.error("The picture must be 2 MB or smaller.");
      return;
    }
    act(() => adminApi.uploadProductImage(current.productId, file), "Picture uploaded.");
  };

  return (
    <Modal title={current ? `Edit ${current.name}` : "Add a product"} onClose={onClose}>
      <form className="stack" onSubmit={save}>
        <div className="form-grid">
          <TextField id="p-name" label="Name" value={name} onChange={setName} required maxLength={100} />
          <div className="field">
            <label htmlFor="p-category">Category</label>
            <input id="p-category" className="input" list="p-categories" value={category}
                   onChange={(e) => setCategory(e.target.value)} placeholder="Detergent" maxLength={50} />
            <datalist id="p-categories">{categories.map((c) => <option key={c} value={c} />)}</datalist>
          </div>
          <TextField id="p-price" label="Price (R)" type="number" min="0.01" step="0.01" value={price} onChange={setPrice} required />
          {!current && <TextField id="p-stock" label="Opening stock" type="number" min={0} value={stock} onChange={setStock} required />}
          <div className="field span-2">
            <label htmlFor="p-desc">Description</label>
            <textarea id="p-desc" className="input" rows={3} maxLength={1000} value={description}
                      onChange={(e) => setDescription(e.target.value)} />
          </div>
        </div>
        {current && <p className="muted small">Change the stock with the Stock button. A new price only applies to new orders.</p>}
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Saving..." : current ? "Save changes" : "Add product"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>{current ? "Close" : "Cancel"}</button>
        </div>
      </form>

      {current && (
        <div className="adm-section">
          <h3>Picture</h3>
          {current.imageUrl
            ? <img src={assetUrl(current.imageUrl)} alt={current.name} style={{ width: 180, height: 180, objectFit: "cover", borderRadius: 14 }} />
            : <p className="muted">No picture yet. Students see a bag icon instead.</p>}
          <div className="btn-row">
            <label className="btn btn-ghost btn-sm" style={{ cursor: busy ? "not-allowed" : "pointer" }}>
              {current.imageUrl ? "Replace picture" : "Upload picture"}
              <input type="file" accept="image/jpeg,image/png,image/webp" hidden disabled={busy} onChange={onPicture} />
            </label>
            {current.imageUrl && (
              <button type="button" className="btn btn-danger btn-sm" disabled={busy}
                      onClick={() => act(() => adminApi.removeProductImage(current.productId), "Picture removed.")}>
                Remove picture
              </button>
            )}
          </div>
          <p className="muted small">JPEG, PNG or WEBP, up to 2 MB.</p>
        </div>
      )}
    </Modal>
  );
}

function StockForm({ product, onClose, onSaved }: { product: AdminProduct; onClose: () => void; onSaved: () => void }) {
  const toast = useToast();
  const [quantity, setQuantity] = useState(String(product.inventoryQuantity));
  const [busy, setBusy] = useState(false);

  const save = async (event: FormEvent) => {
    event.preventDefault();
    setBusy(true);
    try {
      await adminApi.setStock(product.productId, Number(quantity));
      toast.success(`Stock for ${product.name} is now ${quantity}.`);
      onSaved();
    } catch (err) {
      toast.error(friendlyError(err, "The stock couldn't be updated."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title={`Stock: ${product.name}`} onClose={onClose} narrow>
      <form className="stack" onSubmit={save}>
        <TextField id="stock-qty" label={`How many are in stock now? (was ${product.inventoryQuantity})`} type="number"
                   min={0} max={100000} value={quantity} onChange={setQuantity} required autoFocus />
        <div className="btn-row">
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Saving..." : "Save stock"}</button>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </Modal>
  );
}

export default AdminProducts;
