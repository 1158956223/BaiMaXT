import request from './request'

export const listCategories = () => request.get('/course-categories')
export const listAdminCategories = () => request.get('/course-categories/admin')
export const createCategory = (data) => request.post('/course-categories/admin', data)
export const updateCategory = (id, data) => request.put(`/course-categories/admin/${id}`, data)
export const enableCategory = (id) => request.patch(`/course-categories/admin/${id}/enable`)
export const disableCategory = (id) => request.patch(`/course-categories/admin/${id}/disable`)
