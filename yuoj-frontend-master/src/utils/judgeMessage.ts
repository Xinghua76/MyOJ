const JUDGE_MESSAGE_MAP: Record<string, string> = {
  Accepted: "通过",
  "Wrong Answer": "答案错误",
  "Compile Error": "编译错误",
  "Runtime Error": "运行错误",
  "Time Limit Exceeded": "超时",
  "Memory Limit Exceeded": "内存超限",
  "Output Limit Exceeded": "输出超限",
  "Presentation Error": "格式错误",
  "System Error": "系统错误",
};

export const localizeJudgeMessage = (message?: string) => {
  if (!message) {
    return "";
  }
  return JUDGE_MESSAGE_MAP[message] || message;
};

export const isAcceptedJudgeMessage = (message?: string) => {
  return message === "Accepted" || message === "通过" || message === "成功";
};
