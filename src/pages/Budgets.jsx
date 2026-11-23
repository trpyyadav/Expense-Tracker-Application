import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { budgetApi, categoryApi } from '../api';
import useAsync from '../components/useAsync';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { EmptyState, ErrorBox, MonthYearPicker, PageHeader, Spinner, StatCard } from '../components/ui';
import { useToast } from '../context/ToastContext';
import { formatMoney, monthName } from '../utils/format';

function BudgetForm({ initial, month, year, categories, onClose, onSaved }) {
  const toast = useToast();
  const [amount, setAmount] = useState(initial?.amount ?? '');
  const [categoryId, setCategoryId] = useState(initial?.categoryId ?? '');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!categoryId) return setError('Please choose a category.');
    if (!(Number(amount) > 0)) return setError('Amount must be greater than zero.');
    setBusy(true);
    try {
      const payload = { amount: Number(amount), month, year, categoryId: Number(categoryId) };
      if (initial) await budgetApi.update(initial.id, payload);
      else await budgetApi.create(payload);
      toast.success(initial ? 'Budget updated' : 'Budget created');
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      title={`${initial ? 'Edit' : 'New'} budget · ${monthName(month)} ${year}`}
      onClose={onClose}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button form="budget-form" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
        </>
      }
    >
      <form id="budget-form" onSubmit={submit} className="form-stack">
        {error && <div className="error-box">{error}</div>}
        <label className="field">
          <span>Expense category</span>
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
            <option value="">Select a category…</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          {categories.length === 0 && (
            <small className="muted">Budgets apply to expense categories. <Link to="/categories">Create one</Link> first.</small>
          )}
        </label>
        <label className="field">
          <span>Budget amount</span>
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required autoFocus />
        </label>
      </form>
    </Modal>
  );
}

function barTone(pct) {
  if (pct > 100) return 'over';
  if (pct >= 75) return 'warn';
  return 'ok';
}

export default function Budgets() {
  const toast = useToast();
  const now = new Date();
  const [period, setPeriod] = useState({ month: now.getMonth() + 1, year: now.getFullYear() });
  const { month, year } = period;

  const budgets = useAsync(
    () => Promise.all([budgetApi.usage(month, year), budgetApi.byMonth(month, year)]),
    [month, year]
  );
  const cats = useAsync(() => categoryApi.list(), []);

  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  // Usage rows lack categoryId, so join with the plain budget list by id.
  const items = useMemo(() => {
    if (!budgets.data) return [];
    const [usage, plain] = budgets.data;
    const byId = new Map(plain.map((b) => [b.id, b]));
    return usage
      .map((u) => ({ ...u, categoryId: byId.get(u.budgetId)?.categoryId }))
      .sort((a, b) => a.categoryName.localeCompare(b.categoryName));
  }, [budgets.data]);

  const totals = useMemo(() => {
    const budget = items.reduce((s, i) => s + Number(i.budgetAmount), 0);
    const spent = items.reduce((s, i) => s + Number(i.totalSpent), 0);
    return { budget, spent, remaining: budget - spent };
  }, [items]);

  const confirmDelete = async () => {
    setBusy(true);
    try {
      await budgetApi.remove(deleting.budgetId);
      toast.success('Budget deleted');
      setDeleting(null);
      budgets.reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setBusy(false);
    }
  };

  const expenseCats = (cats.data || []).filter((c) => c.type === 'EXPENSE');

  return (
    <>
      <PageHeader title="Budgets" subtitle="Set monthly spending limits per category.">
        <MonthYearPicker month={month} year={year} onChange={(m, y) => setPeriod({ month: m, year: y })} />
        <button className="btn btn-primary" onClick={() => setEditing({})}>
          <Icon name="plus" /> New budget
        </button>
      </PageHeader>

      {budgets.loading || cats.loading ? (
        <Spinner />
      ) : budgets.error || cats.error ? (
        <ErrorBox message={budgets.error || cats.error} onRetry={() => { budgets.reload(); cats.reload(); }} />
      ) : (
        <>
          <div className="grid grid-3">
            <StatCard label="Total budgeted" value={formatMoney(totals.budget)} tone="primary" />
            <StatCard label="Total spent" value={formatMoney(totals.spent)} tone="expense" />
            <StatCard
              label={totals.remaining < 0 ? 'Over budget by' : 'Remaining'}
              value={formatMoney(Math.abs(totals.remaining))}
              tone={totals.remaining < 0 ? 'expense' : 'income'}
            />
          </div>

          {items.length ? (
            <div className="grid grid-cards mt">
              {items.map((b) => {
                const pct = Number(b.percentageUsed);
                return (
                  <div key={b.budgetId} className="card budget-card">
                    <div className="card-head">
                      <strong>{b.categoryName}</strong>
                      <div>
                        <button className="icon-btn" onClick={() => setEditing({ id: b.budgetId, amount: b.budgetAmount, categoryId: b.categoryId })} aria-label="Edit">
                          <Icon name="edit" />
                        </button>
                        <button className="icon-btn danger" onClick={() => setDeleting(b)} aria-label="Delete">
                          <Icon name="trash" />
                        </button>
                      </div>
                    </div>
                    <div className="progress">
                      <div className={`progress-bar ${barTone(pct)}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                    </div>
                    <div className="budget-line">
                      <span>{formatMoney(b.totalSpent)} <span className="muted">of {formatMoney(b.budgetAmount)}</span></span>
                      <b className={b.exceeded ? 'neg' : ''}>{pct.toFixed(0)}%</b>
                    </div>
                    <div className={`muted small ${b.exceeded ? 'neg' : ''}`}>
                      {b.exceeded
                        ? `Over by ${formatMoney(Math.abs(Number(b.remainingAmount)))}`
                        : `${formatMoney(b.remainingAmount)} left`}
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="card mt">
              <EmptyState
                title={`No budgets for ${monthName(month)} ${year}`}
                hint="Create a budget to track spending against a limit."
                action={<button className="btn btn-primary" onClick={() => setEditing({})}>New budget</button>}
              />
            </div>
          )}
        </>
      )}

      {editing && (
        <BudgetForm
          initial={editing.id ? editing : null}
          month={month}
          year={year}
          categories={expenseCats}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); budgets.reload(); }}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete budget"
          message={`Delete the ${deleting.categoryName} budget for ${monthName(month)} ${year}?`}
          busy={busy}
          onConfirm={confirmDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
