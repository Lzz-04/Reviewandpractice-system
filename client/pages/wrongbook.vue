<template>
  <div class="page-container">
    <div class="page-header">
      <h2>错题本</h2>
      <p>自动收录答错的题目，反复练习直到掌握</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards" v-if="stats.total > 0">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e9eefa; color: #3b5dbf;">
          <el-icon :size="20"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.total }}</div>
          <div class="stat-desc">错题总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #ecfdf5; color: #15803d;">
          <el-icon :size="20"><CircleCheckFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.mastered }}</div>
          <div class="stat-desc">已掌握</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fef2f2; color: #dc2626;">
          <el-icon :size="20"><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-num">{{ stats.unMastered }}</div>
          <div class="stat-desc">待复习</div>
        </div>
      </div>
    </div>

    <!-- 练习按钮 -->
    <div v-if="stats.unMastered > 0" class="practice-action">
      <el-button type="primary" size="large" @click="startWrongPractice" :icon="EditPen">
        练习全部未掌握错题
      </el-button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="filterSubjectId" placeholder="按科目筛选" clearable @change="loadList" size="large">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-select v-model="filterMastered" placeholder="掌握状态" clearable @change="loadList" size="large" style="width: 130px">
        <el-option label="未掌握" :value="0" />
        <el-option label="已掌握" :value="1" />
      </el-select>
    </div>

    <!-- 题目列表 -->
    <div v-loading="loading" class="question-list">
      <div v-for="(item, idx) in list" :key="item.id" class="question-card" :style="{ animationDelay: (idx * 0.05) + 's' }">
        <!-- 题头 -->
        <div class="qc-header">
          <span class="qc-type" :class="'qc-type--' + item.type">{{ typeLabel(item.type) }}</span>
          <div class="qc-meta">
            <span class="qc-wrong-count">答错 {{ item.wrongCount }} 次</span>
            <span class="qc-review-count">复习 {{ item.reviewedCount }} 次</span>
            <span class="qc-master-tag" :class="item.mastered ? 'ok' : 'no'">
              {{ item.mastered ? '已掌握' : '未掌握' }}
            </span>
          </div>
        </div>

        <!-- 题目内容 -->
        <div class="qc-content">
          <span class="qc-index">{{ idx + 1 }}.</span>
          {{ item.content }}
        </div>

        <!-- 选项 -->
        <div class="qc-options" v-if="parsedOptions(item).length > 0">
          <div
            v-for="opt in parsedOptions(item)"
            :key="opt.label"
            class="qc-option"
            :class="{ 'qc-option--correct': isCorrectOption(item, opt.label) }"
          >
            <span class="qc-opt-label">{{ opt.label }}</span>
            <span class="qc-opt-text">{{ opt.text }}</span>
            <el-icon v-if="isCorrectOption(item, opt.label)" class="qc-opt-check" :size="16"><CircleCheckFilled /></el-icon>
          </div>
        </div>

        <!-- 判断题答案 -->
        <div v-if="item.type === 'judge'" class="qc-answer-line">
          <span class="qc-answer-label">正确答案：</span>
          <span class="qc-answer-value">{{ item.answer === 'T' ? '正确' : '错误' }}</span>
        </div>

        <!-- 解析 -->
        <div v-if="item.analysis" class="qc-analysis">
          <el-icon :size="14"><InfoFilled /></el-icon>
          {{ item.analysis }}
        </div>

        <!-- 操作 -->
        <div class="qc-actions">
          <el-button type="primary" size="small" @click="handleReview(item)">已复习</el-button>
          <el-button
            :type="item.mastered ? 'warning' : 'success'"
            size="small"
            @click="handleMaster(item)"
          >
            {{ item.mastered ? '取消掌握' : '标记掌握' }}
          </el-button>
          <el-popconfirm title="确定移出？" @confirm="handleRemove(item.id)">
            <template #reference>
              <el-button size="small" type="danger">移出</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无错题，继续加油！" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Document, CircleCheckFilled, WarningFilled, InfoFilled, EditPen } from '@element-plus/icons-vue'

const api = useApi()
const questionStore = useQuestionStore()
const subjects = ref([])
const stats = ref({ total: 0, mastered: 0, unMastered: 0 })
const list = ref([])
const loading = ref(false)
const filterSubjectId = ref(null)
const filterMastered = ref(null)

