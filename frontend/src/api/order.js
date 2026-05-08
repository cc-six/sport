import request from './request'

export const createOrder = (data) => request.post('/orders', data)
export const getMyOrders = (params) => request.get('/orders', { params })
export const getOrder = (id) => request.get(`/orders/${id}`)
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)
export const payOrder = (id) => request.put(`/orders/${id}/pay`)
export const getAllOrders = (params) => request.get('/admin/orders', { params })
