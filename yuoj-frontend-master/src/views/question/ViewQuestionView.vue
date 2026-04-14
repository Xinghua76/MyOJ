<template>
  <div id="viewQuestionView">
    <a-page-header
      :title="`题目：${question?.title ?? ''}`"
      @back="router.back()"
      style="margin-bottom: 24px; padding: 0"
    >
      <template #extra>
        <a-space>
          <a-button
            :type="isFavour ? 'primary' : 'outline'"
            :status="isFavour ? 'danger' : 'normal'"
            @click="doFavour"
          >
            <template #icon>
              <icon-star-fill v-if="isFavour" />
              <icon-star v-else />
            </template>
            {{ isFavour ? "已收藏" : "收藏" }}
          </a-button>
        </a-space>
      </template>
    </a-page-header>
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :bordered="false">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="answer" title="答案">
            <MdViewer
              v-if="question?.answer"
              :value="
                question.answer.includes('```') ||
                question.answer.includes('public class') ||
                question.answer.includes('#include')
                  ? question.answer.includes('```')
                    ? question.answer
                    : `\`\`\`java\n${question.answer}\n\`\`\``
                  : question.answer
              "
            />
            <div v-else>暂无答案</div>
          </a-tab-pane>
          <a-tab-pane key="solution" title="题解">
            <QuestionSolutionList :questionId="question?.id" />
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="code" v-model:active-key="activeKey">
          <a-tab-pane key="code" title="代码编辑">
            <a-form :model="form" layout="inline">
              <a-form-item
                field="language"
                label="编程语言"
                style="min-width: 240px"
              >
                <a-select
                  v-model="form.language"
                  :style="{ width: '320px' }"
                  placeholder="选择编程语言"
                >
                  <a-option>java</a-option>
                  <a-option>cpp</a-option>
                  <a-option>go</a-option>
                  <a-option>html</a-option>
                </a-select>
              </a-form-item>
            </a-form>
            <CodeEditor
              :value="form.code as string"
              :language="form.language"
              :handle-change="changeCode"
            />
            <a-divider size="0" />
            <a-button type="primary" style="min-width: 200px" @click="doSubmit">
              提交代码
            </a-button>
          </a-tab-pane>
          <a-tab-pane key="result" title="判题结果">
            <div
              v-if="!submitResult"
              style="text-align: center; padding: 40px; color: #888"
            >
              <icon-code size="32" style="margin-bottom: 8px" />
              <div>暂无提交结果，请先提交代码</div>
            </div>
            <a-card v-else :bordered="false" class="result-card">
              <template #title>
                <span style="font-size: 16px; font-weight: bold">判题详情</span>
              </template>
              <a-descriptions :column="1" size="large">
                <a-descriptions-item label="判题状态">
                  <a-tag
                    v-if="submitResult.status === 0"
                    color="gray"
                    size="large"
                    >等待中</a-tag
                  >
                  <a-tag
                    v-else-if="submitResult.status === 1"
                    color="arcoblue"
                    size="large"
                  >
                    <template #icon><icon-loading /></template>
                    判题中
                  </a-tag>
                  <template v-else-if="submitResult.status === 2">
                    <a-tag
                      v-if="
                        isAcceptedJudgeMessage(submitResult.judgeInfo?.message)
                      "
                      color="green"
                      size="large"
                    >
                      <template #icon><icon-check-circle-fill /></template>
                      通过
                    </a-tag>
                    <a-tag v-else color="red" size="large">
                      <template #icon><icon-close-circle-fill /></template>
                      {{
                        localizeJudgeMessage(submitResult.judgeInfo?.message)
                      }}
                    </a-tag>
                  </template>
                  <a-tag
                    v-else-if="submitResult.status === 3"
                    color="red"
                    size="large"
                  >
                    <template #icon><icon-close-circle-fill /></template>
                    失败 ({{
                      localizeJudgeMessage(submitResult.judgeInfo?.message)
                    }})
                  </a-tag>
                  <a-tag v-else color="gray" size="large">未知</a-tag>
                </a-descriptions-item>

                <a-descriptions-item label="判题信息">
                  <a-alert
                    v-if="submitResult.judgeInfo?.message"
                    :type="
                      isAcceptedJudgeMessage(submitResult.judgeInfo?.message)
                        ? 'success'
                        : 'error'
                    "
                  >
                    <div style="white-space: pre-wrap">
                      {{
                        localizeJudgeMessage(submitResult.judgeInfo?.message)
                      }}
                    </div>
                  </a-alert>
                  <span v-else>-</span>
                </a-descriptions-item>

                <a-descriptions-item label="执行用时">
                  <a-tag color="cyan"
                    >{{ submitResult.judgeInfo?.time ?? 0 }} ms</a-tag
                  >
                </a-descriptions-item>

                <a-descriptions-item label="消耗内存">
                  <a-tag color="purple"
                    >{{ submitResult.judgeInfo?.memory ?? 0 }} KB</a-tag
                  >
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-tab-pane>
          <!-- AI 分析 Tab -->
          <a-tab-pane key="ai" title="AI 分析">
            <a-card
              :bordered="false"
              class="ai-card"
              style="background: #fafafa"
            >
              <template #title>
                <a-space :size="12">
                  <icon-robot size="20" style="color: #ff7a45" />
                  <span style="font-weight: 500">AI 编程导师</span>
                </a-space>
              </template>

              <!-- 加载状态 -->
              <div
                v-if="isAiLoading"
                style="text-align: center; padding: 40px 20px"
              >
                <a-spin dot size="large" />
                <div style="margin-top: 20px; color: #666; font-size: 14px">
                  正在深度分析你的代码...
                </div>
                <div style="margin-top: 8px; color: #999; font-size: 12px">
                  这通常需要 5-30 秒
                </div>
              </div>

              <!-- 分析成功 -->
              <div v-else-if="aiResult">
                <!-- AI 分析结果信息栏 -->
                <a-alert
                  v-if="aiResult.includes('⚠️')"
                  type="warning"
                  style="margin-bottom: 16px; padding: 12px"
                >
                  <template #title>
                    <span style="font-weight: 500">系统预设建议</span>
                  </template>
                  <div style="font-size: 12px; color: #999">
                    AI 服务暂时不可用，已由系统提供预设建议
                  </div>
                </a-alert>
                <a-alert
                  v-else
                  type="success"
                  style="margin-bottom: 16px; padding: 12px"
                >
                  <template #title>
                    <span style="font-weight: 500">AI 分析完成</span>
                  </template>
                  <div style="font-size: 12px; color: #999">
                    由 DeepSeek AI 提供专业编程指导
                  </div>
                </a-alert>

                <!-- 分析内容 -->
                <div class="ai-result-content">
                  <MdViewer :value="aiResult" />
                </div>

                <!-- 操作按钮栏 -->
                <a-divider style="margin: 16px 0" />
                <a-space style="width: 100%">
                  <a-button
                    type="primary"
                    size="small"
                    @click="copyAiResult"
                    style="flex: 1"
                  >
                    <template #icon><icon-copy /></template>
                    复制结果
                  </a-button>
                  <a-button
                    type="outline"
                    size="small"
                    @click="askAi"
                    :loading="isAiLoading"
                    style="flex: 1"
                  >
                    <template #icon><icon-refresh /></template>
                    重新分析
                  </a-button>
                </a-space>
              </div>

              <!-- 空状态 -->
              <div v-else style="text-align: center; padding: 40px 20px">
                <div style="font-size: 40px; margin-bottom: 16px">🤖</div>
                <div style="color: #666; font-size: 14px; margin-bottom: 8px">
                  AI 代码分析助手
                </div>
                <div style="color: #999; font-size: 12px; margin-bottom: 20px">
                  使用 AI 深度分析你的代码，找到问题所在
                </div>
                <a-button
                  v-if="form.code && form.language"
                  type="primary"
                  @click="askAi"
                  :loading="isAiLoading"
                >
                  <template #icon><icon-robot /></template>
                  开始分析
                </a-button>
                <div v-else style="color: #ccc; font-size: 12px">
                  请先编写代码
                </div>
              </div>
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect, withDefaults, defineProps } from "vue";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import QuestionSolutionList from "@/views/question/QuestionSolutionList.vue";
import {
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionSubmitVO,
  QuestionVO,
  BaseResponse_QuestionSubmitVO_,
} from "../../../generated";
import { AiControllerService } from "../../../generated";
import { useRouter } from "vue-router";
import {
  IconStar,
  IconStarFill,
  IconLoading,
  IconCheckCircleFill,
  IconCloseCircleFill,
  IconCode,
} from "@arco-design/web-vue/es/icon";
import { IconRobot, IconCopy, IconRefresh } from "@arco-design/web-vue/es/icon";
import axios from "axios";
import {
  isAcceptedJudgeMessage,
  localizeJudgeMessage,
} from "@/utils/judgeMessage";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const router = useRouter();
const question = ref<QuestionVO>();
const isFavour = ref(false);
const activeKey = ref("code");
const submitResult = ref<QuestionSubmitVO>();

