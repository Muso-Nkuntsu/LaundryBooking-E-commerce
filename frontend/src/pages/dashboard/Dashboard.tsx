import { Link } from "react-router-dom";

function Dashboard() {
  return (
    <div>
      <h1>Student Dashboard</h1>

      <section>
        <h2>Welcome, Student!</h2>
        <p>Welcome to the Laundry Booking System.</p>
      </section>

      <section>
        <h2>Upcoming Booking</h2>
        <p>No upcoming bookings.</p>
        <button>Make a Booking</button>
      </section>

      <section>
        <h2>Recent Bookings</h2>
        <p>No recent bookings.</p>
      </section>

      <section>
        <h2>Notifications</h2>
        <p>No new notifications.</p>
      </section>

      <section>
        <h2>Quick Actions</h2>

        <Link to="/profile">
          <button>My Profile</button>
        </Link>

        <Link to="/payment">
          <button>Payment</button>
        </Link>

        <button>View Products</button>

        <button>My Orders</button>
      </section>
    </div>
  );
}

export default Dashboard;