#!/bin/bash
# =============================================================================
# GitHub Issues 一键创建脚本
# 前提: 已安装 GitHub CLI (https://cli.github.com/)
#      已执行 gh auth login
# 用法: bash github-issues.sh
# =============================================================================
set -e

REPO="exam-review-system"

# ---- 检查 gh 是否可用 ----
if ! command -v gh &> /dev/null; then
    echo "请先安装 GitHub CLI: https://cli.github.com/"
    exit 1
fi

# ---- 检查登录状态 ----
if ! gh auth status &> /dev/null; then
    echo "请先登录: gh auth login"
    exit 1
fi

echo "============================================"
echo "  GitHub Issues 批量创建"
echo "============================================"

# ---- Step 1: 创建仓库 ----
echo ""
echo "Step 1: 创建仓库 $REPO ..."
gh repo create "$REPO" --public --description "大学生期末复习刷题系统 - Spring Boot 3 + Nuxt 3 + MySQL" --clone 2>/dev/null || echo "  仓库已存在或创建失败，跳过"

# ---- Step 2: 创建 Labels ----
echo ""
echo "Step 2: 创建 Labels ..."

declare -A LABELS=(
    ["backend"]="0E8A16,Spring Boot 后端"
    ["frontend"]="1D76DB,Nuxt 3 前端"
    ["auth"]="D93F0B,认证授权模块"
    ["exam"]="C5DEF5,考试系统模块"
    ["wrongbook"]="FBCA04,错题本模块"
    ["practice"]="B60205,练习模块"
    ["import"]="5319E7,文件导入模块"
    ["ai"]="006B75,AI 出题模块"
    ["question"]="E99695,题库管理模块"
    ["stats"]="FEF2C0,学习统计模块"
    ["bug"]="D73A4A,Bug 报告"
    ["enhancement"]="A2EEEF,功能增强"
    ["documentation"]="0075CA,文档相关"
    ["good first issue"]="7057FF,适合新手"
)

for label in "${!LABELS[@]}"; do
    IFS=',' read -r color desc <<< "${LABELS[$label]}"
    gh label create "$label" --color "$color" --description "$desc" --repo "$REPO" 2>/dev/null && echo "  ✅ $label" || echo "  ⚠️ $label (可能已存在)"
done

# ---- Step 3: 创建 Milestones ----
echo ""
echo "Step 3: 创建 Milestones ..."

gh api repos/:owner/$REPO/milestones -f title="v1.0 基础功能" -f description="科目/章节/题库 CRUD + 登录注册" -f due_on="2025-07-01T00:00:00Z" 2>/dev/null && echo "  ✅ v1.0 基础功能" || echo "  ⚠️ v1.0"
gh api repos/:owner/$REPO/milestones -f title="v1.1 考试系统" -f description="组卷/答题/暂停恢复/评分" -f due_on="2025-07-15T00:00:00Z" 2>/dev/null && echo "  ✅ v1.1 考试系统" || echo "  ⚠️ v1.1"
gh api repos/:owner/$REPO/milestones -f title="v1.2 错题本+统计" -f description="错题本 + 学习统计 + 文件导入" -f due_on="2025-08-01T00:00:00Z" 2>/dev/null && echo "  ✅ v1.2 错题本+统计" || echo "  ⚠️ v1.2"
gh api repos/:owner/$REPO/milestones -f title="v1.3 AI 增强" -f description="AI 出题 + 智能分析" -f due_on="2025-08-15T00:00:00Z" 2>/dev/null && echo "  ✅ v1.3 AI 增强" || echo "  ⚠️ v1.3"

# ---- Step 4: 获取 Milestone IDs ----
echo ""
echo "Step 4: 创建 Issues ..."

M1=$(gh api repos/:owner/$REPO/milestones --jq '.[] | select(.title=="v1.0 基础功能") | .number' 2>/dev/null || echo "")
M2=$(gh api repos/:owner/$REPO/milestones --jq '.[] | select(.title=="v1.1 考试系统") | .number' 2>/dev/null || echo "")
M3=$(gh api repos/:owner/$REPO/milestones --jq '.[] | select(.title=="v1.2 错题本+统计") | .number' 2>/dev/null || echo "")
M4=$(gh api repos/:owner/$REPO/milestones --jq '.[] | select(.title=="v1.3 AI 增强") | .number' 2>/dev/null || echo "")

# ---- 创建 Issues ----
create_issue() {
    local title="$1" body="$2" labels="$3" milestone="$4"
    local m_flag=""
    [ -n "$milestone" ] && m_flag="--milestone $milestone"
    gh issue create --title "$title" --body "$body" --label "$labels" $m_flag --repo "$REPO" 2>/dev/null && echo "  ✅ $title" || echo "  ❌ $title"
}

# --- Auth ---
create_issue "[Auth] 用户登录/注册功能（后端）" \
"## 需求
- 用户注册（用户名、密码、昵称）
- 用户登录（JWT Token 颁发）
- 获取当前用户信息
## 验收标准
- [x] 注册参数校验（用户名2-50字符，密码6-100字符）
- [x] 重复用户名检测
- [x] BCrypt 密码加密
- [x] JWT Token 24h 过期
- [x] 登录错误统一提示" \
"auth,backend" "$M1"