// AI 助手相关
const aiResult = ref("");
const isAiLoading = ref(false);

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
    // 检查是否收藏
    checkFavour();
  } else {
    message.error("加载失败，" + res.message);
  }
};

const checkFavour = async () => {
  // 由于后端没有直接提供“是否收藏”的字段，这里我们可以通过查询我的收藏列表来判断
  // 或者后端提供专门的接口。这里为了简单，先通过查询列表判断
  // 更好的方式是后端 GetQuestionVO 时带上 isFavour 字段
  const res = await axios.post("/api/question_favour/my/list/page", {
    current: 1,
    pageSize: 1,
    id: props.id,
  });
  if (res.data.code === 0 && res.data.data.records.length > 0) {
    isFavour.value = true;
  } else {
    isFavour.value = false;
  }
};

const doFavour = async () => {
  const res = await axios.post("/api/question_favour/", {
    questionId: props.id,
  });
  if (res.data.code === 0) {
    // result = 1 收藏成功， -1 取消收藏成功
    if (res.data.data === 1) {
      isFavour.value = true;
      message.success("收藏成功");
    } else {
      isFavour.value = false;
      message.success("已取消收藏");
    }
  } else {
    message.error("操作失败，" + res.data.message);
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: "",
});

/**
 * 恢复草稿
 */
