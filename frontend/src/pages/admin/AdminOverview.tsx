import { adminService } from "../../services/adminService";
import { Badge, LoadState, StatCard, fmtDateTime, money, useLoad } from "../../components/admin/ui";

const loadOverview = () => adminService.getOverview();

function AdminOverview() {
    const { data, loading, error, reload } = useLoad(loadOverview);

    return (
        <div>
            <LoadState loading={loading} error={error} onRetry={reload} />
            {data && (
                <>
                    <div className="adm-grid">
                        <StatCard label="Registered users" value={data.totalUsers} hint={`${data.activeUsers} active`} />
                        <StatCard label="Total bookings" value={data.totalBookings} />
                        <StatCard label="Pending bookings" value={data.pendingBookings} tone={data.pendingBookings ? "warn" : undefined} />
                        <StatCard label="Completed bookings" value={data.completedBookings} tone="ok" />
                        <StatCard label="Total orders" value={data.totalOrders} hint={`${data.pendingOrders} pending`} />
                        <StatCard label="Pending payments" value={data.pendingPayments} tone={data.pendingPayments ? "warn" : undefined} />
                        <StatCard label="Revenue (verified)" value={money(data.revenue)} tone="ok" />
                        <StatCard label="Low-stock products" value={data.lowStockProducts} tone={data.lowStockProducts ? "bad" : undefined} />
                    </div>

                    <div className="adm-two">
                        <section className="adm-card">
                            <h2>Recent bookings</h2>
                            {data.recentBookings.length === 0 ? (
                                <p className="adm-muted">No bookings yet.</p>
                            ) : (
                                <div className="adm-table-wrap">
                                    <table>
                                        <thead>
                                        <tr><th>#</th><th>Student</th><th>Machine</th><th>Slot</th><th>Status</th></tr>
                                        </thead>
                                        <tbody>
                                        {data.recentBookings.map((b) => (
                                            <tr key={b.id}>
                                                <td>{b.id}</td>
                                                <td>{b.student?.name ?? "-"}</td>
                                                <td>{b.machineNumber ?? "-"}</td>
                                                <td>{b.slotDate} {b.startTime?.slice(0, 5)}</td>
                                                <td><Badge text={b.status} /></td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </section>

                        <section className="adm-card">
                            <h2>Recent orders</h2>
                            {data.recentOrders.length === 0 ? (
                                <p className="adm-muted">No orders yet.</p>
                            ) : (
                                <div className="adm-table-wrap">
                                    <table>
                                        <thead>
                                        <tr><th>#</th><th>Customer</th><th>Total</th><th>Status</th><th>Payment</th></tr>
                                        </thead>
                                        <tbody>
                                        {data.recentOrders.map((o) => (
                                            <tr key={o.orderId} title={fmtDateTime(o.orderDate)}>
                                                <td>{o.orderId}</td>
                                                <td>{o.student?.name ?? "-"}</td>
                                                <td>{money(o.totalAmount)}</td>
                                                <td><Badge text={o.status} /></td>
                                                <td><Badge text={o.paymentStatus} /></td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </section>
                    </div>
                </>
            )}
        </div>
    );
}

export default AdminOverview;