# 🛡️ Insurance Policy and Claim Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-4.x-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

A full-stack, production-grade **Insurance Policy and Claim Management System** built with Spring Boot (backend) and React + Vite (frontend). It supports multi-role access control, OTP-based authentication, document uploads, SMS/email notifications, premium payment tracking, and a complete claims lifecycle workflow.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [Role-Based Access Control](#-role-based-access-control)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Environment Configuration](#-environment-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation (Swagger)](#-api-documentation-swagger)
- [Postman Collection](#-postman-collection)
- [Security](#-security)
- [Contributing](#-contributing)

---

## 🔍 Overview

The **Insurance Policy and Claim Management System (IMS)** is a comprehensive enterprise-grade web application designed to digitize and streamline every stage of the insurance lifecycle — from product and plan management to policy issuance, premium collection, and claim processing.

The platform serves **three distinct user roles**: Customers, Insurance Operations Officers, and Administrators, each with a tailored dashboard and permissions.

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based stateless authentication with access & refresh token support
- Token blacklisting on logout
- OTP verification via **Email (Gmail SMTP)** and **SMS (Twilio)**
- Forgot password / Reset password via OTP
- Rate limiting with **Resilience4j** to prevent brute-force attacks
- Role-based route protection on both frontend and backend

### 👤 Customer Portal
- Self-registration with email OTP verification
- Profile management (view/update personal details)
- Browse insurance products and available plans
- View issued policies and their status
- Raise and track insurance claims with document uploads
- Record premium payments
- View payment history

### 🏢 Admin Panel
- Manage insurance products (CRUD)
- Manage policy plans (CRUD) with premium types and coverage details
- Issue policies to customers
- View all users and manage Insurance Operations Officers
- View all policies, claims, payments, and customers across the system

### 🧑‍💼 Insurance Operations Officer
- View and manage assigned claims
- Update claim status with history tracking
- View customer and policy details

### 📄 Core Business Features
- **Insurance Products**: Define product types (Life, Health, Vehicle, etc.)
- **Policy Plans**: Multiple plans per product with premium types (Monthly, Quarterly, Annually)
- **Policy Management**: Issue, activate, expire policies with full status lifecycle
- **Claims Processing**: Multi-step claim status workflow with document upload (Cloudinary)
- **Claim Status History**: Full audit trail of all claim status changes
- **Premium Payments**: Multiple payment modes (Online, Cheque, Cash, UPI, etc.)
- **Email Notifications**: Automated emails for registration, OTP, policy issuance, claims
- **SMS Notifications**: Twilio-powered SMS alerts
- **PDF Export**: Generate reports using jsPDF & AutoTable

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 25 | Core language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | Latest | Authentication & Authorization |
| Spring Data JPA | Latest | ORM & database abstraction |
| MySQL | 8.0+ | Primary relational database |
| Hibernate | Latest | JPA implementation |
| JJWT | 0.11.5 | JWT token generation & validation |
| ModelMapper | 3.2.6 | DTO ↔ Entity mapping |
| SpringDoc OpenAPI | 3.0.2 | Swagger API documentation |
| Cloudinary | 1.39.0 | Cloud-based file/document storage |
| Twilio SDK | 12.1.1 | SMS notifications |
| Spring Mail | Latest | Email (SMTP/Gmail) notifications |
| Resilience4j | 2.2.0 | Rate limiting & fault tolerance |
| Lombok | Latest | Boilerplate code reduction |
| Spring Boot DevTools | Latest | Hot reload during development |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 19.x | UI library |
| Vite | 8.x | Build tool & dev server |
| React Router DOM | 7.x | Client-side routing |
| TailwindCSS | 4.x | Utility-first CSS framework |
| Axios | 1.x | HTTP client for API calls |
| React Hot Toast | 2.x | Toast notifications |
| Lucide React | 1.x | Icon library |
| React Icons | 5.x | Additional icon sets |
| jsPDF | 4.x | PDF generation |
| jsPDF AutoTable | 5.x | Table-based PDF reports |
| OxLint | 1.x | Fast JavaScript linter |

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                         │
│              React 19 + Vite + TailwindCSS 4                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────┐  │
│  │  Auth    │  │ Customer │  │  Admin   │  │  Officer  │  │
│  │  Pages   │  │  Portal  │  │  Panel   │  │   Views   │  │
│  └──────────┘  └──────────┘  └──────────┘  └───────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS + JWT Bearer Token
                         │
┌────────────────────────▼────────────────────────────────────┐
│                      API GATEWAY LAYER                       │
│            Spring Boot 4 + Spring Security                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              JWT Auth Filter (Stateless)             │    │
│  │         Rate Limiter (Resilience4j)                  │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌───────────┐  │
│  │  Auth    │  │ Policy   │  │  Claim   │  │  Payment  │  │
│  │Controller│  │Controller│  │Controller│  │Controller │  │
│  └──────────┘  └──────────┘  └──────────┘  └───────────┘  │
└─────────┬───────────────────────────────────┬───────────────┘
          │                                   │
┌─────────▼──────────┐             ┌──────────▼──────────────┐
│   SERVICE LAYER    │             │   EXTERNAL SERVICES      │
│  Business Logic    │             │  ┌──────────────────┐   │
│  + Validations     │             │  │  Cloudinary CDN  │   │
└─────────┬──────────┘             │  │  (Document Store)│   │
          │                        │  └──────────────────┘   │
┌─────────▼──────────┐             │  ┌──────────────────┐   │
│  REPOSITORY LAYER  │             │  │  Twilio (SMS)    │   │
│  Spring Data JPA   │             │  └──────────────────┘   │
└─────────┬──────────┘             │  ┌──────────────────┐   │
          │                        │  │  Gmail SMTP      │   │
┌─────────▼──────────┐             │  │  (Email)         │   │
│   DATABASE LAYER   │             │  └──────────────────┘   │
│  MySQL 8.0+        │             └─────────────────────────┘
└────────────────────┘
```

---

## 📁 Project Structure

```
InsurancePolicyAndClaimManagementSystem/
│
├── backend/                              # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/springboot/insurancemanagementsystem/
│   │   │   │   ├── InsuranceManagementSystemApplication.java
│   │   │   │   ├── advice/               # Global exception handlers
│   │   │   │   ├── config/               # App configuration classes
│   │   │   │   │   ├── AppConfig.java
│   │   │   │   │   ├── CloudinaryConfig.java
│   │   │   │   │   ├── MailConfig.java
│   │   │   │   │   ├── OpenApiConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── TwilioConfig.java
│   │   │   │   ├── controller/           # REST API controllers
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── ClaimController.java
│   │   │   │   │   ├── ClaimDocumentController.java
│   │   │   │   │   ├── ClaimStatusHistoryController.java
│   │   │   │   │   ├── CustomerController.java
│   │   │   │   │   ├── InsuranceProductController.java
│   │   │   │   │   ├── OtpController.java
│   │   │   │   │   ├── PolicyController.java
│   │   │   │   │   ├── PolicyPlanController.java
│   │   │   │   │   ├── PremiumPaymentController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   │   ├── entitie/              # JPA Entity classes
│   │   │   │   │   ├── BlacklistedToken.java
│   │   │   │   │   ├── Claim.java
│   │   │   │   │   ├── ClaimDocument.java
│   │   │   │   │   ├── ClaimStatusHistory.java
│   │   │   │   │   ├── Customer.java
│   │   │   │   │   ├── InsuranceProduct.java
│   │   │   │   │   ├── OtpVerification.java
│   │   │   │   │   ├── PasswordResetOtp.java
│   │   │   │   │   ├── Policy.java
│   │   │   │   │   ├── PolicyPlan.java
│   │   │   │   │   ├── PremiumPayment.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── enums/                # Application enumerations
│   │   │   │   │   ├── ClaimStatus.java
│   │   │   │   │   ├── PaymentMode.java
│   │   │   │   │   ├── PaymentStatus.java
│   │   │   │   │   ├── PolicyStatus.java
│   │   │   │   │   ├── PremiumType.java
│   │   │   │   │   ├── ProductType.java
│   │   │   │   │   └── Role.java
│   │   │   │   ├── exception/            # Custom exceptions
│   │   │   │   ├── repository/           # Spring Data JPA repositories
│   │   │   │   ├── security/             # JWT filter, handlers, services
│   │   │   │   │   ├── filter/
│   │   │   │   │   ├── handler/
│   │   │   │   │   ├── service/
│   │   │   │   │   └── util/
│   │   │   │   └── service/              # Business service interfaces + impl
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-local.properties
│   │   └── test/
│   ├── postman/
│   │   └── Insurance Management System.postman_collection.json
│   └── pom.xml
│
├── frontend/                             # React + Vite Application
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/                   # Shared UI components
│   │   │   ├── landing/                  # Landing page components
│   │   │   └── layout/                   # Layout components
│   │   ├── context/
│   │   │   ├── AuthContext.jsx           # Global auth state
│   │   │   └── ThemeContext.jsx          # Dark/light theme
│   │   ├── hooks/                        # Custom React hooks
│   │   ├── layouts/
│   │   │   ├── AuthLayout.jsx
│   │   │   ├── DashboardLayout.jsx
│   │   │   └── MainLayout.jsx
│   │   ├── pages/
│   │   │   ├── auth/                     # Login, Register, OTP, Password Reset
│   │   │   ├── admin/                    # Admin-only pages
│   │   │   ├── customer/                 # Customer-only pages
│   │   │   ├── insurance-operations-officer/
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── ProductsPage.jsx
│   │   │   ├── PoliciesPage.jsx
│   │   │   ├── ClaimsPage.jsx
│   │   │   ├── PaymentsPage.jsx
│   │   │   └── ...
│   │   ├── routes/
│   │   │   ├── ProtectedRoute.jsx
│   │   │   └── PublicOnlyRoute.jsx
│   │   ├── services/                     # Axios API service calls
│   │   ├── utils/                        # Constants, helpers
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── src/                                  # Root-level shared sources
├── .gitignore
├── .gitattributes
└── README.md
```

---

## 👥 Role-Based Access Control

The system implements three distinct roles with fine-grained permissions:

| Feature | Customer | Insurance Operations Officer | Admin |
|---|:---:|:---:|:---:|
| View Products & Plans | ✅ | ✅ | ✅ |
| View Own Policies | ✅ | ❌ | ✅ |
| Raise Claims | ✅ | ❌ | ❌ |
| Record Premium Payments | ✅ | ❌ | ❌ |
| Edit Own Profile | ✅ | ❌ | ❌ |
| View All Customers | ❌ | ✅ | ✅ |
| View Customer Details | ❌ | ✅ | ✅ |
| Manage Assigned Claims | ❌ | ✅ | ✅ |
| View All Plans | ❌ | ✅ | ✅ |
| Issue Policies | ❌ | ✅ | ✅ |
| Create/Edit Products | ❌ | ❌ | ✅ |
| Create/Edit Plans | ❌ | ❌ | ✅ |
| Manage Users | ❌ | ❌ | ✅ |
| Manage Officers | ❌ | ❌ | ✅ |
| View All Payments | ❌ | ❌ | ✅ |

---

## 📡 API Endpoints

Base URL: `http://localhost:8080/api`

### 🔐 Authentication (`/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `POST` | `/auth/register` | Register a new customer | ❌ |
| `POST` | `/auth/login` | Login and receive JWT tokens | ❌ |
| `POST` | `/auth/logout` | Logout and blacklist token | ✅ |
| `POST` | `/auth/refresh-token` | Refresh access token | ✅ |

### 📩 OTP (`/otp`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `POST` | `/otp/send` | Send OTP to email/phone | ❌ |
| `POST` | `/otp/verify` | Verify OTP | ❌ |
| `POST` | `/otp/forgot-password` | Send password reset OTP | ❌ |
| `POST` | `/otp/reset-password` | Reset password with OTP | ❌ |

### 👤 Users (`/users`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/users` | List all users | ADMIN |
| `GET` | `/users/{id}` | Get user by ID | ADMIN |
| `PUT` | `/users/{id}` | Update user | ADMIN |
| `DELETE` | `/users/{id}` | Delete user | ADMIN |

### 🧑 Customers (`/customers`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/customers` | List all customers | ADMIN, OFFICER |
| `GET` | `/customers/{id}` | Get customer details | ADMIN, OFFICER |
| `GET` | `/customers/me` | Get own customer profile | CUSTOMER |
| `PUT` | `/customers/me` | Update own profile | CUSTOMER |

### 📦 Insurance Products (`/products`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/products` | List all products | ✅ |
| `GET` | `/products/{id}` | Get product details | ✅ |
| `POST` | `/products` | Create new product | ADMIN |
| `PUT` | `/products/{id}` | Update product | ADMIN |
| `DELETE` | `/products/{id}` | Delete product | ADMIN |

### 📋 Policy Plans (`/plans`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/plans` | List all plans | ADMIN, OFFICER |
| `GET` | `/plans/product/{productId}` | Plans by product | ✅ |
| `GET` | `/plans/{id}` | Get plan details | ✅ |
| `POST` | `/plans` | Create new plan | ADMIN |
| `PUT` | `/plans/{id}` | Update plan | ADMIN |
| `DELETE` | `/plans/{id}` | Delete plan | ADMIN |

### 📜 Policies (`/policies`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/policies` | List policies (role-scoped) | ✅ |
| `GET` | `/policies/{id}` | Get policy details | ✅ |
| `POST` | `/policies` | Issue a new policy | ADMIN, OFFICER |
| `PUT` | `/policies/{id}` | Update policy | ADMIN, OFFICER |
| `DELETE` | `/policies/{id}` | Delete policy | ADMIN |

### 🩺 Claims (`/claims`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/claims` | List claims (role-scoped) | ✅ |
| `GET` | `/claims/{id}` | Get claim details | ✅ |
| `POST` | `/claims` | Raise a new claim | CUSTOMER |
| `PUT` | `/claims/{id}/status` | Update claim status | ADMIN, OFFICER |
| `DELETE` | `/claims/{id}` | Delete claim | ADMIN |

### 📎 Claim Documents (`/claim-documents`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `POST` | `/claim-documents` | Upload claim document | CUSTOMER |
| `GET` | `/claim-documents/claim/{claimId}` | Get docs for claim | ✅ |
| `DELETE` | `/claim-documents/{id}` | Delete document | ADMIN |

### 📝 Claim Status History (`/claim-status-history`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/claim-status-history/claim/{claimId}` | Get history for claim | ✅ |

### 💰 Premium Payments (`/payments`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|:---:|
| `GET` | `/payments` | List payments (role-scoped) | ✅ |
| `GET` | `/payments/{id}` | Get payment details | ✅ |
| `POST` | `/payments` | Record a premium payment | CUSTOMER |

---

## 🗄 Database Schema

The application uses MySQL with the following core entities and relationships:

```
User
 └── Customer (1:1)
      ├── Policy (1:N)
      │    ├── PremiumPayment (1:N)
      │    └── Claim (1:N)
      │         ├── ClaimDocument (1:N)
      │         └── ClaimStatusHistory (1:N)
      └── OtpVerification (1:N)

InsuranceProduct
 └── PolicyPlan (1:N)
      └── Policy (1:N)

BlacklistedToken  (for JWT logout)
PasswordResetOtp  (for password reset)
```

### Key Entities

| Entity | Description |
|---|---|
| `User` | System users with role (ADMIN, CUSTOMER, INSURANCE_OPERATIONS_OFFICER) |
| `Customer` | Extended profile for customers (linked 1:1 to User) |
| `InsuranceProduct` | Insurance product categories (Life, Health, Vehicle, etc.) |
| `PolicyPlan` | Specific plans under a product with premium details |
| `Policy` | Issued policy for a customer under a specific plan |
| `PremiumPayment` | Payment records for policy premiums |
| `Claim` | Insurance claims raised by customers |
| `ClaimDocument` | Supporting documents uploaded to Cloudinary |
| `ClaimStatusHistory` | Audit log of claim status changes |
| `OtpVerification` | OTP records for email/SMS verification |
| `PasswordResetOtp` | OTP records for password reset |
| `BlacklistedToken` | Revoked JWT tokens for logout support |

### Enumerations

| Enum | Values |
|---|---|
| `Role` | ADMIN, CUSTOMER, INSURANCE_OPERATIONS_OFFICER |
| `PolicyStatus` | ACTIVE, EXPIRED, PENDING, CANCELLED |
| `ClaimStatus` | PENDING, UNDER_REVIEW, APPROVED, REJECTED, SETTLED |
| `PremiumType` | MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY |
| `ProductType` | LIFE, HEALTH, VEHICLE, PROPERTY, TRAVEL |
| `PaymentMode` | ONLINE, CHEQUE, CASH, UPI, NEFT, RTGS |
| `PaymentStatus` | PENDING, COMPLETED, FAILED |

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

| Tool | Version | Download |
|---|---|---|
| JDK | 25+ | https://adoptium.net/ |
| Maven | 3.9+ | https://maven.apache.org/ |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Node.js | 20+ | https://nodejs.org/ |
| npm | 10+ | Bundled with Node.js |

You will also need accounts and credentials for:
- **Cloudinary** – For claim document storage
- **Twilio** – For SMS OTP notifications
- **Gmail** – For email notifications (App Password required)

---

### Backend Setup

**1. Clone the repository**
```bash
git clone https://github.com/your-username/InsurancePolicyAndClaimManagementSystem.git
cd InsurancePolicyAndClaimManagementSystem/backend
```

**2. Configure the database**

Create a MySQL database (or let JPA auto-create it):
```sql
CREATE DATABASE ims_db;
```

**3. Configure application properties**

Edit `src/main/resources/application-local.properties` with your credentials (see Environment Configuration section below).

**4. Build and run the backend**
```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Or using system Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The backend server will start at **`http://localhost:8080`**

---

### Frontend Setup

**1. Navigate to the frontend directory**
```bash
cd ../frontend
```

**2. Install dependencies**
```bash
npm install
```

**3. Configure environment variables**

Create a `.env` file based on the example:
```bash
cp .env.example .env
```

Edit `.env`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

**4. Start the development server**
```bash
npm run dev
```

The frontend will be available at **`http://localhost:5173`**

---

## ⚙️ Environment Configuration

### Backend — `application-local.properties`

```properties
# ── Database ──────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/ims_db?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# ── JWT ───────────────────────────────────────────────────────
app.jwt.secret=YOUR_STRONG_SECRET_KEY_MIN_64_CHARS
app.jwt.expiration=6000000
app.jwt.refresh-expiration=604800000

# ── Cloudinary ────────────────────────────────────────────────
cloudinary.cloud-name=YOUR_CLOUDINARY_CLOUD_NAME
cloudinary.api-key=YOUR_CLOUDINARY_API_KEY
cloudinary.api-secret=YOUR_CLOUDINARY_API_SECRET

# ── Email (Gmail SMTP) ────────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ── OTP ───────────────────────────────────────────────────────
app.otp.expiry-minutes=5

# ── Twilio (SMS) ──────────────────────────────────────────────
app.twilio.account-sid=YOUR_TWILIO_ACCOUNT_SID
app.twilio.auth-token=YOUR_TWILIO_AUTH_TOKEN
app.twilio.from-phone=+1XXXXXXXXXX

# ── Swagger ───────────────────────────────────────────────────
springdoc.swagger-ui.path=/swagger-ui.html
```

> **Security Warning**: Never commit real credentials to version control. Use environment variables or a secrets manager in production.

### Frontend — `.env`
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 🏃 Running the Application

### Development Mode

**Terminal 1 – Backend:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Terminal 2 – Frontend:**
```bash
cd frontend
npm run dev
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs (JSON) | http://localhost:8080/v3/api-docs |

### Production Build

**Backend:**
```bash
cd backend
./mvnw clean package
java -jar target/InsuranceManagementSystem-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Serve the dist/ folder with any static file server
```

---

## 📖 API Documentation (Swagger)

The backend exposes interactive API documentation via **Swagger UI** powered by SpringDoc OpenAPI 3.0.

Once the backend is running, navigate to:

```
http://localhost:8080/swagger-ui.html
```

You can:
- Browse all available endpoints organized by controller
- View request/response schemas
- Test endpoints directly from the browser (use "Authorize" to set your JWT token)

The raw OpenAPI JSON spec is available at:
```
http://localhost:8080/v3/api-docs
```

---

## 📮 Postman Collection

A comprehensive Postman collection is included in the repository:

```
backend/postman/Insurance Management System.postman_collection.json
```

**To import:**
1. Open Postman
2. Click **Import** → **File**
3. Select `Insurance Management System.postman_collection.json`
4. Set the `baseUrl` collection variable to `http://localhost:8080`
5. Run the **Auth → Login** request and copy the JWT token
6. Set the `token` collection variable with the JWT value

---

## 🔒 Security

This application implements multiple security layers:

### JWT Authentication
- **Stateless**: No server-side sessions; all state is in the JWT
- **Dual Tokens**: Short-lived access token + long-lived refresh token
- **Token Blacklist**: Ensures logged-out tokens cannot be reused

### Spring Security
- CSRF disabled (stateless REST API with JWT)
- CORS configured for frontend origin
- Method-level security with role-based endpoint protection
- Custom `AuthenticationEntryPoint` for 401 responses
- Custom `AccessDeniedHandler` for 403 responses

### Rate Limiting (Resilience4j)
- OTP endpoints are rate-limited to prevent abuse
- Configurable limits per endpoint

### Input Validation
- All request DTOs are validated with Spring Boot Validation (@Valid)
- Custom exception handling returns structured error responses

### Passwords
- Passwords are hashed with **BCrypt** before storage

> **Production Recommendations:**
> - Use HTTPS/TLS
> - Store secrets in environment variables or a vault (e.g., AWS Secrets Manager, HashiCorp Vault)
> - Set proper CORS origins (not wildcard)
> - Use a strong JWT secret key (minimum 64 characters)
> - Enable Flyway/Liquibase for production schema migrations instead of ddl-auto=update

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** your changes: `git commit -m 'feat: add your feature'`
4. **Push** to the branch: `git push origin feature/your-feature-name`
5. **Open** a Pull Request

### Commit Convention
This project follows Conventional Commits:
- `feat:` – new feature
- `fix:` – bug fix
- `docs:` – documentation change
- `refactor:` – code refactor
- `test:` – adding tests
- `chore:` – build/tooling changes

---

## 📄 License

This project is for educational and demonstration purposes.

---

<div align="center">

Built with ❤️ using **Spring Boot** + **React**

</div>