const loadDraft = () => {
  const draft = localStorage.getItem(`code-draft-${props.id}`);
  if (draft) {
    try {
      const data = JSON.parse(draft);
      form.value.language = data.language || "java";
      form.value.code = data.code || "";
    } catch (e) {
      console.error("加载草稿失败", e);
    }
  }
};

/**
 * 保存草稿
 */
const saveDraft = () => {
  localStorage.setItem(
    `code-draft-${props.id}`,
    JSON.stringify({
      language: form.value.language,
      code: form.value.code,
    })
  );
};

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const contestId = router.currentRoute.value.query.contestId;

  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
    contestId: contestId ? String(contestId) : undefined,
  } as any);
  if (res.code === 0) {
    message.success("提交成功");
    // 提交后自动跳转到判题结果 tab
    activeKey.value = "result";
    // 模拟等待一段时间后查询结果（或者轮询）
    // 这里简单处理，先重置结果，显示“判题中”
    submitResult.value = {
      status: 1,
      judgeInfo: {},
    } as any;
    // 轮询查询结果
    pollSubmitResult(res.data);
  } else {
    message.error("提交失败," + res.message);
  }
};

const pollSubmitResult = async (submitId: number) => {
  // 简单的轮询逻辑：每隔 2s 查一次，最多查 10 次
  let count = 0;
  const interval = setInterval(async () => {
    count++;
    const res = await QuestionControllerService.getQuestionSubmitByIdUsingGet(
      submitId
    );
    if (res.code === 0) {
      submitResult.value = res.data;
      if (res.data.status !== 1 && res.data.status !== 0) {
        // 判题结束
        clearInterval(interval);
      }
    }
    if (count > 20) {
      clearInterval(interval);
    }
  }, 2000);
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
  loadDraft();
});

/**
 * 复制 AI 分析结果到剪贴板
 */
