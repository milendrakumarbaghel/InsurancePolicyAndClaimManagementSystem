/**
 * Utility helpers for installment-period duplicate-payment detection.
 *
 * Premium types map to billing windows:
 *   MONTHLY      → 1st–last day of the current month
 *   QUARTERLY    → Jan-Mar, Apr-Jun, Jul-Sep, Oct-Dec
 *   HALF_YEARLY  → Jan-Jun, Jul-Dec
 *   ANNUAL       → Jan 1 – Dec 31
 */

/**
 * Returns { start: Date, end: Date } for the current billing cycle
 * based on the given premiumType string.
 */
export function getCurrentInstallmentPeriod(premiumType) {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth(); // 0-indexed

  let start, end;

  switch (premiumType?.toUpperCase()) {
    case "MONTHLY":
      start = new Date(year, month, 1);
      end = new Date(year, month + 1, 0, 23, 59, 59, 999);
      break;

    case "QUARTERLY": {
      const qStart = Math.floor(month / 3) * 3;
      start = new Date(year, qStart, 1);
      end = new Date(year, qStart + 3, 0, 23, 59, 59, 999);
      break;
    }

    case "HALF_YEARLY": {
      const hStart = month < 6 ? 0 : 6;
      start = new Date(year, hStart, 1);
      end = new Date(year, hStart + 6, 0, 23, 59, 59, 999);
      break;
    }

    case "ANNUAL":
      start = new Date(year, 0, 1);
      end = new Date(year, 11, 31, 23, 59, 59, 999);
      break;

    default:
      return null;
  }

  return { start, end };
}

/**
 * Returns true if `payments` contains at least one SUCCESS payment
 * whose paymentDate falls inside the current installment period.
 */
export function hasPaymentInCurrentPeriod(payments, premiumType) {
  if (!payments?.length || !premiumType) return false;

  const period = getCurrentInstallmentPeriod(premiumType);
  if (!period) return false;

  return payments.some((p) => {
    if (p.status !== "SUCCESS") return false;
    const d = new Date(p.paymentDate);
    return d >= period.start && d <= period.end;
  });
}
