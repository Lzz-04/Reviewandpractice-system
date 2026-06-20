#!/bin/bash
# 直接运行创建 Issues —— 确保 GH_TOKEN 已设置
# 用法: GH_TOKEN=xxx bash github-issues-v2.sh
set -e

REPO="Lzz-04/exam-review-system"

# 验证认证
gh auth status --hostname github.com 2>&1 | grep -q "Logged in" || { echo "请先: gh auth login"; exit 1; }

echo "=== 创建 Labels ==="
gh label create "backend"       --color 0E8A16 -R "$REPO" 2>/dev/null ; echo "  backend"
gh label create "frontend"      --color 1D76DB -R "$REPO" 2>/dev/null ; echo "  frontend"
gh label create "auth"          --color D93F0B -R "$REPO" 2>/dev/null ; echo "  auth"
gh label create "exam"          --color C5DEF5 -R "$REPO" 2>/dev/null ; echo "  exam"
gh label create "wrongbook"     --color FBCA04 -R "$REPO" 2>/dev/null ; echo "  wrongbook"
gh label create "practice"      --color B60205 -R "$REPO" 2>/dev/null ; echo "  practice"
gh label create "import"        --color 5319E7 -R "$REPO" 2>/dev/null ; echo "  import"
gh label create "ai"            --color 006B75 -R "$REPO" 2>/dev/null ; echo "  ai"
gh label create "question"      --color E99695 -R "$REPO" 2>/dev/null ; echo "  question"
gh label create "stats"         --color FEF2C0 -R "$REPO" 2>/dev/null ; echo "  stats"
gh label create "bug"           --color D73A4A -R "$REPO" 2>/dev/null ; echo "  bug"
gh label create "enhancement"   --color A2EEEF -R "$REPO" 2>/dev/null ; echo "  enhancement"
gh label create "documentation" --color 0075CA -R "$REPO" 2>/dev/null ; echo "  documentation"
gh label create "good first issue" --color 7057FF -R "$REPO" 2>/dev/null ; echo "  good first issue"
echo "Labels done!"

echo ""
echo "=== 创建 Issues ==="

gh issue create -R "$REPO" -t "[Auth] 用户登录/注册（后端）" \
  -b "用户注册（用户名/密码/昵称校验）+ 登录颁发JWT Token + 获取当前用户信息。BCrypt加密，JWT 24h过期，登录错误统一提示防枚举。" \
  -l "auth,backend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Auth] 前端登录/注册页面" \
  -b "登录表单 + 注册表单 + 路由守卫（未登录跳转）+ localStorage Token持久化 + 401自动跳转登录页。" \
  -l "auth,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Subject] 科目增删改查" \
  -b "科目列表/新建/编辑/删除。同名检测（创建和编辑时均校验），有章节时禁止删除。" \
  -l "backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Chapter] 章节增删改查" \
  -b "按科目展示章节列表、新建（自动排序）、编辑、删除。有题目或错题时禁止删除。" \
  -l "backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Question] 题库管理（CRUD+批量+筛选+随机）" \
  -b "分页列表（科目/章节/题型/难度/关键词筛选），新建single/multiple/judge题目，编辑/删除（级联），批量删除（事务回滚），批量更新，随机抽题。JSON格式选项存储。" \
  -l "question,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Exam] 自动组卷" \
  -b "按科目+可选章节筛选题目池，自动组卷（打乱+按题型single→multiple→judge循环分配），题目不足时提示错误。" \
  -l "exam,backend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Exam] 考试答题流程（开始/暂停/恢复/交卷）" \
  -b "开始考试生成记录，答题卡导航，暂停保存剩余时间，恢复考试，交卷自动评分（百分制）+同步错题本。sessionId精确关联。" \
  -l "exam,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Exam] 考试结果页面" \
  -b "得分环形图 + 正确/错误统计 + 逐题解析（正确答案+我的答案+解析）。正确/错误高亮区分。" \
  -l "exam,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Exam] 考试倒计时组件（ExamTimer）" \
  -b "可视化倒计时，<5分钟警告（黄色），<1分钟危急（红色脉冲）。时间到自动交卷。useExamTimer composable封装，组件卸载自动清理。" \
  -l "exam,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Practice] 章节练习" \
  -b "按章节练习，顺序/随机/错题三种模式。逐题展示+即时反馈，QuestionCard通用组件（单选/多选/判断），错题自动同步到错题本。" \
  -l "practice,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[WrongBook] 错题本管理" \
  -b "错题分页列表（关联题目信息），按科目/掌握状态筛选。复习标记、掌握/取消掌握、移除。统计（总数/已掌握/未掌握）。所有权校验——仅允许操作自己的错题。" \
  -l "wrongbook,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Stats] 学习统计仪表盘" \
  -b "概览卡片（总题数/已答/正确率/学习天数/今日答题），ECharts正确率趋势（7日折线图），每日活跃度（30日柱状图），各科目进度。数据按userId隔离。" \
  -l "stats,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Import] 文件批量导入题目" \
  -b "支持XLSX/DOCX/PDF/TXT四种格式。标准格式解析（题型/题目/选项/答案/解析标签）+试卷格式解析（题号+行内选项+答案区）。导入预览（不写库）+结果反馈（成功/失败计数+错误详情）。携带JWT Token。" \
  -l "import,backend,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[AI] DeepSeek自动出题" \
  -b "调用DeepSeek API生成题目，支持指定科目/章节/题型/数量，返回标准格式题目。需要错误处理和重试机制。" \
  -l "ai,backend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Bug] SecurityConfig Map.of(null) 导致401返回HTML" \
  -b "## 问题 Spring Security未认证时custom authenticationEntryPoint中Map.of('data',null)抛出NPE，Tomcat返回HTML 401页而非JSON。 ## 修复 替换为HashMap（允许null值）。 ## 影响 server/.../config/SecurityConfig.java" \
  -l "bug,auth,backend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Bug] 导入功能未携带Token导致提示请先登录" \
  -b "## 问题 handleImport和handlePreviewExtract使用原生fetch但未携带Authorization Token。 ## 修复 从localStorage读取Token并添加到headers。 ## 影响 client/pages/subject/[id].vue" \
  -l "bug,import,frontend" 2>&1 | head -1

gh issue create -R "$REPO" -t "[Enhancement] GlobalExceptionHandler 添加HttpMessageNotReadableException处理" \
  -b "## 问题 客户端发送非法JSON（编码错误/空body）时Jackson抛异常被通用处理器当作500。 ## 修复 新增Handler返回400并识别UTF-8编码错误给出明确提示。" \
  -l "enhancement,backend" 2>&1 | head -1

echo ""
echo "Done! https://github.com/$REPO/issues"
