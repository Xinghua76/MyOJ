实现判题模块（重点写得像论文）：
- 提交后：将判题任务推入 RabbitMQ 队列（包含 submitId、语言、代码、题目用例引用、时间/内存限制）。
- 判题服务：独立进程/服务从队列消费，进入沙箱执行，产出结果（AC/TLE/MLE/RE/SE/安全违规等）。
- 结果处理：写 judge_log、更新 submit 状态、更新题目统计、回传给前端。
要求：
1) RabbitMQ 的 exchange/queue/routing key 设计；失败重试/死信队列；幂等（submitId 去重）。
2) 判题结果结构体定义（stdout/stderr/exitCode/time/memory/违规类型）。
3) 主站与判题服务之间的接口边界（HTTP 回调 or MQ 回传）。
4) 给出最小可跑通的实现步骤与本地调试方法（docker-compose 优先）。
