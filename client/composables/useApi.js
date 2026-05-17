import { ElMessage } from 'element-plus'

export const useApi = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase

  const request = async (url, options = {}) => {
    const method = (options.method || 'GET').toUpperCase()
    const headers = { ...options.headers }
    if (['POST', 'PUT', 'PATCH'].includes(method)) {
      headers['Content-Type'] = 'application/json'
    }
    try {
      const { data, error } = await useFetch(`${baseURL}${url}`, {
        ...options,
        headers,
      })
      if (error.value) {
        ElMessage.error(error.value.message || '网络请求失败')
        throw new Error(error.value.message)
      }
      const result = data.value
      if (result.code !== 200) {
        ElMessage.error(result.message || '请求失败')
        throw new Error(result.message)
      }
      return result.data
    } catch (err) {
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
    delete: (url) => request(url, { method: 'DELETE' }),
  }
}
