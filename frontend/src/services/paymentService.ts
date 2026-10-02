import { apiPost } from "./Api";

export interface PaymentRecord {
  paymentId: number;
  amount: number;
  paymentMethod: string;
  status: string;
  bookingId?: number | null;
  orderId?: number | null;
}

/**
 * Records a payment for a booking. It starts as PENDING on the backend.
 * No card details are sent: there is no payment gateway connected yet.
 */
export const createBookingPayment = (bookingId: number, amount: number, paymentMethod: string): Promise<PaymentRecord> =>
  apiPost<PaymentRecord>("/payment/create", undefined, { amount, paymentMethod, bookingId });
