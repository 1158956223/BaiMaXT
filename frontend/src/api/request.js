import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 0
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('baimaxt_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (!payload || typeof payload.code === 'undefined') {
      return payload
    }
    if (payload.code !== 200) {
      const message = payload.message || '请求失败'
      ElMessage.error(message)
      throw new Error(message)
    }
    return payload.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络请求失败'
    if (status === 401) {
      const auth = useAuthStore()
      auth.clearSession(false)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
