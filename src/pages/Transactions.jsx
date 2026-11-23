import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { categoryApi, transactionApi } from '../api';
import useAsync from '../components/useAsync';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { EmptyState, ErrorBox, PageHeader, Spinner, TypeBadge } from '../components/ui';
import { useToast } from '../context/ToastContext';
import { formatDate, formatMoney, toISODate } from '../utils/format';

const PAGE_SIZE = 10;

function TransactionForm({ initial, categories, onClose, onSaved }) {
  const toast = useToast();
  const [form, setForm] = useState(() => ({
    type: initial?.type || 'EXPENSE',
    amount: initial?.amount ?? '',
    categoryId: initial?.categoryId ?? '',
    transactionDate: initial?.transactionDate || toISODate(),
    description: initial?.description || '',
  }));
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const options = categories.filter((c) => c.type === form.type);

  const setType = (type) =>
    setForm((f) => ({
      ...f,
      type,
      // The backend requires the category type to match the transaction type.
      categoryId: categories.some((c) => c.id === Number(f.categoryId) && c.type === type) ? f.categoryId : '',
    }));

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.categoryId) return setError('Please choose a category.');
    if (!(Number(form.amount) > 0)) return setError('Amount must be greater than zero.');
    setBusy(true);
    try {
      const payload = {
        amount: Number(form.amount),
        type: form.type,
        description: form.description.trim() || null,
        transactionDate: form.transactionDate,
        categoryId: Number(form.categoryId),
      };
      if (initial) await transactionApi.update(initial.id, payload);
      else await transactionApi.create(payload);
      toast.success(initial ? 'Transaction updated' : 'Transaction added');
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      title={initial ? 'Edit transaction' : 'New transaction'}
      onClose={onClose}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button form="tx-form" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
        </>
      }
    >
      <form id="tx-form" onSubmit={submit} className="form-stack">
        {error && <div className="error-box">{error}</div>}

        <div className="segmented">
          <button type="button" className={form.type === 'EXPENSE' ? 'on expense' : ''} onClick={() => setType('EXPENSE')}>Expense</button>
          <button type="button" className={form.type === 'INCOME' ? 'on income' : ''} onClick={() => setType('INCOME')}>Income</button>
        </div>

        <div className="form-row">
          <label className="field">
            <span>Amount</span>
            <input type="number" min="0.01" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required autoFocus />
          </label>
          <label className="field">
            <span>Date</span>
            <input type="date" value={form.transactionDate} onChange={(e) => setForm({ ...form, transactionDate: e.target.value })} required />
          </label>
        </div>

        <label className="field">
          <span>Category</span>
          <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required>
            <option value="">Select a category…</option>
            {options.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          {options.length === 0 && (
            <small className="muted">
              No {form.type.toLowerCase()} categories yet. <Link to="/categories">Create one</Link> first.
            </small>
          )}
        </label>

        <label className="field">
          <span>Description (optional)</span>
          <textarea rows={3} maxLength={500} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </label>
      </form>
    </Modal>
  );
}

