# A负责模块技术文档

## 一、系统业务流程

### 1.1 认证授权流程

```
用户访问 → 路由守卫检查Token → 无Token跳转登录页 → 输入用户名密码 
    ↓
前端调用 POST /api/auth/login → 后端Spring Security认证 → BCrypt验证密码 
    ↓
生成JWT Token（24h过期）→ 返回Token+用户信息 → 前端存储到localStorage 
    ↓
后续请求自动携带Token → JWT过滤器解析 → 设置SecurityContext → 访问受保护资源
```

**关键流程节点：**
1. **注册**：用户名唯一性校验 → BCrypt加密密码 → 创建用户 → 返回JWT
2. **登录**：AuthenticationManager认证 → 生成JWT → 返回用户信息
3. **鉴权**：JWT过滤器拦截 → 解析Token → 加载UserDetails → 设置SecurityContext
4. **权限控制**：管理员可访问所有数据，普通用户只能访问自己的数据

### 1.2 科目管理流程

```
用户登录 → 进入科目管理页 → 显示科目列表（按sort_order排序）
    ↓
创建科目：输入名称/描述/图标 → 后端校验名称唯一性 → 插入数据库
    ↓
编辑科目：修改信息 → 同名检测排除自身 → 更新数据库
    ↓
删除科目：检查关联章节数 → 有章节则阻止 → 无章节则删除
```

### 1.3 章节管理流程

```
进入科目详情页 → 显示章节列表（按sort_order排序）
    ↓
创建章节：输入名称 → 自动分配sort_order=最大值+1 → 插入数据库
    ↓
编辑章节：修改名称 → 更新数据库
    ↓
删除章节：检查关联题目数+错题数 → 有数据则阻止 → 无数据则删除
```

### 1.4 管理后台流程

```
管理员登录 → 进入管理后台 → 显示用户列表+统计数据
    ↓
查看统计：科目数/题目数/考试数/错题数/答题记录数
    ↓
删除用户：级联删除所有关联数据 → 答题记录→错题本→考试记录→试卷→题目→章节→科目→用户
```

---

## 二、技术角度详解

### 2.1 前端技术栈（Nuxt 3 + Element Plus + Pinia）

#### 2.1.1 登录页面（client/pages/login.vue）

**每句代码详解：**

```vue
<script setup>
import { User, Lock } from '@element-plus/icons-vue'

// 定义页面元信息：不使用默认布局（登录页独立布局）
definePageMeta({
  layout: false,
})

// 使用Pinia Store管理认证状态
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// 响应式表单数据
const form = reactive({
  username: '',
  password: '',
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref(null)  // 表单引用，用于触发表单校验
const loading = ref(false) // 加载状态，防止重复提交

// 如果已登录，直接跳转首页（避免已登录用户访问登录页）
if (authStore.isAuthenticated) {
  router.replace('/')
}

// 登录处理函数
const handleLogin = async () => {
  // 1. 表单校验
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 2. 显示加载状态
  loading.value = true
  
  // 3. 调用authStore.login方法，调用后端登录接口，完成用户身份认证，后端验证成功后返回 JWT Token 和用户信息，同时写入 localStorage
  const success = await authStore.login(form.username, form.password)
  
  // 4. 关闭加载状态
  loading.value = false

  // 5. 登录成功后跳转
  if (success) {
    const redirect = route.query.redirect || '/'  // 优先跳转原访问页面
    router.replace(redirect)
  }
}
</script>
```

**数据流向：**
```
用户输入 → form对象 → authStore.login() → useApi.post('/auth/login') 
    → HTTP请求 → 后端AuthController → AuthService → 数据库查询 
    → 返回JWT+用户信息 → authStore存储到localStorage → 页面跳转
```

#### 2.1.2 认证状态管理（client/stores/auth.js）

**每句代码详解：**