function typeLabel(type) {
  return { single: '单选题', multiple: '多选题', judge: '判断题' }[type] || ''
}

function parsedOptions(item) {
  try { return JSON.parse(item.options || '[]') } catch { return [] }
}

function isCorrectOption(item, label) {
  const correct = (item.answer || '').replace(/\s/g, '').split(',')
  return correct.includes(label)
}

async function loadStats() { try { stats.value = await api.get('/wrongbook/stats') } catch {} }
async function loadList() {
  loading.value = true
  try {
    const params = {}
    if (filterSubjectId.value) params.subjectId = filterSubjectId.value
    if (filterMastered.value !== null) params.mastered = filterMastered.value
    list.value = await api.get('/wrongbook', params)
  } finally { loading.value = false }
}

async function handleReview(item) {
  await api.post(`/wrongbook/${item.id}/review`)
  await Promise.all([loadList(), loadStats()])
}

async function handleMaster(item) {
  await api.post(`/wrongbook/${item.id}/master`)
  await Promise.all([loadList(), loadStats()])
}

async function handleRemove(id) {
  await api.delete(`/wrongbook/${id}`)
  await Promise.all([loadList(), loadStats()])
}

async function startWrongPractice() {
  const subjectId = filterSubjectId.value
  try {
    const api = useApi()
    const url = subjectId ? `/practice/wrong/${subjectId}` : '/practice/wrong'
    const data = await api.get(url, { unMasteredOnly: true })
    questionStore.sessionId = data.sessionId
    questionStore.questions = data.questions
    questionStore.currentIndex = 0
    questionStore.answers = {}
    questionStore.results = {}
    questionStore.mode = 'wrong'
    if (questionStore.questions.length === 0) {
      ElMessage.info('没有待复习的错题')
      return
    }
    navigateTo(`/practice/0`)
  } catch {}
}

onMounted(async () => {
  try { subjects.value = await api.get('/subjects') } catch {}
  await Promise.all([loadStats(), loadList()])
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

.practice-action {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

/* Question Cards */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
  animation: cardSlide 0.35s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes cardSlide {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.qc-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0ede7;
  flex-wrap: wrap;
}

.qc-type {
  padding: 3px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 650;
  flex-shrink: 0;
}

.qc-type--single { background: #e9eefa; color: #3b5dbf; }
.qc-type--multiple { background: #ecfdf5; color: #15803d; }
.qc-type--judge { background: #fef6ee; color: #e0781a; }

.qc-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 13px;
  color: #94a3b8;
}

.qc-wrong-count { color: #dc2626; font-weight: 600; }
.qc-master-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.qc-master-tag.ok { background: #ecfdf5; color: #15803d; }
.qc-master-tag.no { background: #fffbeb; color: #d97706; }

.qc-content {
  padding: 16px 20px;
  font-size: 15px;
  line-height: 1.8;
  color: #1e293b;
  background: #fafaf8;
}

.qc-index {
  font-weight: 700;
  color: #3b5dbf;
  margin-right: 2px;
}

/* Options */
.qc-options {
  border-top: 1px solid #f0ede7;
}

.qc-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-bottom: 1px solid #f8f6f2;
  font-size: 14px;
  color: #334155;
}

.qc-option:last-child { border-bottom: none; }

.qc-option--correct {
  background: #ecfdf5;
}

.qc-opt-label {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #f3f5fc;
  color: #3b5dbf;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.qc-option--correct .qc-opt-label {
  background: #22c55e;
  color: #fff;
}

.qc-opt-text { flex: 1; }

.qc-opt-check { color: #22c55e; flex-shrink: 0; }

/* Judge answer */
.qc-answer-line {
  padding: 12px 20px;
  border-top: 1px solid #f0ede7;
  font-size: 14px;
  color: #334155;
}

.qc-answer-label { color: #94a3b8; }
.qc-answer-value { color: #15803d; font-weight: 600; }

/* Analysis */
.qc-analysis {
  margin: 0 20px 16px;
  padding: 12px 16px;
  background: #fafaf8;
  border-radius: 8px;
  font-size: 13px;
  color: #64748b;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  line-height: 1.6;
}

/* Actions */
.qc-actions {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid #f0ede7;
  justify-content: flex-end;
}
</style>
