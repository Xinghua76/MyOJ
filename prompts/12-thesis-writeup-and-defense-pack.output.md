# 在线判题系统毕业设计论文内容（可直接使用）

## 1. 需求分析

### 1.1 功能需求

本系统围绕“在线编程训练 + 比赛 + 自动判题”构建，核心功能如下：

1. 用户与账号体系
- 用户注册、登录、退出、个人信息维护。
- 支持账号密码登录，以及邮箱验证码、短信验证码相关流程。
- 管理员可进行用户管理（查询、修改、封禁、删除）。

2. 题库管理
- 题目增删改查，包含标题、描述、标签、难度、判题用例、判题配置。
- 题目分页查询、个人题目管理。
- 支持题目批量导入/导出（Excel）。

3. 提交与判题
- 用户提交代码后生成提交记录（`WAITING`），异步投递判题任务。
- 判题 Worker 消费消息后执行判题逻辑，返回 Accepted / Wrong Answer / Compile Error / Runtime Error 等结果。
- 题目统计字段自动更新（提交数、通过数）。

4. 比赛模块
- 管理员创建比赛、配置比赛时间与题目。
- 用户报名后可在比赛时间内提交。
- 系统基于提交记录动态计算排行榜（解题数、总分、罚时）。

5. 社区与运营能力
- 系统公告发布。
- 题解、评论、点赞、收藏等互动能力。
- 管理员操作日志记录，支持审计追溯。

### 1.2 非功能需求

1. 安全性
- 采用会话登录态（Session）管理，后端通过 `@AuthCheck` + AOP 进行角色权限控制（`user/admin/ban`）。
- 提交代码不在主站进程中直接执行，而通过远程沙箱服务执行。
- 沙箱执行增加时间、内存、网络隔离与只读文件系统等限制（Docker 方案）。
- 所有核心接口存在参数校验与统一异常处理，降低非法请求影响面。

2. 性能
- 判题链路采用 RabbitMQ 异步化，解耦“提交请求响应”与“判题执行”，减少接口阻塞时间。
- 判题消费者使用预取控制（`prefetch=1`）和失败重试机制，避免过载与消息风暴。
- 排行榜分页返回，防止一次性大结果集传输。

3. 可用性
- 判题消息配置重试 + 死信队列（DLQ），异常任务可落入死信并标记失败，避免长期卡在 `WAITING/RUNNING`。
- Worker 可独立进程启动，主站与判题执行面可分离部署，降低互相影响。

4. 可扩展性
- 判题沙箱通过工厂模式和统一接口抽象（本地/远程/第三方），便于后续新增语言与执行后端。
- 比赛、题库、用户、公告、日志模块均以独立服务层实现，易于继续拆分成微服务。

---

## 2. 总体设计

### 2.1 系统架构文字描述

系统采用“主站服务 + 判题 Worker + 远程沙箱”的分层协作架构：

1. 主站服务（Spring Boot）
- 承担用户登录、题库管理、比赛管理、提交入口、排行榜查询、管理后台等 API。
- 用户提交代码后，仅负责落库并投递判题消息，不直接执行用户程序。

2. 判题队列（RabbitMQ）
- 主站将提交 ID 投递到判题交换机与队列。
- Worker 进程监听判题队列，执行判题。
- 异常消息经过重试后进入死信队列，由 DLQ 消费器兜底处理。

3. 判题 Worker（后端同代码基、不同启动模式）
- 独立无 Web 进程运行，专注消费判题任务。
- 读取提交记录和题目测试用例，调用沙箱执行代码，写回判题结果。

4. 代码沙箱服务（独立服务）
- 对外提供 `/executeCode` 接口。
- 接收代码、语言、输入用例后在隔离环境执行并返回输出、耗时、内存。

5. 缓存与会话（Redis）
- 支持 Session 存储与高频数据访问能力。
- 为后续排行榜缓存、热点题目缓存预留扩展空间。

6. 持久层（MySQL）
- 存储用户、题目、提交、比赛、排行榜、公告、日志等业务数据。
- 通过 MyBatis-Plus 实现对象映射、逻辑删除与分页查询。

### 2.2 核心调用关系

`前端 -> 主站API -> MySQL/RabbitMQ/Redis`

`判题提交 -> RabbitMQ -> Worker -> 沙箱服务 -> Worker写回MySQL`

`排行榜查询 -> 主站聚合比赛提交记录 -> 动态计算分页结果`

---

## 3. 详细设计（关键模块流程）

### 3.1 登录鉴权模块（时序文字版）

