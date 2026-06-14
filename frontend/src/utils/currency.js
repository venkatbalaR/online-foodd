/** Demo payment failure threshold (matches payment-service rule) */
export const PAYMENT_FAIL_THRESHOLD = 2500;

export function formatRupee(amount) {
  return `₹${Number(amount).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
}
