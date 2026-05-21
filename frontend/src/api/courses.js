import request from './request'

export const listCourses = (params) => request.get('/courses', { params })
export const getCourse = (id) => request.get(`/courses/${id}`)
export const listAdminCourses = (params) => request.get('/courses/admin', { params })
export const getAdminCourse = (id) => request.get(`/courses/admin/${id}`)
export const createCourse = (data) => request.post('/courses/admin', data)
export const updateCourse = (id, data) => request.put(`/courses/admin/${id}`, data)
export const onSaleCourse = (id) => request.patch(`/courses/admin/${id}/on-sale`)
export const offSaleCourse = (id) => request.patch(`/courses/admin/${id}/off-sale`)
export const listTeacherCourses = (params) => request.get('/courses/teacher', { params })
export const getTeacherCourse = (id) => request.get(`/courses/teacher/${id}`)
export const createTeacherCourse = (data) => request.post('/courses/teacher', data)
export const updateTeacherCourse = (id, data) => request.put(`/courses/teacher/${id}`, data)
export const deleteTeacherCourse = (id) => request.delete(`/courses/teacher/${id}`)
export const uploadTeacherCourseCover = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/courses/teacher/covers', formData)
}
