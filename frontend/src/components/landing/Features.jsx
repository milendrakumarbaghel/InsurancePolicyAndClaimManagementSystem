import { ShieldCheck, FileSearch, Wallet, Users, BellRing, Lock } from "lucide-react";

const features = [
  {
    icon: ShieldCheck,
    title: "Plans built for every stage of life",
    description: "Health, motor, life, and travel products with plans your team can price, activate, and retire in a few clicks.",
  },
  {
    icon: FileSearch,
    title: "Claims with a visible paper trail",
    description: "Every review, assignment, and decision is timestamped and attributed — no claim moves without a record.",
  },
  {
    icon: Wallet,
    title: "Premiums and settlements in one place",
    description: "Track every payment against its policy, with transaction references you can hand to a customer on the phone.",
  },
  {
    icon: Users,
    title: "Clear roles, clear responsibility",
    description: "Customers, agents, and administrators each see exactly the tools their role needs — nothing more, nothing hidden.",
  },
  {
    icon: BellRing,
    title: "Status changes you don't have to chase",
    description: "From 'Submitted' to 'Approved', claim status is always one glance away, for the customer and the reviewer alike.",
  },
  {
    icon: Lock,
    title: "Secured by design",
    description: "Token-based sessions, role-gated actions, and OTP-verified accounts keep sensitive policy data where it belongs.",
  },
];

export default function Features() {
  return (
    <section id="features" className="mx-auto max-w-7xl px-5 sm:px-8 py-20 sm:py-28">
      <div className="max-w-2xl">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-harbor-500">What's inside</p>
        <h2 className="mt-3 font-display text-3xl sm:text-4xl font-semibold text-ink-900 dark:text-white">
          Built around the way policies actually move
        </h2>
      </div>

      <div className="mt-12 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {features.map(({ icon: Icon, title, description }, i) => (
          <div
            key={title}
            className="group rounded-2xl border border-ink-200/70 dark:border-ink-800 bg-white dark:bg-ink-900 p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300 dark:hover:border-harbor-700"
            style={{ animationDelay: `${i * 60}ms` }}
          >
            <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-harbor-50 dark:bg-ink-800 text-harbor-600 dark:text-harbor-300 transition-colors group-hover:bg-harbor-600 group-hover:text-white">
              <Icon className="h-5 w-5" />
            </div>
            <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">{title}</h3>
            <p className="mt-2 text-sm leading-relaxed text-ink-500 dark:text-ink-400">{description}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
