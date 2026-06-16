<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理后台</h2>
      <p>查看所有用户的题库与学习数据</p>
    </div>

    <!-- 用户列表 -->
    <div class="list-panel">
      <div class="list-header">用户列表（{{ users.length }} 人）</div>
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subjectCount" label="科目" width="70" align="center" />
        <el-table-column prop="questionCount" label="题目" width="70" align="center" />
        <el-table-column prop="examCount" label="考试" width="70" align="center" />
        <el-table-column prop="wrongCount" label="错题" width="70" align="center" />
        <el-table-column prop="answerCount" label="答题" width="70" align="center" />
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">{{ row.createdAt?.substring(0, 16) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              :title="`确定删除用户「${row.username}」及其全部数据吗？此操作不可撤销！`"
              confirm-button-text="确定删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button size="small" type="danger" :disabled="row.role === 'admin'">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const api = useApi()
const users = ref([])
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    users.value = await api.get('/admin/users')
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  try {
    await api.delete(`/admin/users/${row.id}`)
    ElMessage.success(`用户「${row.username}」已删除`)
    await loadUsers()
  } catch {}
}

onMounted(loadUsers)
</script>

<style scoped>
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
</style>
