import { ElMessage } from 'element-plus'

export const useApi = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase

  const request = async (url, options = {}) => {
    const method = (options.method || 'GET').toUpperCase()
    const headers = { ...options.headers }

    // 自动添加 JWT Token
    if (process.client) {
      const token = localStorage.getItem('auth_token')
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
    }

    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      headers['Content-Type'] = 'application/json'
    }

    try {
      const result = await $fetch(`${baseURL}${url}`, {
        method,
        headers,
        body: options.body,
        params: options.params,
      })
      if (result.code !== 200) {
        ElMessage.error(result.message || '请求失败')
        throw new Error(result.message)
      }
      return result.data
    } catch (err) {
      // 处理 401 未认证错误
      if (err.response?.status === 401) {
        if (process.client) {
          localStorage.removeItem('auth_token')
          localStorage.removeItem('auth_user')
        }
        ElMessage.error('登录已过期，请重新登录')
        // 使用 window.location 强制跳转（避免 Nuxt router 在 composable 中的限制）
        if (process.client && !window.location.pathname.includes('/login')) {
          window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
        }
        throw err
      }

      if (err.message && !err.message.includes('请求失败')) {
        ElMessage.error(err.message || '网络请求失败')
      }
      throw err
    }
  }

  return {
    get: (url, params) => request(url, { method: 'GET', params }),
    post: (url, body) => request(url, { method: 'POST', body }),
    put: (url, body) => request(url, { method: 'PUT', body }),
    delete: (url, body) => request(url, { method: 'DELETE', body }),
  }
}
