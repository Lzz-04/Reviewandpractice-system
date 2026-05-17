<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学习统计</h2>
      <p>可视化你的学习数据，了解自己的进步</p>
    </div>

    <!-- 概览 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in statItems" :key="s.label">
        <div class="stat-icon" :style="{ background: s.bg, color: s.color }">
          <el-icon :size="20"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ s.value }}{{ s.suffix }}</div>
          <div class="stat-desc">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="chart-section">
      <div class="chart-panel">
        <div class="chart-header">正确率趋势（近7天）</div>
        <div ref="accuracyChartRef" class="chart-body"></div>
        <el-empty v-if="!accuracyData.length" description="暂无数据" :image-size="60" />
      </div>

      <div class="chart-panel">
        <div class="chart-header">每日答题量（近30天）</div>
        <div ref="dailyChartRef" class="chart-body"></div>
        <el-empty v-if="!dailyData.length" description="暂无数据" :image-size="60" />
      </div>
    </div>

    <!-- 各科目进度 -->
    <div class="progress-panel">
      <div class="panel-header">各科目学习进度</div>
      <div v-if="subjectProgress.length > 0">
        <div v-for="item in subjectProgress" :key="item.subjectId" class="progress-row">
          <div class="progress-top">
            <span class="progress-name">{{ item.subjectName }}</span>
            <span class="progress-acc" :class="accClass(item.accuracy)">正确率 {{ item.accuracy }}%</span>
          </div>
          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{
                width: (item.totalQuestions > 0 ? Math.min(Math.round((item.answeredCount / item.totalQuestions) * 100), 100) : 0) + '%',
                background: accGradient(item.accuracy),
              }"
            ></div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { Document, DataAnalysis, Tickets, Timer } from '@element-plus/icons-vue'

const api = useApi()
const overview = ref({})
const accuracyData = ref([])
const dailyData = ref([])
const subjectProgress = ref([])
const accuracyChartRef = ref(null)
const dailyChartRef = ref(null)
let accuracyChart = null
let dailyChart = null

const statItems = computed(() => [
  { label: '题库总题数', value: overview.value.totalQuestions || 0, suffix: '', icon: Document, bg: '#e9eefa', color: '#3b5dbf' },
  { label: '累计答题', value: overview.value.totalAnswered || 0, suffix: '', icon: DataAnalysis, bg: '#ecfdf5', color: '#15803d' },
  { label: '整体正确率', value: overview.value.overallAccuracy || 0, suffix: '%', icon: Tickets, bg: '#fef6ee', color: '#e0781a' },
  { label: '今日答题', value: overview.value.todayAnswered || 0, suffix: '', icon: Timer, bg: '#f3f5fc', color: '#5876d8' },
])

onMounted(async () => {
  try {
    const [ov, acc, daily, subj] = await Promise.all([
      api.get('/stats/overview'),
      api.get('/stats/accuracy/trend', { days: 7 }),
      api.get('/stats/daily/activity', { days: 30 }),
      api.get('/stats/subject/progress'),
    ])
    overview.value = ov
    accuracyData.value = acc || []
    dailyData.value = daily || []
    subjectProgress.value = subj || []

    await nextTick()
    if (accuracyData.value.length > 0) initAccuracyChart()
    if (dailyData.value.length > 0) initDailyChart()
  } catch {}
})

function initAccuracyChart() {
  if (!accuracyChartRef.value) return
  accuracyChart = echarts.init(accuracyChartRef.value)
  accuracyChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e8e5df', textStyle: { color: '#334155' } },
    grid: { left: 40, right: 16, top: 20, bottom: 24 },
    xAxis: {
      type: 'category',
      data: accuracyData.value.map(d => d.date),
      axisLine: { lineStyle: { color: '#e8e5df' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
    },
    yAxis: {
      type: 'value', max: 100,
      axisLabel: { formatter: '{value}%', color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0ede7', type: 'dashed' } },
    },
    series: [{
      data: accuracyData.value.map(d => parseFloat(d.accuracy) || 0),
      type: 'line', smooth: true,
      symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#3b5dbf', width: 2.5 },
      itemStyle: { color: '#3b5dbf' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(59,93,191,0.12)' },
        { offset: 1, color: 'rgba(59,93,191,0.01)' },
      ])},
    }],
  })
}

function initDailyChart() {
  if (!dailyChartRef.value) return
  dailyChart = echarts.init(dailyChartRef.value)
  dailyChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e8e5df', textStyle: { color: '#334155' } },
    grid: { left: 40, right: 16, top: 20, bottom: 24 },
    xAxis: {
      type: 'category',
      data: dailyData.value.map(d => d.date),
      axisLine: { lineStyle: { color: '#e8e5df' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0ede7', type: 'dashed' } },
    },
    series: [{
      data: dailyData.value.map(d => d.count || 0),
      type: 'bar',
      barWidth: 14,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#5876d8' },
          { offset: 1, color: '#3b5dbf' },
        ]),
        borderRadius: [6, 6, 0, 0],
      },
    }],
  })
}

function accClass(v) {
  if (v >= 80) return 'acc-high'
  if (v >= 60) return 'acc-mid'
  return 'acc-low'
}

function accGradient(v) {
  if (v >= 80) return 'linear-gradient(135deg, #15803d, #22c55e)'
  if (v >= 60) return 'linear-gradient(135deg, #d97706, #f59e0b)'
  return 'linear-gradient(135deg, #3b5dbf, #5876d8)'
}

onUnmounted(() => {
  accuracyChart?.dispose()
  dailyChart?.dispose()
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.1;
}

.stat-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.chart-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(420px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.chart-panel {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
}

.chart-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
  font-weight: 650;
  font-size: 15px;
  color: #1e293b;
}

.chart-body {
  height: 300px;
}

.progress-panel {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
  font-weight: 650;
  font-size: 15px;
  color: #1e293b;
}

.progress-row {
  padding: 16px 20px;
  border-bottom: 1px solid #fafaf8;
}

.progress-row:last-child { border-bottom: none; }

.progress-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-name {
  font-size: 14px;
  font-weight: 550;
  color: #334155;
}

.progress-acc {
  font-size: 13px;
  font-weight: 600;
}

.progress-acc.acc-high { color: #15803d; }
.progress-acc.acc-mid { color: #d97706; }
.progress-acc.acc-low { color: #3b5dbf; }

.progress-track {
  height: 8px;
  background: #f0ede7;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
