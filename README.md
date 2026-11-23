# Expense Tracker – React Frontend

React (Vite) frontend for the Spring Boot `expense-tracker` backend.

## Features
- Register / login with JWT (token kept in `localStorage`, auto sign-out when it expires)
- **Dashboard** – all-time and current-month totals, 6-month income vs expense chart, category pie chart, recent transactions
- **Transactions** – add / edit / delete, search, filter by type, category and date range, pagination
- **Categories** – income and expense categories (add / edit / delete)
- **Budgets** – per-month budgets with live usage bars (green → amber → red)
- **Reports** – monthly or custom date-range report with charts, plus **Excel export**

## Run locally

Prerequisites: Node.js 18+ and the backend running.

1. Start the backend (port **8081**). It needs a `JWT_SECRET` environment variable and MySQL:
   ```bash
   export JWT_SECRET="a-long-random-secret-at-least-32-characters"   # Windows PowerShell: $env:JWT_SECRET="..."
   ./mvnw spring-boot:run
   ```
2. Start the frontend:
   ```bash
   npm install
   npm run dev
   ```
3. Open http://localhost:5173

No CORS setup is needed: the Vite dev server proxies `/api` to `http://localhost:8081`
(see `vite.config.js`). If your backend uses a different port, change the proxy target there.

## Optional settings
Copy `.env.example` to `.env.local` to change the currency / number format (default INR, `en-IN`).

## Project structure
```
src/
  api/         fetch client (JWT header, error parsing) + endpoint functions
  context/     AuthContext, ToastContext
  components/  Layout, Modal, ConfirmDialog, shared UI
  pages/       Login, Register, Dashboard, Transactions, Categories, Budgets, Reports
  utils/       formatting helpers
  styles/      global stylesheet (light + dark mode)
```

## Production build
`npm run build` creates `dist/`. Because the proxy only exists in the dev server, serving `dist/`
separately requires either a reverse proxy for `/api` or a CORS configuration on the backend.
