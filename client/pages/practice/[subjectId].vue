<template>
  <div class="page-container">
    <el-page-header @back="navigateTo('/subjects')" class="practice-header">
      <template #content><span class="header-title">刷题练习</span></template>
    </el-page-header>

    <!-- 模式选择 -->
    <div v-if="!store.questions.length && !loading" class="mode-select">
      <el-radio-group v-model="practiceMode" @change="startWithMode" size="large">
        <el-radio-button value="all">全部题目</el-radio-button>
        <el-radio-button value="wrong">只练错题</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="store.questions.length > 0" class="practice-area">
      <!-- 进度条 -->
      <div class="progress-section">
        <div class="progress-stats">
          <span>{{ store.mode === 'wrong' ? '错题练习 · ' : '' }}第 {{ store.progress.current }} / {{ store.progress.total }} 题</span>
        </div>
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: store.progress.percent + '%' }"></div>
        </div>
      </div>

      <!-- 题目 -->
      <QuestionCard
        v-if="store.currentQuestion"
        :question="store.currentQuestion"
        :index="store.currentIndex"
        :show-result="showResult"
        :selected="store.answers[store.currentQuestion.id]"
        :key="store.currentQuestion.id"
        @answer="handleSelect"
      />

      <!-- 答题结果 -->
      <div v-if="showResult && currentResult" class="result-area">
        <div class="result-banner" :class="currentResult.isCorrect ? 'correct' : 'wrong'">
          <div class="result-icon">
            <el-icon :size="24"><CircleCheckFilled v-if="currentResult.isCorrect" /><CircleCloseFilled v-else /></el-icon>
          </div>
          <div class="result-text">
            <div class="result-title">{{ currentResult.isCorrect ? '回答正确！' : '回答错误' }}</div>
            <div v-if="!currentResult.isCorrect" class="result-answer">正确答案：{{ currentResult.correctAnswer }}</div>
            <div v-if="currentResult.analysis" class="result-analysis">
              <el-icon><InfoFilled /></el-icon> {{ currentResult.analysis }}
            </div>
          </div>
        </div>
      </div>

      <!-- 操作 -->
      <div class="action-bar">
        <el-button @click="store.prevQuestion()" :disabled="store.currentIndex === 0" size="large">
          上一题
        </el-button>
        <el-button
          v-if="!showResult"
          type="primary"
          size="large"
          :disabled="!store.answers[store.currentQuestion?.id]"
          @click="handleConfirm"
        >
          确认答案
        </el-button>
        <el-button
          v-if="showResult && store.currentIndex < store.questions.length - 1"
          type="primary"
          size="large"
          @click="handleNext"
        >
          下一题
        </el-button>
        <el-button
          v-if="showResult && store.currentIndex === store.questions.length - 1"
          type="success"
          size="large"
          @click="handleFinish"
        >
          完成练习
        </el-button>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="该科目暂无题目" />
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

const route = useRoute()
const store = useQuestionStore()
const showResult = ref(false)
const currentResult = ref(null)
const loading = ref(false)
const practiceMode = ref('all')

async function startWithMode(mode) {
  loading.value = true
  try {
    const subjectId = route.params.subjectId
    await store.startSession(subjectId, 'random', mode === 'wrong')
  } finally { loading.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    const subjectId = route.params.subjectId
    // 如果 store 中已有错题数据（从错题本页面跳转过来），直接使用
    if (store.questions.length > 0 && store.mode === 'wrong') {
      loading.value = false
      return
    }
    await store.startSession(subjectId, 'random')
  } finally { loading.value = false }
})

function handleSelect(answer) {
  if (store.currentQuestion) store.answers[store.currentQuestion.id] = answer
}

async function handleConfirm() {
  if (!store.currentQuestion) return
  const q = store.currentQuestion
  const selected = store.answers[q.id]
  if (!selected) { ElMessage.warning('请先选择答案'); return }
  currentResult.value = await store.submitAnswer(q.id, selected)
  showResult.value = true
}

function handleNext() {
  showResult.value = false
  currentResult.value = null
  store.nextQuestion()
}

function handleFinish() {
  ElMessage.success('练习完成！')
  store.reset()
  navigateTo('/subjects')
}
</script>

<style scoped>
.mode-select {
  text-align: center;
  margin-bottom: 24px;
}

.practice-header {
  margin-bottom: 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 650;
  color: #1e293b;
}

.practice-area {
  max-width: 760px;
  margin: 0 auto;
}

/* Progress */
.progress-section {
  margin-bottom: 20px;
}

.progress-stats {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 10px;
}

.progress-track {
  height: 6px;
  background: #e8e5df;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #3b5dbf, #5876d8);
  border-radius: 10px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Result Banner */
.result-area {
  max-width: 700px;
  margin: 16px auto;
}

.result-banner {
  display: flex;
  gap: 14px;
  padding: 20px;
  border-radius: 12px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-banner.correct {
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
}

.result-banner.wrong {
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.result-icon {
  color: #22c55e;
  flex-shrink: 0;
}

.result-banner.wrong .result-icon {
  color: #ef4444;
}

.result-title {
  font-size: 16px;
  font-weight: 650;
  margin-bottom: 4px;
}

.result-banner.correct .result-title { color: #15803d; }
.result-banner.wrong .result-title { color: #dc2626; }

.result-answer {
  font-size: 14px;
  color: #15803d;
  font-weight: 600;
  margin-top: 4px;
}

.result-analysis {
  margin-top: 8px;
  padding: 10px 14px;
  background: rgba(0,0,0,0.03);
  border-radius: 8px;
  font-size: 13.5px;
  color: #64748b;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  line-height: 1.6;
}

/* Action Bar */
.action-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}
</style>
