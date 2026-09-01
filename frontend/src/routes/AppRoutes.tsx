import React from "react";
import { Routes, Route } from "react-router-dom";
import MakeBooking from "../pages/booking/MakeBooking";
import LaundryServices from "../pages/services/LaundryServices";
import ServiceDetails from "../pages/services/ServiceDetails";


const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {}

      <Route path="/bookings/create" element={<MakeBooking />} />
      <Route path="/services" element={<LaundryServices />} />
      <Route path="/services/:id" element={<ServiceDetails />} />

      {}
    </Routes>
  );
};

export default AppRoutes;