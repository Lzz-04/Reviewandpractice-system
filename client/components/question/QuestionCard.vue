<template>
  <div class="question-card">
    <div class="question-header">
      <el-tag>{{ typeLabel }}</el-tag>
      <span class="difficulty">难度: {{ question.difficulty }}</span>
    </div>
    
    <div class="question-content">
      <p>{{ question.content }}</p>
    </div>

    <div class="options">
      <!-- 单选 -->
      <el-radio-group v-if="question.type === 'single'" v-model="userAnswer" :disabled="showResult">
        <el-radio v-for="(opt, key) in question.options" :key="key" :label="key">
          {{ key }}. {{ opt }}
        </el-radio>
      </el-radio-group>

      <!-- 多选 -->
      <el-checkbox-group v-else-if="question.type === 'multiple'" v-model="userAnswerList" :disabled="showResult">
        <el-checkbox v-for="(opt, key) in question.options" :key="key" :label="key">
          {{ key }}. {{ opt }}
        </el-checkbox>
      </el-checkbox-group>

      <!-- 判断 -->
      <el-radio-group v-else-if="question.type === 'judge'" v-model="userAnswer" :disabled="showResult">
        <el-radio label="T">正确</el-radio>
        <el-radio label="F">错误</el-radio>
      </el-radio-group>
    </div>

    <div v-if="showResult" class="result-area">
      <el-alert
        :title="isCorrect ? '回答正确' : '回答错误'"
        :type="isCorrect ? 'success' : 'error'"
        :closable="false"
        show-icon
      />
      <div class="analysis">
        <p><strong>正确答案:</strong> {{ question.correctAnswer }}</p>
        <p><strong>解析:</strong> {{ question.analysis }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  question: Object,
  showResult: Boolean,
  isCorrect: Boolean
})

const userAnswer = ref('')
const userAnswerList = ref([])

const typeLabel = computed(() => {
  const types = { single: '单选题', multiple: '多选题', judge: '判断题' }
  return types[props.question.type] || '未知'
})
</script>

<style scoped>
.question-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}
.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.question-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}
.options {
  margin-bottom: 20px;
}
.result-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #dcdfe6;
}
.analysis {
  margin-top: 15px;
  color: #606266;
}
</style>
