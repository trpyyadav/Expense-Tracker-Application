import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { analyticsApi } from '../api';
import useAsync from '../components/useAsync';
import { useAuth } from '../context/AuthContext';
import { EmptyState, ErrorBox, PageHeader, Spinner, StatCard, TypeBadge } from '../components/ui';
import {
  CHART_COLORS, formatCompact, formatDate, formatMoney, monthName, shortMonth, withPercentages,
} from '../utils/format';

export default function Dashboard() {
  const { user } = useAuth();
  const { data, loading, error, reload } = useAsync(() => analyticsApi.dashboard(), []);

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const overview = data.financialOverview || {};
  const month = data.monthlySummary || {};
  const categories = withPercentages(data.categoryExpenses || []);
  const trends = (data.monthlyTrends || []).map((t) => ({
    label: `${shortMonth(t.month)} ${String(t.year).slice(2)}`,
    Income: Number(t.totalIncome),
    Expenses: Number(t.totalExpenses),
  }));
  const recent = data.recentTransactions || [];
  const hasTrendData = trends.some((t) => t.Income > 0 || t.Expenses > 0);

  return (
    <>
      <PageHeader title={`Hi, ${user?.name?.split(' ')[0] || 'there'}`} subtitle="Here is how your money looks right now." />

      <h4 className="section-label">All time</h4>
      <div className="grid grid-3">
        <StatCard label="Total income" value={formatMoney(overview.totalIncome)} tone="income" icon="up" />
        <StatCard label="Total expenses" value={formatMoney(overview.totalExpenses)} tone="expense" icon="down" />
        <StatCard
          label="Net savings"
          value={formatMoney(overview.netSavings)}
          tone={Number(overview.netSavings) < 0 ? 'expense' : 'primary'}
          icon="wallet"
        />
      </div>

      <h4 className="section-label">{month.month ? `${monthName(month.month)} ${month.year}` : 'This month'}</h4>
      <div className="grid grid-3">
        <StatCard label="Income" value={formatMoney(month.totalIncome)} tone="income" />
        <StatCard label="Expenses" value={formatMoney(month.totalExpenses)} tone="expense" />
        <StatCard label="Savings" value={formatMoney(month.netSavings)} tone={Number(month.netSavings) < 0 ? 'expense' : 'primary'} />
      </div>

      <div className="grid grid-2 mt">
        <div className="card">
          <h3>Last 6 months</h3>
          {hasTrendData ? (
            <div className="chart-box">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={trends} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} stroke="var(--muted)" />
                  <YAxis tickFormatter={formatCompact} tick={{ fontSize: 12 }} stroke="var(--muted)" width={48} />
                  <Tooltip formatter={(v) => formatMoney(v)} />
                  <Legend />
                  <Bar dataKey="Income" fill="var(--income)" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Expenses" fill="var(--expense)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <EmptyState title="No activity yet" hint="Add a few transactions to see your trend." />
          )}
        </div>

        <div className="card">
          <h3>Spending by category (this month)</h3>
          {categories.length ? (
            <div className="chart-box">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={categories} dataKey="totalSpent" nameKey="categoryName" innerRadius={55} outerRadius={95} paddingAngle={2}>
                    {categories.map((c, i) => (
                      <Cell key={c.categoryId} fill={CHART_COLORS[i % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v, n, p) => [`${formatMoney(v)} (${p.payload.pct.toFixed(1)}%)`, n]} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <EmptyState title="No expenses this month" hint="Your category breakdown will appear here." />
          )}
        </div>
      </div>

      <div className="card mt">
        <div className="card-head">
          <h3>Recent transactions</h3>
          <Link to="/transactions" className="link">View all</Link>
        </div>
        {recent.length ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Description</th>
                  <th>Category</th>
                  <th>Type</th>
                  <th className="num">Amount</th>
                </tr>
              </thead>
              <tbody>
                {recent.map((t) => (
                  <tr key={t.id}>
                    <td>{formatDate(t.transactionDate)}</td>
                    <td>{t.description || <span className="muted">—</span>}</td>
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
          <EmptyState
            title="No transactions yet"
            hint="Create a category, then add your first transaction."
            action={<Link className="btn btn-primary" to="/transactions">Add transaction</Link>}
          />
        )}
      </div>
    </>
  );
}
