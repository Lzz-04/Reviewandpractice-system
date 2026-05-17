<template>
  <div class="page-container">
    <div v-if="!examStore.started">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="!examStore.finished && examStore.questions.length > 0" class="exam-shell">
      <!-- 顶部栏 -->
      <div class="exam-topbar">
        <div class="timer-section">
          <ExamTimer
            :remaining="timerRemaining"
            :format-time="timerFormatTime"
            :warning="timerMinutes < 5"
          />
        </div>
        <div class="progress-section">
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: examStore.progress.percent + '%' }"></div>
          </div>
          <span class="progress-num">{{ examStore.progress.current }}/{{ examStore.progress.total }}</span>
        </div>
        <el-button type="danger" size="large" @click="handleSubmit" class="submit-btn">
          交卷
        </el-button>
      </div>

      <!-- 题目区域 -->
      <div class="exam-body">
        <QuestionCard
          v-if="examStore.currentQuestion"
          :question="examStore.currentQuestion"
          :index="examStore.currentIndex"
          :show-result="false"
          :selected="examStore.answers[examStore.currentQuestion.id]"
          :key="examStore.currentQuestion.id"
          @answer="handleSelect"
        />

        <!-- 答题卡 -->
        <div class="answer-card">
          <div class="answer-card-header">答题卡</div>
          <div class="answer-grid">
            <div
              v-for="(q, idx) in examStore.questions"
              :key="q.id"
              class="answer-num"
              :class="{
                answered: examStore.answers[q.id],
                current: idx === examStore.currentIndex,
              }"
              @click="examStore.jumpTo(idx)"
            >
              {{ idx + 1 }}
            </div>
          </div>
        </div>
      </div>

      <!-- 导航 -->
      <div class="exam-nav">
        <el-button @click="examStore.prevQuestion()" :disabled="examStore.currentIndex === 0" size="large">
          上一题
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="examStore.nextQuestion()"
          :disabled="examStore.currentIndex >= examStore.questions.length - 1"
        >
          下一题
        </el-button>
      </div>
    </div>

    <!-- 交卷结果预览 -->
    <div v-else-if="examStore.finished && examStore.result" class="submit-result">
      <div class="result-card">
        <div class="result-badge">交卷成功</div>
        <div class="result-score">{{ examStore.result.score }}<span>分</span></div>
        <div class="result-meta">
          {{ examStore.result.correctCount }}/{{ examStore.result.totalQuestions }} 题正确
        </div>
        <div class="result-actions">
          <el-button type="primary" size="large" @click="navigateTo(`/exam/result/${examStore.recordId}`)">
            查看详情
          </el-button>
          <el-button size="large" @click="navigateTo('/exam')">返回列表</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const route = useRoute()
const api = useApi()
const examStore = useExamStore()

const timerDuration = ref(30)
const timerRemaining = ref(1800)
const timerFormatTime = ref('30:00')
const timerMinutes = computed(() => Math.floor(timerRemaining.value / 60))
let timerInterval = null

function startTimer() {
  timerInterval = setInterval(() => {
    timerRemaining.value--
    const m = Math.floor(timerRemaining.value / 60)
    const s = timerRemaining.value % 60
    timerFormatTime.value = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    if (timerRemaining.value <= 0) { clearInterval(timerInterval); handleSubmit() }
  }, 1000)
}

onMounted(async () => {
  const examId = route.params.examId
  if (!examStore.started) await examStore.startExam(examId)
  try {
    const paper = await api.get(`/exams/${examId}`)
    timerDuration.value = paper.duration
    timerRemaining.value = paper.duration * 60
    timerFormatTime.value = `${String(paper.duration).padStart(2, '0')}:00`
    const questions = await api.get('/questions', { subjectId: paper.subjectId, pageSize: 100 })
    const shuffled = [...(questions.records || questions)].sort(() => Math.random() - 0.5)
    examStore.setQuestions(shuffled.slice(0, paper.questionCount), paper.duration)
    startTimer()
  } catch {}
})

function handleSelect(answer) {
  if (examStore.currentQuestion) examStore.selectAnswer(examStore.currentQuestion.id, answer)
}

async function handleSubmit() {
  clearInterval(timerInterval)
  await examStore.submitExam()
}

onUnmounted(() => clearInterval(timerInterval))
</script>

<style scoped>
/* Top Bar */
.exam-topbar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
}

.timer-section { flex-shrink: 0; }

.progress-section {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: #e8e5df;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #3b5dbf, #5876d8);
  border-radius: 10px;
  transition: width 0.3s;
}

.progress-num {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 600;
  white-space: nowrap;
}

.submit-btn {
  flex-shrink: 0;
}

/* Body */
.exam-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.exam-body > :first-child {
  flex: 1;
  min-width: 0;
}

/* Answer Card */
.answer-card {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
}

.answer-card-header {
  padding: 14px 16px;
  font-weight: 650;
  font-size: 14px;
  color: #1e293b;
  border-bottom: 1px solid #f0ede7;
}

.answer-grid {
  padding: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.answer-num {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e8e5df;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 550;
  transition: all 0.15s;
  color: #94a3b8;
}

.answer-num:hover {
  border-color: #3b5dbf;
  color: #3b5dbf;
}

.answer-num.answered {
  background: #e9eefa;
  border-color: #3b5dbf;
  color: #3b5dbf;
}

.answer-num.current {
  background: #3b5dbf;
  border-color: #3b5dbf;
  color: #fff;
  box-shadow: 0 2px 8px rgba(59,93,191,0.3);
}

/* Nav */
.exam-nav {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

/* Submit Result */
.submit-result {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.result-card {
  text-align: center;
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 16px;
  padding: 48px 64px;
  box-shadow: 0 4px 24px rgba(22,27,43,0.06);
  animation: popIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes popIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.result-badge {
  display: inline-block;
  padding: 6px 16px;
  background: #ecfdf5;
  color: #15803d;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.result-score {
  font-size: 64px;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -2px;
  line-height: 1;
}

.result-score span {
  font-size: 24px;
  font-weight: 600;
  color: #94a3b8;
  margin-left: 6px;
}

.result-meta {
  margin-top: 12px;
  font-size: 15px;
  color: #64748b;
}

.result-actions {
  margin-top: 28px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
