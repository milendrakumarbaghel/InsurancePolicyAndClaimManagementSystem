import { Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import MainLayout from "./layouts/MainLayout";
import AuthLayout from "./layouts/AuthLayout";
import DashboardLayout from "./layouts/DashboardLayout";
import ProtectedRoute from "./routes/ProtectedRoute";
import PublicOnlyRoute from "./routes/PublicOnlyRoute";
import LandingPage from "./pages/LandingPage";
import NotFoundPage from "./pages/NotFoundPage";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import OtpVerifyPage from "./pages/auth/OtpVerifyPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage";
import DashboardPage from "./pages/DashboardPage";
import MyProfilePage from "./pages/customer/MyProfilePage";
import ProductsPage from "./pages/ProductsPage";
import ProductFormPage from "./pages/admin/ProductFormPage";
import PlansByProductPage from "./pages/PlansByProductPage";
import PlansAdminPage from "./pages/admin/PlansAdminPage";
import PlanFormPage from "./pages/admin/PlanFormPage";
import PoliciesPage from "./pages/PoliciesPage";
import PolicyDetailPage from "./pages/PolicyDetailPage";
import IssuePolicyPage from "./pages/admin/IssuePolicyPage";
import PaymentsPage from "./pages/PaymentsPage";
import RecordPaymentPage from "./pages/customer/RecordPaymentPage";
import ClaimsPage from "./pages/ClaimsPage";
import RaiseClaimPage from "./pages/customer/RaiseClaimPage";
import ClaimDetailPage from "./pages/ClaimDetailPage";
import AssignedClaimsPage from "./pages/insurance-operations-officer/AssignedClaimsPage";
import CustomersPage from "./pages/CustomersPage";
import CustomerDetailPage from "./pages/CustomerDetailPage";
import UsersPage from "./pages/admin/UsersPage";
import InsuranceOperationsOfficersPage from "./pages/admin/InsuranceOperationsOfficersPage";
import { ROLES } from "./utils/constants";

export default function App() {
  return (
    <>
      <Toaster
        position="top-right"
        toastOptions={{
          className: "text-sm font-medium",
          style: {
            background: "var(--color-ink-900)",
            color: "#fff",
            borderRadius: "0.75rem",
          },
          success: { iconTheme: { primary: "#2f9e6e", secondary: "#fff" } },
          error: { iconTheme: { primary: "#c4453d", secondary: "#fff" } },
        }}
      />
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<LandingPage />} />
        </Route>

        <Route element={<PublicOnlyRoute />}>
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
          </Route>
        </Route>

        <Route element={<AuthLayout />}>
          <Route path="/verify-otp" element={<OtpVerifyPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />

            <Route element={<ProtectedRoute allowedRoles={[ROLES.CUSTOMER]} />}>
              <Route path="/dashboard/profile" element={<MyProfilePage />} />
              <Route path="/dashboard/claims/new" element={<RaiseClaimPage />} />
              <Route path="/dashboard/payments/new" element={<RecordPaymentPage />} />
            </Route>

            <Route path="/dashboard/products" element={<ProductsPage />} />
            <Route path="/dashboard/products/:productId/plans" element={<PlansByProductPage />} />

            <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.INSURANCE_OPERATIONS_OFFICER]} />}>
              <Route path="/dashboard/plans" element={<PlansAdminPage />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
              <Route path="/dashboard/products/new" element={<ProductFormPage />} />
              <Route path="/dashboard/products/:productId/edit" element={<ProductFormPage />} />
              <Route path="/dashboard/plans/new" element={<PlanFormPage />} />
              <Route path="/dashboard/plans/:planId/edit" element={<PlanFormPage />} />
              <Route path="/dashboard/users" element={<UsersPage />} />
              <Route path="/dashboard/insurance-operations-officers" element={<InsuranceOperationsOfficersPage />} />
            </Route>

            <Route path="/dashboard/policies" element={<PoliciesPage />} />
            <Route path="/dashboard/policies/:policyId" element={<PolicyDetailPage />} />

            <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
              <Route path="/dashboard/policies-issue" element={<IssuePolicyPage />} />
            </Route>

            <Route path="/dashboard/payments" element={<PaymentsPage />} />
            <Route path="/dashboard/claims" element={<ClaimsPage />} />
            <Route path="/dashboard/claims/:claimId" element={<ClaimDetailPage />} />

            <Route element={<ProtectedRoute allowedRoles={[ROLES.INSURANCE_OPERATIONS_OFFICER]} />}>
              <Route path="/dashboard/assigned-claims" element={<AssignedClaimsPage />} />
            </Route>

            <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.INSURANCE_OPERATIONS_OFFICER]} />}>
              <Route path="/dashboard/customers" element={<CustomersPage />} />
              <Route path="/dashboard/customers/:customerId" element={<CustomerDetailPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  );
}