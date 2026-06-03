#!/usr/bin/perl
# =============================================================================
# GitHub Issues 批量创建脚本
# 用法: perl github-setup.pl <GITHUB_USERNAME> <GITHUB_TOKEN>
# =============================================================================
use strict;
use warnings;
use LWP::UserAgent;
use JSON;
use MIME::Base64;
use Encode;

my $username = $ENV{GITHUB_USER} || $ARGV[0];
my $token    = $ENV{GITHUB_TOKEN} || $ARGV[1];
die "用法: GITHUB_USER=xxx GITHUB_TOKEN=xxx perl $0\n" unless $username && $token;

my $repo  = "exam-review-system";
my $ua    = LWP::UserAgent->new;
my $api   = "https://api.github.com";
my $auth  = "Basic " . encode_base64("$username:$token", '');

sub github_api {
    my ($method, $path, $body) = @_;
    my $url = ($path =~ /^https/) ? $path : "$api$path";
    my $req = HTTP::Request->new($method, $url,
        [ 'Authorization' => $auth,
          'Accept'        => 'application/vnd.github.v3+json',
          'Content-Type'  => 'application/json' ]);
    $req->content(encode_json($body)) if $body;
    my $res = $ua->request($req);
    my $body = $res->decoded_content;
    my $data = eval { decode_json($body) } // { message => $body };
    return ($res->code, $data);
}

# =============================================================================
# Step 1: 创建仓库
# =============================================================================
print "Step 1: Creating repository '$username/$repo'...\n";
my ($code, $data) = github_api('POST', '/user/repos', {
    name        => $repo,
    description => "大学生期末复习刷题系统 - Spring Boot 3 + Nuxt 3 + MySQL",
    private     => 'false',
    has_issues  => 'true',
    has_projects=> 'true',
    homepage    => "",
});
if ($code == 201) {
    print "  ✅ Repository created: $data->{html_url}\n";
} elsif ($code == 422) {
    print "  ⚠️  Repository already exists\n";
} else {
    die "  ❌ Failed: $code - $data->{message}\n";
}

# =============================================================================
# Step 2: 创建 Labels
# =============================================================================
print "\nStep 2: Creating labels...\n";

my @labels = (
    { name => 'backend',       color => '0E8A16', description => 'Spring Boot 后端' },
    { name => 'frontend',      color => '1D76DB', description => 'Nuxt 3 前端' },
    { name => 'auth',          color => 'D93F0B', description => '认证授权模块' },
    { name => 'exam',          color => 'C5DEF5', description => '考试系统模块' },
    { name => 'wrongbook',     color => 'FBCA04', description => '错题本模块' },
    { name => 'practice',      color => 'B60205', description => '练习模块' },
    { name => 'import',        color => '5319E7', description => '文件导入模块' },
    { name => 'ai',            color => '006B75', description => 'AI 出题模块' },
    { name => 'question',      color => 'E99695', description => '题库管理模块' },
    { name => 'stats',         color => 'FEF2C0', description => '学习统计模块' },
    { name => 'bug',           color => 'D73A4A', description => 'Bug 报告' },
    { name => 'enhancement',   color => 'A2EEEF', description => '功能增强' },
    { name => 'documentation', color => '0075CA', description => '文档相关' },
    { name => 'good first issue', color => '7057FF', description => '适合新手' },
);

foreach my $label (@labels) {
    my ($c, $d) = github_api('POST', "/repos/$username/$repo/labels", $label);
    if ($c == 201) { print "  ✅ $label->{name}\n"; }
    elsif ($c == 422) { print "  ⚠️  $label->{name} already exists\n"; }
    else { print "  ❌ $label->{name}: $d->{message}\n"; }
}

# =============================================================================
# Step 3: 创建 Milestones
# =============================================================================
print "\nStep 3: Creating milestones...\n";

my @milestones = (
    { title => 'v1.0 基础功能',    description => '科目/章节/题库 CRUD + 登录注册', due_on => '2025-07-01T00:00:00Z' },
    { title => 'v1.1 考试系统',    description => '组卷/答题/暂停恢复/评分',         due_on => '2025-07-15T00:00:00Z' },
    { title => 'v1.2 错题本+统计', description => '错题本 + 学习统计 + 文件导入',   due_on => '2025-08-01T00:00:00Z' },
    { title => 'v1.3 AI 增强',     description => 'AI 出题 + 智能分析',             due_on => '2025-08-15T00:00:00Z' },
);

foreach my $ms (@milestones) {
    my ($c, $d) = github_api('POST', "/repos/$username/$repo/milestones", $ms);
    if ($c == 201) { print "  ✅ $ms->{title}\n"; }
    elsif ($c == 422) { print "  ⚠️  $ms->{title} already exists\n"; }
    else { print "  ❌ $ms->{title}: $d->{message}\n"; }
}

# =============================================================================
# Step 4: 创建 Issues
# =============================================================================
print "\nStep 4: Creating issues...\n";

