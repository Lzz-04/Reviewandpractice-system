<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="navigateTo('/subjects')" :icon="ArrowLeft" class="back-btn">
        返回科目列表
      </el-button>
      <div class="subject-title-row">
        <h2>{{ subject?.name || '加载中...' }}</h2>
        <div class="title-actions">
          <el-button type="success" @click="showImport = true">
            <el-icon style="margin-right: 5px"><Upload /></el-icon>导入题目
          </el-button>
          <el-button type="primary" @click="showCreate = true">
            <el-icon style="margin-right: 5px"><Plus /></el-icon>添加章节
          </el-button>
        </div>
      </div>
      <p v-if="subject?.description">{{ subject.description }}</p>
    </div>

    <!-- 章节列表 -->
    <div v-if="chapters.length > 0" class="chapter-list">
      <div v-for="(ch, idx) in chapters" :key="ch.id" class="chapter-card" :style="{ animationDelay: (idx * 0.05) + 's' }">
        <div class="chapter-left">
          <div class="chapter-num">{{ idx + 1 }}</div>
          <div class="chapter-info">
            <div class="chapter-name">{{ ch.name }}</div>
          </div>
        </div>
        <div class="chapter-actions">
          <el-button size="small" @click="viewQuestions(ch)">题目</el-button>
          <el-button type="primary" size="small" @click="startPractice(ch)">
            <el-icon style="margin-right: 4px"><Edit /></el-icon>刷题
          </el-button>
          <el-button type="success" size="small" @click="startExamForChapter(ch)">
            <el-icon style="margin-right: 4px"><Tickets /></el-icon>模拟考
          </el-button>
          <el-button size="small" @click="editChapter(ch)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(ch.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-empty v-else description="还没有章节，点击上方按钮添加" />

    <!-- 章节对话框 -->
    <el-dialog v-model="showCreate" :title="editingChapter ? '编辑章节' : '添加章节'" width="460px">
      <el-form :model="chapterForm" label-width="80px">
        <el-form-item label="章节名称">
          <el-input v-model="chapterForm.name" placeholder="如：第一章 函数与极限" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleSaveChapter">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog v-model="showImport" title="导入题目" width="520px" @closed="resetImport">
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="目标章节">
          <el-select v-model="importForm.chapterId" placeholder="选择章节" style="width: 100%">
            <el-option v-for="ch in chapters" :key="ch.id" :label="ch.name" :value="ch.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".docx,.pdf,.xlsx,.txt"
            drag
          >
            <el-icon :size="36" class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">将文件拖到此处，或点击上传</div>
            <div class="upload-hint">支持 .docx / .pdf / .xlsx / .txt 格式</div>
            <div class="upload-hint upload-hint--format">
              支持两种题目格式：<br/>
              ① 标准格式：题型：/ 题目：/ A. 选项 / 答案：等标签式<br/>
              ② 试卷格式：自动识别 1. 题号 + 内联选项 + 文末答案
            </div>
          </el-upload>
        </el-form-item>
      </el-form>

      <!-- 导入结果 -->
      <div v-if="importResult" class="import-result">
        <div class="result-title">导入结果</div>
        <div class="result-stats">
          <span>总计 {{ importResult.total }} 题</span>
          <span class="stat-success">成功 {{ importResult.success }} 题</span>
          <span v-if="importResult.failed > 0" class="stat-fail">失败 {{ importResult.failed }} 题</span>
        </div>
        <div v-if="importResult.errors && importResult.errors.length > 0" class="result-errors">
          <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showImport = false">关闭</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing" :disabled="!importFile">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目列表对话框 -->
    <el-dialog v-model="showQuestions" :title="questionChapter?.name + ' - 题目列表'" width="750px" @closed="questionChapter = null">
      <div v-loading="loadingQuestions">
        <el-empty v-if="!loadingQuestions && questions.length === 0" description="该章节下没有题目" />
        <div v-else class="question-list-dialog">
          <div v-for="(q, i) in questions" :key="q.id" class="q-item">
            <span class="q-index">{{ i + 1 }}</span>
            <span class="q-type">{{ typeLabel(q.type) }}</span>
            <span class="q-content">{{ truncateText(q.content, 50) }}</span>
            <span class="q-answer" :class="q.type === 'judge' ? 'judge-answer' : ''">{{ formatAnswer(q.answer, q.type) }}</span>
            <el-popconfirm title="确定删除该题目？" @confirm="handleDeleteQuestion(q.id)">
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showQuestions = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 考试对话框 -->
    <el-dialog v-model="showExamDialog" title="创建章节模拟考" width="460px">
      <el-form :model="examForm" label-width="110px">
        <el-form-item label="考试标题">
          <el-input v-model="examForm.title" placeholder="如：第一章单元测试" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)">
          <el-input-number v-model="examForm.duration" :min="5" :max="180" />
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="examForm.totalCount" :min="5" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExamDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateExam">创建并开始</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Plus, ArrowLeft, Edit, Tickets, Upload, UploadFilled, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const api = useApi()
const subjectId = route.params.id
const subject = ref(null)
const chapters = ref([])
const showCreate = ref(false)
const editingChapter = ref(null)
const chapterForm = ref({ name: '' })
const showImport = ref(false)
const importForm = ref({ chapterId: null })
const importFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const uploadRef = ref(null)

const showQuestions = ref(false)
const questionChapter = ref(null)
const questions = ref([])
const loadingQuestions = ref(false)

const showExamDialog = ref(false)
const examChapter = ref(null)
const examForm = ref({ title: '', duration: 30, totalCount: 10 })

