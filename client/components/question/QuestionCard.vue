<template>
  <div class="q-card">
    <div class="q-header">
      <span class="q-type" :class="'q-type--' + question.type">{{ typeLabel }}</span>
      <span v-if="question.difficulty" class="q-difficulty">
        <span class="q-star" v-for="i in question.difficulty" :key="i">&#9733;</span>
      </span>
    </div>

    <div class="q-content">
      <span class="q-index">{{ index + 1 }}.</span>
      {{ question.content }}
    </div>

    <!-- 单选题 -->
    <template v-if="question.type === 'single'">
      <label
        v-for="opt in parsedOptions"
        :key="opt.label"
        class="q-option"
        :class="{
          'q-option--selected': selected === opt.label,
          'q-option--correct': showResult && isCorrectOption(opt.label),
          'q-option--wrong': showResult && selected === opt.label && !isCorrectOption(opt.label),
          'q-option--disabled': showResult,
        }"
      >
        <input
          type="radio"
          :value="opt.label"
          :checked="selected === opt.label"
          :disabled="showResult"
          @change="$emit('answer', opt.label)"
          class="q-radio"
        />
        <span class="q-option-label">{{ opt.label }}</span>
        <span class="q-option-text">{{ opt.text }}</span>
        <span v-if="showResult && isCorrectOption(opt.label)" class="q-check">&#10003;</span>
      </label>
    </template>

    <!-- 多选题 -->
    <template v-else-if="question.type === 'multiple'">
      <label
        v-for="opt in parsedOptions"
        :key="opt.label"
        class="q-option"
        :class="{
          'q-option--selected': selectedArray.includes(opt.label),
          'q-option--disabled': showResult,
        }"
      >
        <input
          type="checkbox"
          :value="opt.label"
          :checked="selectedArray.includes(opt.label)"
          :disabled="showResult"
          @change="toggleMultiple(opt.label)"
          class="q-checkbox"
        />
        <span class="q-option-label">{{ opt.label }}</span>
        <span class="q-option-text">{{ opt.text }}</span>
      </label>
    </template>

    <!-- 判断题 -->
    <template v-else-if="question.type === 'judge'">
      <label
        v-for="opt in judgeOptions"
        :key="opt.value"
        class="q-option"
        :class="{
          'q-option--selected': selected === opt.value,
          'q-option--disabled': showResult,
        }"
      >
        <input
          type="radio"
          :value="opt.value"
          :checked="selected === opt.value"
          :disabled="showResult"
          @change="$emit('answer', opt.value)"
          class="q-radio"
        />
        <span class="q-option-label">{{ opt.label }}</span>
        <span class="q-option-text">{{ opt.text }}</span>
      </label>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  question: { type: Object, required: true },
  index: { type: Number, default: 0 },
  showResult: { type: Boolean, default: false },
  selected: { type: String, default: '' },
  correctAnswer: { type: String, default: '' },
})

const emit = defineEmits(['answer'])

const typeLabel = computed(() => ({
  single: '单选题', multiple: '多选题', judge: '判断题',
}[props.question.type] || ''))

const parsedOptions = computed(() => {
  try { return JSON.parse(props.question.options || '[]') } catch { return [] }
})

const selectedArray = computed(() => (props.selected || '').split(',').map(s => s.trim()).filter(Boolean))

const judgeOptions = [
  { value: 'T', label: 'T', text: '正确' },
  { value: 'F', label: 'F', text: '错误' },
]

function isCorrectOption(label) {
  const correct = (props.correctAnswer || props.question.answer || '').replace(/\s/g, '').split(',')
  return correct.includes(label)
}

function toggleMultiple(label) {
  const arr = [...selectedArray.value]
  const idx = arr.indexOf(label)
  if (idx >= 0) arr.splice(idx, 1)
  else arr.push(label)
  emit('answer', [...arr].sort().join(','))
}
</script>

<style scoped>
.q-card {
  background: #fff;
  border: 1px solid #e8e5df;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
  overflow: hidden;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0ede7;
}

.q-type {
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 650;
  letter-spacing: 0.3px;
}

.q-type--single { background: #e9eefa; color: #3b5dbf; }
.q-type--multiple { background: #ecfdf5; color: #15803d; }
.q-type--judge { background: #fef6ee; color: #e0781a; }

.q-difficulty { margin-left: auto; }

.q-star {
  color: #f59e0b;
  font-size: 13px;
  letter-spacing: 1px;
}

.q-content {
  padding: 20px 24px;
  font-size: 16px;
  line-height: 1.8;
  color: #1e293b;
  background: #fafaf8;
}

.q-index {
  font-weight: 700;
  color: #3b5dbf;
  margin-right: 2px;
}

.q-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid #f8f6f2;
  cursor: pointer;
  transition: background 0.15s;
}

.q-option:last-child { border-bottom: none; }

.q-option:hover:not(.q-option--disabled) {
  background: #fafaf8;
}

.q-option--selected {
  background: #f3f5fc;
}

.q-option--correct {
  background: #ecfdf5;
}

.q-option--wrong {
  background: #fef2f2;
}

.q-option--disabled {
  cursor: default;
}

.q-radio, .q-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #3b5dbf;
  cursor: pointer;
}

.q-option--disabled .q-radio,
.q-option--disabled .q-checkbox {
  cursor: default;
}

.q-option-label {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f3f5fc;
  color: #3b5dbf;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.q-option--selected .q-option-label {
  background: #3b5dbf;
  color: #fff;
}

.q-option--correct .q-option-label {
  background: #22c55e;
  color: #fff;
}

.q-option--wrong .q-option-label {
  background: #ef4444;
  color: #fff;
}

.q-option-text {
  font-size: 15px;
  color: #334155;
  line-height: 1.5;
}

.q-check {
  margin-left: auto;
  color: #22c55e;
  font-size: 18px;
  font-weight: 700;
}
</style>
