import request from './request'

export const getEquipments = (params) => request.get('/equipments', { params })
export const addEquipment = (data) => request.post('/equipments', data)
export const updateEquipment = (id, data) => request.put(`/equipments/${id}`, data)
export const rentEquipment = (data) => request.post('/rentals', data)
export const getMyRentals = (params) => request.get('/rentals', { params })
export const getAllRentals = (params) => request.get('/admin/rentals', { params })
export const returnEquipment = (id) => request.put(`/rentals/${id}/return`)
