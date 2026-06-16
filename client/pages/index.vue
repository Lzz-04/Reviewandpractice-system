<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>学习仪表盘</h2>
        <p>欢迎回来，继续你的期末复习之旅</p>
      </div>
      <div class="welcome-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="dashboard-grid">
      <button class="quick-btn primary" @click="navigateTo('/subjects')">
        <div class="q-icon"><el-icon :size="28"><Collection /></el-icon></div>
        <div class="q-info">
          <span class="q-title">科目管理</span>
          <span class="q-desc">浏览科目、章节，管理题库</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
      <button class="quick-btn success" @click="navigateTo('/exam')">
        <div class="q-icon"><el-icon :size="28"><Tickets /></el-icon></div>
        <div class="q-info">
          <span class="q-title">模拟考试</span>
          <span class="q-desc">智能组卷，检验学习成果</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
      <button class="quick-btn warning" @click="navigateTo('/wrongbook')">
        <div class="q-icon"><el-icon :size="28"><WarningFilled /></el-icon></div>
        <div class="q-info">
          <span class="q-title">错题本</span>
          <span class="q-desc">查漏补缺，攻克薄弱环节</span>
        </div>
        <el-icon class="q-arrow"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Collection, Tickets, WarningFilled, ArrowRight } from '@element-plus/icons-vue'

function navigateTo(path) {
  useRouter().push(path)
}
</script>

<style scoped>
.dashboard {
  padding: 24px 28px;
  max-width: 800px;
  margin: 0 auto;
  animation: pageIn 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Welcome */
.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.welcome-text h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.3px;
  margin-bottom: 4px;
}

.welcome-text p {
  font-size: 14.5px;
  color: #64748b;
}

.welcome-decoration {
  display: flex;
  gap: 8px;
}

.deco-circle {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: float 3s ease-in-out infinite;
}

.deco-circle.c1 { background: #3b5dbf; animation-delay: 0s; }
.deco-circle.c2 { background: #e0781a; animation-delay: 0.5s; }
.deco-circle.c3 { background: #22c55e; animation-delay: 1s; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* Dashboard Grid */
.dashboard-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Quick Action Cards */
.quick-btn {
  display: flex;
  align-items: center;
  gap: 18px;
  width: 100%;
  padding: 22px 24px;
  border: 1px solid #e8e5df;
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(22,27,43,0.04);
}

.quick-btn:hover {
  transform: translateX(6px);
  box-shadow: 0 8px 24px rgba(22,27,43,0.08);
}

.quick-btn.primary:hover {
  border-color: #3b5dbf;
  background: #f5f6fc;
}

.quick-btn.success:hover {
  border-color: #15803d;
  background: #ecfdf5;
}

.quick-btn.warning:hover {
  border-color: #d97706;
  background: #fffbeb;
}

.q-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-btn.primary .q-icon { background: #e9eefa; color: #3b5dbf; }
.quick-btn.success .q-icon { background: #ecfdf5; color: #15803d; }
.quick-btn.warning .q-icon { background: #fef6ee; color: #d97706; }

.q-info {
  flex: 1;
  min-width: 0;
}

.q-title {
  display: block;
  font-size: 16px;
  font-weight: 650;
  color: #1e293b;
  margin-bottom: 3px;
}

.q-desc {
  display: block;
  font-size: 13px;
  color: #94a3b8;
}

.q-arrow {
  font-size: 18px;
  color: #cbd5e1;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.quick-btn:hover .q-arrow {
  transform: translateX(3px);
  color: #94a3b8;
}
</style>