```javascript
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  // 响应式状态
  const token = ref(null)        // JWT Token
  const user = ref(null)         // 用户信息对象
  const isAuthenticated = computed(() => !!token.value)  // 计算属性：是否已认证

  // 初始化：从localStorage恢复登录状态（页面刷新时调用）
  const init = () => {
    if (process.client) {  // 仅在客户端执行
      const savedToken = localStorage.getItem('auth_token')
      const savedUser = localStorage.getItem('auth_user')
      if (savedToken) {
        token.value = savedToken
        try {
          user.value = JSON.parse(savedUser)  // 解析用户信息
        } catch (e) {
          user.value = null
        }
      }
    }
  }

  // 登录方法
  const login = async (username, password) => {
    try {
      const api = useApi()
      // 调用后端登录接口
      const data = await api.post('/auth/login', { username, password })
      
      // 存储Token和用户信息
      token.value = data.token
      user.value = {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role || 'user',
      }
      
      // 持久化到localStorage
      if (process.client) {
        localStorage.setItem('auth_token', data.token)
        localStorage.setItem('auth_user', JSON.stringify(user.value))
      }
      
      ElMessage.success('登录成功')
      return true
    } catch (err) {
      return false
    }
  }

  // 注册方法（逻辑类似登录）
  const register = async (username, password, nickname) => { /* ... */ }

  // 退出登录
  const logout = () => {
    token.value = null
    user.value = null
    if (process.client) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
    }
    ElMessage.success('已退出登录')
  }

  // 获取Token（供useApi使用）
  const getToken = () => token.value

  return {
    token,
    user,
    isAuthenticated,
    init,
    login,
    register,
    logout,
    getToken,
  }
})
```

#### 2.1.3 API封装（client/composables/useApi.js）

**每句代码详解：**

```javascript
import { ElMessage } from 'element-plus'

export const useApi = () => {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase  // 从nuxt.config.ts读取API地址

  // 核心请求方法
  const request = async (url, options = {}) => {
    const method = (options.method || 'GET').toUpperCase()
    const headers = { ...options.headers }

    // 自动添加JWT Token到请求头
    if (process.client) {
      const token = localStorage.getItem('auth_token')
      if (token) {
        headers['Authorization'] = `Bearer ${token}`
      }
    }

    // POST/PUT/DELETE请求设置Content-Type
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      headers['Content-Type'] = 'application/json'
    }

    try {
      // 发送HTTP请求
      const result = await $fetch(`${baseURL}${url}`, {
        method,
        headers,
        body: options.body,
        params: options.params,
      })
      
      // 检查业务状态码
      if (result.code !== 200) {
        ElMessage.error(result.message || '请求失败')
        throw new Error(result.message)
      }
      return result.data
    } catch (err) {
      // 处理401未认证错误
      if (err.response?.status === 401) {
        if (process.client) {
          localStorage.removeItem('auth_token')
          localStorage.removeItem('auth_user')
        }
        ElMessage.error('登录已过期，请重新登录')
        // 强制跳转登录页
        if (process.client && !window.location.pathname.includes('/login')) {
          window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
        }
        throw err
      }

      if (err.message && !err.message.includes('请求失败')) {
        ElMessage.error(err.message || '网络请求失败')
      }
      throw err
    }
  }

  // 返回封装后的HTTP方法
  return {
    get: (url, params) => request(url, { method: 'GET', params }),
    post: (url, body) => request(url, { method: 'POST', body }),
    put: (url, body) => request(url, { method: 'PUT', body }),
    delete: (url, body) => request(url, { method: 'DELETE', body }),
  }
}
```

#### 2.1.4 路由守卫（client/middleware/auth.global.js）

**每句代码详解：**

```javascript
export default defineNuxtRouteMiddleware((to) => {
  // 白名单路由（无需登录即可访问）
  const publicRoutes = ['/login', '/register']

  // 仅在客户端执行
  if (process.client) {
    const token = localStorage.getItem('auth_token')

    // 未登录且访问受保护页面 → 跳转登录页
    if (!token && !publicRoutes.includes(to.path)) {
      return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
    }

    // 已登录且访问登录/注册页 → 跳转首页
    if (token && publicRoutes.includes(to.path)) {
      return navigateTo('/')
    }
  }
})
```

