const stages = [
  { n: "01", label: "Submitted", detail: "Customer raises a claim with supporting documents." },
  { n: "02", label: "Assigned", detail: "Admin routes the claim to an insurance operations officer for review." },
  { n: "03", label: "Reviewed", detail: "Insurance operations officer recommends approval or rejection with remarks." },
  { n: "04", label: "Decided", detail: "Admin issues the final approval or rejection." },
];

export default function ClaimLifecycle() {
  return (
    <section className="border-y border-ink-200 dark:border-ink-800 bg-ink-50/60 dark:bg-ink-900/30">
      <div className="mx-auto max-w-7xl px-5 sm:px-8 py-20">
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-harbor-500">The claim lifecycle</p>
        <h2 className="mt-3 font-display text-3xl sm:text-4xl font-semibold text-ink-900 dark:text-white max-w-xl">
          A real sequence, tracked at every step
        </h2>

        <div className="mt-12 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 relative">
          <div className="hidden lg:block absolute top-6 left-[12%] right-[12%] h-px bg-linear-to-r from-harbor-300 via-gold-400 to-harbor-300 dark:from-harbor-700 dark:via-gold-600 dark:to-harbor-700" />
          {stages.map((stage) => (
            <div key={stage.n} className="relative">
              <span className="font-mono-data text-3xl font-semibold text-gold-500">{stage.n}</span>
              <h3 className="mt-3 font-display text-lg font-semibold text-ink-900 dark:text-white">{stage.label}</h3>
              <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400">{stage.detail}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}