<template>
  <div class="page-container">
    <div v-if="result">
      <el-page-header @back="navigateTo('/exam')" class="result-header">
        <template #content><span>考试结果</span></template>
      </el-page-header>

      <!-- 成绩概览 -->
      <div class="score-overview">
        <div class="score-ring" :class="scoreLevel">
          <svg viewBox="0 0 120 120" class="ring-svg">
            <circle cx="60" cy="60" r="52" fill="none" stroke="#e8e5df" stroke-width="6"/>
            <circle
              cx="60" cy="60" r="52" fill="none"
              :stroke="scoreColor"
              stroke-width="6"
              stroke-linecap="round"
              :stroke-dasharray="326.7"
              :stroke-dashoffset="326.7 - (326.7 * result.score / 100)"
              transform="rotate(-90 60 60)"
              class="ring-fill"
            />
          </svg>
          <div class="ring-content">
            <span class="ring-score">{{ result.score }}</span>
            <span class="ring-unit">分</span>
          </div>
        </div>
        <div class="score-info">
          <h3>{{ result.examTitle }}</h3>
          <div class="score-meta-grid">
            <div class="meta-item">
              <div class="meta-num">{{ result.totalQuestions }}</div>
              <div class="meta-text">总题数</div>
            </div>
            <div class="meta-item correct">
              <div class="meta-num">{{ result.correctCount }}</div>
              <div class="meta-text">正确</div>
            </div>
            <div class="meta-item wrong">
              <div class="meta-num">{{ result.wrongCount }}</div>
              <div class="meta-text">错误</div>
            </div>
            <div class="meta-item">
              <div class="meta-num">{{ formatDuration(result.durationUsed) }}</div>
              <div class="meta-text">用时</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 逐题详情 -->
      <div class="detail-section" v-if="result.questions && result.questions.length > 0">
        <div class="detail-header">答题详情</div>
        <div v-for="item in result.questions" :key="item.questionId" class="detail-card">
          <div class="detail-top">
            <span class="detail-idx">第 {{ item.questionIndex }} 题</span>
            <div class="detail-tags">
              <span class="tag" :class="item.isCorrect ? 'tag-ok' : 'tag-err'">
                {{ item.isCorrect ? '正确' : '错误' }}
              </span>
              <span class="tag tag-type">{{ { single: '单选', multiple: '多选', judge: '判断' }[item.type] }}</span>
            </div>
          </div>
          <div class="detail-content">{{ item.content }}</div>
          <div class="detail-answer">
            <p v-if="item.selectedAnswer">
              你的答案：<span :class="item.isCorrect ? 'clr-ok' : 'clr-err'">{{ item.selectedAnswer }}</span>
            </p>
            <p v-else class="clr-warn">未作答</p>
            <p v-if="!item.isCorrect">正确答案：<span class="clr-ok">{{ item.correctAnswer }}</span></p>
            <p v-if="item.analysis" class="detail-analysis">
              {{ item.analysis }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- AI 考试总结 -->
    <div v-if="result" class="ai-summary-card">
      <div class="ai-summary-header">
        <span>🤖 AI 成绩分析</span>
        <el-button size="small" type="primary" @click="loadAISummary" :loading="aiSummaryLoading">
          {{ aiSummary ? '重新分析' : '生成分析' }}
        </el-button>
      </div>
      <div v-if="aiSummaryLoading" class="ai-summary-loading">AI 正在分析考试数据...</div>
      <div v-else-if="aiSummary" class="ai-summary-content">{{ aiSummary }}</div>
      <div v-else class="ai-summary-hint">点击按钮获取 AI 对本次考试的评价和建议</div>
    </div>

    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup>
const route = useRoute()
const api = useApi()
const result = ref(null)
const aiSummary = ref('')
const aiSummaryLoading = ref(false)

const scoreLevel = computed(() => {
  if (!result.value) return ''
  const s = result.value.score
  if (s >= 80) return 'high'
  if (s >= 60) return 'mid'
  return 'low'
})

const scoreColor = computed(() => {
  if (scoreLevel.value === 'high') return '#22c55e'
  if (scoreLevel.value === 'mid') return '#f59e0b'
  return '#ef4444'
})

function formatDuration(seconds) {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

async function loadAISummary() {
  aiSummaryLoading.value = true
  try { aiSummary.value = await api.get(`/exams/records/${route.params.recordId}/ai-summary`) } catch {}
  finally { aiSummaryLoading.value = false }
}

onMounted(async () => {
  try { result.value = await api.get(`/exams/records/${route.params.recordId}`) } catch {}
})
</script>

<style scoped>
.result-header { margin-bottom: 24px; }

/* Score Overview */
.score-overview {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 14px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 36px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
}

.score-ring {
  width: 130px;
  height: 130px;
  position: relative;
  flex-shrink: 0;
}

.ring-svg {
  width: 100%;
  height: 100%;
}

.ring-fill {
  transition: stroke-dashoffset 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.ring-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ring-score {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -1px;
  color: #1e293b;
  line-height: 1;
}

.ring-unit {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.score-info h3 {
  font-size: 18px;
  font-weight: 650;
  color: #1e293b;
  margin-bottom: 16px;
}

.score-meta-grid {
  display: flex;
  gap: 24px;
}

.meta-item {
  text-align: center;
}

.meta-num {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.meta-item.correct .meta-num { color: #15803d; }
.meta-item.wrong .meta-num { color: #dc2626; }

.meta-text {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

/* Detail Section */
.detail-section {
  margin-top: 20px;
}

.detail-header {
  font-size: 16px;
  font-weight: 650;
  color: #1e293b;
  margin-bottom: 12px;
}

.detail-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 10px;
  box-shadow: 0 1px 2px rgba(22,27,43,0.03);
}

.detail-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.detail-idx {
  font-weight: 650;
  font-size: 15px;
  color: #1e293b;
}

.detail-tags { display: flex; gap: 6px; }

.tag {
  padding: 3px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
}

.tag-ok { background: #ecfdf5; color: #15803d; }
.tag-err { background: #fef2f2; color: #dc2626; }
.tag-type { background: #f3f5fc; color: #3b5dbf; }

.detail-content {
  font-size: 14.5px;
  line-height: 1.7;
  color: #334155;
  padding: 12px;
  background: #fafaf8;
  border-radius: 8px;
  margin-bottom: 10px;
}

.detail-answer { font-size: 14px; }
.detail-answer p { margin-bottom: 4px; color: #475569; }

.detail-analysis {
  margin-top: 10px;
  padding: 12px;
  background: #fafaf8;
  border-radius: 8px;
  font-size: 13.5px;
  color: #64748b;
  line-height: 1.6;
}

.clr-ok { color: #15803d; font-weight: 650; }
.clr-err { color: #dc2626; font-weight: 650; }
.clr-warn { color: #d97706; }
.ai-summary-card { background: #fff; border: 1px solid #e8e5df; border-radius: 12px; overflow: hidden; margin-top: 20px; }
.ai-summary-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-bottom: 1px solid #f0ede7; font-weight: 650; font-size: 15px; }
.ai-summary-loading { padding: 24px; color: #94a3b8; font-size: 14px; text-align: center; }
.ai-summary-content { padding: 20px; font-size: 14px; line-height: 1.8; color: #334155; white-space: pre-wrap; }
.ai-summary-hint { padding: 24px; color: #94a3b8; font-size: 14px; text-align: center; }
</style>
