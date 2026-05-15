# basic-starter-parent

一套基于 Spring Boot 的基础设施 Starter 组件集，为微服务项目提供开箱即用的公共能力，包括统一响应、全局异常处理、缓存、数据访问层、安全、工作流等。

## 技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Java | 17 | 编译与运行环境 |
| Spring Boot | 3.2.5 | 基础框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴微服务组件 |
| Spring Security | 6.2.4 | 安全框架 |
| Spring Authorization Server | 1.2.4 | OAuth2 授权服务器 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Knife4j | 4.5.0 | API 文档 |
| Redisson | 3.27.2 | 分布式锁 |
| Caffeine | 3.1.8 | 本地缓存 |
| MapStruct | 1.5.5 | 对象映射 |
| Hutool | 5.8.27 | 工具类库 |
| EasyExcel | 3.3.3 | Excel 导入导出 |
| JJWT | 0.12.5 | JWT 令牌 |
| Fastjson2 | 2.0.50 | JSON 处理 |
| Guava | 33.1.0-jre | Google 工具库 |
| Druid | 1.2.22 | 数据库连接池 |

## 模块结构

```
basic-starter-parent
├── basic-common       # 公共基础模块 ✅
├── baisc-cache        # 缓存模块 ✅
├── basic-dal          # 数据访问层模块 ✅（含多租户）
├── basic-security     # 安全模块 ✅（JWT/OAuth2/租户/验证码/API签名/开放平台）
├── basic-log          # 日志模块 ✅
├── basic-workflows    # 工作流模块（待实现）
└── basic-dependency   # 依赖版本统一管理 ✅
```

### basic-common — 公共基础模块

提供项目中通用的基础组件：

- **统一响应体 `Result<T>`** — 封装 API 返回结果，支持链式调用，提供 success / error 快速构建方法
- **全局异常处理 `GlobalExceptionHandler`** — 通过 `@RestControllerAdvice` 统一拦截各类异常（参数校验、空指针、序列化失败、自定义业务异常等），自动包装为标准 Result 响应
- **业务异常基类 `BaseException`** — 自定义异常，支持错误码和错误信息
- **Swagger/Knife4j 配置 `SpringFoxConfig`** — API 文档自动配置
- **Spring Factories 自动装配** — `GlobalExceptionHandler` 和 `SpringFoxConfig` 通过 `spring.factories` 自动装配

### baisc-cache — 缓存模块

提供缓存和分布式相关能力：

- **Redis 配置** — Redis 自动配置与连接管理
- **本地缓存** — 基于 Caffeine 的本地缓存支持
- **分布式锁** — 基于 Redisson 的分布式锁实现 `IDistributedLockServiceImpl`
- **Redis 工具类** — `RedisUtil` 封装常用 Redis 操作
- **唯一 ID 生成** — `UniqueIdUtil`、`UniqueCodeUtils` 唯一编码生成工具
- **Spring Factories 自动装配** — 所有组件通过 `spring.factories` 自动装配

### basic-log — 日志模块

提供操作日志的自动记录能力，通过 `@Log` 注解零侵入记录接口调用详情：

- **@Log 注解** — 标记 Controller/Service 方法，自动记录操作描述、操作类型、请求参数、返回值、执行耗时
- **@Mask 脱敏注解** — 支持 5 种脱敏策略（`DEFAULT` / `ALL` / `FIRST_3` / `LAST_4` / `EMAIL`），自动对敏感字段脱敏后再存储
- **全局敏感字段配置** — 无需修改代码，通过 `basic.log.mask-fields` 即可全局指定敏感字段名
- **链路追踪 TraceId** — `TraceIdFilter` 自动从请求头获取或生成 TraceId，贯穿整个请求链路，响应头返回
- **异步双写** — 通过 `@Async` 异步存储，SLF4J 日志输出（方便 ELK 采集）+ 数据库持久化，可通过配置独立开关
- **自动装配** — 所有组件通过 `spring.factories` + `LogAutoConfiguration` 自动装配

#### 使用示例