my @issues = (
    # --- 认证模块 ---
    {
        title    => '[Auth] 用户登录/注册功能',
        body     => "## 需求描述\n\n- 用户注册（用户名、密码、昵称）\n- 用户登录（JWT Token 颁发）\n- 获取当前用户信息\n\n## 验收标准\n\n- [x] 注册参数校验（用户名2-50字符，密码6-100字符）\n- [x] 重复用户名检测\n- [x] BCrypt 密码加密\n- [x] JWT Token 24h 过期\n- [x] 登录错误统一提示（防用户名枚举）",
        labels   => ['auth', 'backend'],
        milestone=> 1,
    },
    {
        title    => '[Auth] 前端登录/注册页面',
        body     => "## 需求描述\n\n- 登录页面（用户名+密码表单）\n- 注册页面（用户名+昵称+密码+确认密码）\n- 路由守卫：未登录跳转登录页\n- Token 持久化到 localStorage\n\n## 验收标准\n\n- [x] 登录/注册表单校验\n- [x] 401 响应自动跳转登录页\n- [x] 已登录用户自动跳转首页",
        labels   => ['auth', 'frontend'],
        milestone=> 1,
    },

    # --- 科目管理 ---
    {
        title    => '[Subject] 科目增删改查',
        body     => "## 需求描述\n\n- 科目列表展示\n- 新建科目（名称唯一性校验）\n- 编辑科目\n- 删除科目（有关联章节时阻止删除）\n\n## 验收标准\n\n- [x] CRUD 完整实现\n- [x] 同名检测，创建和编辑时均校验\n- [x] 级联保护：有章节时禁止删除",
        labels   => ['backend', 'frontend'],
        milestone=> 1,
    },

    # --- 章节管理 ---
    {
        title    => '[Chapter] 章节增删改查',
        body     => "## 需求描述\n\n- 按科目展示章节列表\n- 新建章节（自动排序）\n- 编辑章节\n- 删除章节（有关联题目/错题时阻止删除）\n\n## 验收标准\n\n- [x] CRUD 完整实现\n- [x] 自动分配 sort_order\n- [x] 级联保护：有题目或错题时禁止删除",
        labels   => ['backend', 'frontend'],
        milestone=> 1,
    },

    # --- 题库管理 ---
    {
        title    => '[Question] 题目增删改查',
        body     => "## 需求描述\n\n- 分页列表，支持多条件筛选（科目/章节/题型/难度/关键词）\n- 新建题目（单选题/多选题/判断题）\n- 编辑题目\n- 删除题目（级联删除关联的错题/答题记录/试卷关联）\n- 批量删除（事务回滚）\n- 批量更新\n- 随机抽题\n\n## 验收标准\n\n- [x] 三种题型支持（single/multiple/judge）\n- [x] JSON 格式选项存储\n- [x] 级联删除关联数据\n- [x] 批量操作事务原子性\n- [x] 按难度排序",
        labels   => ['question', 'backend', 'frontend'],
        milestone=> 1,
    },

    # --- 考试系统 ---
    {
        title    => '[Exam] 自动组卷',
        body     => "## 需求描述\n\n- 按科目+可选章节筛选题目池\n- 自动组卷（打乱+按题型循环分配）\n- 试卷 CRUD\n\n## 验收标准\n\n- [x] 题目池不足时提示错误\n- [x] 题型均匀分配（single→multiple→judge 循环）\n- [x] 组卷后生成 ExamQuestion 关联",
        labels   => ['exam', 'backend'],
        milestone=> 2,
    },
    {
        title    => '[Exam] 考试答题流程',
        body     => "## 需求描述\n\n- 开始考试（生成考试记录）\n- 答题（通过答题卡导航）\n- 暂停考试（保存剩余时间）\n- 恢复考试\n- 交卷（自动评分+同步错题本）\n\n## 验收标准\n\n- [x] 同时只能有一个进行中的考试\n- [x] 暂停/恢复正确保存和恢复时间\n- [x] 交卷自动评分（百分制，保留1位小数）\n- [x] 错题自动同步到错题本\n- [x] sessionId 精确关联答题记录",
        labels   => ['exam', 'backend', 'frontend'],
        milestone=> 2,
    },
    {
        title    => '[Exam] 考试结果页面',
        body     => "## 需求描述\n\n- 得分环形图\n- 正确/错误题数统计\n- 逐题解析（正确答案+解析+我的答案）\n- 考试用时展示\n\n## 验收标准\n\n- [x] 结果页完整展示所有题目解析\n- [x] 正确/错误题目高亮区分",
        labels   => ['exam', 'frontend'],
        milestone=> 2,
    },
    {
        title    => '[Exam] 考试倒计时组件',
        body     => "## 需求描述\n\n- 可视化倒计时\n- 剩余时间 < 5 分钟：警告状态（黄色）\n- 剩余时间 < 1 分钟：危急状态（红色脉冲动画）\n- 时间到自动交卷\n\n## 验收标准\n\n- [x] useExamTimer composable 封装\n- [x] ExamTimer 组件状态切换\n- [x] 组件卸载自动清理定时器",
        labels   => ['exam', 'frontend'],
        milestone=> 2,
    },

    # --- 练习模块 ---
    {
        title    => '[Practice] 章节练习',
        body     => "## 需求描述\n\n- 按章节开始练习\n- 顺序/随机/错题三种模式\n- 逐题展示，实时反馈\n- 答题后同步错题本\n\n## 验收标准\n\n- [x] QuestionCard 通用组件（单选/多选/判断）\n- [x] 进度条显示\n- [x] 错题自动记录到错题本",
        labels   => ['practice', 'backend', 'frontend'],
        milestone=> 1,
    },

    # --- 错题本 ---
    {
        title    => '[WrongBook] 错题本管理',
        body     => "## 需求描述\n\n- 错题分页列表（关联题目信息）\n- 按科目/掌握状态筛选\n- 复习标记（reviewedCount 递增）\n- 标记掌握/取消掌握\n- 从错题本移除\n- 统计信息（总数/已掌握/未掌握）\n\n## 验收标准\n\n- [x] 错题自动从练习和考试中收集\n- [x] 仅允许操作自己的错题（所有权校验）\n- [x] 掌握状态切换",
        labels   => ['wrongbook', 'backend', 'frontend'],
        milestone=> 3,
    },

    # --- 学习统计 ---
    {
        title    => '[Stats] 学习统计仪表盘',
        body     => "## 需求描述\n\n- 概览卡片（总题数/已答题数/正确率/学习天数/今日答题）\n- 正确率趋势图（7日折线图）\n- 每日活跃度（30日柱状图）\n- 各科目进度\n\n## 验收标准\n\n- [x] ECharts 图表渲染\n- [x] 数据按 userId 隔离\n- [x] 统计接口性能优化（单次查询批处理）",
        labels   => ['stats', 'backend', 'frontend'],
        milestone=> 3,
    },

    # --- 文件导入 ---
    {
        title    => '[Import] 文件批量导入题目',
        body     => "## 需求描述\n\n- 支持 XLSX / DOCX / PDF / TXT 四种格式\n- 标准格式解析（题型/题目/选项/答案/解析标签）\n- 试卷格式解析（题号+行内选项+答案区）\n- 导入预览（前10道单选题）\n- 导入结果反馈（成功/失败计数+错误详情）\n\n## 验收标准\n\n- [x] 四种格式均能正确解析\n- [x] 预览不实际写入数据库\n- [x] 导入携带 JWT Token\n- [x] 错误明细返回",
        labels   => ['import', 'backend', 'frontend'],
        milestone=> 3,
    },

    # --- AI 出题 ---
    {
        title    => '[AI] DeepSeek 自动出题',
        body     => "## 需求描述\n\n- 调用 DeepSeek API 生成题目\n- 支持指定科目/章节/题型/数量\n- 返回标准格式题目\n\n## 验收标准\n\n- [ ] API 调用稳定\n- [ ] 生成题目格式标准化\n- [ ] 错误处理和重试",
        labels   => ['ai', 'backend'],
        milestone=> 4,
    },

    # --- Bug/增强 ---
    {
        title    => '[Bug] 修复导入功能未携带Token',
        body     => "## 问题\n\n前端导入题目时使用原生 fetch 而未携带 Authorization Token，导致始终提示\"请先登录\"。\n\n## 修复\n\n在 fetch 调用中从 localStorage 读取 Token 并添加到请求头。\n\n## 影响文件\n\n- client/pages/subject/[id].vue (handleImport, handlePreviewExtract)",
        labels   => ['bug', 'import', 'frontend'],
    },
    {
        title    => '[Bug] Map.of(null) 导致 401 返回 HTML',
        body     => "## 问题\n\nSecurityConfig 中使用 Map.of() 传入 null 值导致 NPE，未认证请求返回 HTML 而非 JSON。\n\n## 修复\n\n替换为 HashMap（允许 null 值）。\n\n## 影响文件\n\n- server/.../config/SecurityConfig.java",
        labels   => ['bug', 'auth', 'backend'],
    },
    {
        title    => '[Enhancement] 添加 HttpMessageNotReadableException 处理器',
        body     => "## 问题\n\n当客户端发送非法 JSON（如编码错误）时返回 500，应返回 400 并给出明确提示。\n\n## 修复\n\n在 GlobalExceptionHandler 中添加 HttpMessageNotReadableException 处理器，区分 UTF-8 编码错误等场景。\n\n## 影响文件\n\n- server/.../exception/GlobalExceptionHandler.java",
        labels   => ['enhancement', 'backend'],
    },
);

foreach my $issue (@issues) {
    my $body = {
        title  => $issue->{title},
        body   => $issue->{body},
        labels => $issue->{labels},
    };
    $body->{milestone} = $issue->{milestone} if $issue->{milestone};

    my ($c, $d) = github_api('POST', "/repos/$username/$repo/issues", $body);
    if ($c == 201) {
        print "  ✅ #$d->{number} $issue->{title}\n";
    } else {
        print "  ❌ $issue->{title}: $d->{message}\n";
    }
}

print "\n🎉 Done! Repository: https://github.com/$username/$repo\n";
print "    Issues: https://github.com/$username/$repo/issues\n";
print "    Projects: https://github.com/$username/$repo/projects\n";

# MIME::Base64::encode_base64 imported at top
