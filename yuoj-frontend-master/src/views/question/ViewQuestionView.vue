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
import { useRouter } from "vue-router";
import {
  IconStar,
  IconStarFill,
  IconLoading,
  IconCheckCircleFill,
  IconCloseCircleFill,
  IconCode,
} from "@arco-design/web-vue/es/icon";
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
