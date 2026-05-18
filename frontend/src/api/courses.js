import request from './request'

export const listCourses = (params) => request.get('/courses', { params })
export const getCourse = (id) => request.get(`/courses/${id}`)
export const listAdminCourses = (params) => request.get('/courses/admin', { params })
export const getAdminCourse = (id) => request.get(`/courses/admin/${id}`)
export const createCourse = (data) => request.post('/courses/admin', data)
export const updateCourse = (id, data) => request.put(`/courses/admin/${id}`, data)
export const onSaleCourse = (id) => request.patch(`/courses/admin/${id}/on-sale`)
export const offSaleCourse = (id) => request.patch(`/courses/admin/${id}/off-sale`)
