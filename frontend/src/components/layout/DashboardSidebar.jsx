import { NavLink } from "react-router-dom";
import {
  LayoutDashboard, User, ShieldCheck, FileStack, Wallet, FileWarning,
  Users, UserCog, Boxes, ClipboardList, ListChecks, Shield, X,
} from "lucide-react";
import { ROLES } from "../../utils/constants";

const navByRole = {
  [ROLES.CUSTOMER]: [
    { to: "/dashboard", label: "Overview", icon: LayoutDashboard, end: true },
    { to: "/dashboard/profile", label: "My Profile", icon: User },
    { to: "/dashboard/products", label: "Browse Products", icon: Boxes },
    { to: "/dashboard/policies", label: "My Policies", icon: ShieldCheck },
    { to: "/dashboard/payments", label: "Payments", icon: Wallet },
    { to: "/dashboard/claims", label: "My Claims", icon: FileWarning },
  ],
  [ROLES.INSURANCE_OPERATIONS_OFFICER]: [
    { to: "/dashboard", label: "Overview", icon: LayoutDashboard, end: true },
    { to: "/dashboard/assigned-claims", label: "Assigned Claims", icon: ClipboardList },
    { to: "/dashboard/customers", label: "Customers", icon: Users },
    { to: "/dashboard/products", label: "Products", icon: Boxes },
    { to: "/dashboard/plans", label: "Plans", icon: FileStack },
    { to: "/dashboard/policies", label: "Policies", icon: ShieldCheck },
    { to: "/dashboard/payments", label: "Payments", icon: Wallet },
  ],
  [ROLES.ADMIN]: [
    { to: "/dashboard", label: "Overview", icon: LayoutDashboard, end: true },
    { to: "/dashboard/users", label: "All Users", icon: UserCog },
    { to: "/dashboard/insurance-operations-officers", label: "Insurance Operations Officers", icon: Users },
    { to: "/dashboard/customers", label: "Customers", icon: Users },
    { to: "/dashboard/products", label: "Products", icon: Boxes },
    { to: "/dashboard/plans", label: "Plans", icon: FileStack },
    { to: "/dashboard/policies", label: "Policies", icon: ShieldCheck },
    { to: "/dashboard/claims", label: "Claims", icon: ListChecks },
    { to: "/dashboard/payments", label: "Payments", icon: Wallet },
  ],
};

export default function DashboardSidebar({ role, open, onClose }) {
  const items = navByRole[role] || [];

  return (
    <>
      {open && (
        <div className="fixed inset-0 z-30 bg-ink-950/50 lg:hidden" onClick={onClose} />
      )}

      <aside
        className={`fixed z-40 inset-y-0 left-0 w-64 shrink-0 border-r border-ink-200 dark:border-ink-800
          bg-white dark:bg-ink-950 transform transition-transform duration-300 lg:static lg:translate-x-0
          ${open ? "translate-x-0" : "-translate-x-full"}`}
      >
        <div className="flex items-center justify-between px-5 py-5 border-b border-ink-100 dark:border-ink-800">
          <div className="flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-harbor-600 text-white">
              <Shield className="h-4 w-4" />
            </span>
            <span className="font-display text-lg font-semibold text-ink-900 dark:text-white">Assurly</span>
          </div>
          <button onClick={onClose} className="lg:hidden rounded-lg p-1.5 text-ink-400 hover:bg-ink-100 dark:hover:bg-ink-800">
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="flex flex-col gap-1 px-3 py-4">
          {items.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-harbor-600 text-white shadow-sm"
                    : "text-ink-600 dark:text-ink-300 hover:bg-harbor-50 dark:hover:bg-ink-800 hover:text-harbor-700 dark:hover:text-harbor-300"
                }`
              }
            >
              <Icon className="h-4 w-4 flex-shrink-0" />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}