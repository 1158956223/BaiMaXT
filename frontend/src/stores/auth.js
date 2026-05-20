import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, logout as logoutApi, register as registerApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('baimaxt_token') || '',
    user: null,
    loaded: false
  }),
  getters: {
    currentUserId: (state) => state.user?.userId || state.user?.id || state.user?.user?.id || state.user?.user?.uid || null
  },
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.setSession(data)
      return data
    },
    async register(form) {
      const data = await registerApi(form)
      this.setSession(data)
      return data
    },
    async fetchMe() {
      const data = await getCurrentUser()
      this.user = data
      this.loaded = true
      return data
    },
    setSession(data) {
      this.token = data.token
      this.user = data
      this.loaded = true
      localStorage.setItem('baimaxt_token', data.token)
    },
    async logout(redirect = true) {
      if (this.token) {
        await logoutApi().catch(() => {})
      }
      this.clearSession(redirect)
    },
    clearSession(redirect = true) {
      this.token = ''
      this.user = null
      this.loaded = false
      localStorage.removeItem('baimaxt_token')
      if (redirect) {
        window.location.href = '/login'
      }
    }
  }
})
