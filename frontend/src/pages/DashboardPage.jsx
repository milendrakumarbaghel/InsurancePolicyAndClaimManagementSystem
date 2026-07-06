import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  ShieldCheck, FileWarning, Wallet, ArrowRight, Boxes, ClipboardList, Users, UserCog, FileStack,
} from "lucide-react";
import Card from "../components/common/Card";
import Spinner from "../components/common/Spinner";
import Stamp from "../components/common/Stamp";
import Button from "../components/common/Button";
import PageHeader from "../components/common/PageHeader";
import { useAuth } from "../context/AuthContext";
import { policyService } from "../services/policyService";
import { claimService } from "../services/claimService";
import { userService } from "../services/userService";
import { formatCurrency, formatDate } from "../utils/formatters";
import { ROLES } from "../utils/constants";

const ACCENT_CLASSES = {
  harbor: "bg-harbor-50 dark:bg-ink-800 text-harbor-600",
  gold: "bg-gold-50 dark:bg-ink-800 text-gold-600",
  success: "bg-success/10 dark:bg-ink-800 text-success",
};

function StatCard({ icon: Icon, label, value, accent = "harbor" }) {
  return (
    <Card className="flex items-center gap-4">
      <div className={`flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl ${ACCENT_CLASSES[accent]}`}>
        <Icon className="h-6 w-6" />
      </div>
      <div>
        <p className="text-2xl font-display font-semibold text-ink-900 dark:text-white">{value}</p>
        <p className="text-sm text-ink-500 dark:text-ink-400">{label}</p>
      </div>
    </Card>
  );
}

function CustomerOverview() {
  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([policyService.getMy(), claimService.getMy()])
      .then(([p, c]) => {
        setPolicies(p);
        setClaims(c);
      })
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <Spinner label="Loading your overview…" />;

  const activePolicies = policies.filter((p) => p.status === "ACTIVE").length;
  const openClaims = claims.filter((c) => !["APPROVED", "REJECTED"].includes(c.claimStatus)).length;
  const totalPaid = policies.reduce((sum, p) => sum + (p.totalPremiumPaid || 0), 0);

  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard icon={ShieldCheck} label="Active policies" value={activePolicies} />
        <StatCard icon={FileWarning} label="Open claims" value={openClaims} accent="gold" />
        <StatCard icon={Wallet} label="Total premium paid" value={formatCurrency(totalPaid)} accent="success" />
      </div>

      <div className="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">Recent policies</h3>
            <Link to="/dashboard/policies" className="text-sm font-medium text-harbor-600 dark:text-harbor-400 hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
          {policies.length === 0 ? (
            <p className="text-sm text-ink-500">You don't have any policies yet.</p>
          ) : (
            <div className="space-y-3">
              {policies.slice(0, 4).map((p) => (
                <Link
                  key={p.policyId}
                  to={`/dashboard/policies/${p.policyId}`}
                  className="flex items-center justify-between rounded-xl border border-ink-100 dark:border-ink-800 px-4 py-3 hover:border-harbor-300 dark:hover:border-harbor-700 transition-colors"
                >
                  <div>
                    <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">{p.policyNumber}</p>
                    <p className="text-xs text-ink-500">{p.planName}</p>
                  </div>
                  <Stamp status={p.status} />
                </Link>
              ))}
            </div>
          )}
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">Recent claims</h3>
            <Link to="/dashboard/claims" className="text-sm font-medium text-harbor-600 dark:text-harbor-400 hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
          {claims.length === 0 ? (
            <p className="text-sm text-ink-500">No claims raised yet.</p>
          ) : (
            <div className="space-y-3">
              {claims.slice(0, 4).map((c) => (
                <Link
                  key={c.claimId}
                  to={`/dashboard/claims/${c.claimId}`}
                  className="flex items-center justify-between rounded-xl border border-ink-100 dark:border-ink-800 px-4 py-3 hover:border-harbor-300 dark:hover:border-harbor-700 transition-colors"
                >
                  <div>
                    <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">{c.claimNumber}</p>
                    <p className="text-xs text-ink-500">{formatCurrency(c.claimAmount)}</p>
                  </div>
                  <Stamp status={c.claimStatus} />
                </Link>
              ))}
            </div>
          )}
        </Card>
      </div>

      <div className="mt-6 flex flex-wrap gap-3">
        <Button as={Link} to="/dashboard/products">Browse plans</Button>
        <Button as={Link} to="/dashboard/claims/new" variant="outline">Raise a claim</Button>
      </div>
    </>
  );
}

