import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Dashboard from "./pages/dashboard/Dashboard";
import Profile from "./pages/profile/Profile";
import Payment from "./pages/payment/Payment";
import LaundryRooms from "./pages/laundry/LaundryRooms";
import LaundryRoomDetails from "./pages/laundry/LaundryRoomDetails";
import LaundryMachines from "./pages/laundry/LaundryMachines";
import MachineDetails from "./pages/laundry/MachineDetails";
import Products from "./pages/products/Products";
import ProductDetails from "./pages/products/ProductDetails";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/payment" element={<Payment />} />
        <Route path="/laundry-rooms" element={<LaundryRooms />} />
        <Route path="/laundry-rooms/:roomId" element={<LaundryRoomDetails />} />
        <Route path="/laundry-machines" element={<LaundryMachines />} />
        <Route path="/laundry-machines/:machineId" element={<MachineDetails />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:productId" element={<ProductDetails />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
