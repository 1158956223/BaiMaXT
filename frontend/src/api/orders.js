import request from './request'

export const createOrder = (data) => request.post('/orders', data)
export const listMyOrders = (userId) => request.get('/orders/my', { params: { userId } })
export const listMyCourses = (userId) => request.get('/orders/my-courses', { params: { userId } })
export const listTeacherEnrollments = (params) => request.get('/orders/teacher/enrollments', { params })
export const getTeacherStats = () => request.get('/orders/teacher/stats')
export const getOrder = (id, userId) => request.get(`/orders/${id}`, { params: { userId } })
export const cancelOrder = (id, userId) => request.patch(`/orders/${id}/cancel`, null, { params: { userId } })
export const mockPayOrder = (id, userId) => request.patch(`/orders/${id}/mock-pay`, null, { params: { userId } })
export const listAdminOrders = (params) => request.get('/orders/admin', { params })
