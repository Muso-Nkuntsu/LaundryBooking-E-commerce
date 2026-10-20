import React from "react";
import type { ReactNode } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import RequireLogin from "../components/layout/RequireLogin";
import { ToastProvider } from "../context/ToastProvider";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import Dashboard from "../pages/dashboard/Dashboard";
import Profile from "../pages/profile/Profile";
import Payment from "../pages/payment/Payment";

import LaundryRooms from "../pages/laundry/LaundryRooms";
import LaundryRoomDetails from "../pages/laundry/LaundryRoomDetails";
import LaundryServices from "../pages/laundry/LaundryServices";
import ServiceDetails from "../pages/laundry/ServiceDetails";
import Machines from "../pages/laundry/Machines";
import MachineDetails from "../pages/laundry/MachineDetails";

import Products from "../pages/products/Products";
import ProductDetails from "../pages/products/ProductDetails";

import MakeBooking from "../pages/booking/MakeBooking";
import BookingConfirmation from "../pages/booking/BookingConfirmation";
import MyBookings from "../pages/booking/MyBookings";

import OrderItems from "../pages/orders/OrderItems";
import OrderHistory from "../pages/orders/OrderHistory";
import OrderDetails from "../pages/orders/OrderDetails";
import NotificationsPage from "../pages/notifications/NotificationPage";
import ReviewsPage from "../pages/reviews/ReviewPage";

const withNav = (page: ReactNode) => <RequireLogin>{page}</RequireLogin>;

const AppRoutes: React.FC = () => {
  return (
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/dashboard" element={withNav(<Dashboard />)} />
            <Route path="/profile" element={withNav(<Profile />)} />
            <Route path="/payment" element={withNav(<Payment />)} />
            <Route path="/notifications" element={withNav(<NotificationsPage />)} />
            <Route path="/reviews" element={withNav(<ReviewsPage />)} />

            <Route path="/laundry-rooms" element={withNav(<LaundryRooms />)} />
            <Route path="/laundry-rooms/:roomId" element={withNav(<LaundryRoomDetails />)} />
            <Route path="/laundry-rooms/:roomId/machines" element={withNav(<Machines />)} />
            <Route path="/laundry-rooms/:roomId/machines/:machineId" element={withNav(<MachineDetails />)} />

            <Route path="/laundry" element={withNav(<LaundryServices />)} />
            <Route path="/laundry/:id" element={withNav(<ServiceDetails />)} />

            <Route path="/products" element={withNav(<Products />)} />
            <Route path="/products/:productId" element={withNav(<ProductDetails />)} />

            <Route path="/make-booking" element={withNav(<MakeBooking />)} />
            <Route path="/bookings/create" element={withNav(<MakeBooking />)} />
            <Route path="/booking-confirmation" element={withNav(<BookingConfirmation />)} />
            <Route path="/my-bookings" element={withNav(<MyBookings />)} />

            <Route path="/order-items" element={withNav(<OrderItems />)} />
            <Route path="/orders" element={withNav(<OrderHistory />)} />
            <Route path="/orders/:orderId" element={withNav(<OrderDetails />)} />

            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </BrowserRouter>
      </ToastProvider>
  );
};

export default AppRoutes;