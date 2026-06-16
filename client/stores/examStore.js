import { defineStore } from 'pinia'

export const useExamStore = defineStore('exam', () => {
  const examId = ref(null)
  const recordId = ref(null)
  const questions = ref([])
  const currentIndex = ref(0)
  const answers = ref({})
  const duration = ref(0)
  const started = ref(false)
  const finished = ref(false)
  const result = ref(null)

  const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

  const progress = computed(() => ({
    current: currentIndex.value + 1,
    total: questions.value.length,
    percent: questions.value.length > 0 ? Math.round(((currentIndex.value + 1) / questions.value.length) * 100) : 0,
  }))

  const answeredCount = computed(() => Object.keys(answers.value).length)

  async function startExam(eid) {
    const api = useApi()
    const record = await api.post(`/exams/${eid}/start`)
    examId.value = eid
    recordId.value = record.id
    started.value = true
    finished.value = false
    answers.value = {}
    currentIndex.value = 0
    result.value = null
  }

  function setQuestions(qs, dur) {
    questions.value = qs
    duration.value = dur
  }

  function selectAnswer(questionId, answer) {
    answers.value[questionId] = answer
  }

  function nextQuestion() {
    if (currentIndex.value < questions.value.length - 1) {
      currentIndex.value++
    }
  }

  function prevQuestion() {
    if (currentIndex.value > 0) {
      currentIndex.value--
    }
  }

  function jumpTo(index) {
    if (index >= 0 && index < questions.value.length) {
      currentIndex.value = index
    }
  }

  async function submitExam() {
    const api = useApi()
    const answerList = Object.entries(answers.value).map(([qId, sel]) => ({
      questionId: parseInt(qId),
      selectedAnswer: sel,
    }))
    const res = await api.post('/exams/submit', {
      recordId: recordId.value,
      answers: answerList,
    })
    finished.value = true
    result.value = res
    return res
  }

  function reset() {
    examId.value = null
    recordId.value = null
    questions.value = []
    currentIndex.value = 0
    answers.value = {}
    duration.value = 0
    started.value = false
    finished.value = false
    result.value = null
  }

  return {
    examId, recordId, questions, currentIndex, answers, duration, started, finished, result,
    currentQuestion, progress, answeredCount,
    startExam, setQuestions, selectAnswer, nextQuestion, prevQuestion, jumpTo, submitExam, reset,
  }
})
