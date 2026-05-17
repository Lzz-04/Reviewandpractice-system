<template>
  <div class="page-container">
    <div class="page-header">
      <h2>模拟考试</h2>
      <p>创建和管理模拟考试，检验学习成果</p>
    </div>

    <!-- 快速组卷 -->
    <div class="gen-panel">
      <div class="gen-header">
        <el-icon><Tickets /></el-icon>
        <span>快速组卷</span>
      </div>
      <div class="gen-form">
        <el-select v-model="genForm.subjectId" placeholder="选择科目" @change="loadChapters" size="large" style="width: 160px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-input v-model="genForm.title" placeholder="考试标题，如：期末模拟测试" size="large" style="width: 220px" />
        <div class="gen-form-num">
          <span class="num-label">时长</span>
          <el-input-number v-model="genForm.duration" :min="5" :max="180" size="large" />
          <span class="num-unit">分钟</span>
        </div>
        <div class="gen-form-num">
          <span class="num-label">题数</span>
          <el-input-number v-model="genForm.totalCount" :min="5" :max="100" size="large" />
        </div>
        <el-button type="primary" size="large" @click="handleGenerate">生成试卷</el-button>
      </div>
    </div>

    <!-- 试卷列表 -->
    <div class="list-panel">
      <div class="list-header">试卷列表</div>
      <el-table :data="exams" stripe v-loading="loading" class="exam-table">
        <el-table-column prop="id" label="ID" width="65" />
        <el-table-column prop="title" label="试卷名称">
          <template #default="{ row }">
            <span class="exam-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="90">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题数" width="70" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="startExam(row)">开始考试</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && exams.length === 0" description="暂无试卷" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Tickets } from '@element-plus/icons-vue'

const api = useApi()
const examStore = useExamStore()
const subjects = ref([])
const exams = ref([])
const loading = ref(false)
const genForm = ref({ subjectId: null, title: '', duration: 30, totalCount: 20 })

async function loadSubjects() { subjects.value = await api.get('/subjects').catch(() => []) }
async function loadExams() {
  loading.value = true
  try {
    const page = await api.get('/exams', { pageSize: 100 })
    exams.value = page.records || []
  } finally { loading.value = false }
}
const chapters = ref([])

async function loadChapters() {
  if (!genForm.value.subjectId) { chapters.value = []; return }
  chapters.value = await api.get(`/subjects/${genForm.value.subjectId}/chapters`).catch(() => [])
}

async function handleGenerate() {
  if (!genForm.value.subjectId) { ElMessage.warning('请选择科目'); return }
  if (!genForm.value.title.trim()) { ElMessage.warning('请输入考试标题'); return }
  const paper = await api.post('/exams/generate', genForm.value)
  await loadExams()
  ElMessage.success('组卷成功')
  navigateTo(`/exam/start/${paper.id}`)
}

async function startExam(row) {
  navigateTo(`/exam/start/${row.id}`)
}

async function handleDelete(id) { await api.delete(`/exams/${id}`); await loadExams() }

onMounted(async () => { await Promise.all([loadSubjects(), loadExams()]) })
</script>

<style scoped>
/* Generate Panel */
.gen-panel {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  margin-bottom: 18px;
  overflow: hidden;
}

.gen-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
  font-weight: 650;
  font-size: 15px;
  color: #3b5dbf;
}

.gen-form {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.gen-form-num {
  display: flex;
  align-items: center;
  gap: 6px;
}

.num-label { font-size: 13px; color: #94a3b8; }
.num-unit { font-size: 13px; color: #94a3b8; margin-left: 4px; }

/* List Panel */
.list-panel {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
}

.list-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
  font-weight: 650;
  font-size: 15px;
  color: #1e293b;
}

.exam-title {
  font-weight: 550;
  color: #1e293b;
}

.exam-table {
  --el-table-border-color: transparent;
}
</style>
