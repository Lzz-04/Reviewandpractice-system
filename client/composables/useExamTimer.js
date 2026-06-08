export const useExamTimer = (durationMinutes, onTimeUp) => {
  const remaining = ref(durationMinutes * 60)
  const isRunning = ref(false)
  let timer = null

  const formatTime = computed(() => {
    const m = Math.floor(remaining.value / 60)
    const s = remaining.value % 60
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  })

  const minutes = computed(() => Math.floor(remaining.value / 60))

  const start = () => {
    isRunning.value = true
    timer = setInterval(() => {
      remaining.value--
      if (remaining.value <= 0) {
        stop()
        onTimeUp?.()
      }
    }, 1000)
  }

  const stop = () => {
    isRunning.value = false
    clearInterval(timer)
    timer = null
  }

  const pause = () => {
    isRunning.value = false
    clearInterval(timer)
    timer = null
  }

  onUnmounted(() => stop())

  return { remaining, formatTime, minutes, isRunning, start, stop, pause }
}