const copyAiResult = async () => {
  try {
    // 移除 Markdown 标记中的文本内容（仅保留纯文本）
    const plainText = aiResult.value
      .replace(/#{1,6}\s/g, "") // 移除标题标记
      .replace(/\*\*|__/g, "") // 移除粗体标记
      .replace(/\*|_/g, "") // 移除斜体标记
      .replace(/\[([^\]]+)\]\([^)]+\)/g, "$1") // 移除链接标记
      .replace(/`{3}[\s\S]*?`{3}/g, (match) => {
        // 代码块：保留内容，移除标记
        return match.replace(/`{3}/g, "").trim();
      })
      .replace(/`([^`]+)`/g, "$1"); // 移除行内代码标记

    if (navigator.clipboard) {
      await navigator.clipboard.writeText(plainText);
      message.success("已复制到剪贴板");
    } else {
      // 降级方案：使用 document.execCommand
      const textarea = document.createElement("textarea");
      textarea.value = plainText;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand("copy");
      document.body.removeChild(textarea);
      message.success("已复制到剪贴板");
    }
  } catch (e) {
    console.error("复制失败", e);
    message.error("复制失败，请手动复制");
  }
};

/**
 * 询问 AI 执行分析
 */
const askAi = async () => {
  // 前置校验
  if (!question.value?.id) {
    message.error("题目加载中，请稍后");
    return;
  }
  if (!form.value.code || form.value.code.trim() === "") {
    message.warning("请先编写代码");
    return;
  }
  if (!form.value.language) {
    message.warning("请选择编程语言");
    return;
  }

  // 检查代码长度
  if (form.value.code.length > 50000) {
    message.error("代码长度超过限制（最多 50000 字符）");
    return;
  }

  isAiLoading.value = true;
  aiResult.value = ""; // 清空之前的结果，开始新的分析
  // 自动切换到 AI 分析 Tab
  activeKey.value = "ai";

  try {
    const res = await AiControllerService.analyzeCodeUsingPost({
      questionId: question.value.id as any,
      code: form.value.code,
      language: form.value.language,
      judgeInfo: JSON.stringify(submitResult.value?.judgeInfo || {}),
    });

    if (res.code === 0) {
      const analysisResult = res.data || "AI 暂无反馈，请稍后再试";
      aiResult.value = analysisResult;

      if (analysisResult.includes("⚠️")) {
        message.warning("AI 服务暂时不可用，已自动提供系统预设建议");
      } else {
        message.success("AI 分析完成，请查看 AI 分析 Tab 的详细建议");
      }
    } else if (res.code === 401) {
      message.error("请先登录才能使用 AI 助手");
    } else if (res.code === 400) {
      message.error("参数错误: " + res.message);
    } else if (res.code === 404) {
      message.error("题目不存在或已删除");
    } else {
      message.error("AI 分析失败: " + (res.message || "未知错误"));
      aiResult.value = "⚠️ 分析过程中出现错误\n\n请稍后重试，或联系管理员";
    }
  } catch (error: any) {
    console.error("AI 分析异常", error);

    // 区分不同的错误类型，提供更好的用户提示
    if (error.response?.status === 401) {
      message.error("您的登录已过期，请重新登录");
    } else if (error.response?.status === 400) {
      message.error("请求参数有误，请检查代码是否为空");
    } else if (error.response?.status === 403) {
      message.error("没有权限使用 AI 助手");
    } else if (error.response?.status === 404) {
      message.error("题目不存在，请返回重新选择");
    } else if (error.response?.status === 500) {
      message.error("服务器错误，请稍后重试");
      aiResult.value = "⚠️ 服务器出错\n\n请稍后重新分析";
    } else if (error.message?.includes("timeout")) {
      message.error("分析超时，请检查网络或稍后重试");
      aiResult.value =
        "⚠️ 请求超时\n\n AI 分析耗时较长，请点击 '重新分析' 重试";
    } else if (error.message?.includes("Network")) {
      message.error("网络连接失败，请检查网络");
    } else {
      message.error("AI 分析失败，请稍后重试");
      aiResult.value =
        "⚠️ 分析过程中出现错误\n\n请点击 '重新分析' 重试，或稍后再试";
    }
  } finally {
    isAiLoading.value = false;
  }
};

/**
 * 代码变更
 */
const changeCode = (value: string) => {
  form.value.code = value;
  saveDraft();
};

// 监听语言变化也保存草稿
watchEffect(() => {
  if (form.value.language) {
    saveDraft();
  }
});
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

/* 确保 MdViewer 内部样式生效，避免被全局样式污染 */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial,
    sans-serif, "Apple Color Emoji", "Segoe UI Emoji";
  color: #24292e;
}

/* 覆盖 github-markdown-css 的默认背景色，使其透明以适应卡片背景 */
.markdown-body pre {
  background-color: #f6f8fa;
}
</style>
