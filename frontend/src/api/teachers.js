import request from './request'

export const listTeachers = () => request.get('/teachers')
export const listAdminTeachers = () => request.get('/teachers/admin')
export const createTeacher = (data) => request.post('/teachers/admin', data)
export const updateTeacher = (id, data) => request.put(`/teachers/admin/${id}`, data)
export const enableTeacher = (id) => request.patch(`/teachers/admin/${id}/enable`)
export const disableTeacher = (id) => request.patch(`/teachers/admin/${id}/disable`)
