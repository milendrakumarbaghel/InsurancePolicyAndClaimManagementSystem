export const ROLES = {
  ADMIN: "ADMIN",
  AGENT: "AGENT",
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
  SUBMITTED: "text-info border-info",
  ASSIGNED: "text-harbor-500 border-harbor-500",
  UNDER_REVIEW: "text-gold-500 border-gold-500",
  RECOMMENDED_APPROVAL: "text-success border-success",
  RECOMMENDED_REJECTION: "text-danger border-danger",
  APPROVED: "text-success border-success",
  REJECTED: "text-danger border-danger",
  // Policies
  PENDING_PAYMENT: "text-gold-500 border-gold-500",
  ACTIVE: "text-success border-success",
  EXPIRED: "text-ink-400 border-ink-400",
  CANCELLED: "text-danger border-danger",
  // Payments
  SUCCESS: "text-success border-success",
  FAILED: "text-danger border-danger",
  PENDING: "text-gold-500 border-gold-500",
  // Users / products / plans
  true: "text-success border-success",
  false: "text-danger border-danger",
};

export const DOCUMENT_TYPE_SUGGESTIONS = [
  "Medical Bill",
  "Police Report",
  "Repair Estimate",
  "Death Certificate",
  "Identity Proof",
  "Photograph",
  "Invoice",
  "Other",
];
