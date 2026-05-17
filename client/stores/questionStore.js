import { defineStore } from 'pinia'

export const useQuestionStore = defineStore('question', () => {
  const sessionId = ref('')
  const questions = ref([])
  const currentIndex = ref(0)
  const answers = ref({})
  const results = ref({})
  const mode = ref('random')

  const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

  const progress = computed(() => ({
    current: currentIndex.value + 1,
    total: questions.value.length,
    percent: questions.value.length > 0 ? Math.round(((currentIndex.value + 1) / questions.value.length) * 100) : 0,
  }))

  const answeredCount = computed(() => Object.keys(answers.value).length)

  async function startSession(chapterId, practiceMode = 'random', wrongOnly = false) {
    const api = useApi()
    const data = await api.get(`/practice/start/${chapterId}`, { mode: practiceMode, wrongOnly })
    sessionId.value = data.sessionId
    questions.value = data.questions
    currentIndex.value = 0
    answers.value = {}
    results.value = {}
    mode.value = practiceMode
  }

  async function startWrongSession(subjectId, unMasteredOnly = false) {
    const api = useApi()
    const data = await api.get(`/practice/wrong/${subjectId}`, { unMasteredOnly })
    sessionId.value = data.sessionId
    questions.value = data.questions
    currentIndex.value = 0
    answers.value = {}
    results.value = {}
    mode.value = 'wrong'
  }

  async function submitAnswer(questionId, selectedAnswer, timeSpent = 0) {
    const api = useApi()
    const result = await api.post('/practice/answer', {
      questionId, sessionId: sessionId.value, selectedAnswer, timeSpent,
    })
    results.value[questionId] = result
    answers.value[questionId] = selectedAnswer
    return result
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

  function reset() {
    sessionId.value = ''
    questions.value = []
    currentIndex.value = 0
    answers.value = {}
    results.value = {}
  }

  return {
    sessionId, questions, currentIndex, answers, results, mode,
    currentQuestion, progress, answeredCount,
    startSession, startWrongSession, submitAnswer, nextQuestion, prevQuestion, reset,
  }
})
