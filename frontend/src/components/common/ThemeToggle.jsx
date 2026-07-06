import { Moon, Sun } from "lucide-react";
import { useTheme } from "../../context/ThemeContext";

export default function ThemeToggle({ className = "" }) {
  const { isDark, toggleTheme } = useTheme();
  return (
    <button
      onClick={toggleTheme}
      aria-label="Toggle color theme"
      className={`relative inline-flex h-9 w-9 items-center justify-center rounded-full border border-ink-200 dark:border-ink-700
        bg-white dark:bg-ink-800 text-ink-600 dark:text-gold-300 hover:border-harbor-400 dark:hover:border-harbor-400
        transition-all duration-300 hover:rotate-12 ${className}`}
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  );
}
