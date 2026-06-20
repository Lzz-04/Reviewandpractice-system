<template>
  <div class="page-container">
    <div class="page-header">
      <el-button text @click="navigateTo('/subjects')" :icon="ArrowLeft" class="back-btn">
        返回科目列表
      </el-button>
      <div class="subject-title-row">
        <div>
          <h2>{{ subject?.name || '加载中...' }}</h2>
          <p v-if="subject?.description">{{ subject.description }}</p>
        </div>
        <div class="title-actions">
          <el-button type="primary" @click="startPractice">
            <el-icon style="margin-right: 5px"><Edit /></el-icon>刷题
          </el-button>
          <el-button type="success" @click="showImport = true">
            <el-icon style="margin-right: 5px"><Upload /></el-icon>导入题目
          </el-button>
          <el-button type="warning" @click="showAIDialog = true">
            <el-icon style="margin-right: 5px"><MagicStick /></el-icon>AI 出题
          </el-button>
          <el-button type="primary" plain @click="showExamDialog = true">
            <el-icon style="margin-right: 5px"><Tickets /></el-icon>模拟考
          </el-button>
        </div>
      </div>
    </div>

    <div class="question-panel" v-loading="loadingQuestions">
      <div class="panel-header">
        <span>题目列表</span>
        <span class="panel-count">共 {{ questions.length }} 道</span>
      </div>

      <div class="q-toolbar">
        <el-input v-model="qSearch.keyword" placeholder="搜索题目内容..." clearable @input="loadQuestions" style="width: 220px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="qSearch.difficulty" placeholder="难度" clearable @change="loadQuestions" style="width: 110px">
          <el-option label="难度1" :value="1" /><el-option label="难度2" :value="2" />
          <el-option label="难度3" :value="3" /><el-option label="难度4" :value="4" />
          <el-option label="难度5" :value="5" />
        </el-select>
        <el-select v-model="qSearch.sortBy" placeholder="排序" clearable @change="loadQuestions" style="width: 130px">
          <el-option label="默认(最新)" value="" /><el-option label="按难度" value="difficulty" />
        </el-select>
        <el-select v-model="qSearch.type" placeholder="题型" clearable @change="loadQuestions" style="width: 120px">
          <el-option label="单选题" value="single" /><el-option label="多选题" value="multiple" />
          <el-option label="判断题" value="judge" />
        </el-select>
      </div>

      <div v-if="batchIds.length > 0" class="q-batch-bar">
        <span>已选 {{ batchIds.length }} 道题目</span>
        <el-button size="small" @click="showBatchDifficulty = true">批量修改难度</el-button>
        <el-popconfirm title="确定删除？" @confirm="handleBatchDelete">
          <template #reference><el-button size="small" type="danger">批量删除</el-button></template>
        </el-popconfirm>
        <el-button size="small" @click="batchIds = []">取消选择</el-button>
      </div>

      <el-empty v-if="!loadingQuestions && questions.length === 0" description="该科目下没有题目" />
      <div v-else class="question-list">
        <div v-for="(q, i) in questions" :key="q.id" class="q-item">
          <el-checkbox :model-value="batchIds.includes(q.id)" @change="toggleBatch(q.id)" />
          <span class="q-index">{{ i + 1 }}</span>
          <span class="q-type">{{ typeLabel(q.type) }}</span>
          <span class="q-diff" v-if="q.difficulty">L{{ q.difficulty }}</span>
          <span class="q-content">{{ truncateText(q.content, 60) }}</span>
          <span class="q-answer" :class="q.type === 'judge' ? 'judge-answer' : ''">{{ formatAnswer(q.answer, q.type) }}</span>
          <el-popconfirm title="确定删除该题目？" @confirm="handleDeleteQuestion(q.id)">
            <template #reference>
              <el-button size="small" type="danger" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <!-- 导入对话框 -->
    <el-dialog v-model="showImport" title="导入题目" width="520px" @closed="resetImport">
      <el-form label-width="100px">
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

      <div v-if="importFile" style="margin-top: 12px">
        <el-button size="small" @click="handlePreviewExtract" :loading="previewing">
          <el-icon style="margin-right: 4px"><View /></el-icon>提取预览（前10道单选）
        </el-button>
        <div v-if="previewText" class="preview-box">
          <pre class="preview-content">{{ previewText }}</pre>
        </div>
      </div>

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
        <el-button type="primary" @click="handleImport" :loading="importing" :disabled="!importFile">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改难度 -->
    <el-dialog v-model="showBatchDifficulty" title="批量修改难度" width="400px">
      <el-select v-model="batchDifficulty" placeholder="选择难度" style="width: 100%">
        <el-option label="难度1 (简单)" :value="1" /><el-option label="难度2" :value="2" />
        <el-option label="难度3 (中等)" :value="3" /><el-option label="难度4" :value="4" />
        <el-option label="难度5 (困难)" :value="5" />
      </el-select>
      <template #footer>
        <el-button @click="showBatchDifficulty = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdateDifficulty" :disabled="!batchDifficulty">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI 出题配置对话框 -->
    <el-dialog v-model="showAIDialog" title="AI 智能出题" width="480px" @closed="resetAIDialog">
      <el-form :model="aiForm" label-width="100px">
        <el-form-item label="题型">
          <el-select v-model="aiForm.type" placeholder="选择题型" style="width: 100%">
            <el-option label="混合（单选+多选+判断）" value="mixed" />
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="aiForm.difficulty" :max="5" :colors="['#67c23a','#67c23a','#e6a23c','#e6a23c','#f56c6c']" show-text :texts="['很简单','简单','中等','较难','困难']" />
        </el-form-item>
        <el-form-item label="生成数量">
          <el-input-number v-model="aiForm.count" :min="1" :max="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAIDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAIGenerate" :loading="aiGenerating">
          {{ aiGenerating ? 'AI 正在生成...' : '开始生成' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 生成结果预览对话框 -->
    <el-dialog v-model="showAIPreview" title="AI 生成题目预览" width="800px" @closed="resetAIDialog">
      <div v-if="aiGenerated.length > 0">
        <div class="ai-preview-toolbar">
          <el-checkbox v-model="aiAllSelected" @change="toggleAIAll" :indeterminate="aiIndeterminate">全选</el-checkbox>
          <span class="ai-preview-count">已选 {{ aiSelectedCount }} / {{ aiGenerated.length }} 道</span>
          <el-button size="small" type="success" @click="handleAISave" :loading="aiSaving" :disabled="aiSelectedCount === 0">
            保存选中题目
          </el-button>
        </div>
        <div class="ai-preview-list">
          <div v-for="(q, i) in aiGenerated" :key="i" class="ai-question-card">
            <div class="ai-q-top">
              <el-checkbox v-model="q.selected" />
              <span class="ai-q-index">{{ i + 1 }}</span>
              <span class="ai-q-type" :class="'type-' + q.type">{{ typeLabel(q.type) }}</span>
              <span class="ai-q-diff">难度 {{ q.difficulty || aiForm.difficulty }}</span>
            </div>
            <div class="ai-q-content">{{ q.content }}</div>
            <div v-if="q.options && q.options.length > 0" class="ai-q-options">
              <span v-for="(opt, oi) in q.options" :key="oi" class="ai-option">{{ opt }}</span>
            </div>
            <div class="ai-q-answer"><span class="ai-label">答案：</span><span class="ai-value">{{ q.answer }}</span></div>
            <div v-if="q.analysis" class="ai-q-analysis"><span class="ai-label">解析：</span><span class="ai-value">{{ q.analysis }}</span></div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无生成结果" />
      <template #footer><el-button @click="showAIPreview = false">关闭</el-button></template>
    </el-dialog>

    <!-- 考试对话框 -->
    <el-dialog v-model="showExamDialog" title="创建科目模拟考" width="460px">
      <el-form :model="examForm" label-width="110px">
        <el-form-item label="考试标题">
          <el-input v-model="examForm.title" placeholder="如：科目模拟测试" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)">
          <el-input-number v-model="examForm.duration" :min="5" :max="180" />
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="examForm.totalCount" :min="5" :max="100" />
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
import { ArrowLeft, Edit, Tickets, Upload, UploadFilled, Delete, Search, View, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const api = useApi()
const subjectId = route.params.id
const subject = ref(null)
const showImport = ref(false)
const importFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const uploadRef = ref(null)
const previewText = ref('')
const previewing = ref(false)

const questions = ref([])
const loadingQuestions = ref(false)
const qSearch = ref({ keyword: '', difficulty: null, sortBy: '', type: '' })
const batchIds = ref([])
const showBatchDifficulty = ref(false)
const batchDifficulty = ref(null)

const showExamDialog = ref(false)
const examForm = ref({ title: '', duration: 30, totalCount: 10 })

const showAIDialog = ref(false)
const showAIPreview = ref(false)
const aiGenerating = ref(false)
const aiSaving = ref(false)
const aiForm = ref({ type: 'mixed', difficulty: 3, count: 5 })
const aiGenerated = ref([])
const aiAllSelected = ref(false)
const aiIndeterminate = ref(false)
const aiSelectedCount = computed(() => aiGenerated.value.filter(q => q.selected).length)

function toggleAIAll(val) {
  aiGenerated.value.forEach(q => { q.selected = val })
  aiIndeterminate.value = false
}

function resetAIDialog() {
  if (showAIDialog.value || showAIPreview.value) return
  aiForm.value = { type: 'mixed', difficulty: 3, count: 5 }
  aiGenerated.value = []
  aiAllSelected.value = false
  aiIndeterminate.value = false
}

async function handleAIGenerate() {
  aiGenerating.value = true
  try {
    const data = await api.post('/ai/generate', {
      subjectId: parseInt(subjectId),
      subjectName: subject.value?.name || '',
      type: aiForm.value.type,
      difficulty: aiForm.value.difficulty,
      count: aiForm.value.count,
    })
    aiGenerated.value = (data || []).map(q => ({ ...q, selected: true }))
    aiAllSelected.value = true
    showAIDialog.value = false
    showAIPreview.value = true
    ElMessage.success(`AI 已生成 ${aiGenerated.value.length} 道题目`)
  } catch {
  } finally {
    aiGenerating.value = false
  }
}

async function handleAISave() {
  const selected = aiGenerated.value.filter(q => q.selected)
  if (selected.length === 0) { ElMessage.warning('请至少选择一道题目'); return }

  aiSaving.value = true
  let saved = 0
  let failed = 0
  for (const q of selected) {
    try {
      const optionsJson = q.options && q.options.length > 0
        ? JSON.stringify(q.options.map((o, i) => ({ label: String.fromCharCode(65 + i), text: o.replace(/^[A-D][.、\s]+/, '') })))
        : '[]'
      await api.post('/questions', {
        subjectId: parseInt(subjectId),
        type: q.type,
        content: q.content,
        options: optionsJson,
        answer: q.answer,
        analysis: q.analysis || '',
        difficulty: q.difficulty || aiForm.value.difficulty,
      })
      saved++
    } catch {
      failed++
    }
  }
  aiSaving.value = false
  if (saved > 0) {
    ElMessage.success(`成功保存 ${saved} 道题目` + (failed > 0 ? `，${failed} 道失败` : ''))
    showAIPreview.value = false
    resetAIDialog()
    await loadQuestions()
  }
}

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
    examForm.value.title = `${subject.value?.name || '科目'}模拟测试`
    await loadQuestions()
  } catch {}
}

