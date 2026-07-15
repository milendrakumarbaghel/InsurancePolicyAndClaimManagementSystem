import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import Button from "../common/Button";

export default function CTA() {
  return (
    <section className="mx-auto max-w-7xl px-5 sm:px-8 pb-20 sm:pb-28">
      <div className="relative overflow-hidden rounded-3xl bg-ink-950 px-8 sm:px-16 py-16 text-center">
        <div className="absolute inset-0 opacity-30 bg-[radial-gradient(circle_at_80%_0%,var(--color-harbor-700),transparent_50%)]" />
        <div className="relative z-10">
          <h2 className="font-display text-3xl sm:text-4xl font-semibold text-white max-w-xl mx-auto">
            Bring your policies into one honest ledger
          </h2>
          <p className="mt-4 text-harbor-100/70 max-w-md mx-auto">
            Create your account in minutes and see exactly where every plan and claim stands.
          </p>
          <div className="mt-8 flex flex-col sm:flex-row justify-center gap-3">
            <Button as={Link} to="/register" size="lg" variant="gold" icon={ArrowRight} className="flex-row-reverse">
              Create your account
            </Button>
            <Button as={Link} to="/login" size="lg" variant="outline" className="border-white/30 text-white hover:bg-white/10">
              I already have one
            </Button>
          </div>
        </div>
      </div>
    </section>
  );
}
