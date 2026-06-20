/**
 * 全局路由守卫：未登录用户强制跳转登录页
 */
export default defineNuxtRouteMiddleware((to) => {
  // 白名单路由（无需登录即可访问）
  const publicRoutes = ['/login', '/register']

  // 客户端才进行登录状态检查
  if (process.client) {
    const token = localStorage.getItem('auth_token')

    // 未登录，且不在白名单中 → 跳转登录页
    if (!token && !publicRoutes.includes(to.path)) {
      return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
    }

    // 已登录，访问登录/注册页 → 跳转首页
    if (token && publicRoutes.includes(to.path)) {
      return navigateTo('/')
    }

    // 管理员只能访问管理后台，不允许进入用户端页面
    if (token && to.path !== '/admin' && !publicRoutes.includes(to.path)) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        if (payload.role === 'admin') {
          return navigateTo('/admin')
        }
      } catch {}
    }
  }
})