const typeLabel = (t) => ({ single: '单选', multiple: '多选', judge: '判断' }[t] || t)
const truncateText = (s, max) => s && s.length > max ? s.slice(0, max) + '…' : s
const formatAnswer = (ans, type) => {
  if (!ans) return ''
  if (type === 'judge') return ans === 'T' || ans === '√' || ans === '对' ? '正确' : '错误'
  return ans
}

async function load() {
  try {
    subject.value = await api.get(`/subjects/${subjectId}`)
    chapters.value = await api.get(`/subjects/${subjectId}/chapters`)
  } catch {}
}

function editChapter(ch) {
  editingChapter.value = ch
  chapterForm.value = { name: ch.name }
  showCreate.value = true
}

async function handleSaveChapter() {
  if (!chapterForm.value.name.trim()) { ElMessage.warning('请输入章节名称'); return }
  if (editingChapter.value) await api.put(`/chapters/${editingChapter.value.id}`, chapterForm.value)
  else await api.post(`/subjects/${subjectId}/chapters`, { ...chapterForm.value, subjectId: parseInt(subjectId) })
  showCreate.value = false
  editingChapter.value = null
  chapterForm.value = { name: '' }
  await load()
}

async function handleDelete(id) {
  try {
    await api.delete(`/chapters/${id}`)
    await load()
  } catch {
    // 错误提示已在 useApi 中处理
  }
}

function handleFileChange(file) { importFile.value = file.raw }
function handleFileRemove() { importFile.value = null }

function resetImport() {
  importForm.value = { chapterId: null }
  importFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

async function handleImport() {
  if (!importForm.value.chapterId) { ElMessage.warning('请选择目标章节'); return }
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }

  importing.value = true
  importResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('subjectId', subjectId)
    formData.append('chapterId', importForm.value.chapterId)

    const config = useRuntimeConfig()
    const response = await fetch(`${config.public.apiBase}/import`, {
      method: 'POST',
      body: formData,
    })
    const res = await response.json()
    if (res.code !== 200) { ElMessage.error(res.message || '导入失败'); return }
    importResult.value = res.data
    if (res.data.failed === 0) ElMessage.success(res.message)
    else ElMessage.warning(res.message)
  } catch { ElMessage.error('导入失败')
  } finally { importing.value = false }
}

async function viewQuestions(ch) {
  questionChapter.value = ch
  showQuestions.value = true
  loadingQuestions.value = true
  try {
    const data = await api.get('/questions', { chapterId: ch.id, pageSize: 1000 })
    questions.value = data.records || data || []
  } catch {
    questions.value = []
  } finally {
    loadingQuestions.value = false
  }
}

async function handleDeleteQuestion(id) {
  try {
    await api.delete(`/questions/${id}`)
    questions.value = questions.value.filter(q => q.id !== id)
  } catch {
    // 错误提示已在 useApi 中处理
  }
}

function startPractice(ch) { navigateTo(`/practice/${ch.id}`) }

function startExamForChapter(ch) {
  examChapter.value = ch
  examForm.value = { title: ch.name + ' 单元测试', duration: 30, totalCount: 10 }
  showExamDialog.value = true
}

async function handleCreateExam() {
  const examPaper = await api.post('/exams/generate', {
    subjectId: parseInt(subjectId), title: examForm.value.title,
    duration: examForm.value.duration, totalCount: examForm.value.totalCount,
    chapterIds: [examChapter.value.id],
  })
  showExamDialog.value = false
  await useExamStore().startExam(examPaper.id)
  navigateTo(`/exam/start/${examPaper.id}`)
}

onMounted(load)
</script>

<style scoped>
.back-btn { margin-bottom: 8px; }

.subject-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 4px;
  flex-wrap: wrap;
  gap: 10px;
}

.subject-title-row h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.title-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chapter-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 2px rgba(22,27,43,0.03);
  transition: box-shadow 0.2s, transform 0.2s;
  animation: cardSlide 0.35s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes cardSlide {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.chapter-card:hover {
  box-shadow: 0 4px 16px rgba(22,27,43,0.06);
  transform: translateX(3px);
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chapter-num {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b5dbf, #5876d8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.chapter-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.chapter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* Import dialog */
.upload-icon { color: #3b5dbf; margin-bottom: 8px; }
.upload-text { font-size: 14px; color: #334155; margin-bottom: 4px; }
.upload-hint { font-size: 12px; color: #94a3b8; }
.upload-hint--format { margin-top: 6px; line-height: 1.6; }

.import-result {
  margin-top: 16px;
  padding: 16px;
  background: #fafaf8;
  border-radius: 8px;
  border: 1px solid #e8e5df;
}

.result-title {
  font-weight: 650;
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 8px;
}

.result-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}
.stat-success { color: #15803d; font-weight: 600; }
.stat-fail { color: #dc2626; font-weight: 600; }

.result-errors {
  margin-top: 10px;
  max-height: 150px;
  overflow-y: auto;
}
.error-item {
  font-size: 12px;
  color: #dc2626;
  padding: 4px 0;
  border-bottom: 1px solid #fee2e2;
}

/* 题目列表对话框 */
.question-list-dialog {
  max-height: 50vh;
  overflow-y: auto;
}
.q-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 8px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}
.q-item:hover { background: #f8fafc; }
.q-index {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #e2e8f0;
  color: #475569;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.q-type {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #eff6ff;
  color: #3b5dbf;
  flex-shrink: 0;
}
.q-content {
  flex: 1;
  color: #334155;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.q-answer {
  font-weight: 600;
  color: #15803d;
  flex-shrink: 0;
}
.judge-answer { color: #dc2626; }
</style>
