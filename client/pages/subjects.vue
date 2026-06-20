<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>科目管理</h2>
        <p>管理你的学习科目，进入科目开始刷题</p>
      </div>
      <el-button type="primary" size="large" @click="showCreate = true">
        <el-icon style="margin-right: 6px"><Plus /></el-icon>添加科目
      </el-button>
    </div>

    <div class="card-grid">
      <div
        v-for="sub in subjects"
        :key="sub.id"
        class="subject-card"
        @click="navigateTo(`/subject/${sub.id}`)"
      >
        <div class="card-accent"></div>
        <div class="card-icon">{{ sub.icon || '📚' }}</div>
        <h3>{{ sub.name }}</h3>
        <p>{{ sub.description || '暂无描述' }}</p>
        <div class="card-actions" @click.stop>
          <el-button size="small" @click="navigateTo(`/subject/${sub.id}`)">进入学习</el-button>
          <el-button size="small" @click="editSubject(sub)">编辑</el-button>
          <el-popconfirm title="确定删除此科目？" @confirm="handleDelete(sub.id)">
            <template #reference>
              <el-button size="small" type="danger" :icon="Delete" />
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && subjects.length === 0" description="还没有科目，点击上方按钮添加" />

    <!-- 对话框 -->
    <el-dialog v-model="showCreate" :title="editing ? '编辑科目' : '添加科目'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="科目名称">
          <el-input v-model="form.name" placeholder="如：高等数学" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="科目描述（可选）" :rows="3" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如：📐" maxlength="2" style="width: 100px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const api = useApi()
const subjects = ref([])
const loading = ref(false)
const showCreate = ref(false)
const editing = ref(null)
const form = ref({ name: '', description: '', icon: '' })

async function loadSubjects() {
  loading.value = true
  try { subjects.value = await api.get('/subjects') } finally { loading.value = false }
}

function editSubject(sub) {
  editing.value = sub
  form.value = { name: sub.name, description: sub.description || '', icon: sub.icon || '' }
  showCreate.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入科目名称'); return }
  if (editing.value) await api.put(`/subjects/${editing.value.id}`, form.value)
  else await api.post('/subjects', form.value)
  showCreate.value = false
  editing.value = null
  form.value = { name: '', description: '', icon: '' }
  await loadSubjects()
}

async function handleDelete(id) {
  await api.delete(`/subjects/${id}`)
  await loadSubjects()
}

onMounted(loadSubjects)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.subject-card {
  position: relative;
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.subject-card:hover {
  box-shadow: 0 12px 32px rgba(22,27,43,0.1);
  transform: translateY(-4px);
}

.card-accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, #3b5dbf, #5876d8);
  opacity: 0;
  transition: opacity 0.25s;
}

.subject-card:hover .card-accent {
  opacity: 1;
}

.card-icon {
  font-size: 42px;
  margin-bottom: 14px;
  line-height: 1;
}

.subject-card h3 {
  font-size: 17px;
  font-weight: 650;
  color: #1e293b;
  margin-bottom: 6px;
}

.subject-card p {
  font-size: 13.5px;
  color: #94a3b8;
  margin-bottom: 18px;
  line-height: 1.5;
  min-height: 40px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