```java
// 1. 在 Controller 方法上加 @Log 注解
@RestController
public class UserController {

    @Log(value = "创建用户", type = LogTypeEnum.INSERT)
    @PostMapping("/user")
    public Result<UserVO> createUser(@RequestBody UserDTO dto) {
        return Result.success(userService.save(dto));
    }
}

// 2. 在 DTO 敏感字段上加 @Mask 注解
@Data
public class UserDTO {
    private String username;

    @Mask(type = MaskType.DEFAULT)
    private String mobile;

    @Mask(type = MaskType.ALL)
    private String password;
}
```

#### 配置示例

```yaml
basic:
  log:
    db-enabled: true       # 是否写入数据库，默认 true
    slf4j-enabled: true    # 是否输出到 SLF4J 日志，默认 true
    mask-enabled: true     # 是否启用脱敏，默认 true
    mask-fields:           # 全局敏感字段名（无需 @Mask 注解的场景）
      - password
      - mobile
      - idCard
```

#### 数据库表

建表脚本位于 `basic-log/src/main/resources/sql/operation_log.sql`，对应实体 `LogDO` 继承 `BaseDO`，包含 7 个查询索引。

### basic-dal — 数据访问层模块

提供数据访问层基础组件：

- **MyBatis-Plus 配置** — 分页插件、乐观锁、逻辑删除等自动配置
- **自动填充 `MyMetaObjectHandler`** — 自动填充 `create_time`、`modify_time` 字段
- **基础实体 `BaseDO`** — 所有数据库实体的父类，包含：
  - `id` — 雪花算法主键（ASSIGN_ID）
  - `create_time` — 创建时间（自动填充）
  - `modify_time` — 更新时间（自动填充）
  - `deleted` — 逻辑删除标识（@TableLogic）
  - `version` — 乐观锁版本号（@Version）
- **多租户支持** — `TenantLineInnerInterceptor` 全局租户SQL拦截器 + `TenantMetaObjectHandler` 自动填充 tenant_id
- **Spring Factories 自动装配** — 通过 `spring.factories` 自动装配

### basic-security — 安全模块

提供全面的安全认证与授权能力，涵盖 JWT、OAuth2、多租户、验证码、API 签名、开放平台等 10 大子模块：

#### 子模块概览

| 子模块 | 说明 | 关键类 |
|--------|------|--------|
| **core** | 统一配置属性 + 全局常量 + 分层异常体系 + 登录用户模型 + 安全上下文 | `SecurityProperties`、`SecurityConstant`、`SecurityException`、`LoginUser`、`SecurityContextHolder` |
| **jwt** | JWT 签发/解析/校验 + Token 创建/刷新/黑名单管理 + OncePerRequestFilter | `JwtUtil`、`TokenService`、`JwtAuthenticationFilter` |
| **oauth2** | 授权服务器 + 资源服务器 + JDBC ClientDetails 管理 | `AuthorizationServerConfig`、`ResourceServerConfig`、`JdbcClientDetailsService` |
| **tenant** | 多租户 Filter：X-Tenant-Id 提取 → TenantContextHolder → finally清除 + 白名单跳过 | `TenantFilter` |
| **captcha** | 算术验证码生成 + Redis/Memory 双存储 + Filter 校验 | `CaptchaService`、`CaptchaFilter` |
| **openplatform** | AppId + MD5签名 + Timestamp + Nonce 防重放认证 | `OpenPlatformAuthFilter` |
| **api** | HMAC-SHA256 / MD5 / RSA 签名工具类 + ApiSignFilter + Nonce Redis 防重放 | `ApiSignUtil`、`ApiSignFilter` |
| **annotation** | `@AnonymousAccess` 注解（标记匿名可访问接口） | `AnonymousAccess` |
| **config** | `SecurityAutoConfiguration`：条件装配所有 Filter（按 enabled 开关） | `SecurityAutoConfiguration` |
| **dal 联动** | 与 basic-dal 联动提供全局租户 SQL 拦截 + 自动填充 | 跨模块联动 |

