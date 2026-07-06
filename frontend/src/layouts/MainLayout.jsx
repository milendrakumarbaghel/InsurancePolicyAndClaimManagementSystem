import { Outlet } from "react-router-dom";
import PublicNavbar from "../components/layout/PublicNavbar";
import PublicFooter from "../components/layout/PublicFooter";

export default function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-paper dark:bg-ink-950 transition-colors duration-300">
      <PublicNavbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <PublicFooter />
    </div>
  );
}
