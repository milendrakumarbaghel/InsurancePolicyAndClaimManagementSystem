import { Link, Outlet } from "react-router-dom";
import { Shield } from "lucide-react";
import ThemeToggle from "../components/common/ThemeToggle";

export default function AuthLayout() {
  return (
    <div className="grid min-h-screen grid-cols-1 lg:grid-cols-2 bg-paper dark:bg-ink-950 transition-colors">
      <div className="flex flex-col justify-between px-6 sm:px-10 py-8">
        <div className="flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-harbor-600 text-white">
              <Shield className="h-5 w-5" />
            </span>
            <span className="font-display text-xl font-semibold text-ink-900 dark:text-white">Assurly</span>
          </Link>
          <ThemeToggle />
        </div>
        <div className="mx-auto w-full max-w-md py-10">
          <Outlet />
        </div>
        <p className="text-center text-xs text-ink-400">
          © {new Date().getFullYear()} Assurly Insurance Management
        </p>
      </div>
      <div className="relative hidden lg:flex items-center justify-center overflow-hidden bg-harbor-900">
        <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_30%_20%,white,transparent_45%)]" />
        <div className="relative z-10 max-w-md px-10 text-center">
          <div className="mx-auto mb-8 flex h-20 w-20 items-center justify-center rounded-2xl bg-gold-500/90 rotate-3 shadow-xl animate-float-slow">
            <Shield className="h-10 w-10 text-ink-950" />
          </div>
          <h2 className="font-display text-3xl font-semibold text-white leading-tight">
            One ledger for every policy, claim, and payout.
          </h2>
          <p className="mt-4 text-harbor-100/80 text-sm leading-relaxed">
            Assurly keeps customers, agents, and administrators aligned — from the first premium
            to the final settlement — with a clear, auditable trail at every step.
          </p>
        </div>
      </div>
    </div>
  );
}