#### 分层异常体系

```
SecurityException (RuntimeException)
 ├── JwtException          # JWT 相关异常（Token过期/无效/缺失）
 ├── CaptchaException      # 验证码异常
 ├── TenantException       # 租户异常（缺失租户标识）
 ├── OpenPlatformException # 开放平台认证异常
 └── ApiSignException      # API 签名异常
```

#### 配置示例

```yaml
basic:
  security:
    # 认证模式：jwt / oauth2
    auth-mode: jwt
    # 匿名访问白名单
    anonymous-urls:
      - /api/public/**
      - /auth/login
      - /auth/captcha
    # 多租户开关
    tenant-enabled: true
    # JWT 配置
    jwt:
      secret: your-256-bit-secret-key-change-in-production
      expiration: 86400000      # 24h
      refresh-expiration: 604800000  # 7d
      token-header: Authorization
      token-prefix: "Bearer "
    # 验证码配置
    captcha:
      enabled: false
      type: arithmetic           # arithmetic / gif
      store: memory              # memory / redis
      expire-seconds: 120
      urls:
        - /auth/login
    # API 签名配置
    api-sign:
      enabled: false
      algorithm: HMAC-SHA256     # HMAC-SHA256 / MD5 / RSA
      app-secrets:
        app001: secret-key-001
    # 开放平台配置
    open-platform:
      enabled: false
      urls:
        - /openapi/**
```

#### 过滤器执行顺序

```
Order -100: Spring Security Filter Chain
Order  -90: JwtAuthenticationFilter    (JWT Token 校验)
Order  -80: TenantFilter              (多租户校验)
Order  -70: CaptchaFilter             (验证码校验)
Order  -60: OpenPlatformAuthFilter    (开放平台认证)
Order  -50: ApiSignFilter             (API 签名校验)
```

#### 使用示例

JWT 认证：

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/current")
    public Result<LoginUser> currentUser() {
        // 线程级安全上下文自动获取
        return Result.success(SecurityContextHolder.getLoginUser());
    }
}
```

匿名访问：

```java
@AnonymousAccess
@PostMapping("/login")
public Result<LoginUser> login(@RequestBody LoginDTO dto) {
    // 无需 Token 即可访问
}
```

#### 数据库表

建表脚本位于 `basic-security/src/main/resources/sql/security_schema.sql`，包含：
- `oauth2_client` — OAuth2 客户端配置
- `open_platform_app` — 开放平台 App 管理
- `tenant_config` — 租户配置

### basic-common 扩展

- **`AuthModeEnum`** — 认证模式枚举（JWT / OAUTH2 / SESSION）
- **`TenantStrategyEnum`** — 租户策略枚举（IGNORE / COLUMN / SCHEMA）
- **`TenantContextHolder`** — 线程级租户上下文持有者（基于 ThreadLocal）

### basic-dependency — 依赖版本管理

统一管理所有第三方依赖的版本号，已升级至 Spring Boot 3.2.5 生态。子模块通过继承此 POM 即可获得一致的版本控制，避免版本冲突。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 安装

在项目中引用各模块：

```xml
<!-- 示例：引入 basic-common -->
<dependency>
    <groupId>com.basic</groupId>
    <artifactId>basic-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

组件通过 Spring Boot 自动装配机制（`spring.factories`）加载，引入依赖后即可直接使用，无需额外配置。

### 使用示例

**统一响应体：**

```java
@RestController
public class DemoController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello World");
    }

    @GetMapping("/error")
    public Result<Void> error() {
        return Result.error("参数不合法");
    }
}
```

**自定义业务异常：**

```java
throw new BaseException("1001", "用户不存在");
```

**数据库实体继承 BaseDO：**

```java
@Data
@TableName("t_user")
public class UserDO extends BaseDO {
    private String username;
    private String email;
}
```

## 项目构建

```bash
# 编译全部模块
mvn clean install -DskipTests

# 编译单个模块
mvn clean install -pl basic-common -DskipTests
```

## 版本

当前版本：**1.0.0**
