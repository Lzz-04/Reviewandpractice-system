<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon">
          <svg viewBox="0 0 32 32" fill="none" width="40" height="40">
            <rect width="32" height="32" rx="8" fill="url(#g1)" />
            <path d="M9 14l4-4 6 6 4-4v8l-14 2V14z" fill="#fff" opacity="0.95" />
            <defs>
              <linearGradient id="g1" x1="0" y1="0" x2="32" y2="32">
                <stop stop-color="#3b5dbf" />
                <stop offset="1" stop-color="#5876d8" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="auth-title">期末刷题助手</h1>
        <p class="auth-subtitle">登录你的账号，继续学习之旅</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="auth-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <NuxtLink to="/register" class="auth-link">立即注册</NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Lock } from '@element-plus/icons-vue'

definePageMeta({
  layout: false,
})

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref(null)
const loading = ref(false)

// 如果已登录，根据角色跳转
if (authStore.isAuthenticated) {
  const target = authStore.user?.role === 'admin' ? '/admin' : '/'
  router.replace(target)
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const success = await authStore.login(form.username, form.password)
  loading.value = false

  if (success) {
    const isAdmin = authStore.user?.role === 'admin'
    const redirect = route.query.redirect || (isAdmin ? '/admin' : '/')
    router.replace(redirect)
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
  padding: 20px;
}

.auth-card {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 8px 40px rgba(22, 27, 43, 0.08), 0 2px 8px rgba(22, 27, 43, 0.04);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-icon {
  margin-bottom: 16px;
  display: inline-block;
}

.auth-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
}

.auth-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.auth-form {
  margin-bottom: 8px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.auth-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  height: 44px;
  background: linear-gradient(135deg, #3b5dbf, #5876d8);
  border: none;
}

.auth-btn:hover {
  background: linear-gradient(135deg, #3450a8, #4b68c8);
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #94a3b8;
}

.auth-link {
  color: #3b5dbf;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
