import { Link } from "react-router-dom";
import { Shield, Mail, Phone, MapPin } from "lucide-react";

export default function PublicFooter() {
  return (
    <footer id="faq" className="border-t border-ink-200 dark:border-ink-800 bg-ink-50/60 dark:bg-ink-900/40">
      <div className="mx-auto max-w-7xl px-5 sm:px-8 py-12 grid gap-10 sm:grid-cols-2 lg:grid-cols-4">
        <div className="lg:col-span-1">
          <div className="flex items-center gap-2 mb-3">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-harbor-600 text-white">
              <Shield className="h-4 w-4" />
            </span>
            <span className="font-display text-lg font-semibold text-ink-900 dark:text-white">Assurly</span>
          </div>
          <p className="text-sm text-ink-500 dark:text-ink-400 max-w-xs">
            A clearer way to hold your policies, track your claims, and know exactly where you stand.
          </p>
        </div>

        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-3">Product</h4>
          <ul className="space-y-2 text-sm text-ink-600 dark:text-ink-300">
            <li><a href="/#features" className="hover:text-harbor-500 transition-colors">Features</a></li>
            <li><a href="/#about" className="hover:text-harbor-500 transition-colors">About</a></li>
            <li><Link to="/register" className="hover:text-harbor-500 transition-colors">Get covered</Link></li>
            <li><Link to="/login" className="hover:text-harbor-500 transition-colors">Log in</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-3">Coverage</h4>
          <ul className="space-y-2 text-sm text-ink-600 dark:text-ink-300">
            <li>Health</li>
            <li>Motor</li>
            <li>Life</li>
            <li>Travel</li>
          </ul>
        </div>

        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-ink-400 mb-3">Contact</h4>
          <ul className="space-y-2.5 text-sm text-ink-600 dark:text-ink-300">
            <li className="flex items-center gap-2"><Mail className="h-4 w-4 text-harbor-500" /> support@assurly.example</li>
            <li className="flex items-center gap-2"><Phone className="h-4 w-4 text-harbor-500" /> 1800-123-4567</li>
            <li className="flex items-center gap-2"><MapPin className="h-4 w-4 text-harbor-500" /> Indore, India</li>
          </ul>
        </div>
      </div>
      <div className="border-t border-ink-200 dark:border-ink-800 py-5 text-center text-xs text-ink-400">
        © {new Date().getFullYear()} Assurly Insurance Management. All rights reserved.
      </div>
    </footer>
  );
}
