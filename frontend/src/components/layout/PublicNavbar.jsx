import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Menu, Shield, X } from "lucide-react";
import ThemeToggle from "../common/ThemeToggle";
import Button from "../common/Button";
import { useAuth } from "../../context/AuthContext";

const navLinks = [
  { to: "/#features", label: "Features" },
  { to: "/#about", label: "About" },
  { to: "/#faq", label: "FAQ" },
];

export default function PublicNavbar() {
  const [open, setOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className={`sticky top-0 z-40 w-full transition-all duration-300 ${
        scrolled
          ? "bg-white/80 dark:bg-ink-950/80 backdrop-blur-md border-b border-ink-200/60 dark:border-ink-800/60 shadow-sm"
          : "bg-transparent"
      }`}
    >
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-5 sm:px-8 py-4">
        <Link to="/" className="flex items-center gap-2 group">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-harbor-600 text-white transition-transform group-hover:rotate-6">
            <Shield className="h-5 w-5" />
          </span>
          <span className="font-display text-xl font-semibold tracking-tight text-ink-900 dark:text-white">
            Assurly
          </span>
        </Link>

        <div className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <a
              key={link.to}
              href={link.to}
              className="text-sm font-medium text-ink-600 dark:text-ink-300 hover:text-harbor-600 dark:hover:text-harbor-300 transition-colors"
            >
              {link.label}
            </a>
          ))}
        </div>

        <div className="hidden md:flex items-center gap-3">
          <ThemeToggle />
          {isAuthenticated ? (
            <Button size="sm" onClick={() => navigate("/dashboard")}>
              Go to dashboard
            </Button>
          ) : (
            <>
              <Button as={Link} to="/login" variant="ghost" size="sm">
                Log in
              </Button>
              <Button as={Link} to="/register" size="sm">
                Get covered
              </Button>
            </>
          )}
        </div>

        <button
          className="md:hidden rounded-lg p-2 text-ink-700 dark:text-ink-200"
          onClick={() => setOpen((o) => !o)}
          aria-label="Toggle menu"
        >
          {open ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </nav>

      {open && (
        <div className="md:hidden border-t border-ink-200 dark:border-ink-800 bg-white dark:bg-ink-950 px-5 py-4 animate-fade-in-up">
          <div className="flex flex-col gap-3">
            {navLinks.map((link) => (
              <a key={link.to} href={link.to} className="py-1.5 text-sm font-medium text-ink-700 dark:text-ink-200" onClick={() => setOpen(false)}>
                {link.label}
              </a>
            ))}
            <div className="flex items-center justify-between pt-2">
              <ThemeToggle />
              <div className="flex gap-2">
                {isAuthenticated ? (
                  <Button size="sm" onClick={() => navigate("/dashboard")}>Dashboard</Button>
                ) : (
                  <>
                    <Button as={Link} to="/login" variant="outline" size="sm">Log in</Button>
                    <Button as={Link} to="/register" size="sm">Get covered</Button>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
