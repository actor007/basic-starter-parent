# basic-starter-parent

一套基于 Spring Boot 的基础设施 Starter 组件集，为微服务项目提供开箱即用的公共能力，包括统一响应、全局异常处理、缓存、数据访问层、安全、工作流等。

## 技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Java | 17 | 编译与运行环境 |
| Spring Boot | 2.7.4 | 基础框架 |
| Spring Cloud | 2020.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2021.1 | 阿里巴巴微服务组件 |
| MyBatis-Plus | 3.5.1 | ORM 框架 |
| Knife4j | 3.0.3 | API 文档 |
| Redisson | 3.39.0 | 分布式锁 |
| Caffeine | 2.8.8 | 本地缓存 |
| MapStruct | 1.5.5 | 对象映射 |
| Hutool | 5.8.6 | 工具类库 |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| JJWT | 0.9.1 | JWT 令牌 |
| Fastjson | 1.2.83 | JSON 处理 |
| Guava | 31.0.1-jre | Google 工具库 |
| Druid | 1.1.23 | 数据库连接池 |

## 模块结构

```
basic-starter-parent
├── basic-common       # 公共基础模块
├── baisc-cache        # 缓存模块
├── basic-dal          # 数据访问层模块
├── basic-security     # 安全模块（待实现）
├── basic-log          # 日志模块（待实现）
├── basic-workflows    # 工作流模块（待实现）
└── basic-dependency   # 依赖版本统一管理
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
- **Spring Factories 自动装配** — 通过 `spring.factories` 自动装配

### basic-dependency — 依赖版本管理

统一管理所有第三方依赖的版本号，子模块通过继承此 POM 即可获得一致的版本控制，避免版本冲突。

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
