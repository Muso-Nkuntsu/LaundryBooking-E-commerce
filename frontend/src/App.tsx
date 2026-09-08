import React from "react";
import AppRoutes from "./routes/AppRoutes";

const App: React.FC = () => {
  return <AppRoutes />;
};
import MakeBooking from "./pages/booking/MakeBooking";
import BookingConfirmation from "./pages/booking/BookingConfirmation";
import MyBookings from "./pages/booking/MyBookings";

import OrderItems from "./pages/orders/OrderItems";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/*Authentication*/}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/*Main Pages */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/payment" element={<Payment />} />

        {/**Laundry Rooms */}
        <Route path="/laundry-rooms" element={<LaundryRooms />} />
        <Route path="/laundry-rooms/:roomId" element={<LaundryRoomDetails />} />

        {/*Laundry Machines*/}
        <Route path="/laundry-machines" element={<LaundryMachines />} />
        <Route path="/laundry-machines/:machineId" element={<MachineDetails />} />

          {/**Products */}
        <Route path="/products" element={<Products />} />
        <Route path="/products/:productId" element={<ProductDetails />} />

        {/*Booking */}
        <Route path = "/make-booking" element ={<MakeBooking/>}/>
        <Route path="/booking-confirmation" element ={<BookingConfirmation/>}/>
        <Route path="/my-bookings" element ={<MyBookings/>}/>

        {/* Orders */}
        <Route path="/order-items" element ={<OrderItems/>}/>

        {/*Default */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;