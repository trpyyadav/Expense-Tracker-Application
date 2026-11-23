import { useState } from 'react';
import { categoryApi } from '../api';
import useAsync from '../components/useAsync';
import Icon from '../components/Icon';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { EmptyState, ErrorBox, PageHeader, Spinner, TypeBadge } from '../components/ui';
import { useToast } from '../context/ToastContext';

function CategoryForm({ initial, defaultType, onClose, onSaved }) {
  const toast = useToast();
  const [name, setName] = useState(initial?.name || '');
  const [type, setType] = useState(initial?.type || defaultType);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!name.trim()) return setError('Category name is required.');
    setBusy(true);
    try {
      const payload = { name: name.trim(), type };
      if (initial) await categoryApi.update(initial.id, payload);
      else await categoryApi.create(payload);
      toast.success(initial ? 'Category updated' : 'Category created');
      onSaved();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      title={initial ? 'Edit category' : 'New category'}
      onClose={onClose}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button form="cat-form" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button>
        </>
      }
    >
      <form id="cat-form" onSubmit={submit} className="form-stack">
        {error && <div className="error-box">{error}</div>}
        <label className="field">
          <span>Name</span>
          <input value={name} onChange={(e) => setName(e.target.value)} autoFocus required />
        </label>
        <div className="field">
          <span>Type</span>
          <div className="segmented">
            <button type="button" className={type === 'EXPENSE' ? 'on expense' : ''} onClick={() => setType('EXPENSE')}>Expense</button>
            <button type="button" className={type === 'INCOME' ? 'on income' : ''} onClick={() => setType('INCOME')}>Income</button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

export default function Categories() {
  const toast = useToast();
  const { data, loading, error, reload } = useAsync(() => categoryApi.list(), []);
  const [tab, setTab] = useState('EXPENSE');
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  const confirmDelete = async () => {
    setBusy(true);
    try {
      await categoryApi.remove(deleting.id);
      toast.success('Category deleted');
      setDeleting(null);
      reload();
    } catch (err) {
      // e.g. 409: category is used by a transaction or budget
      toast.error(err.message);
      setDeleting(null);
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const list = (data || []).filter((c) => c.type === tab).sort((a, b) => a.name.localeCompare(b.name));
  const count = (t) => (data || []).filter((c) => c.type === t).length;

  return (
    <>
      <PageHeader title="Categories" subtitle="Organise your income and expenses.">
        <button className="btn btn-primary" onClick={() => setEditing({})}>
          <Icon name="plus" /> New category
        </button>
      </PageHeader>

      <div className="tabs">
        <button className={tab === 'EXPENSE' ? 'on' : ''} onClick={() => setTab('EXPENSE')}>Expense ({count('EXPENSE')})</button>
        <button className={tab === 'INCOME' ? 'on' : ''} onClick={() => setTab('INCOME')}>Income ({count('INCOME')})</button>
      </div>

      {list.length ? (
        <div className="grid grid-cards">
          {list.map((c) => (
            <div key={c.id} className="card cat-card">
              <div>
                <strong>{c.name}</strong>
                <div><TypeBadge type={c.type} /></div>
              </div>
              <div>
                <button className="icon-btn" onClick={() => setEditing(c)} aria-label={`Edit ${c.name}`}><Icon name="edit" /></button>
                <button className="icon-btn danger" onClick={() => setDeleting(c)} aria-label={`Delete ${c.name}`}><Icon name="trash" /></button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card">
          <EmptyState
            title={`No ${tab.toLowerCase()} categories`}
            hint="Create one to start recording transactions."
            action={<button className="btn btn-primary" onClick={() => setEditing({})}>New category</button>}
          />
        </div>
      )}

      {editing && (
        <CategoryForm
          initial={editing.id ? editing : null}
          defaultType={tab}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); reload(); }}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete category"
          message={`Delete “${deleting.name}”? Categories used by a transaction or budget cannot be deleted.`}
          busy={busy}
          onConfirm={confirmDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
