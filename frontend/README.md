# Assurly — Insurance Policy & Claim Management (Frontend)

A React (Vite) + Tailwind CSS frontend for the Insurance Policy and Claim Management
System backend, covering registration/OTP, login, role-based dashboards
(Customer / Agent / Admin), products, plans, policies, payments, claims (with
document uploads and a full status history), and light/dark theming.

## Stack

- React 19 + Vite
- Tailwind CSS v4
- React Router v7
- Axios (with automatic JWT refresh-token interceptor)
- React Context API (auth + theme)
- lucide-react icons
- react-hot-toast notifications

## Getting started

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173` by default, which matches the CORS
origin already allowed by the Spring Boot backend's `SecurityConfig`.

## Configuring the API URL

Set the backend base URL in `.env` (an `.env.example` is provided):

```
VITE_API_BASE_URL=http://localhost:8080/api
```

Make sure the Spring Boot backend is running on that host/port before using
the app — every page other than the public landing page talks to the real
API (no mocked data).

## Folder structure

```
src/
  components/    Reusable UI: common/ (buttons, inputs, tables...), layout/, landing/
  context/       AuthContext, ThemeContext
  hooks/         useForm, usePagedResource
  layouts/       MainLayout (public), AuthLayout, DashboardLayout
  pages/         Route-level pages, grouped by area (auth/, admin/, agent/, customer/)
  routes/        ProtectedRoute, PublicOnlyRoute guards
  services/      One module per backend resource (authService, claimService, ...)
  utils/         validators, formatters, constants, storage
```

## Roles

- **Customer** — browse products/plans, purchase policies, pay premiums, raise
  and track claims, manage their KYC profile.
- **Agent** — review claims assigned to them and recommend approval/rejection.
- **Admin** — manage products, plans, users, agents, customers; issue policies;
  assign and finally approve/reject claims.

## Notes on a couple of API quirks

- The backend's `/policies/{id}` response doesn't include the underlying plan's
  ID, only its name. The "Pay a Premium" page correlates a policy to its plan
  by matching `planName` against the plans list to recover the premium amount
  required by the payment endpoint — this is a best-effort bridge for a gap in
  the response DTO, not a mock.
- `ClaimRequestDto.documents` takes document **metadata** (name/type/reference)
  at claim-creation time; actual file uploads happen afterwards from the claim
  detail page via the dedicated multipart upload endpoint.
