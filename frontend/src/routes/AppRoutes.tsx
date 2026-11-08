import React from "react";
import type { ReactNode } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import RequireLogin from "../components/layout/RequireLogin";
import RequireAdmin from "../components/layout/RequireAdmin";
import { ToastProvider } from "../context/ToastProvider";
import { CartProvider } from "../context/CartProvider";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import VerifyEmail from "../pages/auth/VerifyEmail";
import ForgotPassword from "../pages/auth/ForgotPassword";
import ResetPassword from "../pages/auth/ResetPassword";
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
import BookingSummary from "../pages/booking/BookingSummary";
import BookingConfirmation from "../pages/booking/BookingConfirmation";
import MyBookings from "../pages/booking/MyBookings";

import OrderItems from "../pages/orders/OrderItems";
import Cart from "../pages/cart/Cart";
import NotificationsPage from "../pages/notifications/NotificationPage";
import ReviewsPage from "../pages/reviews/ReviewPage";

import AdminDashboard from "../pages/admin/AdminDashboard";
import AdminUsers from "../pages/admin/AdminUsers";
import AdminBookings from "../pages/admin/AdminBookings";
import AdminFacilities from "../pages/admin/AdminFacilities";
import AdminProducts from "../pages/admin/AdminProducts";
import AdminOrders from "../pages/admin/AdminOrders";
import AdminReports from "../pages/admin/AdminReports";

// Every page except login and register needs a logged-in student.
const withNav = (page: ReactNode) => <RequireLogin>{page}</RequireLogin>;
// Admin pages: only for accounts with the ADMIN role.
const asAdmin = (page: ReactNode) => <RequireAdmin>{page}</RequireAdmin>;

const AppRoutes: React.FC = () => {
  return (
    <ToastProvider>
      <CartProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Main pages */}
          <Route path="/dashboard" element={withNav(<Dashboard />)} />
          <Route path="/profile" element={withNav(<Profile />)} />
          <Route path="/payment" element={withNav(<Payment />)} />
          <Route path="/notifications" element={withNav(<NotificationsPage />)} />
          <Route path="/reviews" element={withNav(<ReviewsPage />)} />

          {/* Laundry rooms */}
          <Route path="/laundry-rooms" element={withNav(<LaundryRooms />)} />
          <Route path="/laundry-rooms/:roomId" element={withNav(<LaundryRoomDetails />)} />

          {/* Laundry services */}
          <Route path="/laundry" element={withNav(<LaundryServices />)} />
          <Route path="/laundry/:id" element={withNav(<ServiceDetails />)} />

          {/* Products */}
          <Route path="/products" element={withNav(<Products />)} />
          <Route path="/products/:productId" element={withNav(<ProductDetails />)} />
          <Route path="/cart" element={withNav(<Cart />)} />

          {/* Booking */}
          <Route path="/make-booking" element={withNav(<MakeBooking />)} />
          <Route path="/bookings/create" element={withNav(<MakeBooking />)} />
          <Route path="/booking-summary" element={withNav(<BookingSummary />)} />
          <Route path="/booking-confirmation" element={withNav(<BookingConfirmation />)} />
          <Route path="/my-bookings" element={withNav(<MyBookings />)} />

          {/* Orders */}
          <Route path="/order-items" element={withNav(<OrderItems />)} />

          {/* Admin dashboard */}
          <Route path="/admin" element={asAdmin(<AdminDashboard />)} />
          <Route path="/admin/users" element={asAdmin(<AdminUsers />)} />
          <Route path="/admin/bookings" element={asAdmin(<AdminBookings />)} />
          <Route path="/admin/facilities" element={asAdmin(<AdminFacilities />)} />
          <Route path="/admin/products" element={asAdmin(<AdminProducts />)} />
          <Route path="/admin/orders" element={asAdmin(<AdminOrders />)} />
          <Route path="/admin/reports" element={asAdmin(<AdminReports />)} />

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
      </CartProvider>
    </ToastProvider>
  );
};

export default AppRoutes;
