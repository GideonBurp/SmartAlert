# SmartAlert 项目脚手架

基于 Spring Boot 3 + Java 21 + Sa-Token 的现代化后端脚手架

## 技术栈

- **Spring Boot 3.2.2** - 核心框架
- **Java 21** - JDK版本
- **Sa-Token 1.37.0** - 轻量级权限认证框架
- **MyBatis-Plus 3.5.5** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存和会话存储
- **Druid** - 数据库连接池
- **Validation** - 参数校验
- **Lombok** - 简化代码

## 项目结构

```
src/main/java/cn/gideon/smartalert/
├── auth/                          # 认证模块
│   └── controller/
│       └── AuthController.java    # 登录注册接口
├── common/                        # 通用模块
│   ├── exception/
│   │   ├── BusinessException.java # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   └── response/
│       └── Result.java            # 统一响应封装
├── user/                          # 用户模块（领域）
│   ├── constant/                  # 常量
│   │   ├── UserPermission.java    # 用户权限
│   │   ├── UserRole.java          # 用户角色
│   │   └── UserStateEnum.java     # 用户状态
│   ├── dto/                       # 数据传输对象
│   │   ├── LoginRequest.java      # 登录请求
│   │   └── RegisterRequest.java   # 注册请求
│   ├── entity/                    # 实体类
│   │   └── User.java              # 用户实体
│   ├── mapper/                    # 数据访问层
│   │   └── UserMapper.java        # 用户Mapper
│   ├── service/                   # 业务逻辑层
│   │   └── UserService.java       # 用户服务
│   └── response/                  # 响应数据
│       └── data/
│           ├── BasicUserInfo.java
│           ├── InviteRankInfo.java
│           └── UserInfo.java
├── web/                           # Web配置模块（网络相关）
│   ├── config/
│   │   ├── SaTokenConfigure.java  # Sa-Token配置
│   │   └── StpInterfaceImpl.java  # 权限验证实现
│   └── filter/                    # 过滤器
│       ├── CorsFilter.java        # CORS跨域过滤器
│       ├── RateLimitFilter.java   # 限流过滤器
│       └── RequestLogFilter.java  # 请求日志过滤器
└── SmartAlertApplication.java     # 启动类
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化

执行 `src/main/resources/db/init.sql` 脚本创建数据库和表：

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartalert
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

或者在 IDE 中运行 `SmartAlertApplication` 类。

## API 接口

### 认证接口

#### 1. 用户注册

```http
POST /auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123",
  "telephone": "13800138000",
  "email": "test@example.com"
}
```

响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "message": "注册成功"
  },
  "timestamp": 1234567890
}
```

#### 2. 用户登录

```http
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123"
}
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "tokenName": "satoken"
  },
  "timestamp": 1234567890
}
```

#### 3. 用户登出

```http
POST /auth/logout
Authorization: satoken {token}
```

#### 4. 检查登录状态

```http
GET /auth/isLogin
Authorization: satoken {token}
```

### 使用 Token

登录成功后，在后续请求的 Header 中携带 token：

```http
Authorization: satoken {your-token}
```

## 核心功能

### 1. 统一响应封装

所有接口返回统一的 `Result` 格式：

```java
Result.success(data);           // 成功响应
Result.error("错误信息");        // 失败响应
Result.error(400, "错误信息");  // 指定错误码
```

### 2. 全局异常处理

- `BusinessException` - 业务异常
- 参数校验异常自动处理
- 其他异常统一捕获

### 3. 请求过滤器

#### CORS 跨域过滤器
- 支持跨域请求
- 自动处理 OPTIONS 预检请求

#### 请求日志过滤器
- 记录每个请求的详细信息
- 包含请求ID、方法、路径、IP、耗时等

#### 限流过滤器
- 基于 IP 的限流
- 默认每秒 100 次请求
- 可通过配置调整

### 4. 权限认证

基于 Sa-Token 实现的权限控制：

- 登录验证
- 角色验证（ADMIN、CUSTOMER）
- 权限验证（BASIC、AUTH、FROZEN）

配置位置：`SaTokenConfigure.java`

## 扩展开发

### 添加新的领域模块

1. 创建新的包，如 `cn.gideon.smartalert.order`
2. 按照标准结构创建子包：
   - `entity` - 实体类
   - `mapper` - Mapper接口
   - `service` - 业务逻辑
   - `controller` - 控制器
   - `dto` - 数据传输对象

### 自定义权限规则

编辑 `StpInterfaceImpl.java` 添加自定义权限逻辑。

### 调整限流策略

在 `application.yml` 中修改：

```yaml
rate:
  limit:
    enabled: true
    requests-per-second: 100  # 每秒请求数
```

## 注意事项

1. **密码加密**：当前示例使用明文密码，生产环境请使用 BCrypt 加密
2. **Redis 必需**：Sa-Token 依赖 Redis 存储 session
3. **数据库配置**：确保 MySQL 和 Redis 已启动并正确配置
4. **端口占用**：默认端口为 8080，可在配置文件中修改

## 测试账号

数据库初始化后会创建两个测试账号：

1. 管理员账号
   - 用户名：admin
   - 密码：admin123

2. 普通用户
   - 用户名：test
   - 密码：test123

## License

MIT