1. 用户发起登录请求（账号密码或验证码模式）。
2. 后端校验参数合法性（账号长度、密码长度、验证码有效期等）。
3. 服务层查询用户并校验状态（封禁用户拒绝登录）。
4. 登录成功后，将用户对象写入 Session（键：`user_login`），并更新最近登录时间。
5. 受保护接口通过 `@AuthCheck(mustRole=...)` 标注。
6. AOP 在方法执行前读取当前登录用户，校验角色是否满足（如管理员权限）。
7. 校验通过后放行；不通过返回无权限错误。

### 3.2 提交判题链路（流程文字版）

1. 用户调用提交接口，传入题目 ID、语言、代码（比赛场景附带 `contestId`）。
2. 后端校验：语言是否支持、题目是否存在、比赛是否在有效时间、用户是否已报名、题目是否属于该比赛。
3. 创建 `question_submit` 记录，初始状态 `WAITING`。
4. 更新题目提交数 `submit_num + 1`。
5. 将 `questionSubmitId` 投递到 RabbitMQ。
6. Worker 消费消息后再次校验提交状态，避免重复判题。
7. Worker 将状态改为 `RUNNING`，加载题目 `judgeCase`，调用沙箱执行。
8. 沙箱返回输出与资源消耗后，判题策略模块比对标准答案。
9. 回写 `judge_info` 与最终状态（成功/失败），若 Accepted 则更新 `accepted_num + 1`。
10. 若消费异常达到重试上限，消息进入 DLQ，由死信消费者兜底标记为系统失败。

### 3.3 排行榜模块（流程文字版）

1. 客户端请求比赛排行榜，传入 `contestId` 与分页参数。
2. 服务查询比赛信息并获取开始时间。
3. 查询该比赛全部提交记录，按用户分组。
4. 对每个用户再按题目分组，按提交时间排序。
5. 每题只统计首次 AC：
- 计算解题数 `solvedCount`。
- 累计题目分值 `totalScore`。
- 计算罚时 `penalty = 解题耗时 + 错误提交罚时`。
6. 按“解题数降序、罚时升序”排序生成榜单。
7. 执行分页截取，并补充用户展示信息后返回。

### 3.4 沙箱隔离模块（流程文字版）

1. Worker 通过统一 `CodeSandbox` 接口调用远程沙箱。
2. 沙箱服务校验请求头（`auth`）后接收执行请求。
3. 将用户代码写入临时目录并编译。
4. 在受限环境运行代码：
- 设置时间上限；
- 限制内存/Cpu；
- 禁用网络；
- 只读根文件系统（Docker 方案）。
5. 汇总每组输入的输出、错误、耗时、内存。
6. 返回结构化执行结果给 Worker。
7. Worker 根据执行状态映射为业务判题状态并落库。

---

## 4. 数据库设计（ER 说明）

### 4.1 核心实体关系

1. `user` 与 `question`：一对多（一个用户可创建多道题）。
2. `user` 与 `question_submit`：一对多（一个用户可有多次提交）。
3. `question` 与 `question_submit`：一对多（一道题对应多次提交）。
4. `contest` 与 `contest_question`：一对多；`contest_question` 与 `question` 多对一。
5. `contest` 与 `contest_signup`：一对多；`contest_signup` 与 `user` 多对一。
6. `contest` 与 `contest_rank`：一对多；`contest_rank` 与 `user` 多对一。
7. `admin_op_log` 与 `user`：多对一（记录管理员操作人）。

### 4.2 关键数据表字段说明

| 表名 | 关键字段 | 含义 |
|---|---|---|
| user | id, user_account, user_password, user_role, status, last_login_time | 用户身份、角色与状态 |
| user_verify_code | email/phone, code, scene, status, expire_time | 邮箱/短信验证码与有效期 |
| question | title, content, tags, difficulty, judge_case, judge_config, submit_num, accepted_num, status | 题目主体、判题配置、统计信息 |
| question_submit | language, code, judge_info, status, question_id, contest_id, user_id, submit_time | 提交源码、判题结果、归属关系 |
| contest | title, start_time, end_time, status, creator_id | 比赛元数据 |
| contest_question | contest_id, question_id, score, order_no | 比赛与题目映射、分值与顺序 |
| contest_signup | contest_id, user_id, signup_time, status | 报名关系与状态 |
| contest_rank | contest_id, user_id, solved_count, total_score, penalty | 排行榜快照/结果 |
| notice | title, content, publisher_id, status, publish_time | 公告发布 |
| admin_op_log | user_id, op_type, op_desc, op_data, create_time | 管理员行为审计 |

### 4.3 索引与约束设计要点

1. 高频查询字段建立索引：如 `question_submit(question_id/user_id/status)`、`contest_question(contest_id)`。
2. 关键唯一约束防止重复关系：如 `contest_signup(contest_id,user_id)`。
3. 采用 `is_delete` 逻辑删除，兼顾业务恢复与审计需求。

