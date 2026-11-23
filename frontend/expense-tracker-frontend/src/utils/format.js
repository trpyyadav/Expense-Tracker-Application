const CURRENCY = import.meta.env.VITE_CURRENCY || 'INR';
const LOCALE = import.meta.env.VITE_LOCALE || 'en-IN';

const currencyFmt = new Intl.NumberFormat(LOCALE, {
  style: 'currency',
  currency: CURRENCY,
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const compactFmt = new Intl.NumberFormat(LOCALE, {
  notation: 'compact',
  maximumFractionDigits: 1,
});

export const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

export const formatMoney = (value) => currencyFmt.format(Number(value ?? 0));
export const formatCompact = (value) => compactFmt.format(Number(value ?? 0));
export const monthName = (m) => MONTH_NAMES[(Number(m) || 1) - 1];
export const shortMonth = (m) => monthName(m).slice(0, 3);

// Dates from the API are 'YYYY-MM-DD'. Parse as local date to avoid timezone shifts.
export function formatDate(iso) {
  if (!iso) return '';
  const [y, m, d] = String(iso).split('-').map(Number);
  return new Date(y, m - 1, d).toLocaleDateString(LOCALE, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

export function toISODate(date = new Date()) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function firstDayOfMonth(date = new Date()) {
  return toISODate(new Date(date.getFullYear(), date.getMonth(), 1));
}

export function lastDayOfMonth(date = new Date()) {
  return toISODate(new Date(date.getFullYear(), date.getMonth() + 1, 0));
}

// The backend's category "percentage" field is unreliable for large totals,
// so percentages are always computed here from totalSpent.
export function withPercentages(items = []) {
  const total = items.reduce((s, i) => s + Number(i.totalSpent || 0), 0);
  return items.map((i) => ({
    ...i,
    pct: total > 0 ? (Number(i.totalSpent || 0) / total) * 100 : 0,
  }));
}

export const CHART_COLORS = [
  '#4f46e5', '#06b6d4', '#f59e0b', '#ef4444', '#10b981',
  '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#64748b',
];
