import request from './request'

export const createPayment = (data) => request.post('/payments', data)
export const listMyPayments = (userId) => request.get('/payments/my', { params: { userId } })
export const getPayment = (payNo, userId) => request.get(`/payments/${payNo}`, { params: { userId } })
export const mockPaymentSuccess = (payNo, userId) => request.post(`/payments/${payNo}/mock-success`, null, { params: { userId } })
export const mockPaymentFail = (payNo, userId) => request.post(`/payments/${payNo}/mock-fail`, null, { params: { userId } })
export const listAdminPayments = (params) => request.get('/payments/admin', { params })
