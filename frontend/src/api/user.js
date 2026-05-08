import request from './request'

export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserRole = (id, data) => request.put(`/admin/users/${id}/role`, data)
