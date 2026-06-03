import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(null)
  const user = ref(null)
  const isAuthenticated = computed(() => !!token.value)

  // 从 localStorage 恢复登录状态
  const init = () => {
    if (process.client) {
      const savedToken = localStorage.getItem('auth_token')
      const savedUser = localStorage.getItem('auth_user')
      if (savedToken) {
        token.value = savedToken
        try {
          user.value = JSON.parse(savedUser)
        } catch (e) {
          user.value = null
        }
      }
    }
  }

  // 登录
  const login = async (username, password) => {
    try {
      const api = useApi()
      const data = await api.post('/auth/login', { username, password })
      token.value = data.token
      user.value = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role || 'user',
      }
      if (process.client) {
        localStorage.setItem('auth_token', data.token)
        localStorage.setItem('auth_user', JSON.stringify(user.value))
      }
      ElMessage.success('登录成功')
      return true
    } catch (err) {
      return false
    }
  }

  // 注册
  const register = async (username, password, nickname) => {
    try {
      const api = useApi()
      const data = await api.post('/auth/register', {
        username,
        password,
        nickname: nickname || username,
      })
      token.value = data.token
      user.value = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role || 'user',
      }
      if (process.client) {
        localStorage.setItem('auth_token', data.token)
        localStorage.setItem('auth_user', JSON.stringify(user.value))
      }
      ElMessage.success('注册成功')
      return true
    } catch (err) {
      return false
    }
  }

  // 退出登录
  const logout = () => {
    token.value = null
    user.value = null
    if (process.client) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
    }
    ElMessage.success('已退出登录')
  }

  // 获取 token（供 useApi 使用）
  const getToken = () => token.value

  return {
    token,
    user,
    isAuthenticated,
    init,
    login,
    register,
    logout,
    getToken,
  }
})