export default function Transactions() {
  const toast = useToast();
  const tx = useAsync(() => transactionApi.list(), []);
  const cats = useAsync(() => categoryApi.list(), []);

  const [filters, setFilters] = useState({ search: '', type: '', categoryId: '', from: '', to: '' });
  const [page, setPage] = useState(1);
  const [editing, setEditing] = useState(null); // null | {} (new) | transaction
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  const setFilter = (k, v) => {
    setFilters((f) => ({ ...f, [k]: v }));
    setPage(1);
  };

  const filtered = useMemo(() => {
    const list = [...(tx.data || [])].sort(
      (a, b) => b.transactionDate.localeCompare(a.transactionDate) || b.id - a.id
    );
    const q = filters.search.trim().toLowerCase();
    return list.filter((t) => {
      if (filters.type && t.type !== filters.type) return false;
      if (filters.categoryId && t.categoryId !== Number(filters.categoryId)) return false;
      if (filters.from && t.transactionDate < filters.from) return false;
      if (filters.to && t.transactionDate > filters.to) return false;
      if (q && !`${t.description || ''} ${t.categoryName}`.toLowerCase().includes(q)) return false;
      return true;
    });
  }, [tx.data, filters]);

  const totals = useMemo(() => {
    let income = 0, expense = 0;
    filtered.forEach((t) => (t.type === 'INCOME' ? (income += Number(t.amount)) : (expense += Number(t.amount))));
    return { income, expense };
  }, [filtered]);

  const pages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const current = Math.min(page, pages);
  const rows = filtered.slice((current - 1) * PAGE_SIZE, current * PAGE_SIZE);
  const hasFilters = Object.values(filters).some(Boolean);

  const confirmDelete = async () => {
    setBusy(true);
    try {
      await transactionApi.remove(deleting.id);
      toast.success('Transaction deleted');
      setDeleting(null);
      tx.reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setBusy(false);
    }
  };

  if (tx.loading || cats.loading) return <Spinner />;
  if (tx.error || cats.error) return <ErrorBox message={tx.error || cats.error} onRetry={() => { tx.reload(); cats.reload(); }} />;

  const categories = cats.data || [];

  return (
    <>
      <PageHeader title="Transactions" subtitle={`${filtered.length} of ${tx.data.length} transactions`}>
        <button className="btn btn-primary" onClick={() => setEditing({})}>
          <Icon name="plus" /> Add transaction
        </button>
      </PageHeader>

      <div className="card filters">
        <div className="search">
          <Icon name="search" size={16} />
          <input placeholder="Search description or category" value={filters.search} onChange={(e) => setFilter('search', e.target.value)} />
        </div>
        <select value={filters.type} onChange={(e) => setFilter('type', e.target.value)} aria-label="Type">
          <option value="">All types</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </select>
        <select value={filters.categoryId} onChange={(e) => setFilter('categoryId', e.target.value)} aria-label="Category">
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name} ({c.type.toLowerCase()})</option>
          ))}
        </select>
        <input type="date" value={filters.from} onChange={(e) => setFilter('from', e.target.value)} aria-label="From date" />
        <input type="date" value={filters.to} onChange={(e) => setFilter('to', e.target.value)} aria-label="To date" />
        {hasFilters && (
          <button className="btn btn-ghost btn-sm" onClick={() => { setFilters({ search: '', type: '', categoryId: '', from: '', to: '' }); setPage(1); }}>
            Clear
          </button>
        )}
      </div>

      <div className="card mt">
        {rows.length ? (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Description</th>
                    <th>Category</th>
                    <th>Type</th>
                    <th className="num">Amount</th>
                    <th className="actions-col" />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((t) => (
                    <tr key={t.id}>
                      <td>{formatDate(t.transactionDate)}</td>
                      <td className="wrap">{t.description || <span className="muted">—</span>}</td>
                      <td>{t.categoryName}</td>
                      <td><TypeBadge type={t.type} /></td>
                      <td className={`num ${t.type === 'INCOME' ? 'pos' : 'neg'}`}>
                        {t.type === 'INCOME' ? '+' : '−'}{formatMoney(t.amount)}
                      </td>
                      <td className="actions-col">
                        <button className="icon-btn" onClick={() => setEditing(t)} aria-label="Edit"><Icon name="edit" /></button>
                        <button className="icon-btn danger" onClick={() => setDeleting(t)} aria-label="Delete"><Icon name="trash" /></button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="table-foot">
              <span className="muted">
                Filtered: <b className="pos">+{formatMoney(totals.income)}</b> · <b className="neg">−{formatMoney(totals.expense)}</b>
              </span>
              <div className="pager">
                <button className="btn btn-ghost btn-sm" disabled={current <= 1} onClick={() => setPage(current - 1)}>Previous</button>
                <span className="muted">Page {current} of {pages}</span>
                <button className="btn btn-ghost btn-sm" disabled={current >= pages} onClick={() => setPage(current + 1)}>Next</button>
              </div>
            </div>
          </>
        ) : (
          <EmptyState
            title={hasFilters ? 'No transactions match your filters' : 'No transactions yet'}
            hint={hasFilters ? 'Try adjusting or clearing the filters.' : 'Add your first income or expense.'}
          />
        )}
      </div>

      {editing && (
        <TransactionForm
          initial={editing.id ? editing : null}
          categories={categories}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); tx.reload(); }}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete transaction"
          message={`Delete this ${formatMoney(deleting.amount)} ${deleting.type.toLowerCase()} (${deleting.categoryName})? This cannot be undone.`}
          busy={busy}
          onConfirm={confirmDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
