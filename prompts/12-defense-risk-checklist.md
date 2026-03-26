# 毕设答辩风险清单（OJ 项目）

## 1. 为什么要用 MQ 异步判题，不直接同步判题？
- 风险点：老师质疑“为了复杂而复杂”。
- 标准回答：同步判题会让提交接口阻塞，用户体验差，且高并发时 Web 线程被长任务占满。异步化后提交接口只做落库+投递，判题由 Worker 消费，吞吐和稳定性更好。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/QuestionSubmitServiceImpl.java`、`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/producer/JudgeTaskProducer.java`。

## 2. 判题任务失败会不会丢？会不会一直卡住？
- 风险点：老师追问可靠性。
- 标准回答：队列配置了重试和死信。重试超过阈值后进 DLQ，死信消费者将任务标记为失败，避免提交长期停留在 `WAITING/RUNNING`。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/config/JudgeRabbitMqConfig.java`、`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/consumer/JudgeDlqConsumer.java`。

## 3. 你怎么避免重复判题？
- 风险点：老师问幂等与并发一致性。
- 标准回答：消费前会检查提交状态，仅 `WAITING` 才进入判题；判题前先原子更新为 `RUNNING`，非目标状态直接忽略。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/queue/consumer/JudgeTaskConsumer.java`、`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/JudgeServiceImpl.java`。

## 4. 用户代码安全怎么保证？
- 风险点：老师质疑“执行任意代码很危险”。
- 标准回答：主站不直接执行用户代码，转发给独立沙箱服务；沙箱使用受限执行策略（超时、内存限制、网络禁用、只读文件系统/Docker 隔离）。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/judge/codesandbox/impl/RemoteCodeSandbox.java`、`yuoj-code-sandbox-master/src/main/java/com/yupi/yuojcodesandbox/JavaDockerCodeSandbox.java`。

## 5. 权限控制怎么做？普通用户能访问管理接口吗？
- 风险点：老师问 RBAC 是否落地。
- 标准回答：接口通过 `@AuthCheck` 标注所需角色，AOP 统一拦截校验；管理员接口要求 `admin`，普通用户访问会被拒绝。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/annotation/AuthCheck.java`、`yuoj-backend-master/src/main/java/com/yupi/yuoj/aop/AuthInterceptor.java`。

## 6. 登录态和会话怎么处理？
- 风险点：老师问鉴权细节。
- 标准回答：登录成功后写入 Session（键 `user_login`），后续接口从 Session 读取当前用户；项目支持 Redis Session 存储，便于多实例共享登录态。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/UserServiceImpl.java`、`yuoj-backend-master/src/main/resources/application.yml`。

## 7. 比赛排行榜怎么计算？为什么这样设计？
- 风险点：老师问算法合理性。
- 标准回答：按用户分组、再按题目分组，取首次 AC 计算 solved/score/penalty，最终按解题数降序、罚时升序排序。该方案符合常见 ACM 计分逻辑，结果可解释。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/ContestRankServiceImpl.java`。

## 8. 数据库设计是否支持核心业务闭环？
- 风险点：老师问“是不是只做了接口，没做好数据模型”。
- 标准回答：核心链路表完整：用户、题目、提交、比赛、报名、比赛题目、排行榜、公告、管理员操作日志；关键关系有唯一约束与索引支撑。
- 证据位：`yuoj-backend-master/sql/create_table.sql`、`yuoj-backend-master/sql/admin_op_log.sql`。

## 9. 管理员行为怎么追溯？
- 风险点：老师问审计能力。
- 标准回答：系统实现 `admin_op_log`，记录操作人、操作类型、描述、数据快照和时间，可用于问题定位和审计。
- 证据位：`yuoj-backend-master/src/main/java/com/yupi/yuoj/service/impl/AdminOpLogServiceImpl.java`。

## 10. 你项目里最关键的工程取舍是什么？
- 风险点：老师看你是否真正理解架构。
- 标准回答：核心取舍是“复杂度换稳定性”：引入 MQ + Worker + 沙箱，牺牲一点部署复杂度，换来判题链路可扩展、可恢复、可隔离。对于 OJ 这类长任务系统，这是必要取舍。

## 11. 现场演示建议（5 分钟）
1. 用户登录 -> 打开题目 -> 提交一份正确代码（展示快速返回 submitId）。
2. 后台查看该提交从 `WAITING` 到最终结果的变化。
3. 展示比赛排行榜刷新结果。
4. 用普通用户访问管理员接口，展示被拒绝。
5. 展示管理员操作日志记录。

## 12. 容易失分的答辩说法（避免）
- “这个是框架自动的，我也不太清楚。”
- “压力测试没做，但理论上应该可以。”
- “安全这块先不考虑，后续再做。”

## 13. 一句话收尾模板
“本项目重点不在页面堆功能，而在判题链路的工程可靠性：异步解耦、失败兜底、权限隔离和可扩展架构，这些设计保证了系统在真实并发场景下可运行、可维护、可演进。”

