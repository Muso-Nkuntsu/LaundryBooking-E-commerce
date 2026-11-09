import { useMemo, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";

import { assetUrl, getApiErrorMessage } from "../../services/Api";
import { adminService } from "../../services/adminService";
import { Badge, LoadState, Notice, money, useLoad } from "../../components/admin/ui";
import type { AdminProduct, ProductInput } from "../../types/admin";

const EMPTY: ProductInput = { name: "", description: "", price: 0, category: "", inventoryQuantity: 0, imageUrl: "", active: true };
const loadProducts = () => adminService.getProducts();

const toInput = (p: AdminProduct): ProductInput => ({
    name: p.name,
    description: p.description ?? "",
    price: p.price,
    category: p.category ?? "",
    inventoryQuantity: p.inventoryQuantity,
    imageUrl: p.imageUrl ?? "",
    active: p.active,
});

function AdminProducts() {
    const { data, loading, error, reload } = useLoad(loadProducts);
    const [editingId, setEditingId] = useState<number | "new" | null>(null);
    const [form, setForm] = useState<ProductInput>(EMPTY);
    const [stockEdits, setStockEdits] = useState<Record<number, string>>({});
    const [search, setSearch] = useState("");
    const [msg, setMsg] = useState<{ kind: "err" | "okk" | "inf"; text: string } | null>(null);
    const [busy, setBusy] = useState(false);

    const editing = useMemo(
        () => (typeof editingId === "number" ? data?.find((p) => p.productId === editingId) ?? null : null),
        [data, editingId]
    );

    const shown = useMemo(() => {
        const q = search.trim().toLowerCase();
        return (data ?? []).filter((p) => !q || `${p.name} ${p.category ?? ""}`.toLowerCase().includes(q));
    }, [data, search]);

    const run = async (action: () => Promise<unknown>, success: string, kind: "okk" | "inf" = "okk") => {
        setBusy(true);
        setMsg(null);
        try {
            await action();
            setMsg({ kind, text: success });
            await reload();
            return true;
        } catch (err) {
            setMsg({ kind: "err", text: getApiErrorMessage(err) });
            return false;
        } finally {
            setBusy(false);
        }
    };

    const startNew = () => { setEditingId("new"); setForm(EMPTY); setMsg(null); };
    const startEdit = (p: AdminProduct) => { setEditingId(p.productId); setForm(toInput(p)); setMsg(null); };

    const save = async (event: FormEvent) => {
        event.preventDefault();
        if (form.price < 0 || form.inventoryQuantity < 0) {
            setMsg({ kind: "err", text: "Price and stock cannot be negative." });
            return;
        }
        if (editingId === "new") {
            const ok = await run(() => adminService.createProduct(form), "Product added.");
            if (ok) { setEditingId(null); setForm(EMPTY); }
        } else if (typeof editingId === "number") {
            await run(() => adminService.updateProduct(editingId, form), "Product saved.");
        }
    };

    const saveStock = (p: AdminProduct) => {
        const value = Number(stockEdits[p.productId]);
        if (!Number.isInteger(value) || value < 0) {
            setMsg({ kind: "err", text: "Stock must be a whole number, 0 or more." });
            return;
        }
        void run(() => adminService.setProductStock(p.productId, value), `Stock for ${p.name} updated.`).then(() =>
            setStockEdits((prev) => { const next = { ...prev }; delete next[p.productId]; return next; })
        );
    };

    const remove = (p: AdminProduct) => {
        if (!window.confirm(`Delete ${p.name}? Products that are part of past orders are deactivated instead.`)) return;
        void (async () => {
            setBusy(true);
            setMsg(null);
            try {
                const result = await adminService.deleteProduct(p.productId);
                setMsg({ kind: result.deactivated ? "inf" : "okk", text: result.message });
                if (editingId === p.productId) setEditingId(null);
                await reload();
            } catch (err) {
                setMsg({ kind: "err", text: getApiErrorMessage(err) });
            } finally {
                setBusy(false);
            }
        })();
    };

    const upload = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = "";
        if (!file || typeof editingId !== "number") return;
        if (file.size > 2 * 1024 * 1024) {
            setMsg({ kind: "err", text: "The image must be 2 MB or smaller." });
            return;
        }
        void run(() => adminService.uploadProductImage(editingId, file), "Image uploaded.");
    };

    return (
        <div>
            {msg && <Notice kind={msg.kind}>{msg.text}</Notice>}
            <LoadState loading={loading} error={error} onRetry={reload} />

            <div className="adm-two">
                <section className="adm-card">
                    <div className="adm-toolbar">
                        <input type="search" placeholder="Search products" value={search} onChange={(e) => setSearch(e.target.value)} aria-label="Search products" />
                        <button className="primary" onClick={startNew}>Add product</button>
                    </div>
                    <div className="adm-table-wrap">
                        <table>
                            <thead><tr><th></th><th>Name</th><th>Price</th><th>Stock</th><th>Status</th><th></th></tr></thead>
                            <tbody>
                            {shown.map((p) => {
                                const img = assetUrl(p.imageUrl);
                                return (
                                    <tr key={p.productId} className={p.productId === editingId ? "sel" : ""}>
                                        <td>{img ? <img className="adm-thumb" src={img} alt="" /> : <span className="adm-thumb" />}</td>
                                        <td>{p.name}<div className="adm-muted">{p.category}</div></td>
                                        <td>{money(p.price)}</td>
                                        <td>
                                            <div className="adm-row">
                                                <input
                                                    type="number" min={0} style={{ width: 80 }}
                                                    aria-label={`Stock for ${p.name}`}
                                                    value={stockEdits[p.productId] ?? String(p.inventoryQuantity)}
                                                    onChange={(e) => setStockEdits({ ...stockEdits, [p.productId]: e.target.value })}
                                                />
                                                {stockEdits[p.productId] !== undefined && stockEdits[p.productId] !== String(p.inventoryQuantity) && (
                                                    <button disabled={busy} onClick={() => saveStock(p)}>Save</button>
                                                )}
                                                {p.inventoryQuantity === 0 ? <Badge text="OUT" /> : p.inventoryQuantity <= 5 ? <Badge text="LOW" /> : null}
                                            </div>
                                        </td>
                                        <td><Badge text={p.active ? "Active" : "Inactive"} /></td>
                                        <td>
                                            <div className="adm-row">
                                                <button onClick={() => startEdit(p)}>Edit</button>
                                                <button disabled={busy} onClick={() => void run(() => adminService.setProductActive(p.productId, !p.active), p.active ? "Product deactivated." : "Product activated.")}>
                                                    {p.active ? "Deactivate" : "Activate"}
                                                </button>
                                                <button className="danger" disabled={busy} onClick={() => remove(p)}>Delete</button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                </section>

                <section className="adm-card">
                    <h2>{editingId === "new" ? "New product" : editing ? `Edit: ${editing.name}` : "Product form"}</h2>
                    {editingId === null && <p className="adm-muted">Choose "Add product" or "Edit" to work on a product.</p>}
                    {editingId !== null && (
                        <form className="adm-form" onSubmit={save}>
                            <div className="wide">
                                <label htmlFor="p-name">Name</label>
                                <input id="p-name" value={form.name} maxLength={120} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
                            </div>
                            <div className="wide">
                                <label htmlFor="p-desc">Description</label>
                                <textarea id="p-desc" rows={3} maxLength={1000} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
                            </div>
                            <div>
                                <label htmlFor="p-cat">Category</label>
                                <input id="p-cat" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
                            </div>
                            <div>
                                <label htmlFor="p-price">Price (R)</label>
                                <input id="p-price" type="number" min={0} step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: Number(e.target.value) })} required />
                            </div>
                            <div>
                                <label htmlFor="p-stock">Stock quantity</label>
                                <input id="p-stock" type="number" min={0} step={1} value={form.inventoryQuantity} onChange={(e) => setForm({ ...form, inventoryQuantity: Number(e.target.value) })} required />
                            </div>
                            <div>
                                <label htmlFor="p-active">Visible to customers</label>
                                <select id="p-active" value={form.active ? "yes" : "no"} onChange={(e) => setForm({ ...form, active: e.target.value === "yes" })}>
                                    <option value="yes">Yes (active)</option>
                                    <option value="no">No (inactive)</option>
                                </select>
                            </div>
                            <div className="wide">
                                <label htmlFor="p-img">Image URL (optional, http/https)</label>
                                <input id="p-img" value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} placeholder="https://..." />
                            </div>
                            {editing && (
                                <div className="wide">
                                    <label htmlFor="p-file">Or upload a picture (PNG, JPEG, WEBP · max 2 MB)</label>
                                    <input id="p-file" type="file" accept="image/png,image/jpeg,image/webp" onChange={upload} disabled={busy} />
                                    {assetUrl(editing.imageUrl) && <img className="adm-thumb" style={{ width: 96, height: 96, marginTop: 8 }} src={assetUrl(editing.imageUrl) ?? ""} alt="Current product" />}
                                </div>
                            )}
                            <div className="wide adm-row">
                                <button className="primary" type="submit" disabled={busy}>{editingId === "new" ? "Add product" : "Save changes"}</button>
                                <button type="button" onClick={() => setEditingId(null)}>Close</button>
                            </div>
                        </form>
                    )}
                </section>
            </div>
        </div>
    );
}

export default AdminProducts;