function handleFileChange(file) { importFile.value = file.raw }
function handleFileRemove() { importFile.value = null }

function resetImport() {
  importFile.value = null
  importResult.value = null
  previewText.value = ''
  uploadRef.value?.clearFiles()
}

async function handleImport() {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }

  importing.value = true
  importResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('subjectId', subjectId)

    const config = useRuntimeConfig()
    const headers = {}
    const token = localStorage.getItem('auth_token')
    if (token) headers['Authorization'] = `Bearer ${token}`
    const response = await fetch(`${config.public.apiBase}/import`, { method: 'POST', headers, body: formData })
    const res = await response.json()
    if (res.code !== 200) { ElMessage.error(res.message || '导入失败'); return }
    importResult.value = res.data
    if (res.data.failed === 0) ElMessage.success(res.message)
    else ElMessage.warning(res.message)
    await loadQuestions()
  } catch { ElMessage.error('导入失败')
  } finally { importing.value = false }
}

async function handlePreviewExtract() {
  if (!importFile.value) { ElMessage.warning('请先选择文件'); return }
  previewing.value = true
  previewText.value = ''
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const config = useRuntimeConfig()
    const headers = {}
    const token = localStorage.getItem('auth_token')
    if (token) headers['Authorization'] = `Bearer ${token}`
    const response = await fetch(`${config.public.apiBase}/import/preview`, { method: 'POST', headers, body: formData })
    const res = await response.json()
    if (res.code !== 200) { ElMessage.error(res.message || '预览失败'); return }
    previewText.value = res.data
    if (!res.data) ElMessage.info('未提取到单选题')
  } catch { ElMessage.error('预览失败')
  } finally { previewing.value = false }
}

