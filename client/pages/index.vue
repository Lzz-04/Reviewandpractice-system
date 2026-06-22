<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>学习仪表盘</h2>
        <p>欢迎回来，继续你的复习之旅</p>
      </div>
      <div class="welcome-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.iconBg }">
          <el-icon :size="20"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="dashboard-grid">
      <button class="quick-btn primary" @click="navigateTo('/subjects')">
        <div class="q-icon"><el-icon :size="28"><Collection /></el-icon></div>
        <div class="q-info">
          <span class="q-title">科目管理</span>
          <span class="q-desc">浏览科目，管理题库</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
      <button class="quick-btn success" @click="navigateTo('/exam')">
        <div class="q-icon"><el-icon :size="28"><Tickets /></el-icon></div>
        <div class="q-info">
          <span class="q-title">模拟考试</span>
          <span class="q-desc">智能组卷，检验学习成果</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
      <button class="quick-btn warning" @click="navigateTo('/wrongbook')">
        <div class="q-icon"><el-icon :size="28"><WarningFilled /></el-icon></div>
        <div class="q-info">
          <span class="q-title">错题本</span>
          <span class="q-desc">查漏补缺，攻克薄弱环节</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Collection, Tickets, WarningFilled, ArrowRight, Document, DataAnalysis, Timer } from '@element-plus/icons-vue'

const api = useApi()
const overview = ref({})

const statCards = computed(() => [
  { label: '题库总题数', value: overview.value.totalQuestions || 0, icon: Document, iconBg: '#e9eefa' },
  { label: '已答题数', value: overview.value.totalAnswered || 0, icon: DataAnalysis, iconBg: '#ecfdf5' },
  { label: '整体正确率', value: (overview.value.overallAccuracy || 0) + '%', icon: Tickets, iconBg: '#fef6ee' },
  { label: '学习天数', value: overview.value.studyDays || 0, icon: Timer, iconBg: '#f0fdf4' },
])

function navigateTo(path) { useRouter().push(path) }

onMounted(async () => {
  try { overview.value = await api.get('/stats/overview') } catch {}
})
</script>

<style scoped>
.dashboard {
  padding: 24px 28px;
  max-width: 800px;
  margin: 0 auto;
  animation: pageIn 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes pageIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.welcome-section { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.welcome-text h2 { font-size: 26px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.welcome-text p { font-size: 14px; color: #64748b; }
.welcome-decoration { display: flex; gap: 8px; }
.deco-circle { width: 10px; height: 10px; border-radius: 50%; animation: float 3s ease-in-out infinite; }
.deco-circle.c1 { background: #3b5dbf; animation-delay: 0s; }
.deco-circle.c2 { background: #e0781a; animation-delay: 0.5s; }
.deco-circle.c3 { background: #22c55e; animation-delay: 1s; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }

.stat-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { background: #fff; border: 1px solid #e8e5df; border-radius: 12px; padding: 16px; display: flex; align-items: center; gap: 12px; box-shadow: 0 1px 3px rgba(22,27,43,0.04); }
.stat-icon { width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #3b5dbf; flex-shrink: 0; }
.stat-info { min-width: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: #1e293b; line-height: 1.1; }
.stat-label { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.dashboard-grid { display: flex; flex-direction: column; gap: 12px; }
.quick-btn { display: flex; align-items: center; gap: 18px; width: 100%; padding: 22px 24px; border: 1px solid #e8e5df; border-radius: 14px; background: #fff; cursor: pointer; text-align: left; transition: all 0.25s; box-shadow: 0 1px 3px rgba(22,27,43,0.04); }
.quick-btn:hover { transform: translateX(6px); box-shadow: 0 8px 24px rgba(22,27,43,0.08); }
.quick-btn.primary:hover { border-color: #3b5dbf; background: #f5f6fc; }
.quick-btn.success:hover { border-color: #15803d; background: #ecfdf5; }
.quick-btn.warning:hover { border-color: #d97706; background: #fffbeb; }
.q-icon { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.quick-btn.primary .q-icon { background: #e9eefa; color: #3b5dbf; }
.quick-btn.success .q-icon { background: #ecfdf5; color: #15803d; }
.quick-btn.warning .q-icon { background: #fef6ee; color: #d97706; }
.q-info { flex: 1; }
.q-title { display: block; font-size: 16px; font-weight: 650; color: #1e293b; margin-bottom: 3px; }
.q-desc { display: block; font-size: 13px; color: #94a3b8; }
.q-arrow { font-size: 18px; color: #cbd5e1; flex-shrink: 0; transition: transform 0.2s; }
.quick-btn:hover .q-arrow { transform: translateX(3px); color: #94a3b8; }
</style>
