<template>
  <div class="app-shell">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" fill="none" width="28" height="28">
            <rect width="32" height="32" rx="8" fill="url(#g1)"/>
            <path d="M9 14l4-4 6 6 4-4v8l-14 2V14z" fill="#fff" opacity="0.95"/>
            <defs><linearGradient id="g1" x1="0" y1="0" x2="32" y2="32"><stop stop-color="#3b5dbf"/><stop offset="1" stop-color="#5876d8"/></linearGradient></defs>
          </svg>
        </div>
        <span class="brand-text">期末刷题助手</span>
      </div>

      <nav class="sidebar-nav">
        <NuxtLink to="/" class="nav-item" :class="{ active: route.path === '/' }">
          <span class="nav-icon"><HomeFilled /></span>
          <span class="nav-label">首页仪表盘</span>
        </NuxtLink>
        <NuxtLink to="/subjects" class="nav-item" :class="{ active: route.path.startsWith('/subject') || route.path === '/subjects' }">
          <span class="nav-icon"><Collection /></span>
          <span class="nav-label">科目管理</span>
        </NuxtLink>
        <NuxtLink to="/exam" class="nav-item" :class="{ active: route.path.startsWith('/exam') }">
          <span class="nav-icon"><Tickets /></span>
          <span class="nav-label">模拟考试</span>
        </NuxtLink>
        <NuxtLink to="/wrongbook" class="nav-item" :class="{ active: route.path === '/wrongbook' }">
          <span class="nav-icon"><WarningFilled /></span>
          <span class="nav-label">错题本</span>
        </NuxtLink>
        <NuxtLink to="/stats" class="nav-item" :class="{ active: route.path === '/stats' }">
          <span class="nav-icon"><DataAnalysis /></span>
          <span class="nav-label">学习统计</span>
        </NuxtLink>
        <NuxtLink v-if="authStore.user?.role === 'admin'" to="/admin" class="nav-item" :class="{ active: route.path === '/admin' }">
          <span class="nav-icon"><Setting /></span>
          <span class="nav-label">管理后台</span>
        </NuxtLink>
      </nav>

      <!-- 用户信息区域 -->
      <div class="sidebar-footer">
        <div class="user-info" v-if="authStore.isAuthenticated && authStore.user">
          <div class="user-avatar">
            <el-avatar :size="36" :icon="UserFilled" />
          </div>
          <div class="user-detail">
            <span class="user-name">{{ authStore.user.nickname || authStore.user.username }}</span>
          </div>
          <button class="logout-btn" title="修改密码" @click="showPwdDialog = true">
            <el-icon><Lock /></el-icon>
          </button>
          <el-popconfirm
            title="确定要退出登录吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleLogout"
          >
            <template #reference>
              <button class="logout-btn" title="退出登录">
                <el-icon><SwitchButton /></el-icon>
              </button>
            </template>
          </el-popconfirm>
        </div>
        <div class="footer-tip" v-else>
          <span class="tip-dot"></span>
          专注学习，每天进步
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-breadcrumb">
          <el-icon class="breadcrumb-home"><HomeFilled /></el-icon>
          <span class="breadcrumb-sep">/</span>
          <span class="breadcrumb-current">{{ pageTitle }}</span>
        </div>
        <div class="topbar-right" v-if="authStore.isAuthenticated && authStore.user">
          <span class="greeting-text">👋 你好，{{ authStore.user.nickname || authStore.user.username }}</span>
        </div>
      </header>
      <main class="main-content">
        <slot />
      </main>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPwdDialog" title="修改密码" width="400px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwdDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePwd" :loading="pwdLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  HomeFilled, Collection, Tickets, WarningFilled, DataAnalysis, SwitchButton, UserFilled, Setting, Lock,
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// 初始化 auth store（从 localStorage 恢复登录状态）
if (process.client) {
  authStore.init()
}

const pageTitle = computed(() => {
  const map = {
    '/': '仪表盘',
    '/subjects': '科目管理',
    '/exam': '模拟考试',
    '/wrongbook': '错题本',
    '/stats': '学习统计',
    '/admin': '管理后台',
  }
  if (route.path.startsWith('/subject/')) return '科目详情'
  if (route.path.startsWith('/practice/')) return '刷题练习'
  if (route.path.startsWith('/exam/start/')) return '考试中'
  if (route.path.startsWith('/exam/result/')) return '考试结果'
  return map[route.path] || ''
})

const showPwdDialog = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: (r, v, cb) => v !== pwdForm.newPassword ? cb('两次密码不一致') : cb(), trigger: 'blur' }],
}

async function handleChangePwd() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    const api = useApi()
    await api.put('/auth/password', { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    showPwdDialog.value = false
    authStore.logout()
    ElMessage.success('密码修改成功，请重新登录')
    router.push('/login')
  } catch {} finally { pwdLoading.value = false }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== Sidebar ===== */
.sidebar {
  width: 230px;
  min-width: 230px;
  background: linear-gradient(180deg, #141829 0%, #1a2035 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  border-right: 1px solid rgba(255,255,255,0.04);
}

.sidebar::after {
  content: '';
  position: absolute;
  top: 0; right: 0; bottom: 0; left: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(59,93,191,0.08) 0%, transparent 70%);
  pointer-events: none;
}

/* Brand */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.brand-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.brand-text {
  font-size: 17px;
  font-weight: 700;
  color: #e8ecf4;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 11px 14px;
  border-radius: 8px;
  color: #8e98b0;
  text-decoration: none;
  font-size: 14.5px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-item:hover {
  color: #c8d0e0;
  background: rgba(255,255,255,0.04);
}

.nav-item.active {
  color: #ffffff;
  background: rgba(59,93,191,0.25);
  box-shadow: 0 0 0 1px rgba(59,93,191,0.3);
}

.nav-item.active .nav-icon {
  color: #90a8f0;
}

.nav-icon {
  font-size: 18px;
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: color 0.2s;
}

.nav-label {
  white-space: nowrap;
}

/* Footer - 用户信息 */
.sidebar-footer {
  padding: 14px 16px;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #3b5dbf, #5876d8);
  font-size: 16px;
}

.user-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.user-name {
  font-size: 13.5px;
  font-weight: 600;
  color: #d8dde8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}



.logout-btn {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255,255,255,0.06);
  border-radius: 6px;
  color: #6b7390;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 16px;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.footer-tip {
  font-size: 12px;
  color: #5a6380;
  display: flex;
  align-items: center;
  gap: 7px;
}

.tip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 6px rgba(34,197,94,0.4);
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ===== Main Area ===== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--c-bg, #f4f2ee);
}

.topbar {
  height: 52px;
  min-height: 52px;
  background: #fff;
  border-bottom: 1px solid var(--c-border, #e7e5e0);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 2px rgba(22,27,43,0.03);
  z-index: 5;
}

.topbar-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--c-text-secondary, #64748b);
}

.breadcrumb-home {
  font-size: 15px;
  color: var(--c-text-muted, #94a3b8);
}

.breadcrumb-sep {
  color: #d4d0c8;
  font-weight: 300;
}

.breadcrumb-current {
  color: var(--c-text, #1e293b);
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
}

.greeting-text {
  font-size: 13px;
  color: #94a3b8;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