async function loadQuestions() {
  loadingQuestions.value = true
  try {
    const params = { subjectId, pageSize: 1000 }
    if (qSearch.value.keyword) params.keyword = qSearch.value.keyword
    if (qSearch.value.difficulty) params.difficulty = qSearch.value.difficulty
    if (qSearch.value.sortBy) params.sortBy = qSearch.value.sortBy
    if (qSearch.value.type) params.type = qSearch.value.type
    const data = await api.get('/questions', params)
    questions.value = data.records || data || []
  } catch {
    questions.value = []
  } finally {
    loadingQuestions.value = false
  }
}

function toggleBatch(id) {
  const idx = batchIds.value.indexOf(id)
  if (idx === -1) batchIds.value.push(id)
  else batchIds.value.splice(idx, 1)
}

async function handleDeleteQuestion(id) {
  try {
    await api.delete(`/questions/${id}`)
    questions.value = questions.value.filter(q => q.id !== id)
    batchIds.value = batchIds.value.filter(bid => bid !== id)
  } catch {}
}

async function handleBatchDelete() {
  try {
    await api.delete('/questions/batch', batchIds.value)
    ElMessage.success('批量删除成功')
    batchIds.value = []
    await loadQuestions()
  } catch {}
}

async function handleBatchUpdateDifficulty() {
  try {
    await api.put('/questions/batch', { ids: batchIds.value, difficulty: batchDifficulty.value })
    ElMessage.success('批量修改难度成功')
    showBatchDifficulty.value = false
    batchIds.value = []
    await loadQuestions()
  } catch {}
}

