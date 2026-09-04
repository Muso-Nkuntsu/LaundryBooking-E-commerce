import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import Dashboard from "../pages/dashboard/Dashboard";
import Profile from "../pages/profile/Profile";
import Payment from "../pages/payment/Payment";

import LaundryRooms from "../pages/laundry/LaundryRooms";
import LaundryRoomDetails from "../pages/laundry/LaundryRoomDetails";
import LaundryServices from "../pages/laundry/LaundryServices";
import ServiceDetails from "../pages/laundry/ServiceDetails";

import Products from "../pages/products/Products";
import ProductDetails from "../pages/products/ProductDetails";

import MakeBooking from "../pages/booking/MakeBooking";

const AppRoutes: React.FC = () => {
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

        <Route path="/laundry" element={<LaundryServices />} />
        <Route path="/laundry/:id" element={<ServiceDetails />} />

        <Route path="/bookings/create" element={<MakeBooking />} />

        <Route path="/products" element={<Products />} />
        <Route path="/products/:productId" element={<ProductDetails />} />

        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;