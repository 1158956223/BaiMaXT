import request from './request'

export const listUsers = () => request.get('/users')
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const enableUser = (id) => request.patch(`/users/${id}/enable`)
export const disableUser = (id) => request.patch(`/users/${id}/disable`)