---

## 5. 测试设计

### 5.1 功能测试用例（示例）

| 用例编号 | 测试目标 | 输入/操作 | 预期结果 |
|---|---|---|---|
| F-01 | 账号密码登录 | 正确账号+密码 | 返回登录成功并建立会话 |
| F-02 | 封禁用户登录 | `user_role=ban` 用户登录 | 返回无权限错误 |
| F-03 | 题目创建 | 提交完整题目参数 | 题目落库成功，默认统计字段正确 |
| F-04 | 比赛提交鉴权 | 未报名用户提交比赛题目 | 返回 `not signed up` |
| F-05 | 提交入队 | 提交代码 | 生成 `question_submit(WAITING)` 且消息投递成功 |
| F-06 | 判题成功 | 正确代码 + 正确样例 | 状态更新为成功，`judge_info=Accepted` |
| F-07 | 判题编译失败 | 存在语法错误代码 | 返回编译错误，状态为成功态但结果为 Compile Error（按当前实现） |
| F-08 | 排行榜排序 | 多用户多题提交数据 | 按解题数降序、罚时升序排序 |
| F-09 | 管理日志 | 管理员更新/删除用户 | `admin_op_log` 生成对应记录 |
| F-10 | Excel 导入题目 | 合法 Excel 文件 | 批量写入成功，字段映射正确 |

### 5.2 压力测试点

1. 并发提交压测
- 场景：同一时段多用户高并发提交（如 200~1000 并发请求）。
- 关注指标：提交接口 P95、消息堆积长度、Worker 消费速率、失败率。
- 验证目标：提交接口稳定快速返回；判题任务可被持续消费，不出现大面积超时。

2. 排行榜刷新压测
- 场景：比赛进行中高频刷新榜单（多用户同时分页查询）。
- 关注指标：榜单接口响应时间、数据库查询耗时、CPU 占用。
- 验证目标：在分页条件下维持可接受延迟，且排序结果一致。

### 5.3 安全测试点

1. 权限绕过测试
- 普通用户访问管理员接口（如用户管理、题目批量删除）应被拦截。

2. 提交代码安全测试
- 构造恶意代码（死循环、超内存、文件读写、网络访问）验证沙箱限时与隔离生效。

3. 参数与输入安全
- 对关键接口进行空值、超长、非法类型、越权 ID 访问测试，验证统一异常处理。

4. 消息可靠性测试
- 人为制造判题异常，验证重试与死信机制是否将任务正确标记为失败，避免“悬挂任务”。

---

## 6. 总结与展望

本系统以“主站业务 + 消息队列 + 独立判题 Worker + 沙箱隔离执行”为核心，实现了在线判题平台从题库、提交、判题到比赛排行榜的完整闭环。工程上重点解决了同步判题阻塞、代码执行安全与异常任务兜底问题，具备继续演进为更大规模 OJ 平台的基础。

后续可扩展方向如下：

1. 多机判题与弹性扩容
- 将 Worker 横向扩展到多实例，按队列长度自动扩缩容。

2. 分布式沙箱
- 支持多沙箱节点调度，按语言或负载分流，提升吞吐与容灾能力。

3. 更多语言支持
- 在统一 `CodeSandbox` 协议下接入 C/C++/Python/Go 等执行镜像。

4. 反作弊能力
- 增加代码相似度检测、异常提交行为检测、比赛实时风控策略。

5. 排行榜性能优化
- 引入 Redis 缓存和增量计算，降低大规模比赛下全量重算成本。

---

## 附：与实现对应的主要代码位置

- 登录与会话：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/UserServiceImpl.java`
- 权限拦截：`yuoj-backend-master/src/main/java/com/yupi/yuoj/aop/AuthInterceptor.java`
- 提交入口：`yuoj-backend-master/src/main/java/com/yupi/yuoj/controller/QuestionController.java`
- 提交服务：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/QuestionSubmitServiceImpl.java`
- 判题核心：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/JudgeServiceImpl.java`
- MQ 配置：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/config/JudgeRabbitMqConfig.java`
- Worker 消费：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/consumer/JudgeTaskConsumer.java`
- 死信兜底：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/consumer/JudgeDlqConsumer.java`
- 排行榜计算：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/ContestRankServiceImpl.java`
- 沙箱服务：`yuoj-code-sandbox-master/src/main/java/com/yupi/yuojcodesandbox`
- 数据库脚本：`yuoj-backend-master/sql/create_table.sql`、`yuoj-backend-master/sql/admin_op_log.sql`
