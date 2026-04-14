# MyOJ

在线判题系统项目，包含后端服务、前端页面和独立代码沙箱。

## 项目结构

- `yuoj-backend-master`
  Spring Boot 后端，负责用户、题目、提交、比赛、讨论等业务接口。
- `yuoj-frontend-master`
  Vue 3 + Vue CLI 前端，负责题库、做题、比赛、管理后台等页面。
- `yuoj-code-sandbox-master`
  独立代码沙箱服务，负责代码编译与执行。

## 技术栈

- 后端：Spring Boot 2.7、MyBatis-Plus、Redis、RabbitMQ、MySQL
- 前端：Vue 3、Vue Router、Vuex、Arco Design、TypeScript
- 判题：独立沙箱服务 + 异步判题队列

## 本地启动

### 1. 启动后端

在 `yuoj-backend-master` 目录执行：

```bash
mvn spring-boot:run
```

默认接口地址：

```text
http://localhost:8121/api
```

### 2. 启动前端

在 `yuoj-frontend-master` 目录执行：

```bash
npm install
npm run dev
```

默认页面地址：

```text
http://localhost:8080
```

### 3. 启动代码沙箱

在 `yuoj-code-sandbox-master` 目录执行：

```bash
mvn spring-boot:run
```

## 当前整理内容

- 补充了根目录 README
- 清理了前端失效的 `WxMp` 生成代码引用
- 修复了比赛详情页、比赛做题页的乱码展示
- 收紧了部分接口边界和比赛模块逻辑

## 说明

当前项目以课程设计 / 毕业设计可交付为目标，重点是核心功能闭环、可演示性和工程结构清晰，不以商用级完备性为目标。