create_issue "[Auth] 前端登录/注册页面" \
"## 需求
- 登录页面 + 注册页面
- 路由守卫：未登录跳转登录页
- Token localStorage 持久化
## 验收标准
- [x] 表单校验
- [x] 401 自动跳转登录页" \
"auth,frontend" "$M1"

# --- Subject ---
create_issue "[Subject] 科目增删改查" \
"## 需求
- 科目列表/新建/编辑/删除
- 同名检测（创建和编辑时均校验）
- 级联保护：有章节时禁止删除" \
"backend,frontend" "$M1"

# --- Chapter ---
create_issue "[Chapter] 章节增删改查" \
"## 需求
- 按科目展示章节列表
- 新建章节（自动排序）· 编辑· 删除
- 级联保护：有题目或错题时禁止删除" \
"backend,frontend" "$M1"

# --- Question ---
create_issue "[Question] 题目增删改查 + 批量操作" \
"## 需求
- 分页列表 + 多条件筛选（科目/章节/题型/难度/关键词）
- 新建/编辑/删除题目（级联删除关联数据）
- 批量删除（事务回滚）· 批量更新
- 随机抽题
## 验收标准
- [x] 三种题型（single/multiple/judge）
- [x] 批量操作事务原子性" \
"question,backend,frontend" "$M1"

# --- Exam ---
create_issue "[Exam] 自动组卷" \
"## 需求
- 按科目+可选章节筛选题目池
- 自动组卷（打乱+按题型循环分配）
- 题目不足时提示错误" \
"exam,backend" "$M2"

create_issue "[Exam] 考试答题流程（开始/暂停/恢复/交卷）" \
"## 需求
- 开始考试 · 答题卡导航
- 暂停（保存剩余时间）· 恢复
- 交卷 → 自动评分 + 同步错题本
## 验收标准
- [x] sessionId 精确关联答题记录
- [x] 暂停/恢复正确保存时间
- [x] 交卷百分制评分" \
"exam,backend,frontend" "$M2"

create_issue "[Exam] 考试结果页面" \
"## 需求
- 得分环形图 · 逐题解析
- 正确/错误题目高亮区分" \
"exam,frontend" "$M2"

create_issue "[Exam] 考试倒计时组件" \
"## 需求
- 可视化倒计时
- 警告状态（<5分钟）· 危急状态（<1分钟脉冲动画）
- 时间到自动交卷" \
"exam,frontend" "$M2"

# --- Practice ---
create_issue "[Practice] 章节练习" \
"## 需求
- 按章节练习（顺序/随机/错题三种模式）
- 逐题展示 + 实时反馈
- QuestionCard 通用组件
- 错题自动同步" \
"practice,backend,frontend" "$M1"

# --- WrongBook ---
create_issue "[WrongBook] 错题本管理" \
"## 需求
- 错题分页列表 + 按科目/掌握状态筛选
- 复习标记 · 掌握/取消掌握 · 移除
- 统计（总数/已掌握/未掌握）
- 所有权校验（仅允许操作自己的错题）" \
"wrongbook,backend,frontend" "$M3"

# --- Stats ---
create_issue "[Stats] 学习统计仪表盘" \
"## 需求
- 概览卡片 + 正确率趋势（7日折线图）
- 每日活跃度（30日柱状图）
- 各科目进度（ECharts）" \
"stats,backend,frontend" "$M3"

# --- Import ---
create_issue "[Import] 文件批量导入题目" \
"## 需求
- 支持 XLSX/DOCX/PDF/TXT 四种格式
- 标准格式 + 试卷格式两种解析模式
- 导入预览 + 结果反馈（成功/失败计数）
## 验收标准
- [x] 四种格式均能正确解析
- [x] 携带 JWT Token" \
"import,backend,frontend" "$M3"

# --- AI ---
create_issue "[AI] DeepSeek 自动出题" \
"## 需求
- 调用 DeepSeek API 生成题目
- 支持指定科目/章节/题型/数量" \
"ai,backend" "$M4"

# --- Bugs ---
create_issue "[Bug] Map.of(null) 导致 401 返回 HTML 而非 JSON" \
"## 问题
SecurityConfig 中 Map.of() 传入 null 值导致 NPE
## 修复
替换为 HashMap（允许 null 值）
## 影响文件
server/.../config/SecurityConfig.java" \
"bug,auth,backend" ""

create_issue "[Bug] 导入功能未携带 Token" \
"## 问题
前端 handleImport/handlePreviewExtract 使用原生 fetch 但未携带 Authorization 头
## 修复
从 localStorage 读取 Token 并添加到请求头
## 影响文件
client/pages/subject/[id].vue" \
"bug,import,frontend" ""

create_issue "[Enhancement] 添加 HttpMessageNotReadableException 处理器" \
"## 问题
非法 JSON 请求返回 500，应返回 400
## 修复
GlobalExceptionHandler 新增 HttpMessageNotReadableException 处理器" \
"enhancement,backend" ""

echo ""
echo "============================================"
echo "  🎉 全部完成！"
echo "  https://github.com/$(gh api user --jq .login)/$REPO/issues"
echo "============================================"
