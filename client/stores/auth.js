import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {

  const token = ref(null)
  const user = ref(null)
  const isAuthenticated = computed(() => !!token.value)

 
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

  /**
   * 用户登录方法
   * 调用后端登录接口，获取 Token 和用户信息
   * 
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @returns {boolean} - 登录是否成功
   */
  const login = async (username, password) => {
    try {
      // 创建 API 实例
      const api = useApi()
      // 调用后端登录接口 POST /api/auth/login
      const data = await api.post('/auth/login', { username, password })
      
      // 更新状态
      token.value = data.token
      user.value = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role || 'user',  // 默认角色为普通用户
      }
      
      // 持久化到 localStorage，防止页面刷新后登录状态丢失
      if (process.client) {
        localStorage.setItem('auth_token', data.token)
        localStorage.setItem('auth_user', JSON.stringify(user.value))
      }
      
      // 显示登录成功提示
      ElMessage.success('登录成功')
      return true
    } catch (err) {
      return false
    }
  }

  /**
   * 用户注册方法
   * 调用后端注册接口，创建新用户并自动登录
   * 
   * @param {string} username - 用户名
   * @param {string} password - 密码
   * @param {string} nickname - 昵称（可选，默认等于用户名）
   * @returns {boolean} - 注册是否成功
   */
  const register = async (username, password, nickname) => {
    try {
      const api = useApi()
      // 调用后端注册接口 POST /api/auth/register
      const data = await api.post('/auth/register', {
        username,
        password,
        nickname: nickname || username,  // 昵称为空时使用用户名
      })
      
      // 注册成功后自动设置登录状态
      token.value = data.token
      user.value = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role || 'user',
      }
      
      // 持久化到 localStorage
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

  /**
   * 退出登录方法
   * 清除本地状态和 localStorage 中的登录信息
   */
  const logout = () => {
    // 清除内存中的状态
    token.value = null
    user.value = null
    
    // 清除 localStorage 中的持久化数据
    if (process.client) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
    }
    
    ElMessage.success('已退出登录')
  }

  /**
   * 获取 Token（供 useApi 使用）
   * 在发送请求时自动携带 Token 到请求头
   * 
   * @returns {string|null} - 当前用户的 JWT Token
   */
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