function AgentOverview() {
  const [assigned, setAssigned] = useState({ content: [], totalElements: 0 });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    claimService
      .getAssigned({ page: 0, size: 5 })
      .then(setAssigned)
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <Spinner label="Loading your queue…" />;

  const pendingReview = assigned.content.filter((c) => c.claimStatus === "ASSIGNED" || c.claimStatus === "UNDER_REVIEW").length;

  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard icon={ClipboardList} label="Assigned to you" value={assigned.totalElements} />
        <StatCard icon={FileWarning} label="Awaiting your review" value={pendingReview} accent="gold" />
        <StatCard icon={Users} label="Role" value="Agent" accent="success" />
      </div>

      <Card className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-display text-lg font-semibold text-ink-900 dark:text-white">Recently assigned claims</h3>
          <Link to="/dashboard/assigned-claims" className="text-sm font-medium text-harbor-600 dark:text-harbor-400 hover:underline flex items-center gap-1">
            View all <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>
        {assigned.content.length === 0 ? (
          <p className="text-sm text-ink-500">No claims assigned to you yet.</p>
        ) : (
          <div className="space-y-3">
            {assigned.content.map((c) => (
              <Link
                key={c.claimId}
                to={`/dashboard/claims/${c.claimId}`}
                className="flex items-center justify-between rounded-xl border border-ink-100 dark:border-ink-800 px-4 py-3 hover:border-harbor-300 dark:hover:border-harbor-700 transition-colors"
              >
                <div>
                  <p className="font-mono-data text-sm font-semibold text-ink-800 dark:text-ink-100">{c.claimNumber}</p>
                  <p className="text-xs text-ink-500">{c.customerName} — {formatCurrency(c.claimAmount)}</p>
                </div>
                <Stamp status={c.claimStatus} />
              </Link>
            ))}
          </div>
        )}
      </Card>
    </>
  );
}

function AdminOverview() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      userService.getAll({ page: 0, size: 1 }),
      userService.getCustomers(),
      userService.getAgents(),
    ])
      .then(([users, customers, agents]) => {
        setStats({
          totalUsers: users.totalElements ?? 0,
          totalCustomers: customers?.length ?? 0,
          totalAgents: agents?.length ?? 0,
        });
      })
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <Spinner label="Loading system overview…" />;

  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard icon={UserCog} label="Total users" value={stats.totalUsers} />
        <StatCard icon={Users} label="Customers" value={stats.totalCustomers} accent="gold" />
        <StatCard icon={Users} label="Agents" value={stats.totalAgents} accent="success" />
      </div>

      <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <Link to="/dashboard/products" className="group">
          <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300">
            <Boxes className="h-6 w-6 text-harbor-600 mb-3" />
            <p className="font-display font-semibold text-ink-900 dark:text-white">Manage Products</p>
            <p className="text-xs text-ink-500 mt-1">Create and activate insurance products</p>
          </Card>
        </Link>
        <Link to="/dashboard/plans" className="group">
          <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300">
            <FileStack className="h-6 w-6 text-harbor-600 mb-3" />
            <p className="font-display font-semibold text-ink-900 dark:text-white">Manage Plans</p>
            <p className="text-xs text-ink-500 mt-1">Set coverage, premiums, and terms</p>
          </Card>
        </Link>
        <Link to="/dashboard/policies" className="group">
          <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300">
            <ShieldCheck className="h-6 w-6 text-harbor-600 mb-3" />
            <p className="font-display font-semibold text-ink-900 dark:text-white">View Policies</p>
            <p className="text-xs text-ink-500 mt-1">Monitor and manage all customer policies</p>
          </Card>
        </Link>
        <Link to="/dashboard/claims" className="group">
          <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300">
            <FileWarning className="h-6 w-6 text-harbor-600 mb-3" />
            <p className="font-display font-semibold text-ink-900 dark:text-white">Review Claims</p>
            <p className="text-xs text-ink-500 mt-1">Assign, approve, or reject claims</p>
          </Card>
        </Link>
        <Link to="/dashboard/agents" className="group">
          <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg hover:border-harbor-300">
            <UserCog className="h-6 w-6 text-harbor-600 mb-3" />
            <p className="font-display font-semibold text-ink-900 dark:text-white">Manage Agents</p>
            <p className="text-xs text-ink-500 mt-1">Onboard new claim-review agents</p>
          </Card>
        </Link>
      </div>
    </>
  );
}

export default function DashboardPage() {
  const { user, role } = useAuth();

  return (
    <div>
      <PageHeader
        eyebrow={formatDate(new Date())}
        title={`Welcome back, ${user?.name?.split(" ")[0] || ""}`}
        description="Here's where things stand right now."
      />
      {role === ROLES.CUSTOMER && <CustomerOverview />}
      {role === ROLES.AGENT && <AgentOverview />}
      {role === ROLES.ADMIN && <AdminOverview />}
    </div>
  );
}
