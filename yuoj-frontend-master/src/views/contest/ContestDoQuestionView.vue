<template>
  <div id="contestDoQuestionView">
    <a-layout style="height: 100vh">
      <a-layout-header class="header">
        <div class="header-left">
          <a-button type="text" @click="router.back()">
            <icon-arrow-left /> 返回比赛详情
          </a-button>
          <span class="contest-title">{{ contest?.title }}</span>
        </div>
        <div class="header-right">
          <span v-if="contestStatus === 1" class="timer">
            <icon-clock-circle /> 剩余时间：{{ countdownText }}
          </span>
          <span v-else-if="contestStatus === 2" class="timer ended">
            比赛已结束
          </span>
        </div>
      </a-layout-header>
      <a-layout>
        <a-layout-sider
          theme="light"
          breakpoint="lg"
          :width="220"
          collapsible
          :collapsed="collapsed"
          @collapse="onCollapse"
        >
          <div class="menu-title">题目列表</div>
          <a-menu
            :selected-keys="[String(currentQuestionId)]"
            @menu-item-click="handleMenuClick"
          >
            <a-menu-item
              v-for="(item, index) in questionList"
              :key="String(item.questionId)"
            >
              <div class="question-menu-item">
                <span class="question-index">{{
                  String.fromCharCode(65 + index)
                }}</span>
                <span class="question-title">{{
                  item.questionVO?.title || "题目"
                }}</span>
                <span v-if="userQuestionStatus[item.questionId] === 2">
                  <icon-check-circle-fill style="color: green" />
                </span>
                <span v-else-if="userQuestionStatus[item.questionId] === 3">
                  <icon-close-circle-fill style="color: red" />
                </span>
              </div>
            </a-menu-item>
          </a-menu>
        </a-layout-sider>
        <a-layout-content class="content">
          <a-tabs type="card-gutter" v-model:active-key="activeTab">
            <a-tab-pane key="solve" title="做题">
              <!-- 鍋氶鍖哄煙锛岄€昏緫澶嶇敤 ViewQuestionView -->
              <div v-if="currentQuestionId && question" class="solve-container">
                <a-split
                  :style="{
                    height: 'calc(100vh - 150px)',
                    width: '100%',
                    minWidth: '500px',
                    border: '1px solid var(--color-border)',
                  }"
                  v-model:size="splitSize"
                  min="0.2"
                  max="0.8"
                >
                  <template #first>
                    <div class="pane-content">
                      <a-card :bordered="false" class="question-card">
                        <template #title>
                          <span style="font-size: 18px; font-weight: bold">{{
                            question.title
                          }}</span>
                        </template>
                        <template #extra>
                          <a-space wrap>
                            <a-tag
                              v-for="(tag, index) of question.tags"
                              :key="index"
                              color="green"
                              >{{ tag }}</a-tag
                            >
                          </a-space>
                        </template>
                        <a-descriptions
                          :column="{ xs: 1, md: 2, lg: 3 }"
                          size="small"
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
                        <a-divider style="margin: 10px 0" />
                        <MdViewer :value="question.content || ''" />
                      </a-card>
                    </div>
                  </template>
                  <template #second>
                    <div class="pane-content right-pane">
                      <a-tabs v-model:active-key="codeActiveKey">
                        <a-tab-pane key="code" title="代码编辑">
                          <div class="code-editor-container">
                            <div class="code-toolbar">
                              <a-select
                                v-model="form.language"
                                :style="{ width: '150px' }"
                                placeholder="选择语言"
                                size="small"
                              >
                                <a-option>java</a-option>
                                <a-option>cpp</a-option>
                                <a-option>go</a-option>
                                <a-option>html</a-option>
                              </a-select>
                              <a-button
                                type="primary"
                                size="small"
                                @click="doSubmit"
                                :loading="submitting"
                              >
                                提交代码
                              </a-button>
                            </div>
                            <CodeEditor
                              :value="form.code as string"
                              :language="form.language"
                              :handle-change="changeCode"
                              style="height: calc(100% - 40px)"
                            />
                          </div>
                        </a-tab-pane>
                        <a-tab-pane key="result" title="判题结果">
                          <div v-if="!submitResult" class="empty-result">
                            <icon-code size="32" />
                            <div>暂无提交结果，请先提交代码</div>
                          </div>
                          <a-card v-else :bordered="false">
                            <a-descriptions :column="1" size="medium">
                              <a-descriptions-item label="判题状态">
                                <template v-if="submitResult.status === 2">
                                  <a-tag
                                    v-if="
                                      isAcceptedJudgeMessage(
                                        submitResult.judgeInfo?.message
                                      )
                                    "
                                    color="green"
                                    size="large"
                                  >
                                    <template #icon
                                      ><icon-check-circle-fill
                                    /></template>
                                    通过
                                  </a-tag>
                                  <a-tag v-else color="red" size="large">
                                    <template #icon
                                      ><icon-close-circle-fill
                                    /></template>
                                    {{
                                      localizeJudgeMessage(
                                        submitResult.judgeInfo?.message
                                      )
                                    }}
                                  </a-tag>
                                </template>
                                <a-tag
                                  v-else-if="submitResult.status === 3"
                                  color="red"
                                  size="large"
                                >
                                  <template #icon
                                    ><icon-close-circle-fill
                                  /></template>
                                  失败 ({{
                                    localizeJudgeMessage(
                                      submitResult.judgeInfo?.message
                                    )
                                  }})
                                </a-tag>
                                <a-tag
                                  v-else-if="submitResult.status === 1"
                                  color="arcoblue"
                                  size="large"
                                >
                                  <template #icon><icon-loading /></template>
                                  判题中
                                </a-tag>
                                <a-tag v-else color="gray" size="large"
                                  >等待中</a-tag
                                >
                              </a-descriptions-item>
                              <a-descriptions-item label="信息">
                                <a-alert
                                  v-if="submitResult.judgeInfo?.message"
                                  :type="
                                    isAcceptedJudgeMessage(
                                      submitResult.judgeInfo?.message
                                    )
                                      ? 'success'
                                      : 'error'
                                  "
                                >
                                  {{
                                    localizeJudgeMessage(
                                      submitResult.judgeInfo?.message
                                    )
                                  }}
                                </a-alert>
                              </a-descriptions-item>
                              <a-descriptions-item label="用时">
                                {{ submitResult.judgeInfo?.time ?? 0 }} ms
                              </a-descriptions-item>
                              <a-descriptions-item label="内存">
                                {{ submitResult.judgeInfo?.memory ?? 0 }} KB
                              </a-descriptions-item>
                            </a-descriptions>
                          </a-card>
                        </a-tab-pane>
                      </a-tabs>
                    </div>
                  </template>
                </a-split>
              </div>
              <div v-else class="loading-placeholder">
                <a-spin dot />
              </div>
            </a-tab-pane>
            <a-tab-pane key="rank" title="排行榜">
              <a-table
                :columns="rankColumns"
                :data="rankList"
                :pagination="false"
                :scroll="{ y: 'calc(100vh - 200px)' }"
              >
                <template #rank="{ record, rowIndex }">
                  {{ record.rank ?? rowIndex + 1 }}
                </template>
                <template #user="{ record }">
                  {{ record.userVO?.userName }}
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch, defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useStore } from "vuex";
import moment from "moment";
import message from "@arco-design/web-vue/es/message";
import {
  IconArrowLeft,
  IconClockCircle,
  IconCheckCircleFill,
  IconCloseCircleFill,
  IconLoading,
  IconCode,
} from "@arco-design/web-vue/es/icon";
import {
  isAcceptedJudgeMessage,
  localizeJudgeMessage,
} from "@/utils/judgeMessage";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import {
  ContestControllerService,
  ContestQuestionControllerService,
  ContestRankControllerService,
  QuestionControllerService,
  ContestVO,
  QuestionVO,
  QuestionSubmitVO,
  QuestionSubmitAddRequest,
} from "../../../generated";

