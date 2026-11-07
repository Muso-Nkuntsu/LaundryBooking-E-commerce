import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { ErrorState, Loading } from "../../components/common/States";
import { useToast } from "../../context/useToast";
import { useFetch } from "../../hooks/useFetch";
import { ORDER_NEXT, adminApi } from "../../services/adminApi";
import type { AdminPayment, OrderStatus } from "../../services/adminApi";
import { friendlyError } from "../../utilis/errorMessage";
import { ACTION_LABEL, Modal, PageHead, Segmented, Status, dateTime, money, statusLabel } from "./adminUi";

type Tab = "orders" | "payments";

// Shop orders and every payment, including checking EFTs against the bank statement.
function AdminOrders() {
  const [params] = useSearchParams();
  const [tab, setTab] = useState<Tab>(params.get("tab") === "payments" ? "payments" : "orders");

  return (
    <>
      <PageHead title="Orders & payments" intro="Approve an EFT only once the money shows in the bank account." />
      <div className="adm-tabs">
        <Segmented label="Section" value={tab} onChange={setTab} options={[
          { value: "orders", label: "Shop orders" },
          { value: "payments", label: "Payments" },
        ]} />
      </div>
      {tab === "orders" ? <Orders initialStatus={params.get("status") ?? ""} /> : <Payments />}
    </>
  );
}

// =====================================================================
// Orders
// =====================================================================

