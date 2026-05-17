<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>学习仪表盘</h2>
        <p>欢迎回来，继续你的期末复习之旅</p>
      </div>
      <div class="welcome-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in stats" :key="s.label">
        <div class="stat-icon" :style="{ background: s.iconBg }">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 内容双栏 -->
    <div class="dashboard-grid">
      <!-- 科目进度 -->
      <div class="card-panel">
        <div class="panel-header">
          <span class="panel-title">各科目学习进度</span>
        </div>
        <div class="panel-body" v-if="subjectProgress.length > 0">
          <div v-for="item in subjectProgress" :key="item.subjectId" class="progress-row">
            <div class="progress-top">
              <span class="progress-name">{{ item.subjectName }}</span>
              <span class="progress-pct" :class="pctClass(item.accuracy)">
                {{ item.accuracy }}%
              </span>
            </div>
            <div class="progress-bar-wrap">
              <div
                class="progress-bar-fill"
                :style="{
                  width: (item.totalQuestions > 0 ? Math.min(Math.round((item.answeredCount / item.totalQuestions) * 100), 100) : 0) + '%',
                  background: progressGradient(item.accuracy),
                }"
              ></div>
            </div>
            <div class="progress-meta">
              {{ item.answeredCount }} / {{ item.totalQuestions }} 题
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无学习数据" :image-size="80" />
      </div>

      <!-- 快捷入口 -->
      <div class="card-panel">
        <div class="panel-header">
          <span class="panel-title">快捷入口</span>
        </div>
        <div class="panel-body quick-actions">
          <button class="quick-btn primary" @click="navigateTo('/subjects')">
            <el-icon><Collection /></el-icon>
            <span>选择科目</span>
            <small>开始刷题练习</small>
          </button>
          <button class="quick-btn success" @click="navigateTo('/exam')">
            <el-icon><Tickets /></el-icon>
            <span>模拟考试</span>
            <small>检验学习成果</small>
          </button>
          <button class="quick-btn warning" @click="navigateTo('/wrongbook')">
            <el-icon><WarningFilled /></el-icon>
            <span>错题本</span>
            <small>查漏补缺</small>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Collection, Tickets, WarningFilled, DataAnalysis, Document, Timer } from '@element-plus/icons-vue'

const api = useApi()
const overview = ref({})
const subjectProgress = ref([])

const stats = computed(() => [
  { label: '题库总题数', value: overview.value.totalQuestions || 0, icon: Document, iconBg: '#e9eefa' },
  { label: '已答题数', value: overview.value.totalAnswered || 0, icon: DataAnalysis, iconBg: '#ecfdf5' },
  { label: '整体正确率', value: (overview.value.overallAccuracy || 0) + '%', icon: Tickets, iconBg: '#fef6ee' },
  { label: '今日答题', value: overview.value.todayAnswered || 0, icon: Timer, iconBg: '#f3f5fc' },
])

function pctClass(v) {
  if (v >= 80) return 'pct-high'
  if (v >= 60) return 'pct-mid'
  return 'pct-low'
}

function progressGradient(v) {
  if (v >= 80) return 'linear-gradient(135deg, #15803d, #22c55e)'
  if (v >= 60) return 'linear-gradient(135deg, #d97706, #f59e0b)'
  return 'linear-gradient(135deg, #3b5dbf, #5b7de0)'
}

onMounted(async () => {
  try {
    overview.value = await api.get('/stats/overview')
    subjectProgress.value = await api.get('/stats/subject/progress')
  } catch {}
})
</script>

<style scoped>
.dashboard {
  padding: 24px 28px;
  max-width: 1200px;
  margin: 0 auto;
  animation: pageIn 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Welcome */
.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.welcome-text h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.3px;
  margin-bottom: 4px;
}

.welcome-text p {
  font-size: 14.5px;
  color: #64748b;
}

.welcome-decoration {
  display: flex;
  gap: 8px;
}

.deco-circle {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: float 3s ease-in-out infinite;
}

.deco-circle.c1 { background: #3b5dbf; animation-delay: 0s; }
.deco-circle.c2 { background: #e0781a; animation-delay: 0.5s; }
.deco-circle.c3 { background: #22c55e; animation-delay: 1s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* Stat Cards */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.05);
  transition: box-shadow 0.2s, transform 0.2s;
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(22,27,43,0.08);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b5dbf;
  flex-shrink: 0;
}

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1e293b;
  line-height: 1.1;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 3px;
}

/* Dashboard Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 16px;
  align-items: start;
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

/* Panels */
.card-panel {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.05);
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
}

.panel-title {
  font-weight: 650;
  font-size: 15px;
  color: #1e293b;
}

.panel-body {
  padding: 20px;
}

/* Progress Rows */
.progress-row {
  margin-bottom: 18px;
}

.progress-row:last-child {
  margin-bottom: 0;
}

.progress-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-name {
  font-size: 14px;
  font-weight: 550;
  color: #334155;
}

.progress-pct {
  font-size: 13px;
  font-weight: 650;
}

.progress-pct.pct-high { color: #15803d; }
.progress-pct.pct-mid { color: #d97706; }
.progress-pct.pct-low { color: #3b5dbf; }

.progress-bar-wrap {
  height: 8px;
  background: #f0ede7;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 5px;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid #e8e5df;
  border-radius: 10px;
  background: #fafaf8;
  cursor: pointer;
  text-align: left;
  font-size: 14.5px;
  font-weight: 550;
  color: #334155;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.quick-btn:hover {
  transform: translateX(4px);
}

.quick-btn.primary:hover {
  border-color: #3b5dbf;
  background: #f3f5fc;
  color: #3b5dbf;
}

.quick-btn.success:hover {
  border-color: #15803d;
  background: #ecfdf5;
  color: #15803d;
}

.quick-btn.warning:hover {
  border-color: #d97706;
  background: #fffbeb;
  color: #d97706;
}

.quick-btn small {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  font-weight: 400;
  margin-left: auto;
}
</style>
