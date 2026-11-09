import { useState } from "react";
import { ErrorState, Loading } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { adminApi } from "../../services/adminApi";
import { Bars, DayChart, PageHead, Segmented, StatTile, money } from "./adminUi";

type Period = "7" | "30" | "90" | "365";

// Statistics worked out by the backend from the database, for the period chosen.
function AdminReports() {
  const [period, setPeriod] = useState<Period>("30");
  const { data, loading, error, reload } = useFetch(() => adminApi.report(Number(period)), period);

  return (
    <>
      <PageHead title="Reports" intro="Bookings and orders are counted by the day they were made. Stock shows what's on the shelf right now.">
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => window.print()}>Print</button>
      </PageHead>
      <div className="adm-tabs">
        <Segmented label="Period" value={period} onChange={setPeriod} options={[
          { value: "7", label: "Last 7 days" },
          { value: "30", label: "Last 30 days" },
          { value: "90", label: "Last 90 days" },
          { value: "365", label: "Last 12 months" },
        ]} />
      </div>

      {error ? <ErrorState message={error} onRetry={reload} /> : loading && !data ? <Loading message="Working out the numbers..." /> : data && (
        <>
          <h2 style={{ marginBottom: 12 }}>Overview</h2>
          <div className="adm-stats">
            <StatTile label="Money received" value={money(data.paymentsReceived)} tone="ok" note="Approved card and EFT payments" />
            <StatTile label="Bookings made" value={data.bookingsMade} note={`${data.cancellationRate}% cancelled`} />
            <StatTile label="Orders placed" value={data.ordersPlaced} note={`${data.ordersCancelled} cancelled`} />
            <StatTile label="Average order" value={money(data.averageOrderValue)} />
            <StatTile label="New students" value={data.newStudents} />
          </div>

          <h2 style={{ margin: "8px 0 12px" }}>Bookings</h2>
          <section className="card">
            <h3 style={{ marginBottom: 10 }}>Bookings made per day</h3>
            <DayChart points={data.bookingsPerDay} />
          </section>
          <div className="adm-two">
            <section className="card">
              <h3 style={{ marginBottom: 12 }}>By status</h3>
              <Bars items={data.bookingsByStatus} kind="booking" empty="No bookings in this period." />
            </section>
            <section className="card">
              <h3 style={{ marginBottom: 12 }}>Busiest machines</h3>
              <Bars items={data.bookingsByMachine.slice(0, 10)} empty="No bookings in this period." />
            </section>
          </div>

          <h2 style={{ margin: "24px 0 12px" }}>Orders and payments</h2>
          <div className="adm-two">
            <section className="card">
              <h3 style={{ marginBottom: 10 }}>Orders placed per day</h3>
              <DayChart points={data.ordersPerDay} />
            </section>
            <section className="card">
              <h3 style={{ marginBottom: 10 }}>Money received per day</h3>
              <DayChart points={data.paymentsPerDay} mode="amount" />
            </section>
          </div>
          <div className="adm-two">
            <section className="card">
              <h3 style={{ marginBottom: 12 }}>Orders by status</h3>
              <Bars items={data.ordersByStatus} kind="order" empty="No orders in this period." />
            </section>
            <section className="card">
              <h3 style={{ marginBottom: 12 }}>Best-selling products</h3>
              {data.topProducts.length === 0 ? <p className="muted">No sales in this period.</p> : (
                <table className="table" style={{ minWidth: 0 }}>
                  <thead><tr><th>Product</th><th className="num">Sold</th><th className="num">Value</th></tr></thead>
                  <tbody>
                    {data.topProducts.map((p) => (
                      <tr key={p.name}><td>{p.name}</td><td className="num">{p.quantity}</td><td className="num">{money(p.revenue)}</td></tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          </div>

          <h2 style={{ margin: "24px 0 12px" }}>Stock right now</h2>
          <div className="adm-stats">
            <StatTile label="Products in the shop" value={data.activeProducts} />
            <StatTile label="Low on stock" value={data.lowStockCount} tone={data.lowStockCount > 0 ? "warn" : undefined} to="/admin/products?status=low" />
            <StatTile label="Sold out" value={data.outOfStockCount} tone={data.outOfStockCount > 0 ? "bad" : undefined} />
            <StatTile label="Value of stock" value={money(data.stockValue)} note="At selling price" />
          </div>
          <section className="card">
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Product</th><th>Category</th><th className="num">In stock</th><th className="num">Sold (all time)</th><th className="num">Price</th><th>Status</th></tr></thead>
                <tbody>
                  {data.stock.map((p) => (
                    <tr key={p.productId}>
                      <td>{p.name}</td>
                      <td>{p.category || "—"}</td>
                      <td className="num">{p.inventoryQuantity}</td>
                      <td className="num">{p.unitsSold}</td>
                      <td className="num">{money(p.price)}</td>
                      <td>
                        {!p.active ? <span className="pill pill-off">Deactivated</span>
                          : p.inventoryQuantity === 0 ? <span className="pill pill-bad">Sold out</span>
                          : p.lowStock ? <span className="pill pill-warn">Low</span>
                          : <span className="pill pill-ok">OK</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {data.stock.length === 0 && <p className="adm-empty">No products yet.</p>}
            </div>
          </section>
        </>
      )}
    </>
  );
}

export default AdminReports;
