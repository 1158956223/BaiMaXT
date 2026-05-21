import request from './request'

export const searchCourses = (params) => request.get('/search/courses', { params })
