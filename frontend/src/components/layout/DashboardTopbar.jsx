import { useState } from "react";
import { Menu, LogOut, ChevronDown } from "lucide-react";
import ThemeToggle from "../common/ThemeToggle";
import { useAuth } from "../../context/AuthContext";
import { initialsFromName, toTitleCase } from "../../utils/formatters";
import { useNavigate } from "react-router-dom";

export default function DashboardTopbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between border-b border-ink-200 dark:border-ink-800 bg-white/90 dark:bg-ink-950/90 backdrop-blur px-4 sm:px-6 py-3.5">
      <button onClick={onMenuClick} className="lg:hidden rounded-lg p-2 text-ink-600 dark:text-ink-300 hover:bg-ink-100 dark:hover:bg-ink-800">
        <Menu className="h-5 w-5" />
      </button>
      <div className="hidden lg:block" />
      <div className="flex items-center gap-3">
        <ThemeToggle />
        <div className="relative">
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 rounded-full pl-1 pr-2.5 py-1 border border-ink-200 dark:border-ink-700 hover:bg-ink-50 dark:hover:bg-ink-800 transition-colors"
          >
            <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gold-500 text-xs font-bold text-ink-950">
              {initialsFromName(user?.name)}
            </span>
            <span className="hidden sm:block text-sm font-medium text-ink-700 dark:text-ink-200">
              {user?.name}
            </span>
            <ChevronDown className="h-3.5 w-3.5 text-ink-400" />
          </button>
          {menuOpen && (
            <div
              className="absolute right-0 mt-2 w-52 rounded-xl border border-ink-200 dark:border-ink-700 bg-white dark:bg-ink-900 shadow-lg py-2 animate-fade-in-up"
              onMouseLeave={() => setMenuOpen(false)}
            >
              <div className="px-3.5 py-2 border-b border-ink-100 dark:border-ink-800">
                <p className="text-sm font-medium text-ink-800 dark:text-ink-100 truncate">{user?.email}</p>
                <p className="text-xs text-harbor-500 font-semibold">{toTitleCase(user?.role)}</p>
              </div>
              <button
                onClick={handleLogout}
                className="flex w-full items-center gap-2 px-3.5 py-2.5 text-sm font-medium text-danger hover:bg-danger/5 transition-colors"
              >
                <LogOut className="h-4 w-4" /> Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
