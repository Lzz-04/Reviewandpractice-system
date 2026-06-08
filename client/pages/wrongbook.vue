<template>
  <div class="wrong-book">
    <h1>错题本</h1>
    
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>总错题数</template>
            <div class="stat-value">{{ stats.total }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>已掌握</template>
            <div class="stat-value text-success">{{ stats.mastered }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>未掌握</template>
            <div class="stat-value text-danger">{{ stats.unMastered }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="filter-bar">
      <el-select v-model="filter.subjectId" placeholder="选择科目" clearable>
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-checkbox v-model="filter.onlyUnmastered">仅看未掌握</el-checkbox>
    </div>

    <div class="wrong-list">
      <el-empty v-if="questions.length === 0" description="暂无错题" />
      <div v-for="q in questions" :key="q.id" class="wrong-item">
        <QuestionCard :question="q" :show-result="true" />
        <div class="item-actions">
          <el-button size="small" @click="toggleMaster(q)">标记为已掌握</el-button>
          <el-button size="small" type="danger" @click="remove(q)">移除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import QuestionCard from '~/components/question/QuestionCard.vue'

const stats = ref({ total: 0, mastered: 0, unMastered: 0 })
const filter = ref({ subjectId: null, onlyUnmastered: true })
const questions = ref([])
const subjects = ref([])

const toggleMaster = (q) => {
  // 调用接口切换掌握状态
}

const remove = (q) => {
  // 调用接口移除错题
}
</script>

<style scoped>
.wrong-book {
  padding: 20px;
}
.stats-cards {
  margin-bottom: 30px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
}
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}
.wrong-item {
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
}
.item-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
