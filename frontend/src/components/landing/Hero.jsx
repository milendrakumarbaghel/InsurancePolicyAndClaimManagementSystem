import { Link } from "react-router-dom";
import { ArrowRight, ShieldCheck } from "lucide-react";
import Button from "../common/Button";

export default function Hero() {
  return (
    <section className="relative overflow-hidden">
      <div className="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_15%_10%,theme(colors.harbor.100),transparent_45%)] dark:bg-[radial-gradient(circle_at_15%_10%,theme(colors.harbor.900),transparent_45%)]" />

      <div className="mx-auto grid max-w-7xl grid-cols-1 items-center gap-14 px-5 sm:px-8 py-16 sm:py-24 lg:grid-cols-2">
        <div className="animate-fade-in-up">
          <span className="inline-flex items-center gap-2 rounded-full border border-harbor-200 dark:border-harbor-700 bg-harbor-50 dark:bg-ink-800 px-3.5 py-1.5 text-xs font-semibold uppercase tracking-wider text-harbor-700 dark:text-harbor-300">
            <ShieldCheck className="h-3.5 w-3.5" /> Policy & claim management, unified
          </span>

          <h1 className="mt-6 font-display text-4xl sm:text-5xl lg:text-6xl font-semibold leading-[1.08] tracking-tight text-ink-900 dark:text-white">
            Every policy.
            <br />
            Every claim.
            <br />
            <span className="text-harbor-600 dark:text-harbor-400">One clear ledger.</span>
          </h1>

          <p className="mt-6 max-w-lg text-base sm:text-lg text-ink-600 dark:text-ink-300 leading-relaxed">
            Assurly gives customers, agents, and administrators a single, honest record of
            coverage — from the moment a plan is purchased to the day a claim is settled.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row gap-3">
            <Button as={Link} to="/register" size="lg" icon={ArrowRight} className="flex-row-reverse">
              Get covered today
            </Button>
            <Button as={Link} to="/login" variant="outline" size="lg">
              Sign in to your account
            </Button>
          </div>

          <div className="mt-10 flex items-center gap-6 text-sm text-ink-500 dark:text-ink-400">
            <div>
              <p className="font-display text-2xl font-semibold text-ink-900 dark:text-white">4</p>
              <p>Product lines</p>
            </div>
            <div className="h-8 w-px bg-ink-200 dark:bg-ink-700" />
            <div>
              <p className="font-display text-2xl font-semibold text-ink-900 dark:text-white">24/7</p>
              <p>Claim tracking</p>
            </div>
            <div className="h-8 w-px bg-ink-200 dark:bg-ink-700" />
            <div>
              <p className="font-display text-2xl font-semibold text-ink-900 dark:text-white">100%</p>
              <p>Audit trail</p>
            </div>
          </div>
        </div>

        <div className="relative h-[420px] sm:h-[480px] hidden sm:block">
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="relative w-72">
              <div className="absolute -left-10 top-16 w-64 rounded-2xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-800 shadow-xl p-5 animate-float-slow" style={{ animationDelay: "0.4s" }}>
                <p className="text-[0.65rem] font-mono-data uppercase tracking-wider text-ink-400">Policy No.</p>
                <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">POL-2026-0417</p>
                <div className="mt-4 h-px bg-ink-100 dark:bg-ink-700" />
                <p className="mt-4 text-xs text-ink-400">Health Shield Plus</p>
                <span className="stamp mt-2 text-[0.6rem] text-success border-success">Active</span>
              </div>

              <div className="absolute -right-8 top-2 w-64 rounded-2xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-800 shadow-xl p-5 animate-float-slow" style={{ animationDelay: "0s" }}>
                <p className="text-[0.65rem] font-mono-data uppercase tracking-wider text-ink-400">Claim No.</p>
                <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">CLM-2026-1182</p>
                <div className="mt-4 h-px bg-ink-100 dark:bg-ink-700" />
                <p className="mt-4 text-xs text-ink-400">₹45,000 requested</p>
                <span className="stamp mt-2 text-[0.6rem] text-gold-600 border-gold-500">Under Review</span>
              </div>

              <div className="relative z-10 w-72 rounded-2xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-800 shadow-2xl p-6 mt-40">
                <p className="text-[0.65rem] font-mono-data uppercase tracking-wider text-ink-400">Claim No.</p>
                <p className="font-mono-data text-base font-semibold text-ink-900 dark:text-white">CLM-2026-0965</p>
                <div className="mt-4 h-px bg-ink-100 dark:bg-ink-700" />
                <div className="mt-4 flex items-center justify-between">
                  <p className="text-xs text-ink-400">Settlement</p>
                  <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">₹1,20,000</p>
                </div>
                <span className="stamp mt-4 text-success border-success">Approved</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
