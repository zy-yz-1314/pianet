# PCMS · 病人看诊管理系统

一个基于 Vue 3 + Spring Boot 的诊所门诊管理系统，覆盖病人档案、看诊记录、挂号预约、AI 智能咨询、科室与药品管理等核心业务。

## 功能概览

| 功能模块 | 说明 | 角色 |
|---------|------|------|
| 统计控制台 | 关键指标看板、可视化图表 | 全部 |
| 病人档案 | 病人信息的增删改查、详情预览 | 全部 |
| 看诊记录 | 看诊记录的创建、编辑、历史查询 | 全部 |
| 挂号预约 | 在线挂号、排班管理、状态流转 | 全部 |
| AI 咨询 | 智能问诊辅助对话 | 全部 |
| 科室管理 | 科室字典维护 | 管理员 |
| 药品字典 | 药品信息维护 | 管理员 |

### 角色体系

- **管理员（ADMIN）** — 可访问全部功能，管理科室与药品字典
- **医生（DOCTOR）** — 查看/创建病人档案，书写看诊记录，管理排班
- **病人（PATIENT）** — 查看个人档案与看诊记录，在线挂号

## 技术栈

### 后端

- **语言**：Java 17
- **框架**：Spring Boot 3.2.5
- **持久化**：Spring Data JPA + MySQL
- **安全**：Spring Security Crypto（BCrypt 密码哈希）+ 自定义 Token 认证
- **构建**：Maven

### 前端

- **框架**：Vue 3（Composition API）
- **构建工具**：Vite 5
- **UI 组件库**：Element Plus（中文）
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **HTTP**：Axios（带拦截器）

### 数据库

- MySQL 8.x，字符集 utf8mb4

## 项目结构

```
pianet/
├── backend/                   # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/pianet/
│       ├── PianetApplication.java        # 应用入口
│       ├── auth/                         # 认证（拦截器、Token 管理）
│       ├── bootstrap/                    # 数据初始化
│       ├── config/                       # 配置（CORS、MVC、Beans）
│       ├── dto/                          # 请求/响应 DTO
│       ├── entity/                       # JPA 实体
│       ├── model/                        # 枚举等模型
│       ├── repository/                   # Spring Data 仓库
│       ├── service/                      # 业务服务
│       └── web/                          # REST 控制器
├── frontend/                  # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                       # 应用入口
│       ├── App.vue                       # 根组件（布局壳）
│       ├── api/                          # HTTP 客户端
│       ├── components/                   # 公共组件
│       ├── router/                       # 路由配置
│       ├── stores/                       # Pinia 状态
│       ├── styles/                       # 样式主题
│       ├── utils/                        # 工具函数
│       └── views/                        # 页面组件
└── database/
    └── init.sql               # 数据库初始化脚本
```

## 快速开始

### 前置条件

- JDK 17+
- Node.js 18+
- MySQL 8.x
- Maven 3.8+

### 1. 数据库初始化

在 MySQL 中执行初始化脚本：

```bash
mysql -u root -p < database/init.sql
```

或者使用 GUI 工具打开 `database/init.sql` 并执行。

### 2. 启动后端

```bash
cd backend

# 配置数据库连接（按需修改）
# 编辑 src/main/resources/application.properties 中的数据库连接信息

./mvnw spring-boot:run    # macOS / Linux
mvnw.cmd spring-boot:run  # Windows
```

后端运行在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到后端。

## 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pianet
spring.datasource.username=root
spring.datasource.password=your_password
```

### 前端代理

前端开发服务器已配置 `/api` 代理到 `http://localhost:8080`，无需额外配置。

## 默认账户

系统首次启动时会自动创建默认管理员账户（通过 `DataInitializer` 引导类）。

## License

MIT

---

**PCMS** — 让门诊管理更高效。