const props = defineProps<{
  contestId: string;
  questionId?: string;
}>();

const route = useRoute();
const router = useRouter();
const store = useStore();

// UI State
const collapsed = ref(false);
const activeTab = ref("solve");
const codeActiveKey = ref("code");
const splitSize = ref(0.5);
const submitting = ref(false);

// Data State
const contest = ref<ContestVO>();
const questionList = ref([]);
const currentQuestionId = ref<string>(props.questionId || "");
const question = ref<QuestionVO>();
const rankList = ref([]);
const userQuestionStatus = ref<Record<number, number>>({});
const submitResult = ref<QuestionSubmitVO>();

// Form State
const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: "",
});

// Timer
const currentTime = ref(Date.now());
let timer: number | null = null;

// --- Computed ---
const contestStatus = computed(() => {
  if (!contest.value) return -1;
  const now = currentTime.value;
  const start = new Date(contest.value.startTime).getTime();
  const end = new Date(contest.value.endTime).getTime();
  if (now < start) return 0;
  if (now > end) return 2;
  return 1;
});

const countdownText = computed(() => {
  if (!contest.value) return "";
  const now = currentTime.value;
  const end = new Date(contest.value.endTime).getTime();
  if (now >= end) return "00:00:00";
  const diff = end - now;
  const duration = moment.duration(diff);
  const hours = Math.floor(duration.asHours());
  const minutes = duration.minutes();
  const seconds = duration.seconds();
  return `${hours}:${minutes}:${seconds}`;
});

