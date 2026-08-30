import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Dashboard from "./pages/dashboard/Dashboard";
import Profile from "./pages/profile/Profile";
import Payment from "./pages/payment/Payment";
import BookingPage from "./pages/booking/BookingPage";
import BookingConfirmationPage from "./pages/booking/BookingConfirmationPage";
import LaundryPage from "./pages/laundry/LaundryPage";
import ServiceDetailsPage from "./pages/laundry/ServiceDetailsPage";
import { BookingProvider } from "./context/BookingContext";

function App() {
  return (
    <BookingProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<Profile/>}/>
          <Route path="/payment" element={<Payment />} />

          <Route path="/booking" element={<BookingPage />} />
          <Route path="/booking/confirm" element={<BookingConfirmationPage />} />
          <Route path="/laundry" element={<LaundryPage />} />
          <Route path="/laundry/:serviceId" element={<ServiceDetailsPage />} />

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </BookingProvider>
  );
}

export default App;