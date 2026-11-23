import { http } from './client';

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
};

export const analyticsApi = {
  dashboard: () => http.get('/analytics/dashboard'),
};

export const transactionApi = {
  list: () => http.get('/transactions'),
  create: (data) => http.post('/transactions', data),
  update: (id, data) => http.put(`/transactions/${id}`, data),
  remove: (id) => http.del(`/transactions/${id}`),
};

export const categoryApi = {
  list: () => http.get('/categories'),
  create: (data) => http.post('/categories', data),
  update: (id, data) => http.put(`/categories/${id}`, data),
  remove: (id) => http.del(`/categories/${id}`),
};

export const budgetApi = {
  byMonth: (month, year) => http.get('/budgets/filter', { month, year }),
  usage: (month, year) => http.get('/budgets/usage', { month, year }),
  create: (data) => http.post('/budgets', data),
  update: (id, data) => http.put(`/budgets/${id}`, data),
  remove: (id) => http.del(`/budgets/${id}`),
};

export const reportApi = {
  range: (startDate, endDate) => http.get('/reports', { startDate, endDate }),
  monthly: (month, year) => http.get('/reports/monthly', { month, year }),
  exportRange: (startDate, endDate) =>
    http.blob('/reports/export/excel', { startDate, endDate }),
  exportMonthly: (month, year) =>
    http.blob('/reports/monthly/export/excel', { month, year }),
};

// Triggers a browser download for a Blob.
export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