---

### 2.2 后端技术栈（Spring Boot 3 + Spring Security + JWT + MyBatis-Plus）

#### 2.2.1 认证控制器（AuthController.java）

**每句代码详解：**

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * 前端发送：POST /api/auth/login
     * 请求体：{"username": "xxx", "password": "xxx"}
     * 返回：{"code": 200, "data": {"token": "xxx", "userId": 1, ...}, "message": "登录成功"}
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // @Valid触发参数校验（用户名/密码非空）
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response, "登录成功");
    }

    /**
     * 注册接口
     * 前端发送：POST /api/auth/register
     * 请求体：{"username": "xxx", "password": "xxx", "nickname": "xxx"}
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.ok(response, "注册成功");
    }

    /**
     * 获取当前用户信息
     * 前端发送：GET /api/auth/me
     * 请求头：Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        // @AuthenticationPrincipal从SecurityContext中获取当前用户
        LoginResponse response = authService.getCurrentUser(principal.getId());
        return ApiResponse.ok(response);
    }

    /**
     * 修改密码
     * 前端发送：PUT /api/auth/password
     * 请求体：{"oldPassword": "xxx", "newPassword": "xxx"}
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        authService.changePassword(principal.getId(), dto);
        return ApiResponse.ok(null, "密码修改成功");
    }
}
```

#### 2.2.2 认证服务实现（AuthServiceImpl.java）

**每句代码详解：**

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;           // MyBatis-Plus Mapper
    private final JwtUtil jwtUtil;                 // JWT工具类
    private final PasswordEncoder passwordEncoder; // BCrypt密码加密器
    private final AuthenticationManager authenticationManager; // Spring Security认证管理器

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 使用Spring Security的AuthenticationManager进行认证
        // 内部会调用UserDetailsServiceImpl.loadUserByUsername()加载用户
        // 然后使用BCryptPasswordEncoder.matches()验证密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        // 2. 认证成功，获取用户主体
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        
        // 3. 生成JWT Token（包含userId和username）
        String token = jwtUtil.generateToken(principal.getId(), principal.getUsername());

        // 4. 查询用户完整信息
        User user = userMapper.selectById(principal.getId());
        
        // 5. 构建返回对象
        return new LoginResponse(
                token, principal.getId(), principal.getUsername(),
                user != null ? user.getNickname() : principal.getUsername(),
                user != null && user.getRole() != null ? user.getRole() : "user"
        );
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        // BCrypt加密密码（$2a$10$...格式）
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        
        // 3. 插入数据库
        userMapper.insert(user);

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return new LoginResponse(
                token, user.getId(), user.getUsername(), user.getNickname(),
                user.getRole() != null ? user.getRole() : "user"
        );
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        
        // 2. 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 3. 加密新密码并更新
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
```

#### 2.2.3 Spring Security配置（SecurityConfig.java）

**每句代码详解：**

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper; // Jackson JSON序列化

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS（使用下方corsConfigurationSource配置）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 关闭CSRF（前后端分离架构不需要）
            .csrf(csrf -> csrf.disable())
            // 无状态会话（不使用Session）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 放行OPTIONS预检请求
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // 放行登录/注册接口
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 其余接口需要认证
                .anyRequest().authenticated()
            )
            // 未认证时返回JSON（而不是默认的HTML登录页）
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    Map<String, Object> body = new HashMap<>();
                    body.put("code", 401);
                    body.put("message", "请先登录");
                    body.put("data", null);
                    objectMapper.writeValue(response.getWriter(), body);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    Map<String, Object> body = new HashMap<>();
                    body.put("code", 403);
                    body.put("message", "权限不足");
                    body.put("data", null);
                    objectMapper.writeValue(response.getWriter(), body);
                })
            )
            // 添加JWT过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS跨域配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的前端地址
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:3001"));
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        // 允许携带Cookie
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

#### 2.2.4 JWT过滤器（JwtAuthenticationFilter.java）

**每句代码详解：**

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头提取Token
        String token = getTokenFromRequest(request);

        // 2. Token存在且有效
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 3. 从Token解析用户名
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 4. 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. 创建认证对象
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. 设置到SecurityContext（后续可通过@AuthenticationPrincipal获取）
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取Token
     * 格式：Authorization: Bearer <token>
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // 去掉"Bearer "前缀
        }
        return null;
    }
}
```

#### 2.2.5 JWT工具类（JwtUtil.java）

**每句代码详解：**

```java
@Component
public class JwtUtil {

    private final SecretKey key;        // HMAC-SHA密钥
    private final long expiration;      // 过期时间

    // 从配置文件读取密钥和过期时间
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成JWT Token
     * Payload包含：sub(userId)、username、iat(签发时间)、exp(过期时间)
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // 用户ID作为subject
                .claim("username", username)      // 自定义声明：用户名
                .issuedAt(now)                    // 签发时间
                .expiration(expiryDate)           // 过期时间
                .signWith(key)                    // HMAC-SHA签名
                .compact();
    }

    /**
     * 从Token解析用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从Token解析用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException
                 | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析Token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)          // 验证签名
                .build()
                .parseSignedClaims(token) // 解析Claims
                .getPayload();
    }
}
```

#### 2.2.6 科目服务实现（SubjectServiceImpl.java）

**每句代码详解：**

```java
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final ChapterMapper chapterMapper;

    @Override
    public List<Subject> getAll(Long userId) {
        // 管理员可查看所有科目，普通用户只能查看自己的科目
        // SecurityUtil.isAdmin()判断当前用户是否为管理员
        return subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>()
                        .eq(!SecurityUtil.isAdmin(), Subject::getUserId, userId)  // 条件拼接
                        .orderByAsc(Subject::getSortOrder));  // 按sort_order升序
    }

    @Override
    public Subject create(Subject subject, Long userId) {
        // 1. 校验科目名称非空
        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            throw new BusinessException("科目名称不能为空");
        }
        
        // 2. 校验科目名称长度
        if (subject.getName().trim().length() > 50) {
            throw new BusinessException("科目名称不能超过50个字符");
        }
        
        // 3. 检查同名科目（当前用户范围内）
        Long count = subjectMapper.selectCount(
                new LambdaQueryWrapper<Subject>()
                        .eq(!SecurityUtil.isAdmin(), Subject::getUserId, userId)
                        .eq(Subject::getName, subject.getName().trim()));
        if (count > 0) {
            throw new BusinessException("科目名称已存在");
        }
        
        // 4. 设置字段并插入
        subject.setName(subject.getName().trim());
        subject.setUserId(userId);
        subjectMapper.insert(subject);
        return subject;
    }

    @Override
    @Transactional  // 事务保证原子性
    public void delete(Integer id, Long userId) {
        // 1. 校验科目存在且属于当前用户
        getById(id, userId);
        
        // 2. 检查关联章节数
        Long chapterCount = chapterMapper.selectCount(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getSubjectId, id));
        if (chapterCount > 0) {
            throw new BusinessException("该科目下还有 " + chapterCount + " 个章节，请先删除章节");
        }
        
        // 3. 删除科目
        subjectMapper.deleteById(id);
    }
}
```

---

## 三、数据库表设计

### 3.1 表结构

#### 3.1.1 用户表（users）

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | AUTO_INCREMENT |
| username | VARCHAR(50) | 用户名 | UNIQUE, NOT NULL |
| password | VARCHAR(255) | 密码（BCrypt加密） | NOT NULL |
| nickname | VARCHAR(50) | 昵称 | DEFAULT '' |
| avatar | VARCHAR(255) | 头像URL | DEFAULT '' |
| role | VARCHAR(20) | 角色 | DEFAULT 'user' |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | ON UPDATE CURRENT_TIMESTAMP |

**数据示例：**
```sql
INSERT INTO users (username, password, nickname, role) VALUES
('admin', '$2a$10$...', '管理员', 'admin'),
('user1', '$2a$10$...', '用户1', 'user');
```

#### 3.1.2 科目表（subjects）

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键 | AUTO_INCREMENT |
| name | VARCHAR(50) | 科目名称 | NOT NULL |
| description | VARCHAR(200) | 描述 | DEFAULT '' |
| icon | VARCHAR(100) | 图标 | DEFAULT '' |
| sort_order | INT | 排序号 | DEFAULT 0 |
| user_id | BIGINT | 所属用户ID | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

**数据示例：**
```sql
INSERT INTO subjects (name, description, icon, sort_order, user_id) VALUES
('高等数学', '大学数学基础课程', '📐', 1, 1),
('数据结构', '计算机专业核心课程', '💻', 2, 1);
```

#### 3.1.3 章节表（chapters）

| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | INT | 主键 | AUTO_INCREMENT |
| subject_id | INT | 所属科目ID | FOREIGN KEY |
| name | VARCHAR(100) | 章节名称 | NOT NULL |
| sort_order | INT | 排序号 | DEFAULT 0 |
| user_id | BIGINT | 所属用户ID | DEFAULT 0 |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

**数据示例：**
```sql
INSERT INTO chapters (subject_id, name, sort_order, user_id) VALUES
(1, '第一章 函数与极限', 1, 1),
(1, '第二章 导数与微分', 2, 1);
```

### 3.2 表关联关系

```
users (id)
  ├── subjects (user_id)          -- 一对多：一个用户有多个科目
  │     └── chapters (subject_id) -- 一对多：一个科目有多个章节
  │           └── questions (chapter_id) -- 一对多：一个章节有多道题目
  │                 ├── wrong_questions (question_id) -- 一对多
  │                 └── answer_records (question_id)  -- 一对多
  │
  └── exam_records (user_id)      -- 一对多：一个用户有多条考试记录
        └── answer_records (session_id) -- 一对多：通过sessionId关联
```

### 3.3 数据流向详解

#### 3.3.1 登录流程数据流

```
前端 → 后端 → 数据库

1. 前端发送请求：
   POST /api/auth/login
   Headers: {"Content-Type": "application/json"}
   Body: {"username": "admin", "password": "123456"}

2. 后端处理：
   AuthController.login()
     → AuthService.login()
       → AuthenticationManager.authenticate()  // Spring Security认证
         → UserDetailsService.loadUserByUsername("admin")
           → UserMapper.selectOne(username="admin")  // 查询数据库
       → JwtUtil.generateToken(userId, username)  // 生成JWT
       → 返回LoginResponse

3. 数据库查询：
   SELECT * FROM users WHERE username = 'admin'

4. 返回数据：
   {
     "code": 200,
     "data": {
       "token": "eyJhbGciOiJIUzI1NiJ9...",
       "userId": 1,
       "username": "admin",
       "nickname": "管理员",
       "role": "admin"
     },
     "message": "登录成功"
   }

5. 前端存储：
   localStorage.setItem('auth_token', token)
   localStorage.setItem('auth_user', JSON.stringify(user))
```

#### 3.3.2 创建科目数据流

```
前端 → 后端 → 数据库

1. 前端发送请求：
   POST /api/subjects
   Headers: {
     "Content-Type": "application/json",
     "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..."
   }
   Body: {
     "name": "高等数学",
     "description": "大学数学基础课程",
     "icon": "📐"
   }

2. 后端处理：
   JwtAuthenticationFilter.doFilterInternal()
     → 解析Token获取userId
     → 设置SecurityContext
   
   SubjectController.create()
     → SecurityUtil.getCurrentUserId()  // 从SecurityContext获取
     → SubjectService.create(subject, userId)
       → 校验名称非空、长度、唯一性
       → subject.setUserId(userId)
       → SubjectMapper.insert(subject)  // 插入数据库

3. 数据库插入：
   INSERT INTO subjects (name, description, icon, sort_order, user_id)
   VALUES ('高等数学', '大学数学基础课程', '📐', 0, 1)

4. 返回数据：
   {
     "code": 200,
     "data": {
       "id": 1,
       "name": "高等数学",
       "description": "大学数学基础课程",
       "icon": "📐",
       "sortOrder": 0,
       "userId": 1,
       "createdAt": "2026-06-12T10:00:00"
     },
     "message": "创建成功"
   }
```

#### 3.3.3 删除科目数据流（级联检查）

```
前端 → 后端 → 数据库

1. 前端发送请求：
   DELETE /api/subjects/1
   Headers: {"Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..."}

2. 后端处理：
   SubjectController.delete(1)
     → SubjectService.delete(1, userId)
       → getById(1, userId)  // 校验科目存在且属于当前用户
       → 检查关联章节数：
         SELECT COUNT(*) FROM chapters WHERE subject_id = 1
       → 如果count > 0，抛出BusinessException("该科目下还有X个章节，请先删除章节")
       → 如果count == 0，执行删除：
         DELETE FROM subjects WHERE id = 1

3. 数据库操作：
   -- 第一步：查询章节数
   SELECT COUNT(*) FROM chapters WHERE subject_id = 1;
   
   -- 第二步（如果无章节）：删除科目
   DELETE FROM subjects WHERE id = 1;
```

---

## 四、关键设计要点

### 4.1 数据隔离设计

**问题**：多用户系统需要确保用户只能访问自己的数据。

**解决方案**：
1. **数据库层面**：所有业务表添加`user_id`字段
2. **后端层面**：所有查询自动拼接`user_id`条件
3. **权限提升**：管理员可访问所有数据

**代码实现**：
```java
// 普通用户只能查看自己的数据
.eq(!SecurityUtil.isAdmin(), Subject::getUserId, userId)
```

### 4.2 级联删除设计

**问题**：删除科目时，如果有关联章节/题目，会导致数据不一致。

**解决方案**：
1. **阻止删除**：检查关联数据，有数据则阻止删除并提示用户
2. **级联删除**：管理员删除用户时，级联删除所有关联数据

**代码实现**：
```java
// 检查关联章节数
Long chapterCount = chapterMapper.selectCount(
    new LambdaQueryWrapper<Chapter>().eq(Chapter::getSubjectId, id));
if (chapterCount > 0) {
    throw new BusinessException("该科目下还有 " + chapterCount + " 个章节，请先删除章节");
}
```

### 4.3 排序号自动分配

**问题**：用户创建章节时，需要自动分配排序号。

**解决方案**：查询当前科目最大排序号，+1后赋值。

**代码实现**：
```java
List<Chapter> existing = chapterMapper.selectList(
    new LambdaQueryWrapper<Chapter>()
        .eq(Chapter::getSubjectId, chapter.getSubjectId())
        .orderByDesc(Chapter::getSortOrder)
        .last("LIMIT 1"));
chapter.setSortOrder(existing.isEmpty() ? 1 : existing.get(0).getSortOrder() + 1);
```

---

## 五、总结

A负责的模块是整个系统的基础架构，包括：
1. **认证授权**：JWT + Spring Security实现无状态认证
2. **数据隔离**：通过user_id字段实现多用户数据隔离
3. **级联检查**：删除前检查关联数据，保证数据一致性
4. **前端状态管理**：Pinia + localStorage实现登录态持久化
5. **路由守卫**：全局拦截未登录用户访问受保护页面

这些基础设施为其他模块（题库管理、章节练习、模拟考试等）提供了认证、授权、数据隔离等核心能力。