function startPractice() { navigateTo(`/practice/${subjectId}`) }

async function handleCreateExam() {
  const examPaper = await api.post('/exams/generate', {
    subjectId: parseInt(subjectId),
    title: examForm.value.title,
    duration: examForm.value.duration,
    totalCount: examForm.value.totalCount,
  })
  showExamDialog.value = false
  await useExamStore().startExam(examPaper.id)
  navigateTo(`/exam/start/${examPaper.id}`)
}

onMounted(load)
</script>

<style scoped>
.back-btn { margin-bottom: 8px; }
.subject-title-row { display: flex; justify-content: space-between; align-items: center; margin: 8px 0 4px; flex-wrap: wrap; gap: 10px; }
.subject-title-row h2 { font-size: 24px; font-weight: 700; color: #1e293b; }
.title-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.question-panel { background: #fff; border: 1px solid #e8e5df; border-radius: 12px; box-shadow: 0 1px 3px rgba(22,27,43,0.04); overflow: hidden; }
.panel-header { display: flex; align-items: center; gap: 10px; padding: 16px 20px; border-bottom: 1px solid #f0ede7; font-weight: 650; color: #1e293b; }
.panel-count { font-size: 12px; color: #94a3b8; font-weight: 500; }
.q-toolbar { display: flex; gap: 10px; padding: 14px 20px; border-bottom: 1px solid #f1f5f9; flex-wrap: wrap; }
.q-batch-bar { display: flex; align-items: center; gap: 8px; padding: 8px 12px; margin: 12px 20px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; font-size: 13px; color: #1e40af; flex-wrap: wrap; }
.q-batch-bar span { font-weight: 600; }
.question-list { max-height: 62vh; overflow-y: auto; }
.q-item { display: flex; align-items: center; gap: 10px; padding: 12px 20px; border-bottom: 1px solid #f1f5f9; font-size: 13px; }
.q-item:hover { background: #f8fafc; }
.q-index { width: 24px; height: 24px; border-radius: 6px; background: #e2e8f0; color: #475569; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.q-type { font-size: 12px; padding: 2px 6px; border-radius: 4px; background: #eff6ff; color: #3b5dbf; flex-shrink: 0; }
.q-diff { font-size: 11px; padding: 1px 5px; border-radius: 4px; background: #fef6ee; color: #e0781a; flex-shrink: 0; font-weight: 600; }
.q-content { flex: 1; color: #334155; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.q-answer { font-weight: 600; color: #15803d; flex-shrink: 0; }
.judge-answer { color: #dc2626; }
.upload-icon { color: #3b5dbf; margin-bottom: 8px; }
.upload-text { font-size: 14px; color: #334155; margin-bottom: 4px; }
.upload-hint { font-size: 12px; color: #94a3b8; }
.upload-hint--format { margin-top: 6px; line-height: 1.6; }
.import-result { margin-top: 16px; padding: 16px; background: #fafaf8; border-radius: 8px; border: 1px solid #e8e5df; }
.result-title { font-weight: 650; font-size: 14px; color: #1e293b; margin-bottom: 8px; }
.result-stats { display: flex; gap: 16px; font-size: 13px; color: #64748b; }
.stat-success { color: #15803d; font-weight: 600; }
.stat-fail { color: #dc2626; font-weight: 600; }
.result-errors { margin-top: 10px; max-height: 150px; overflow-y: auto; }
.error-item { font-size: 12px; color: #dc2626; padding: 4px 0; border-bottom: 1px solid #fee2e2; }
.preview-box { margin-top: 10px; padding: 14px; background: #fafaf8; border: 1px solid #e8e5df; border-radius: 8px; max-height: 400px; overflow-y: auto; }
.preview-content { margin: 0; font-size: 13px; line-height: 1.7; color: #334155; white-space: pre-wrap; word-break: break-all; font-family: 'Menlo', 'Consolas', monospace; }
.ai-preview-toolbar { display: flex; align-items: center; gap: 16px; padding: 10px 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 16px; }
.ai-preview-count { flex: 1; font-size: 13px; color: #64748b; }
.ai-preview-list { max-height: 55vh; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; }
.ai-question-card { padding: 16px; border: 1px solid #e8e5df; border-radius: 10px; background: #fafaf8; transition: box-shadow 0.2s; }
.ai-question-card:hover { box-shadow: 0 2px 8px rgba(22,27,43,0.06); }
.ai-q-top { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.ai-q-index { width: 28px; height: 28px; border-radius: 7px; background: linear-gradient(135deg, #e6a23c, #f0a842); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; flex-shrink: 0; }
.ai-q-type { font-size: 12px; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; }
.type-single { background: #eff6ff; color: #3b5dbf; }
.type-multiple { background: #fef3c7; color: #d97706; }
.type-judge { background: #f0fdf4; color: #15803d; }
.ai-q-diff { font-size: 12px; color: #94a3b8; }
.ai-q-content { font-size: 14px; color: #1e293b; line-height: 1.6; margin-bottom: 8px; }
.ai-q-options { display: flex; flex-wrap: wrap; gap: 6px 16px; margin-bottom: 8px; }
.ai-option { font-size: 13px; color: #475569; background: #fff; padding: 2px 10px; border-radius: 4px; border: 1px solid #e2e8f0; }
.ai-q-answer, .ai-q-analysis { font-size: 13px; line-height: 1.5; margin-top: 4px; }
.ai-label { font-weight: 600; color: #64748b; }
.ai-q-answer .ai-value { color: #15803d; font-weight: 600; }
.ai-q-analysis .ai-value { color: #475569; }
</style>
