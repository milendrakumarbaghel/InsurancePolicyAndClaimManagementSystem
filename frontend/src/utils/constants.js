export const ROLES = {
  ADMIN: "ADMIN",
  INSURANCE_OPERATIONS_OFFICER: "INSURANCE_OPERATIONS_OFFICER",
  CUSTOMER: "CUSTOMER",
};

export const PRODUCT_TYPES = ["HEALTH", "MOTOR", "LIFE", "TRAVEL"];

export const PREMIUM_TYPES = ["MONTHLY", "QUARTERLY", "HALF_YEARLY", "ANNUAL"];

export const PAYMENT_MODES = ["UPI", "CARD", "NET_BANKING", "CASH"];

export const POLICY_STATUSES = ["PENDING_PAYMENT", "ACTIVE", "EXPIRED", "CANCELLED"];

export const CLAIM_STATUSES = [
  "SUBMITTED",
  "ASSIGNED",
  "UNDER_REVIEW",
  "RECOMMENDED_APPROVAL",
  "RECOMMENDED_REJECTION",
  "APPROVED",
  "REJECTED",
];

// Tailwind color classes per status — used by the Stamp/Badge component
export const STATUS_STYLES = {
  // Claims
  SUBMITTED: "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-950/30 dark:text-blue-300 dark:border-blue-900/50",
  ASSIGNED: "bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/30 dark:text-purple-300 dark:border-purple-900/50",
  UNDER_REVIEW: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/30 dark:text-amber-300 dark:border-amber-900/50",
  RECOMMENDED_APPROVAL: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-900/50",
  RECOMMENDED_REJECTION: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-300 dark:border-rose-900/50",
  APPROVED: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-900/50",
  REJECTED: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-300 dark:border-rose-900/50",
  // Policies
  PENDING_PAYMENT: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/30 dark:text-amber-300 dark:border-amber-900/50",
  ACTIVE: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-900/50",
  EXPIRED: "bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800/40 dark:text-slate-300 dark:border-slate-700/50",
  CANCELLED: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-300 dark:border-rose-900/50",
  // Payments
  SUCCESS: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-900/50",
  FAILED: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-300 dark:border-rose-900/50",
  PENDING: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/30 dark:text-amber-300 dark:border-amber-900/50",
  // Users / products / plans
  true: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-900/50",
  false: "bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-300 dark:border-rose-900/50",
};

export const DOCUMENT_TYPE_SUGGESTIONS = [
  { value: "Medical Bill", label: "Medical Bill" },
  { value: "Police Report", label: "Police Report" },
  { value: "Repair Estimate", label: "Repair Estimate" },
  { value: "Death Certificate", label: "Death Certificate" },
  { value: "Identity Proof", label: "Identity Proof" },
  { value: "Photograph", label: "Photograph" },
  { value: "Invoice", label: "Invoice" },
  { value: "Other", label: "Other" },
];

export const ALLOWED_RELATIONS = [
  { value: "FATHER", label: "Father" },
  { value: "MOTHER", label: "Mother" },
  { value: "SPOUSE", label: "Spouse" },
  { value: "SON", label: "Son" },
  { value: "DAUGHTER", label: "Daughter" },
  { value: "BROTHER", label: "Brother" },
  { value: "SISTER", label: "Sister" },
  { value: "OTHER", label: "Other" }
];