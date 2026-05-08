import request from './request'

export const getVenues = (params) => request.get('/venues', { params })
export const getAllVenues = () => request.get('/venues/all')
export const getVenue = (id) => request.get(`/venues/${id}`)
export const getSchedule = (id, date) => request.get(`/venues/${id}/schedule`, { params: { date } })
export const addVenue = (data) => request.post('/venues', data)
export const updateVenue = (id, data) => request.put(`/venues/${id}`, data)
export const updateVenueStatus = (id, data) => request.put(`/venues/${id}/status`, data)