function Orders({ initialStatus }: { initialStatus: string }) {
  const [status, setStatus] = useState(initialStatus);
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [selected, setSelected] = useState<number | null>(null);
  const { data, loading, error, reload } = useFetch(() => adminApi.orders({ status, q }), `${status}|${q}`);
  const orders = data ?? [];

  return (
    <>
      <form className="adm-tools" onSubmit={(e) => { e.preventDefault(); setQ(search.trim()); }}>
        <input className="input grow" type="search" placeholder="Search student, email or #order" value={search}
               onChange={(e) => setSearch(e.target.value)} aria-label="Search orders" />
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
      </form>
      <div className="adm-tools">
        <Segmented label="Status" value={status} onChange={setStatus} options={[
          { value: "", label: "All" },
          { value: "PENDING", label: "Awaiting payment" },
          { value: "OUTSTANDING", label: "To prepare" },
          { value: "COMPLETED", label: "Completed" },
          { value: "CANCELLED", label: "Cancelled" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : (
        <section className="card">
          <p className="adm-count">{orders.length} {orders.length === 1 ? "order" : "orders"}</p>
          <div className="table-wrap">
            <table className="table" style={{ minWidth: 720 }}>
              <thead><tr><th>#</th><th>Student</th><th>Placed</th><th className="num">Items</th><th className="num">Total</th><th>Payment</th><th>Status</th></tr></thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.orderId} className="clickable" onClick={() => setSelected(o.orderId)}>
                    <td>
                      <button type="button" className="link-btn" onClick={(e) => { e.stopPropagation(); setSelected(o.orderId); }}>#{o.orderId}</button>
                    </td>
                    <td>{o.studentName}<span className="sub">{o.studentEmail}</span></td>
                    <td>{dateTime(o.orderDate)}</td>
                    <td className="num">{o.itemCount}</td>
                    <td className="num">{money(o.totalAmount)}</td>
                    <td><Status kind="orderpay" value={o.paymentStatus} /></td>
                    <td><Status kind="order" value={o.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {orders.length === 0 && <p className="adm-empty">No orders match.</p>}
          </div>
        </section>
      )}

      {selected !== null && <OrderDetails id={selected} onClose={() => setSelected(null)} onChanged={reload} />}
    </>
  );
}

function OrderDetails({ id, onClose, onChanged }: { id: number; onClose: () => void; onChanged: () => void }) {
  const toast = useToast();
  const { data, loading, error, reload, setData } = useFetch(() => adminApi.order(id), id);
  const [busy, setBusy] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [verifying, setVerifying] = useState<{ payment: AdminPayment; approved: boolean } | null>(null);

  const change = async (status: OrderStatus) => {
    setBusy(true);
    try {
      const updated = await adminApi.setOrderStatus(id, status);
      setData(() => updated);
      toast.success(`Order #${id}: ${statusLabel("order", status)}.`);
      onChanged();
    } catch (err) {
      toast.error(friendlyError(err, "The status couldn't be changed."));
    } finally {
      setBusy(false);
      setCancelling(false);
    }
  };

  const verify = async (payment: AdminPayment, approved: boolean) => {
    setBusy(true);
    try {
      await adminApi.verifyPayment(payment.paymentId, approved);
      toast.success(approved ? "Payment approved. The order is now paid." : "Payment rejected.");
      reload();
      onChanged();
    } catch (err) {
      toast.error(friendlyError(err, "The payment couldn't be updated."));
    } finally {
      setBusy(false);
      setVerifying(null);
    }
  };

  const order = data?.order;

  return (
    <Modal title={`Order #${id}`} onClose={onClose}>
      {loading && !data ? <Loading /> : error || !data || !order ? <ErrorState message={error ?? "Not found"} onRetry={reload} /> : (
        <>
          <div className="btn-row"><Status kind="order" value={order.status} /><Status kind="orderpay" value={order.paymentStatus} /></div>
          <dl className="adm-facts">
            <div><dt>Student</dt><dd>{order.studentName}</dd></div>
            <div><dt>Email</dt><dd>{order.studentEmail}</dd></div>
            <div><dt>Phone</dt><dd>{data.studentPhone || "—"}</dd></div>
            <div><dt>Placed</dt><dd>{dateTime(order.orderDate)}</dd></div>
          </dl>

          <div className="adm-section">
            <h3>Items</h3>
            <div className="table-wrap">
              <table className="table" style={{ minWidth: 420 }}>
                <thead><tr><th>Product</th><th className="num">Price</th><th className="num">Qty</th><th className="num">Total</th></tr></thead>
                <tbody>
                  {data.items.map((item) => (
                    <tr key={item.orderItemId}>
                      <td>{item.productName}</td>
                      <td className="num">{money(item.unitPrice)}</td>
                      <td className="num">{item.quantity}</td>
                      <td className="num">{money(item.subtotal)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot><tr><td colSpan={3}>Order total</td><td className="num">{money(order.totalAmount)}</td></tr></tfoot>
              </table>
            </div>
          </div>

          <div className="adm-section">
            <h3>Payments</h3>
            {data.payments.length === 0 ? <p className="muted">The student hasn't paid yet.</p> : (
              <ul className="list">
                {data.payments.map((p) => (
                  <li key={p.paymentId}>
                    <span>
                      {p.method} · {money(p.amount)} · {dateTime(p.paymentDate)}
                      {p.transactionRef && <span className="sub muted small">Ref {p.transactionRef}{p.payerBank ? ` · ${p.payerBank}` : ""}</span>}
                    </span>
                    {p.status === "PENDING" ? (
                      <span className="btn-row">
                        <button type="button" className="btn btn-primary btn-sm" disabled={busy}
                                onClick={() => setVerifying({ payment: p, approved: true })}>Approve</button>
                        <button type="button" className="btn btn-danger btn-sm" disabled={busy}
                                onClick={() => setVerifying({ payment: p, approved: false })}>Reject</button>
                      </span>
                    ) : <Status kind="payment" value={p.status} />}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="adm-section">
            <h3>Change status</h3>
            {ORDER_NEXT[order.status].length === 0 ? (
              <p className="muted">This order is finished, so its status can't change any more.</p>
            ) : (
              <>
                {order.status === "PENDING" && (
                  <p className="muted small">"Mark as paid" is for cash paid at the office. For an EFT, approve the payment above instead.</p>
                )}
                <div className="btn-row">
                  {ORDER_NEXT[order.status].map((next) => (
                    <button key={next} type="button" disabled={busy}
                            className={`btn btn-sm ${next === "CANCELLED" ? "btn-danger" : "btn-primary"}`}
                            onClick={() => (next === "CANCELLED" ? setCancelling(true) : change(next))}>
                      {ACTION_LABEL[next]}
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>
        </>
      )}

      {cancelling && (
        <ConfirmDialog title={`Cancel order #${id}?`} confirmLabel="Cancel order" cancelLabel="Keep it" busy={busy}
                       message={`The items go back into stock.${order && order.status !== "PENDING" ? " This order was paid, so remember to refund the student." : ""}`}
                       onCancel={() => setCancelling(false)} onConfirm={() => change("CANCELLED")} />
      )}
      {verifying && <VerifyDialog payment={verifying.payment} approved={verifying.approved} busy={busy}
                                  onCancel={() => setVerifying(null)} onConfirm={() => verify(verifying.payment, verifying.approved)} />}
    </Modal>
  );
}

// =====================================================================
// Payments
// =====================================================================

function Payments() {
  const toast = useToast();
  const [status, setStatus] = useState("PENDING");
  const [method, setMethod] = useState("");
  const [search, setSearch] = useState("");
  const [q, setQ] = useState("");
  const [verifying, setVerifying] = useState<{ payment: AdminPayment; approved: boolean } | null>(null);
  const [busy, setBusy] = useState(false);
  const { data, loading, error, reload } = useFetch(() => adminApi.payments({ status, method, q }), `${status}|${method}|${q}`);
  const payments = data ?? [];

  const verify = async (payment: AdminPayment, approved: boolean) => {
    setBusy(true);
    try {
      await adminApi.verifyPayment(payment.paymentId, approved);
      toast.success(approved
        ? `Payment approved. ${payment.paidFor} is now ${payment.bookingId ? "confirmed" : "paid"}.`
        : "Payment rejected. The student can pay again.");
      reload();
    } catch (err) {
      toast.error(friendlyError(err, "The payment couldn't be updated."));
    } finally {
      setBusy(false);
      setVerifying(null);
    }
  };

  return (
    <>
      <form className="adm-tools" onSubmit={(e) => { e.preventDefault(); setQ(search.trim()); }}>
        <input className="input grow" type="search" placeholder="Search student, reference or payer" value={search}
               onChange={(e) => setSearch(e.target.value)} aria-label="Search payments" />
        <select className="input" value={method} onChange={(e) => setMethod(e.target.value)} aria-label="Method">
          <option value="">Card and EFT</option>
          <option value="EFT">EFT only</option>
          <option value="CARD">Card only</option>
        </select>
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
      </form>
      <div className="adm-tools">
        <Segmented label="Status" value={status} onChange={setStatus} options={[
          { value: "PENDING", label: "To verify" },
          { value: "COMPLETED", label: "Received" },
          { value: "FAILED", label: "Rejected" },
          { value: "", label: "All" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading /> : (
        <section className="card">
          <p className="adm-count">
            {payments.length} {payments.length === 1 ? "payment" : "payments"} · {money(payments.reduce((sum, p) => sum + p.amount, 0))}
          </p>
          <div className="table-wrap">
            <table className="table" style={{ minWidth: 820 }}>
              <thead>
                <tr><th>Date</th><th>For</th><th>Student</th><th>EFT details</th><th className="num">Amount</th><th>Status</th><th /></tr>
              </thead>
              <tbody>
                {payments.map((p) => (
                  <tr key={p.paymentId}>
                    <td>{dateTime(p.paymentDate)}<span className="sub">{p.method}</span></td>
                    <td>{p.paidFor}</td>
                    <td>{p.studentName || "—"}<span className="sub">{p.studentEmail}</span></td>
                    <td>
                      {p.method === "EFT" ? (
                        <>
                          Ref: <strong>{p.transactionRef || "—"}</strong>
                          <span className="sub">{p.payerName || "—"} · {p.payerBank || "—"}</span>
                        </>
                      ) : <span className="muted">—</span>}
                    </td>
                    <td className="num">{money(p.amount)}</td>
                    <td><Status kind="payment" value={p.status} /></td>
                    <td>
                      {p.status === "PENDING" && (
                        <div className="actions-cell">
                          <button type="button" className="btn btn-primary btn-sm" disabled={busy}
                                  onClick={() => setVerifying({ payment: p, approved: true })}>Approve</button>
                          <button type="button" className="btn btn-danger btn-sm" disabled={busy}
                                  onClick={() => setVerifying({ payment: p, approved: false })}>Reject</button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {payments.length === 0 && <p className="adm-empty">{status === "PENDING" ? "No payments are waiting to be checked." : "No payments match."}</p>}
          </div>
        </section>
      )}

      {verifying && <VerifyDialog payment={verifying.payment} approved={verifying.approved} busy={busy}
                                  onCancel={() => setVerifying(null)} onConfirm={() => verify(verifying.payment, verifying.approved)} />}
    </>
  );
}

function VerifyDialog({ payment, approved, busy, onCancel, onConfirm }: {
  payment: AdminPayment; approved: boolean; busy: boolean; onCancel: () => void; onConfirm: () => void;
}) {
  const details = `${money(payment.amount)} for ${payment.paidFor}` +
    (payment.transactionRef ? `, reference "${payment.transactionRef}"` : "") +
    (payment.payerName ? `, paid by ${payment.payerName}` : "") +
    (payment.payerBank ? ` (${payment.payerBank})` : "");
  return approved ? (
    <ConfirmDialog title="Approve this payment?" confirmLabel="Approve" cancelLabel="Go back" busy={busy} onCancel={onCancel} onConfirm={onConfirm}
                   message={`Only approve once you can see ${details} in the bank account. The ${payment.bookingId ? "booking will be confirmed" : "order will be marked paid"} and the student notified.`} />
  ) : (
    <ConfirmDialog title="Reject this payment?" confirmLabel="Reject" cancelLabel="Go back" busy={busy} onCancel={onCancel} onConfirm={onConfirm}
                   message={`Reject ${details} if the money never arrived or the details don't match. The student can then pay again.`} />
  );
}

export default AdminOrders;
