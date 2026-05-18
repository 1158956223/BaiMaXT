import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, register as registerApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('baimaxt_token') || '',
    user: null,
    loaded: false
  }),
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
    logout(redirect = true) {
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
