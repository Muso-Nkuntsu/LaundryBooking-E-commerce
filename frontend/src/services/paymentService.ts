import { apiGet, apiPost } from "./Api";

export type PaymentMethod = "CARD" | "EFT";

export interface PaymentRecord {
  paymentId: number;
  amount: number;
  paymentMethod: string;
  /** PENDING (EFT waiting to be checked), COMPLETED or FAILED. */
  status: string;
  transactionRef?: string | null;
  payerName?: string | null;
  payerBank?: string | null;
  bookingId?: number | null;
  orderId?: number | null;
}

/** What is being paid for: a booking or a shop order. */
export interface PaymentTarget {
  kind: "booking" | "order";
  id: number;
  amount: number;
  description: string;
}

export interface EftDetails {
  reference: string;
  payerName: string;
  payerBank: string;
}

/** The reference a student must use for an EFT, so the payment can be matched on the bank statement. */
export const eftReference = (target: PaymentTarget): string =>
  `SPIN-${target.kind === "booking" ? "B" : "O"}${target.id}`;

/**
 * Records a payment. The backend charges the booking's or order's own total and decides the status:
 * - CARD: no gateway is connected, so it is treated as approved (simulated) and confirms the booking / order.
 * - EFT: saved as PENDING until someone sees the money in the bank account.
 * Card details are never sent.
 */
export const createPayment = (target: PaymentTarget, method: PaymentMethod, eft?: EftDetails): Promise<PaymentRecord> =>
  apiPost<PaymentRecord>("/payment/create", undefined, {
    paymentMethod: method,
    bookingId: target.kind === "booking" ? target.id : undefined,
    orderId: target.kind === "order" ? target.id : undefined,
    transactionRef: eft?.reference,
    payerName: eft?.payerName,
    payerBank: eft?.payerBank,
  });

export const getPaymentsFor = (kind: PaymentTarget["kind"], id: number): Promise<PaymentRecord[]> =>
  apiGet<PaymentRecord[]>(kind === "booking" ? `/payment/booking/${id}` : `/payment/order/${id}`);

/** True when an EFT has been submitted but the money has not been confirmed yet. */
export const isAwaitingVerification = (payments: PaymentRecord[]): boolean =>
  payments.some((payment) => payment.status?.toUpperCase() === "PENDING");
