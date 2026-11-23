import { useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { downloadBlob, reportApi } from '../api';
import Icon from '../components/Icon';
import { EmptyState, ErrorBox, MonthYearPicker, PageHeader, Spinner, StatCard, TypeBadge } from '../components/ui';
import { useToast } from '../context/ToastContext';
import {
  CHART_COLORS, firstDayOfMonth, formatCompact, formatDate, formatMoney, lastDayOfMonth, monthName, withPercentages,
} from '../utils/format';

export default function Reports() {
  const toast = useToast();
  const now = new Date();
  const [mode, setMode] = useState('monthly'); // 'monthly' | 'range'
  const [period, setPeriod] = useState({ month: now.getMonth() + 1, year: now.getFullYear() });
  const [range, setRange] = useState({ start: firstDayOfMonth(now), end: lastDayOfMonth(now) });

  const [report, setReport] = useState(null); // normalised report
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');

  const rangeInvalid = mode === 'range' && (!range.start || !range.end || range.start > range.end);

  const generate = async (e) => {
    e?.preventDefault();
    if (rangeInvalid) return setError('Start date must be on or before the end date.');
    setError('');
    setLoading(true);
    try {
      if (mode === 'monthly') {
        const r = await reportApi.monthly(period.month, period.year);
        setReport({
          title: `${monthName(r.month)} ${r.year}`,
          overview: r.financialOverview,
          categories: r.categoryExpenses,
          transactions: r.transactions,
          budgetUsage: r.budgetUsage,
        });
      } else {
        const r = await reportApi.range(range.start, range.end);
        setReport({
          title: `${formatDate(r.startDate)} – ${formatDate(r.endDate)}`,
          overview: r.financialOverview,
          categories: r.categoryExpense, // note: singular in the backend DTO
          transactions: r.transactions,
          budgetUsage: null,
        });
      }
    } catch (err) {
      setReport(null);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const exportExcel = async () => {
    if (rangeInvalid) return setError('Start date must be on or before the end date.');
    setExporting(true);
    try {
      if (mode === 'monthly') {
        const blob = await reportApi.exportMonthly(period.month, period.year);
        downloadBlob(blob, `monthly_report_${period.year}-${String(period.month).padStart(2, '0')}.xlsx`);
      } else {
        const blob = await reportApi.exportRange(range.start, range.end);
        downloadBlob(blob, `financial_report_${range.start}_to_${range.end}.xlsx`);
      }
      toast.success('Excel report downloaded');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setExporting(false);
    }
  };

  const categories = withPercentages(report?.categories || []);
  const overview = report?.overview || {};
  const txs = report?.transactions || [];

  return (
    <>
      <PageHeader title="Reports" subtitle="Generate a summary for a month or any date range, and export it to Excel." />

      <form className="card report-controls" onSubmit={generate}>
        <div className="segmented">
          <button type="button" className={mode === 'monthly' ? 'on' : ''} onClick={() => setMode('monthly')}>Monthly</button>
          <button type="button" className={mode === 'range' ? 'on' : ''} onClick={() => setMode('range')}>Date range</button>
        </div>

        {mode === 'monthly' ? (
          <MonthYearPicker month={period.month} year={period.year} onChange={(m, y) => setPeriod({ month: m, year: y })} />
        ) : (
          <div className="inline-fields">
            <input type="date" value={range.start} onChange={(e) => setRange({ ...range, start: e.target.value })} aria-label="Start date" required />
            <span className="muted">to</span>
            <input type="date" value={range.end} onChange={(e) => setRange({ ...range, end: e.target.value })} aria-label="End date" required />
          </div>
        )}

        <div className="spacer" />
        <button className="btn btn-primary" disabled={loading}>{loading ? 'Generating…' : 'Generate report'}</button>
        <button type="button" className="btn btn-ghost" onClick={exportExcel} disabled={exporting}>
          <Icon name="download" /> {exporting ? 'Exporting…' : 'Export Excel'}
        </button>
      </form>

      {error && <div className="mt"><ErrorBox message={error} /></div>}
      {loading && <Spinner label="Generating report…" />}

      {!loading && !report && !error && (
        <div className="card mt">
          <EmptyState title="No report generated yet" hint="Choose a period above and click “Generate report”." />
        </div>
      )}

      {!loading && report && (
        <>
          <h4 className="section-label">{report.title}</h4>
          <div className="grid grid-3">
            <StatCard label="Income" value={formatMoney(overview.totalIncome)} tone="income" icon="up" />
            <StatCard label="Expenses" value={formatMoney(overview.totalExpenses)} tone="expense" icon="down" />
            <StatCard label="Net savings" value={formatMoney(overview.netSavings)} tone={Number(overview.netSavings) < 0 ? 'expense' : 'primary'} icon="wallet" />
          </div>

          <div className="grid grid-2 mt">
            <div className="card">
              <h3>Expenses by category</h3>
              {categories.length ? (
                <>
                  <div className="chart-box">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={categories} layout="vertical" margin={{ left: 8, right: 16 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" horizontal={false} />
                        <XAxis type="number" tickFormatter={formatCompact} tick={{ fontSize: 12 }} stroke="var(--muted)" />
                        <YAxis type="category" dataKey="categoryName" width={96} tick={{ fontSize: 12 }} stroke="var(--muted)" />
                        <Tooltip formatter={(v, n, p) => [`${formatMoney(v)} (${p.payload.pct.toFixed(1)}%)`, 'Spent']} />
                        <Bar dataKey="totalSpent" radius={[0, 4, 4, 0]}>
                          {categories.map((c, i) => (
                            <Cell key={c.categoryId} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </>
              ) : (
                <EmptyState title="No expenses in this period" />
              )}
            </div>

            <div className="card">
              <h3>{report.budgetUsage ? 'Budget usage' : 'Category breakdown'}</h3>
              {report.budgetUsage ? (
                report.budgetUsage.length ? (
                  <div className="budget-list">
                    {report.budgetUsage.map((b) => {
                      const pct = Number(b.percentageUsed);
                      return (
                        <div key={b.budgetId}>
                          <div className="budget-line">
                            <span>{b.categoryName}</span>
                            <span className={b.exceeded ? 'neg' : 'muted'}>
                              {formatMoney(b.totalSpent)} / {formatMoney(b.budgetAmount)}
                            </span>
                          </div>
                          <div className="progress">
                            <div className={`progress-bar ${pct > 100 ? 'over' : pct >= 75 ? 'warn' : 'ok'}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <EmptyState title="No budgets set for this month" />
                )
              ) : categories.length ? (
                <div className="table-wrap">
                  <table>
                    <thead><tr><th>Category</th><th className="num">Spent</th><th className="num">Share</th></tr></thead>
                    <tbody>
                      {categories.map((c) => (
                        <tr key={c.categoryId}>
                          <td>{c.categoryName}</td>
                          <td className="num">{formatMoney(c.totalSpent)}</td>
                          <td className="num">{c.pct.toFixed(1)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <EmptyState title="No expenses in this period" />
              )}
            </div>
          </div>

          <div className="card mt">
            <h3>Transactions ({txs.length})</h3>
            {txs.length ? (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Date</th><th>Description</th><th>Category</th><th>Type</th><th className="num">Amount</th></tr>
                  </thead>
                  <tbody>
                    {txs.map((t) => (
                      <tr key={t.id}>
                        <td>{formatDate(t.transactionDate)}</td>
                        <td className="wrap">{t.description || <span className="muted">—</span>}</td>
                        <td>{t.categoryName}</td>
                        <td><TypeBadge type={t.type} /></td>
                        <td className={`num ${t.type === 'INCOME' ? 'pos' : 'neg'}`}>
                          {t.type === 'INCOME' ? '+' : '−'}{formatMoney(t.amount)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <EmptyState title="No transactions in this period" />
            )}
          </div>
        </>
      )}
    </>
  );
}
