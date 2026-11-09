import { Link } from "react-router-dom";
import { Loading, ErrorState } from "../../components/common/States";
import { useFetch } from "../../hooks/useFetch";
import { adminApi } from "../../services/adminApi";
import { getDisplayName } from "../../services/session";
import { PageHead, StatTile, Status, dateTime, money, shortDate, shortTime } from "./adminUi";

// Admin home: the key numbers, what needs attention, and the latest bookings and orders.
function AdminDashboard() {
  const { data, loading, error, reload } = useFetch(() => adminApi.dashboard());

  if (loading && !data) return <Loading message="Loading the dashboard..." />;
  if (error || !data) return <ErrorState message={error ?? "The dashboard didn't load."} onRetry={reload} />;

  const todo = [
    { count: data.paymentsToVerify, text: `EFT payments to check (${money(data.amountToVerify)})`, to: "/admin/orders?tab=payments" },
    { count: data.ordersToFulfil, text: "Paid orders to prepare or hand over", to: "/admin/orders?status=OUTSTANDING" },
    { count: data.pendingBookings, text: "Bookings waiting for payment", to: "/admin/bookings?status=PENDING" },
    { count: data.machinesOutOfOrder, text: "Machines out of order", to: "/admin/facilities?tab=machines" },
    { count: data.lowStockProducts, text: "Products low on stock", to: "/admin/products?status=low" },
    { count: data.unverifiedStudents, text: "Students who haven't verified their email", to: "/admin/users?status=unverified" },
  ].filter((item) => item.count > 0);

  return (
    <>
      <PageHead title={`Hello ${getDisplayName()}`} intro="Here's what's happening in the laundry today.">
        <button type="button" className="btn btn-ghost btn-sm" onClick={reload} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </PageHead>

      <div className="adm-stats">
        <StatTile label="Registered students" value={data.totalStudents}
                  note={`${data.activeStudents} active · ${data.admins} admin${data.admins === 1 ? "" : "s"}`} to="/admin/users" />
        <StatTile label="Total bookings" value={data.totalBookings} note={`${data.bookingsToday} for today`} to="/admin/bookings" />
        <StatTile label="Pending bookings" value={data.pendingBookings} tone="warn" note="Waiting for payment"
                  to="/admin/bookings?status=PENDING" />
        <StatTile label="Confirmed bookings" value={data.confirmedBookings} to="/admin/bookings?status=CONFIRMED" />
        <StatTile label="Completed bookings" value={data.completedBookings} tone="ok" to="/admin/bookings?status=COMPLETED" />
        <StatTile label="Total orders" value={data.totalOrders} note={`${data.completedOrders} completed`} to="/admin/orders" />
        <StatTile label="Unpaid orders" value={data.unpaidOrders} tone="warn" to="/admin/orders?status=PENDING" />
        <StatTile label="Payments to verify" value={data.paymentsToVerify} tone={data.paymentsToVerify > 0 ? "bad" : undefined}
                  note={money(data.amountToVerify)} to="/admin/orders?tab=payments" />
        <StatTile label="Machines working" value={`${data.machinesAvailable} / ${data.machinesTotal}`}
                  tone={data.machinesOutOfOrder > 0 ? "warn" : "ok"} to="/admin/facilities?tab=machines" />
        <StatTile label="Low-stock products" value={data.lowStockProducts}
                  note={`${data.outOfStockProducts} sold out`} tone={data.lowStockProducts > 0 ? "warn" : undefined}
                  to="/admin/products?status=low" />
      </div>

      <section className="card" style={{ marginBottom: 16 }}>
        <h2 style={{ marginBottom: 12 }}>Needs your attention</h2>
        {todo.length === 0 ? (
          <p className="muted">Nothing is waiting. Everything is up to date.</p>
        ) : (
          <ul className="adm-todo">
            {todo.map((item) => (
              <li key={item.text}>
                <Link to={item.to}>
                  <span>{item.text}</span>
                  <b>{item.count}</b>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      <div className="adm-two">
        <section className="card">
          <div className="row" style={{ marginBottom: 8 }}>
            <h2>Recent bookings</h2>
            <Link to="/admin/bookings" className="link-btn">See all</Link>
          </div>
          {data.recentBookings.length === 0 ? <p className="muted">No bookings yet.</p> : (
            <div className="table-wrap">
              <table className="table" style={{ minWidth: 420 }}>
                <thead><tr><th>Student</th><th>Wash</th><th>Status</th></tr></thead>
                <tbody>
                  {data.recentBookings.map((b) => (
                    <tr key={b.bookingId}>
                      <td>{b.studentName}<span className="sub">{b.machineNumber} · Room {b.roomNumber}</span></td>
                      <td>{shortDate(b.date)}<span className="sub">{shortTime(b.startTime)}–{shortTime(b.endTime)}</span></td>
                      <td><Status kind="booking" value={b.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="card">
          <div className="row" style={{ marginBottom: 8 }}>
            <h2>Recent orders</h2>
            <Link to="/admin/orders" className="link-btn">See all</Link>
          </div>
          {data.recentOrders.length === 0 ? <p className="muted">No orders yet.</p> : (
            <div className="table-wrap">
              <table className="table" style={{ minWidth: 420 }}>
                <thead><tr><th>Order</th><th className="num">Total</th><th>Status</th></tr></thead>
                <tbody>
                  {data.recentOrders.map((o) => (
                    <tr key={o.orderId}>
                      <td>#{o.orderId} · {o.studentName}<span className="sub">{dateTime(o.orderDate)}</span></td>
                      <td className="num">{money(o.totalAmount)}</td>
                      <td><Status kind="order" value={o.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </>
  );
}

export default AdminDashboard;