const rankColumns = [
  { title: "排名", slotName: "rank", width: 80 },
  { title: "用户", slotName: "user" },
  { title: "解决数", dataIndex: "solvedCount" },
  { title: "总分", dataIndex: "totalScore" },
  { title: "罚时", dataIndex: "penalty" },
];

// --- Methods ---

const onCollapse = (val: boolean) => {
  collapsed.value = val;
};

const handleMenuClick = (key: string) => {
  if (key !== currentQuestionId.value) {
    currentQuestionId.value = key;
    // Update URL without reload
    router.replace({
      params: { ...route.params, questionId: key },
    });
    loadQuestionDetail(key);
  }
};

const loadContestInfo = async () => {
  const res = await ContestControllerService.getContestVoByIdUsingGet(
    props.contestId as any
  );
  if (res.code === 0) {
    contest.value = res.data;
    if (contestStatus.value === 0) {
      message.warning("比赛尚未开始");
      router.replace(`/contest/detail/${props.contestId}`);
    }
  }
};

const loadQuestionList = async () => {
  const res =
    await ContestQuestionControllerService.listContestQuestionByPageUsingPost({
      contestId: props.contestId as any,
      pageSize: 100,
    });
  if (res.code === 0) {
    questionList.value = res.data.records;
    // Load status
    loadUserQuestionStatus();
    // If no question selected, select first
    if (!currentQuestionId.value && questionList.value.length > 0) {
      currentQuestionId.value = String(questionList.value[0].questionId);
      router.replace({
        params: { ...route.params, questionId: currentQuestionId.value },
      });
      loadQuestionDetail(currentQuestionId.value);
    } else if (currentQuestionId.value) {
      loadQuestionDetail(currentQuestionId.value);
    }
  }
};

const loadUserQuestionStatus = async () => {
  const loginUser = store.state.user.loginUser;
  if (!loginUser.id) return;
  try {
    const res =
      await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        contestId: props.contestId as any,
        userId: loginUser.id,
        pageSize: 1000,
      });
    if (res.code === 0 && res.data.records) {
      const statusMap: Record<number, number> = {};
      res.data.records.forEach((submit: QuestionSubmitVO) => {
        const qId = submit.questionId;
        if (!qId) return;
        if (statusMap[qId] === 2) return;
        if (submit.status === 2 && submit.judgeInfo?.message === "Accepted") {
          statusMap[qId] = 2;
        } else if (
          submit.status === 3 ||
          (submit.status === 2 && submit.judgeInfo?.message !== "Accepted")
        ) {
          if (statusMap[qId] !== 2) statusMap[qId] = 3;
        }
      });
      userQuestionStatus.value = statusMap;
    }
  } catch (e) {
    console.error(e);
  }
};

const loadQuestionDetail = async (qId: string) => {
  if (!qId) return;
  // Clear previous result
  submitResult.value = undefined;
  codeActiveKey.value = "code";

  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    qId as any
  );
  if (res.code === 0) {
    question.value = res.data;
    loadDraft(qId);
  } else {
    message.error("加载题目失败: " + res.message);
  }
};

