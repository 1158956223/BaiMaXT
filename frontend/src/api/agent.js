import request from './request'

export const chatWithAgent = (data) => request.post('/agent/chat', data)
