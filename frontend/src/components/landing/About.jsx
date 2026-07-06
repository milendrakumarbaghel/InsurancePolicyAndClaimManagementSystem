export default function About() {
  return (
    <section id="about" className="mx-auto max-w-7xl px-5 sm:px-8 py-20 sm:py-28 grid grid-cols-1 lg:grid-cols-2 gap-14 items-center">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[0.18em] text-harbor-500">About Assurly</p>
        <h2 className="mt-3 font-display text-3xl sm:text-4xl font-semibold text-ink-900 dark:text-white leading-tight">
          Insurance operations shouldn't feel like a black box
        </h2>
        <p className="mt-5 text-ink-600 dark:text-ink-300 leading-relaxed">
          Assurly was built for the three people who touch every policy: the customer who
          needs a straight answer, the agent who needs the full case file, and the
          administrator who needs to trust the number on the page. It replaces scattered
          spreadsheets and phone calls with one system that everyone reads from — and writes
          to — the same way.
        </p>
        <p className="mt-4 text-ink-600 dark:text-ink-300 leading-relaxed">
          Nothing changes hands without a record: who assigned the claim, who reviewed it,
          and who signed off. That trail is the product.
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="rounded-2xl bg-harbor-600 text-white p-6 flex flex-col justify-between h-44">
          <p className="font-display text-3xl font-semibold">3</p>
          <p className="text-sm text-harbor-100">Roles working from one shared record</p>
        </div>
        <div className="rounded-2xl bg-gold-500 text-ink-950 p-6 flex flex-col justify-between h-44 mt-8">
          <p className="font-display text-3xl font-semibold">7</p>
          <p className="text-sm text-ink-900/80">Tracked claim stages, start to settlement</p>
        </div>
        <div className="rounded-2xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 p-6 flex flex-col justify-between h-44">
          <p className="font-display text-3xl font-semibold text-ink-900 dark:text-white">4</p>
          <p className="text-sm text-ink-500 dark:text-ink-400">Insurance product lines supported</p>
        </div>
        <div className="rounded-2xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 p-6 flex flex-col justify-between h-44 mt-8">
          <p className="font-display text-3xl font-semibold text-ink-900 dark:text-white">JWT</p>
          <p className="text-sm text-ink-500 dark:text-ink-400">Secured, role-based session access</p>
        </div>
      </div>
    </section>
  );
}
