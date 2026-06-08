<template>
  <div class="practice-container">
    <el-page-header @back="$router.back()" title="章节练习" />
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <div v-else-if="finished" class="result-summary">
      <h2>练习完成！</h2>
      <p>正确数: {{ correctCount }}</p>
      <p>错误数: {{ wrongCount }}</p>
      <el-button type="primary" @click="$router.push('/subjects')">返回科目</el-button>
    </div>

    <div v-else class="practice-main">
      <div class="progress">
        <el-progress :percentage="progress" />
        <span>{{ currentIndex + 1 }} / {{ questions.length }}</span>
      </div>

      <QuestionCard 
        :question="currentQuestion" 
        :show-result="showResult"
        :is-correct="lastResult"
      />

      <div class="actions">
        <el-button v-if="!showResult" type="primary" @click="submitAnswer">提交答案</el-button>
        <el-button v-else type="success" @click="nextQuestion">下一题</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import QuestionCard from '~/components/question/QuestionCard.vue'

const route = useRoute()
const chapterId = route.params.chapterId

const questions = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const finished = ref(false)
const showResult = ref(false)
const lastResult = ref(false)
const correctCount = ref(0)
const wrongCount = ref(0)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const progress = computed(() => (currentIndex.value / questions.value.length) * 100)

const submitAnswer = async () => {
  // 模拟提交
  showResult.value = true
  // 这里调用后端接口
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    showResult.value = false
  } else {
    finished.value = true
  }
}
</script>

<style scoped>
.practice-container {
  max-width: 800px;
  margin: 20px auto;
}
.practice-main {
  margin-top: 30px;
}
.progress {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.actions {
  margin-top: 30px;
  text-align: center;
}
</style>
