# 期末复习刷题系统 (Exam Review System)

大学生期末复习刷题平台，支持科目管理、章节练习、智能组卷、错题本、AI 出题等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 17 + Spring Boot 3.2.5 |
| 安全认证 | Spring Security + JWT（jjwt 0.12.5）+ BCrypt |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.x |
| 文件解析 | Apache POI 5.2.5（XLSX/DOCX）+ Apache PDFBox 3.0.1（PDF） |
| AI 接口 | Spring WebClient → DeepSeek API |
| 前端框架 | Nuxt 3（SPA）+ Vue 3 Composition API |
| UI 组件 | Element Plus 2.8.0 |
| 状态管理 | Pinia |
| 图表 | ECharts 5.5.0 |

## 主要模块

| 模块 | 功能说明 |
|------|----------|
| 🔐 认证授权 | 登录/注册，JWT 鉴权，BCrypt 密码加密 |
| 📚 科目管理 | 科目增删改查，同名检测 |
| 📖 章节管理 | 章节增删改查，关联科目，级联保护 |
| 📝 题库管理 | 题目 CRUD、批量操作、多条件筛选、随机抽题 |
| 📋 模拟考试 | 自动组卷、开始/暂停/恢复/交卷、自动评分 |
| ✏️ 章节练习 | 顺序/随机/错题模式，逐题即时反馈 |
| 📕 错题本 | 自动收集、复习追踪、标记掌握 |
| 📊 学习统计 | 概览仪表盘、正确率趋势、科目进度 |
| 📥 文件导入 | 支持 TXT/DOCX/PDF/XLSX 四种格式批量导入 |
| 🤖 AI 出题 | 调用 DeepSeek 大模型自动生成题目 |

## 项目结构

```
exam-review-system/
├── server/                     # Spring Boot 后端
│   ├── src/main/java/com/examreview/
│   │   ├── controller/         # 接口控制器
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # MyBatis 数据访问层
│   │   ├── entity/             # 数据库实体
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # Spring 配置
│   │   ├── security/           # JWT 过滤器 + 安全配置
│   │   ├── exception/          # 全局异常处理
│   │   └── util/               # 工具类
│   └── src/main/resources/
│       ├── application-template.yml  # 配置模板
│       └── db/
│           ├── schema.sql      # 建表语句（9 张表）
│           └── data.sql        # 种子数据（60 道题）
├── client/                     # Nuxt 3 前端
│   ├── pages/                  # 11 个页面
│   ├── components/             # 可复用组件
│   ├── composables/            # 组合式函数（useApi, useExamTimer）
│   ├── stores/                 # Pinia 状态管理（auth, exam, question）
│   ├── layouts/                # 布局
│   └── middleware/             # 路由守卫
├── docs/                       # 项目文档
├── .env.example                # 环境变量模板
├── package.json                # Monorepo 编排脚本
└── .gitignore
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+

### 2. 配置

```bash
# 复制配置模板
cp server/src/main/resources/application-template.yml server/src/main/resources/application.yml
```

按需设置以下环境变量：

| 变量 | 说明 |
|------|------|
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 签名密钥（至少 32 字符） |
| `DEEPSEEK_API_KEY` | DeepSeek API Key（可选） |

### 3. 启动后端

```bash
cd server
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 4. 启动前端

```bash
cd client
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 5. 登录

默认管理员账号：`admin / 123456`

## 数据库表

| 表名 | 说明 |
|------|------|
| `users` | 用户表 |
| `subjects` | 科目表 |
| `chapters` | 章节表 |
| `questions` | 题目表 |
| `exam_papers` | 试卷表 |
| `exam_questions` | 试卷-题目关联表 |
| `exam_records` | 考试记录表 |
| `answer_records` | 答题记录表 |
| `wrong_questions` | 错题本表 |

## 许可证

MIT License — 详见 [LICENSE](LICENSE) 文件
# ExamReviewSystem