const loadRank = async () => {
  const res = await ContestRankControllerService.listContestRankByPageUsingPost(
    {
      contestId: props.contestId as any,
      pageSize: 100,
    }
  );
  if (res.code === 0) {
    rankList.value = res.data.records;
  }
};

// --- Code Logic ---

const loadDraft = (qId: string) => {
  const draft = localStorage.getItem(`code-draft-${qId}`);
  if (draft) {
    try {
      const data = JSON.parse(draft);
      form.value.language = data.language || "java";
      form.value.code = data.code || "";
    } catch (e) {
      console.error(e);
    }
  } else {
    form.value.code = ""; // Clear code if no draft
  }
};

const saveDraft = () => {
  if (currentQuestionId.value) {
    localStorage.setItem(
      `code-draft-${currentQuestionId.value}`,
      JSON.stringify({
        language: form.value.language,
        code: form.value.code,
      })
    );
  }
};

const changeCode = (value: string) => {
  form.value.code = value;
  saveDraft();
};

const doSubmit = async () => {
  if (!question.value?.id) return;
  submitting.value = true;
  try {
    const res = await QuestionControllerService.doQuestionSubmitUsingPost({
      ...form.value,
      questionId: question.value.id,
      contestId: props.contestId as any,
    } as unknown as QuestionSubmitAddRequest);
    if (res.code === 0) {
      message.success("提交成功");
      codeActiveKey.value = "result";
      submitResult.value = { status: 1, judgeInfo: {} } as QuestionSubmitVO;
      pollSubmitResult(res.data);
      // Update status map tentatively (not accurate until judged, but shows activity)
    } else {
      message.error("提交失败: " + res.message);
    }
  } catch (e) {
    message.error("提交出错: " + (e as Error).message);
  } finally {
    submitting.value = false;
  }
};

const pollSubmitResult = async (submitId: number) => {
  let count = 0;
  const interval = setInterval(async () => {
    count++;
    const res = await QuestionControllerService.getQuestionSubmitByIdUsingGet(
      submitId
    );
    if (res.code === 0) {
      submitResult.value = res.data;
      if (res.data.status !== 1 && res.data.status !== 0) {
        clearInterval(interval);
        // Refresh user status after judge done
        loadUserQuestionStatus();
        // Also refresh rank if user is looking at rank? Maybe not automatically.
      }
    }
    if (count > 20) clearInterval(interval);
  }, 2000);
};

// --- Lifecycle ---

watch(
  () => props.questionId,
  (newId) => {
    if (newId && newId !== currentQuestionId.value) {
      currentQuestionId.value = newId;
      loadQuestionDetail(newId);
    }
  }
);

watch(activeTab, (newTab) => {
  if (newTab === "rank") {
    loadRank();
  }
});

onMounted(() => {
  loadContestInfo().then(() => {
    if (contestStatus.value !== 0) {
      loadQuestionList();
    }
  });
  timer = setInterval(() => {
    currentTime.value = Date.now();
  }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
#contestDoQuestionView {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.contest-title {
  font-size: 16px;
  font-weight: bold;
}

.timer {
  font-family: monospace;
  font-size: 16px;
  font-weight: bold;
  color: #165dff;
}

.timer.ended {
  color: #86909c;
}

.menu-title {
  padding: 12px 16px;
  font-weight: bold;
  color: #86909c;
  border-bottom: 1px solid #f2f3f5;
}

.question-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-index {
  font-weight: bold;
  width: 20px;
}

.question-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content {
  padding: 0;
  background: #f7f8fa;
}

.solve-container {
  height: calc(100vh - 100px);
  padding: 10px;
  box-sizing: border-box;
}

.pane-content {
  height: 100%;
  overflow-y: auto;
  background: #fff;
}

.right-pane {
  display: flex;
  flex-direction: column;
}

.code-editor-container {
  height: 600px; /* fallback */
  height: 100%;
  display: flex;
  flex-direction: column;
}

.code-toolbar {
  padding: 8px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-result {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #c9cdd4;
}

:deep(.arco-tabs-content) {
  padding-top: 10px;
}

:deep(.arco-split-pane) {
  overflow: hidden;
}
</style>
