import { useCallback, useMemo, useState } from "react";

import { getApiErrorMessage } from "../../services/Api";
import { adminService } from "../../services/adminService";
import { Badge, LoadState, Notice, StatCard, fmtDateTime, money, useLoad } from "../../components/admin/ui";
import type { AdminOrderDetail, AdminPayment, OrderStatus } from "../../types/admin";

const ORDER_STATUSES: OrderStatus[] = ["PENDING", "PAID", "IN_PROGRESS", "COMPLETED", "CANCELLED"];
const loadOrders = () => adminService.getOrders();
const loadPayments = () => adminService.getPayments();

function AdminOrders() {
    const [tab, setTab] = useState<"orders" | "payments">("orders");
    const orders = useLoad(loadOrders);
    const payments = useLoad(loadPayments);
    const [filter, setFilter] = useState<"ALL" | OrderStatus>("ALL");
    const [payFilter, setPayFilter] = useState("ALL");
    const [detail, setDetail] = useState<AdminOrderDetail | null>(null);
    const [msg, setMsg] = useState<{ kind: "err" | "okk"; text: string } | null>(null);
    const [busy, setBusy] = useState(false);

    const shownOrders = useMemo(
        () => (orders.data ?? []).filter((o) => filter === "ALL" || o.status === filter),
        [orders.data, filter]
    );
    const shownPayments = useMemo(
        () => (payments.data ?? []).filter((p) => payFilter === "ALL" || p.status === payFilter),
        [payments.data, payFilter]
    );

    const completed = (orders.data ?? []).filter((o) => o.status === "COMPLETED").length;
    const outstanding = (orders.data ?? []).filter((o) => o.status !== "COMPLETED" && o.status !== "CANCELLED").length;
    const unpaidValue = (orders.data ?? [])
        .filter((o) => o.paymentStatus !== "Completed" && o.status !== "CANCELLED")
        .reduce((sum, o) => sum + o.totalAmount, 0);

    const refreshAll = useCallback(async () => {
        await Promise.all([orders.reload(), payments.reload()]);
    }, [orders, payments]);

    const openOrder = async (id: number) => {
        setMsg(null);
        try {
            setDetail(await adminService.getOrder(id));
        } catch (err) {
            setMsg({ kind: "err", text: getApiErrorMessage(err) });
        }
    };

    const run = async (action: () => Promise<unknown>, success: string, orderToReopen?: number) => {
        setBusy(true);
        setMsg(null);
        try {
            await action();
            setMsg({ kind: "okk", text: success });
            await refreshAll();
            if (orderToReopen !== undefined) setDetail(await adminService.getOrder(orderToReopen));
        } catch (err) {
            setMsg({ kind: "err", text: getApiErrorMessage(err) });
        } finally {
            setBusy(false);
        }
    };

    const verify = (p: AdminPayment) => {
        const label = p.paymentMethod === "EFT" ? "Confirm you have received this EFT" : "Confirm this payment";
        if (!window.confirm(`${label} (${p.transactionRef}, ${money(p.amount)})?`)) return;
        void run(() => adminService.verifyPayment(p.paymentId), `Payment ${p.transactionRef} verified.`, detail?.order.orderId);
    };

    const reject = (p: AdminPayment) => {
        if (!window.confirm(`Reject payment ${p.transactionRef}?`)) return;
        void run(() => adminService.rejectPayment(p.paymentId), `Payment ${p.transactionRef} rejected.`, detail?.order.orderId);
    };

    const paymentActions = (p: AdminPayment) =>
        p.status === "Pending" ? (
            <div className="adm-row">
                <button className="primary" disabled={busy} onClick={() => verify(p)}>
                    {p.paymentMethod === "EFT" ? "Verify EFT" : "Verify"}
                </button>
                <button className="danger" disabled={busy} onClick={() => reject(p)}>Reject</button>
            </div>
        ) : null;

    return (
        <div>
            <div className="adm-grid">
                <StatCard label="Completed orders" value={completed} tone="ok" />
                <StatCard label="Outstanding orders" value={outstanding} tone={outstanding ? "warn" : undefined} />
                <StatCard label="Unpaid order value" value={money(unpaidValue)} />
                <StatCard label="Pending payments" value={(payments.data ?? []).filter((p) => p.status === "Pending").length} tone="warn" />
            </div>

            <div className="adm-tabs">
                <button className={tab === "orders" ? "on" : ""} onClick={() => setTab("orders")}>Orders</button>
                <button className={tab === "payments" ? "on" : ""} onClick={() => setTab("payments")}>Payments</button>
            </div>
            {msg && <Notice kind={msg.kind}>{msg.text}</Notice>}

            {tab === "orders" && (
                <div className="adm-two">
                    <section className="adm-card">
                        <div className="adm-toolbar">
                            <label htmlFor="o-filter" style={{ margin: 0 }}>Status</label>
                            <select id="o-filter" value={filter} onChange={(e) => setFilter(e.target.value as "ALL" | OrderStatus)}>
                                <option value="ALL">All</option>
                                {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                            </select>
                            <button onClick={refreshAll}>Refresh</button>
                        </div>
                        <LoadState loading={orders.loading} error={orders.error} onRetry={orders.reload} />
                        <div className="adm-table-wrap">
                            <table>
                                <thead><tr><th>#</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th><th>Payment</th></tr></thead>
                                <tbody>
                                {shownOrders.map((o) => (
                                    <tr key={o.orderId} className={`clickable ${detail?.order.orderId === o.orderId ? "sel" : ""}`} onClick={() => void openOrder(o.orderId)}>
                                        <td>{o.orderId}</td>
                                        <td>{o.student?.name ?? "-"}</td>
                                        <td>{fmtDateTime(o.orderDate)}</td>
                                        <td>{money(o.totalAmount)}</td>
                                        <td><Badge text={o.status} /></td>
                                        <td><Badge text={o.paymentStatus} /></td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </section>

                    <section className="adm-card">
                        <h2>Order details</h2>
                        {!detail && <p className="adm-muted">Select an order to see its items and payments.</p>}
                        {detail && (
                            <>
                                <p>
                                    <strong>Order #{detail.order.orderId}</strong> · {detail.order.student?.name} ({detail.order.student?.email})<br />
                                    <span className="adm-muted">{fmtDateTime(detail.order.orderDate)} · {money(detail.order.totalAmount)}</span>
                                </p>
                                <div className="adm-row" style={{ marginBottom: 12 }}>
                                    <label htmlFor="o-status" style={{ margin: 0 }}>Status</label>
                                    <select
                                        id="o-status"
                                        value={detail.order.status}
                                        disabled={busy}
                                        onChange={(e) => void run(() => adminService.updateOrderStatus(detail.order.orderId, e.target.value as OrderStatus), "Order status updated.", detail.order.orderId)}
                                    >
                                        {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                                    </select>
                                </div>

                                <h3 style={{ fontSize: ".9rem" }}>Items</h3>
                                {detail.items.length === 0 ? <p className="adm-muted">No items.</p> : (
                                    <ul className="adm-list">
                                        {detail.items.map((i) => (
                                            <li key={i.orderItemId}><span>{i.productName ?? "Product"} × {i.quantity}</span><span>{money(i.subtotal)}</span></li>
                                        ))}
                                    </ul>
                                )}

                                <h3 style={{ fontSize: ".9rem", marginTop: 14 }}>Payments</h3>
                                {detail.payments.length === 0 ? <p className="adm-muted">No payment has been made for this order.</p> : (
                                    <ul className="adm-list">
                                        {detail.payments.map((p) => (
                                            <li key={p.paymentId} style={{ flexWrap: "wrap" }}>
                                                <span>{p.transactionRef} · {p.paymentMethod} · {money(p.amount)}</span>
                                                <span><Badge text={p.status} /></span>
                                                {paymentActions(p)}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </>
                        )}
                    </section>
                </div>
            )}

            {tab === "payments" && (
                <section className="adm-card">
                    <div className="adm-toolbar">
                        <label htmlFor="p-filter" style={{ margin: 0 }}>Status</label>
                        <select id="p-filter" value={payFilter} onChange={(e) => setPayFilter(e.target.value)}>
                            <option value="ALL">All</option>
                            <option value="Pending">Pending</option>
                            <option value="Completed">Completed</option>
                            <option value="Failed">Failed</option>
                        </select>
                        <button onClick={refreshAll}>Refresh</button>
                    </div>
                    <LoadState loading={payments.loading} error={payments.error} onRetry={payments.reload} />
                    <div className="adm-table-wrap">
                        <table>
                            <thead><tr><th>Reference</th><th>Customer</th><th>For</th><th>Method</th><th>Amount</th><th>Date</th><th>Status</th><th></th></tr></thead>
                            <tbody>
                            {shownPayments.map((p) => (
                                <tr key={p.paymentId}>
                                    <td>{p.transactionRef}</td>
                                    <td>{p.customer?.name ?? "-"}</td>
                                    <td>{p.orderId ? `Order #${p.orderId}` : p.bookingId ? `Booking #${p.bookingId}` : "-"}</td>
                                    <td>{p.paymentMethod}</td>
                                    <td>{money(p.amount)}</td>
                                    <td>{fmtDateTime(p.paymentDate)}</td>
                                    <td><Badge text={p.status} /></td>
                                    <td>{paymentActions(p)}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}
        </div>
    );
}

export default AdminOrders;