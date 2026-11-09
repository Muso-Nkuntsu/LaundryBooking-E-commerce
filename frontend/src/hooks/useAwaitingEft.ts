import { useFetch } from "./useFetch";
import { getPaymentsFor, isAwaitingVerification } from "../services/paymentService";
import type { PaymentTarget } from "../services/paymentService";

/**
 * For unpaid bookings or orders, finds the ones whose EFT has been submitted but not yet confirmed,
 * so the page can show "EFT being verified" instead of a second "Pay now" button.
 */
export function useAwaitingEft(kind: PaymentTarget["kind"], ids: number[]): Set<number> {
  const key = `${kind}:${ids.join(",")}`;
  const { data } = useFetch(async () => {
    const results = await Promise.all(
      ids.map(async (id) => {
        try {
          return isAwaitingVerification(await getPaymentsFor(kind, id)) ? id : null;
        } catch {
          return null; // If this can't be checked, just offer "Pay now"; the backend still blocks a double payment.
        }
      }),
    );
    return new Set(results.filter((id): id is number => id !== null));
  }, key);
  return data ?? new Set<number>();
}
