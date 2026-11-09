import { adminService } from "../../services/adminService";
import { Badge, LoadState, StatCard, fmtDateTime, money, useLoad } from "../../components/admin/ui";

const loadReports = () => adminService.getReports();

function Bars({ data }: { data: Record<string, number> }) {
    const max = Math.max(1, ...Object.values(data));
    return (
        <div>
            {Object.entries(data).map(([name, count]) => (
                <div className="adm-bar" key={name}>
                    <span className="name">{name.replace(/_/g, " ")}</span>
                    <span className="track"><span className="fill" style={{ width: `${(count / max) * 100}%`, display: "block" }} /></span>
                    <span className="num">{count}</span>
                </div>
            ))}
        </div>
    );
}

function AdminReports() {
    const { data, loading, error, reload } = useLoad(loadReports);

    return (
        <div>
            <LoadState loading={loading} error={error} onRetry={reload} />
            {data && (
                <>
                    <div className="adm-grid">
                        <StatCard label="Verified revenue" value={money(data.revenueCompleted)} tone="ok" />
                        <StatCard label="Awaiting verification" value={money(data.revenuePending)} tone="warn" />
                        <StatCard label="Units in stock" value={data.totalStockUnits} />
                        <StatCard label="Low stock" value={data.lowStockProducts} tone={data.lowStockProducts ? "warn" : undefined} />
                        <StatCard label="Out of stock" value={data.outOfStockProducts} tone={data.outOfStockProducts ? "bad" : undefined} />
                    </div>

                    <div className="adm-grid" style={{ gridTemplateColumns: "repeat(auto-fit,minmax(260px,1fr))" }}>
                        <section className="adm-card"><h2>Bookings by status</h2><Bars data={data.bookingsByStatus} /></section>
                        <section className="adm-card"><h2>Orders by status</h2><Bars data={data.ordersByStatus} /></section>
                        <section className="adm-card"><h2>Machines by status</h2><Bars data={data.machinesByStatus} /></section>
                    </div>

                    <div className="adm-two">
                        <section className="adm-card">
                            <h2>Product stock</h2>
                            <div className="adm-table-wrap">
                                <table>
                                    <thead><tr><th>Product</th><th>Category</th><th>Stock</th><th>Level</th><th>Listed</th></tr></thead>
                                    <tbody>
                                    {data.stock.map((s) => (
                                        <tr key={s.productId}>
                                            <td>{s.name}</td>
                                            <td>{s.category ?? "-"}</td>
                                            <td>{s.inventoryQuantity}</td>
                                            <td><Badge text={s.level} /></td>
                                            <td>{s.active ? "Yes" : "No"}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        <section className="adm-card">
                            <h2>System activity</h2>
                            {data.recentActivity.length === 0 ? <p className="adm-muted">No activity yet.</p> : (
                                <ul className="adm-list">
                                    {data.recentActivity.map((a) => (
                                        <li key={`${a.type}-${a.id}`}>
                                            <span><Badge text={a.type} /> {a.description}</span>
                                            <span className="adm-muted">{fmtDateTime(a.at)}</span>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </section>
                    </div>
                </>
            )}
        </div>
    );
}

export default AdminReports;