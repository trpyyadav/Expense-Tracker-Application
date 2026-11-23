import Icon from './Icon';
import { MONTH_NAMES } from '../utils/format';

export function Spinner({ label = 'Loading…' }) {
  return (
    <div className="center-box">
      <div className="spinner" />
      <span className="muted">{label}</span>
    </div>
  );
}

export function EmptyState({ title, hint, action }) {
  return (
    <div className="center-box empty">
      <strong>{title}</strong>
      {hint && <span className="muted">{hint}</span>}
      {action}
    </div>
  );
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div className="error-box">
      <span>{message}</span>
      {onRetry && (
        <button className="btn btn-ghost btn-sm" onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  );
}

export function PageHeader({ title, subtitle, children }) {
  return (
    <div className="page-head">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="muted">{subtitle}</p>}
      </div>
      <div className="page-actions">{children}</div>
    </div>
  );
}

export function StatCard({ label, value, tone = 'neutral', icon, hint }) {
  return (
    <div className={`card stat stat-${tone}`}>
      <div className="stat-top">
        <span className="stat-label">{label}</span>
        {icon && (
          <span className="stat-icon">
            <Icon name={icon} />
          </span>
        )}
      </div>
      <div className="stat-value">{value}</div>
      {hint && <div className="stat-hint muted">{hint}</div>}
    </div>
  );
}

export function MonthYearPicker({ month, year, onChange }) {
  const thisYear = new Date().getFullYear();
  const years = [];
  for (let y = thisYear + 1; y >= thisYear - 6; y--) years.push(y);
  return (
    <div className="inline-fields">
      <select value={month} onChange={(e) => onChange(Number(e.target.value), year)} aria-label="Month">
        {MONTH_NAMES.map((n, i) => (
          <option key={n} value={i + 1}>
            {n}
          </option>
        ))}
      </select>
      <select value={year} onChange={(e) => onChange(month, Number(e.target.value))} aria-label="Year">
        {years.map((y) => (
          <option key={y} value={y}>
            {y}
          </option>
        ))}
      </select>
    </div>
  );
}

export function TypeBadge({ type }) {
  return <span className={`badge badge-${type === 'INCOME' ? 'income' : 'expense'}`}>{type === 'INCOME' ? 'Income' : 'Expense'}</span>;
}
