# 期末复习刷题系统 (Exam Review System)

面向高校学生的在线复习刷题平台，支持多科目题库管理、章节练习、模拟考试、错题追踪、学习统计，并集成 AI 大语言模型实现智能出题和个性化分析。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Java 17 + Spring Boot | 3.2.5 |
| 安全认证 | Spring Security + JWT + BCrypt | jjwt 0.12.5 |
| ORM 框架 | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.x |
| 文件解析 | Apache POI（XLSX/DOCX）+ PDFBox（PDF） | POI 5.2.5 / PDFBox 3.0.1 |
| AI 接口 | Spring WebClient → DeepSeek API | deepseek-chat |
| 前端框架 | Nuxt 3（SPA）+ Vue 3 Composition API | 3.x |
| UI 组件库 | Element Plus | 2.8.0 |
| 状态管理 | Pinia | latest |
| 图表库 | ECharts | 5.5.0 |

## 功能模块

| 模块 | 功能说明 |
|------|----------|
| **认证授权** | 登录 / 注册 / 修改密码，JWT 无状态鉴权，BCrypt 密码加密，全局路由守卫 |
| **科目管理** | 科目增删改查，名称唯一性校验，数据隔离（管理员可访问全部） |
| **题库管理** | 单选 / 多选 / 判断三种题型，CRUD + 批量操作 + 多条件筛选 + 随机抽题 |
| **文件导入** | 支持 XLSX / DOCX / PDF / TXT 四种格式批量导入题目，含预览功能 |
| **章节练习** | 顺序练习 / 随机练习 / 错题专项三种模式，逐题即时判分反馈 |
| **模拟考试** | 智能组卷算法，倒计时答题，支持暂停 / 恢复 / 交卷，自动评分 + 成绩分析 |
| **错题本** | 答错自动收录，Upsert 去重逻辑，掌握状态标记，AI 智能解析 |
| **学习统计** | 题库总数 / 正确率 / 学习天数等多维指标，ECharts 可视化图表 |
| **AI 辅助** | DeepSeek 大模型智能出题，考试总结，错题解析，学习分析和复习计划生成 |
| **管理后台** | 用户列表与统计，级联删除用户及所有关联数据 |

## 项目结构

```
exam-review-system/
├── server/                          # Spring Boot 后端
│   ├── src/main/java/com/examreview/
│   │   ├── controller/              # 11 个接口控制器
│   │   ├── service/                 # 服务层（接口 + 实现）
│   │   ├── mapper/                  # MyBatis-Plus Mapper
│   │   ├── entity/                  # 数据库实体类
│   │   ├── dto/                     # 数据传输对象（12 个）
│   │   ├── config/                  # Security / CORS / MyBatis 配置
│   │   ├── security/                # JWT 过滤器 + UserPrincipal
│   │   ├── exception/               # 全局异常处理
│   │   └── util/                    # 工具类（JWT / AnswerChecker / SecurityUtil）
│   └── src/main/resources/
│       ├── application.yml          # 应用配置
│       └── db/
│           ├── schema.sql           # 建表语句（8 张核心表）
│           └── data.sql             # 种子数据
├── client/                          # Nuxt 3 前端
│   ├── pages/                       # 页面（首页 / 登录 / 注册 / 科目 / 练习 / 考试 / 错题本 / 统计 / 管理后台）
│   ├── components/                  # 可复用组件（QuestionCard / ExamTimer 等）
│   ├── composables/                 # 组合式函数（useApi / useExamTimer）
│   ├── stores/                      # Pinia Store（auth / examStore / questionStore）
│   ├── layouts/                     # 主布局（侧边栏导航 + 顶栏面包屑）
│   └── middleware/                   # 全局路由守卫（auth.global.js）
├── docs/                            # 项目文档
│   ├── A模块技术文档.md              # 认证授权 / 科目章节 / 管理后台 技术详解
│   ├── 数据传递流程图.md             # 前后端数据流向时序图
│   ├── 模块功能详细设计.md           # 各模块需求分析与功能设计
│   └── 功能模块详细介绍.md           # 全部10个模块的完整功能说明
├── .env.example                     # 环境变量模板
├── package.json                     # Monorepo 编排脚本
└── .gitignore
```

## 数据库设计

### 表关系

```
users (用户)
  ├── subjects (科目) ──→ questions (题目)
  │                              │
  ├── exam_records (考试记录) ←── exam_papers (试卷) ──→ exam_questions ←── questions
  │       │                                                                  │
  ├── answer_records (答题记录) ←────────────────────────────────────────────┤
  │                                                                          │
  └── wrong_questions (错题本) ←────────────────────────────────────────────┘
```

### 核心表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `users` | 用户表 | id, username, password(BCrypt), nickname, role(admin/user) |
| `subjects` | 科目表 | id, name, description, icon, sort_order, user_id(数据隔离) |
| `questions` | 题目表 | id, subject_id, type(single/multiple/judge), content, options(JSON), answer, difficulty(1-5), analysis |
| `exam_papers` | 试卷表 | id, subject_id, title, duration(分钟), question_count |
| `exam_questions` | 试卷-题目关联 | id, exam_id, question_id, sort_order |
| `exam_records` | 考试记录 | id, exam_id, score, status(in_progress/paused/finished), duration_used |
| `answer_records` | 答题记录 | id, question_id, selected_answer, is_correct, time_spent, session_id |
| `wrong_questions` | 错题本 | id, question_id, wrong_count, reviewed_count, mastered(0/1) |

> 所有业务表均包含 `user_id` 字段，实现多租户数据隔离。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+

### 1. 配置数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS exam_review CHARACTER SET utf8mb4;"
```

编辑 `server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_review?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&createDatabaseIfNotExist=true
    username: root
    password: 你的密码        # ← 修改此处
jwt:
  secret: your-jwt-secret-key-at-least-32-chars  # ← 修改此处
deepseek:
  api-key: sk-your-deepseek-api-key              # ← 可选，用于AI功能
```

### 2. 启动后端

```bash
cd server
mvn spring-boot:run
# 后端运行在 http://localhost:8080
# 首次启动会自动建表并创建默认管理员账号 admin / 123456
```

### 3. 启动前端

```bash
cd client
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 4. 登录使用

- 默认管理员：`admin / 123456`
- 可在登录页注册新账号

## API 接口总览

| 模块 | 路径前缀 | 核心接口 |
|------|----------|----------|
| 认证授权 | `/api/auth` | POST login/register, GET me, PUT password |
| 科目管理 | `/api/subjects` | CRUD |
| 题库管理 | `/api/questions` | CRUD, DELETE batch, PUT batch, GET random |
| 文件导入 | `/api/import` | POST import, POST preview |
| 章节练习 | `/api/practice` | GET start, GET wrong, POST answer |
| 模拟考试 | `/api/exams` | CRUD, POST generate/start/submit/pause/resume, GET records |
| 错题本 | `/api/wrongbook` | GET list/stats, POST review/master, DELETE remove |
| 学习统计 | `/api/stats` | GET overview/subject-progress/ai-analysis/study-plan |
| AI 辅助 | `/api/ai` | POST generate |
| 管理后台 | `/api/admin` | GET users, DELETE users/{id} |

## 团队分工

详见 [docs/团队分工方案.md](docs/团队分工方案.md) 和 [docs/系统模块说明.md](docs/系统模块说明.md)。

## 许可证

MIT License — 详见 [LICENSE](LICENSE) 文件
