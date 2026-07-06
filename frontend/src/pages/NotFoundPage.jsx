import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import Button from "../components/common/Button";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-paper dark:bg-ink-950 px-6 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-harbor-50 dark:bg-ink-800 text-harbor-600 rotate-3">
        <Compass className="h-8 w-8" />
      </div>
      <p className="font-mono-data text-sm font-semibold uppercase tracking-widest text-gold-500">Error 404</p>
      <h1 className="font-display text-3xl font-semibold text-ink-900 dark:text-white">This page isn't on file</h1>
      <p className="max-w-sm text-sm text-ink-500 dark:text-ink-400">
        The page you're looking for doesn't exist or may have moved.
      </p>
      <Button as={Link} to="/" className="mt-2">
        Back to home
      </Button>
    </div>
  );
}
