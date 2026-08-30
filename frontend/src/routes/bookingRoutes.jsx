import { Route } from 'react-router-dom';
import HomePage from '../pages/HomePage';
import BookingPage from '../pages/booking/BookingPage';
import BookingConfirmationPage from '../pages/booking/BookingConfirmationPage';
import LaundryPage from '../pages/laundry/LaundryPage';
import ServiceDetailsPage from '../pages/ServiceDetailsPage';


export const bookingRoutes = [
  <Route key="home" path="/" element={<HomePage />} />,
  <Route key="booking" path="/booking" element={<BookingPage />} />,
  <Route key="booking-confirm" path="/booking/confirm" element={<BookingConfirmationPage />} />,
  <Route key="laundry" path="/laundry" element={<LaundryPage />} />,
  <Route key="laundry-details" path="/laundry/:serviceId" element={<ServiceDetailsPage />} />,
];