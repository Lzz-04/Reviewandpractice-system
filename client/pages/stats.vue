<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学习统计</h2>
      <p>查看你的学习数据和答题趋势</p>
    </div>

    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.iconBg }">
          <el-icon :size="22"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- AI 分析 -->
    <div class="ai-panel">
      <div class="ai-header">
        <span>🤖 AI 学习分析</span>
        <el-button type="primary" size="small" @click="loadAIAnalysis" :loading="loadingAI" :disabled="!hasData">
          {{ aiAnalysis ? '重新分析' : '生成分析' }}
        </el-button>
      </div>
      <div v-if="loadingAI" class="ai-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>AI 正在分析你的学习数据...</span>
      </div>
      <div v-else-if="aiAnalysis" class="ai-content">{{ aiAnalysis }}</div>
      <div v-else class="ai-empty">
        <span v-if="!hasData">暂无答题数据，先去做几道题吧</span>
        <span v-else>点击"生成分析"获取 AI 学习建议</span>
      </div>
    </div>

    <!-- AI 复习计划 -->
    <div class="ai-panel">
      <div class="ai-header">
        <span>📅 AI 复习计划</span>
        <el-button type="primary" size="small" @click="loadStudyPlan" :loading="loadingPlan">
          {{ studyPlan ? '重新生成' : '生成计划' }}
        </el-button>
      </div>
      <div v-if="loadingPlan" class="ai-loading"><el-icon class="is-loading"><Loading /></el-icon> AI 正在制定复习计划...</div>
      <div v-else-if="studyPlan" class="ai-content">{{ studyPlan }}</div>
      <div v-else class="ai-empty">点击生成 AI 定制的复习计划</div>
    </div>

    <!-- 科目进度 -->
    <div class="list-panel">
      <div class="list-header">各科目答题进度</div>
      <div v-if="subjectProgress.length > 0" class="panel-body">
        <div v-for="item in subjectProgress" :key="item.subjectId" class="progress-row">
          <div class="progress-top">
            <span class="progress-name">{{ item.subjectName }}</span>
            <span class="progress-pct" :class="pctClass(item.accuracy)">{{ item.accuracy }}%</span>
          </div>
          <div class="progress-bar-wrap">
            <div class="progress-bar-fill"
              :style="{ width: Math.min(Math.round(item.accuracy), 100) + '%', background: progressGradient(item.accuracy) }"
            ></div>
          </div>
          <div class="progress-meta">已答 {{ item.answeredCount }} / {{ item.totalQuestions }} 题</div>
        </div>
      </div>
      <el-empty v-else description="暂无答题数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { Document, DataAnalysis, Tickets, Timer, Loading } from '@element-plus/icons-vue'

const api = useApi()
const overview = ref({})
const subjectProgress = ref([])
const loadingAI = ref(false)
const loadingPlan = ref(false)
const aiAnalysis = ref('')
const studyPlan = ref('')

const hasData = computed(() => (overview.value.totalAnswered || 0) > 0)

const statCards = computed(() => [
  { label: '题库总题数', value: overview.value.totalQuestions || 0, icon: Document, iconBg: '#e9eefa' },
  { label: '已答题数', value: overview.value.totalAnswered || 0, icon: DataAnalysis, iconBg: '#ecfdf5' },
  { label: '整体正确率', value: (overview.value.overallAccuracy || 0) + '%', icon: Tickets, iconBg: '#fef6ee' },
  { label: '今日答题', value: overview.value.todayAnswered || 0, icon: Timer, iconBg: '#f3f5fc' },
  { label: '学习天数', value: overview.value.studyDays || 0, icon: Timer, iconBg: '#f0fdf4' },
])

function pctClass(v) { if (v >= 80) return 'pct-high'; if (v >= 60) return 'pct-mid'; return 'pct-low' }
function progressGradient(v) { if (v >= 80) return 'linear-gradient(135deg, #15803d, #22c55e)'; if (v >= 60) return 'linear-gradient(135deg, #d97706, #f59e0b)'; return 'linear-gradient(135deg, #3b5dbf, #5b7de0)' }

async function loadOverview() {
  try { overview.value = await api.get('/stats/overview') } catch {}
}

async function loadSubjectProgress() {
  try { subjectProgress.value = await api.get('/stats/subject/progress') } catch {}
}

async function loadAIAnalysis() {
  loadingAI.value = true
  try { aiAnalysis.value = await api.get('/stats/ai-analysis') } catch {} finally { loadingAI.value = false }
}

async function loadStudyPlan() {
  loadingPlan.value = true
  try { studyPlan.value = await api.get('/stats/study-plan') } catch {} finally { loadingPlan.value = false }
}

onMounted(async () => {
  await Promise.all([loadOverview(), loadSubjectProgress()])
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon { width: 44px; height: 44px; border-radius: 11px; display: flex; align-items: center; justify-content: center; color: #3b5dbf; flex-shrink: 0; }
.stat-info { min-width: 0; }
.stat-value { font-size: 26px; font-weight: 700; color: #1e293b; line-height: 1.1; }
.stat-label { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* AI Panel */
.ai-panel { background: #fff; border: 1px solid #e8e5df; border-radius: 12px; overflow: hidden; margin-bottom: 20px; }
.ai-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-bottom: 1px solid #f0ede7; font-weight: 650; font-size: 15px; color: #1e293b; }
.ai-loading { display: flex; align-items: center; gap: 10px; padding: 24px; color: #94a3b8; font-size: 14px; }
.ai-content { padding: 20px; font-size: 14px; line-height: 1.8; color: #334155; white-space: pre-wrap; }
.ai-empty { padding: 24px; text-align: center; color: #94a3b8; font-size: 14px; }

.list-panel { background: #fff; border: 1px solid #e8e5df; border-radius: 12px; overflow: hidden; }
.list-header { padding: 14px 18px; border-bottom: 1px solid #f0ede7; font-weight: 650; font-size: 14px; color: #1e293b; }
.panel-body { padding: 18px; }

.progress-row { margin-bottom: 16px; }
.progress-row:last-child { margin-bottom: 0; }
.progress-top { display: flex; justify-content: space-between; margin-bottom: 6px; }
.progress-name { font-size: 14px; font-weight: 550; color: #334155; }
.progress-pct { font-size: 13px; font-weight: 650; }
.pct-high { color: #15803d; } .pct-mid { color: #d97706; } .pct-low { color: #3b5dbf; }
.progress-bar-wrap { height: 8px; background: #f0ede7; border-radius: 10px; overflow: hidden; }
.progress-bar-fill { height: 100%; border-radius: 10px; transition: width 0.6s; }
.progress-meta { font-size: 12px; color: #94a3b8; margin-top: 4px; }
</style>
