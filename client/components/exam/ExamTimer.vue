<template>
  <div class="timer" :class="{ 'timer--warn': warning, 'timer--critical': critical }">
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" class="timer-icon">
      <circle cx="12" cy="13" r="8" stroke="currentColor" stroke-width="1.8"/>
      <path d="M12 8v5l3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
      <path d="M9 2h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      <path d="M12 2v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    </svg>
    <span class="timer-display">{{ formatTime }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  remaining: { type: Number, default: 0 },
  formatTime: { type: String, default: '00:00' },
  warning: { type: Boolean, default: false },
})

const critical = computed(() => props.remaining < 60)
</script>

<style scoped>
.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f3f5fc;
  border-radius: 10px;
  color: #3b5dbf;
  transition: all 0.3s;
}

.timer-icon {
  flex-shrink: 0;
}

.timer-display {
  font-size: 22px;
  font-weight: 700;
  font-family: 'SF Mono', 'Cascadia Code', 'Consolas', monospace;
  letter-spacing: 1px;
  font-variant-numeric: tabular-nums;
}

.timer--warn {
  background: #fffbeb;
  color: #d97706;
}

.timer--critical {
  background: #fef2f2;
  color: #dc2626;
  animation: pulse 0.8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.65; }
}
</style>
