<template>
  <div id="contestDetailView">
    <a-page-header
      :title="contest?.title || '比赛详情'"
      @back="router.back()"
      style="margin-bottom: 16px; padding: 0"
    />
    <a-spin :loading="loading" style="width: 100%">
      <a-card v-if="contest" :bordered="false">
        <template #extra>
          <a-space>
            <a-tag :color="statusMeta.color">{{ statusMeta.label }}</a-tag>
            <a-button
              v-if="showSignupButton"
              type="primary"
              @click="handleSignup"
            >
              报名参赛
            </a-button>
            <a-popconfirm
              v-if="showCancelButton"
              content="确定取消报名吗？"
              @ok="handleCancelSignup"
            >
              <a-button status="danger">取消报名</a-button>
            </a-popconfirm>
            <a-button
              v-if="showEnterButton"
              type="primary"
              @click="enterContest"
            >
              进入比赛
            </a-button>
          </a-space>
        </template>

        <a-alert :type="statusMeta.alertType" style="margin-bottom: 16px">
          {{ statusMessage }}
        </a-alert>

        <a-row :gutter="16" style="margin-bottom: 16px">
          <a-col :xs="24" :sm="12" :md="6">
            <a-statistic
              title="开始时间"
              :value="formatTime(contest.startTime) as any"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-statistic
              title="结束时间"
              :value="formatTime(contest.endTime) as any"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card size="small" class="text-stat-card">
              <div class="text-stat-title">比赛时长</div>
              <div class="text-stat-value">{{ durationText }}</div>
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-card size="small" class="text-stat-card">
              <div class="text-stat-title">当前倒计时</div>
              <div class="text-stat-value">{{ countdownText }}</div>
            </a-card>
          </a-col>
        </a-row>

        <a-row :gutter="16" style="margin-bottom: 20px">
          <a-col :xs="24" :sm="8">
            <a-card size="small">
              <a-statistic title="报名人数" :value="signupCount" />
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card size="small">
              <a-statistic title="题目数量" :value="questionList.length" />
            </a-card>
          </a-col>
          <a-col :xs="24" :sm="8">
            <a-card size="small">
              <a-statistic title="榜单人数" :value="rankList.length" />
            </a-card>
          </a-col>
        </a-row>

        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="创建者">
            {{ contest.creatorVO?.userName || "未知" }}
          </a-descriptions-item>
          <a-descriptions-item label="我的状态">
            {{ hasSignedUp ? "已报名" : "未报名" }}
          </a-descriptions-item>
          <a-descriptions-item label="比赛说明" :span="2">
            {{ contest.description || "暂无说明" }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <a-tabs v-if="canViewContent" default-active-key="questions">
          <a-tab-pane key="questions" title="题目列表">
            <a-table
              :columns="questionColumns"
              :data="questionList"
              :pagination="false"
              row-key="id"
            >
              <template #title="{ record, rowIndex }">
                <a-link @click="toDoQuestion(record.questionId)">
                  {{ String.fromCharCode(65 + rowIndex) }}.
                  {{ record.questionVO?.title || `题目 ${record.questionId}` }}
                </a-link>
              </template>
              <template #status="{ record }">
                <a-tag
                  v-if="userQuestionStatus[record.questionId] === 2"
                  color="green"
                >
                  已通过
                </a-tag>
                <a-tag
                  v-else-if="userQuestionStatus[record.questionId] === 3"
                  color="red"
                >
                  已尝试
                </a-tag>
                <a-tag v-else color="gray">未提交</a-tag>
              </template>
              <template #action="{ record }">
                <a-button
                  type="primary"
                  size="small"
                  @click="toDoQuestion(record.questionId)"
                >
                  做题
                </a-button>
              </template>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="rank" title="排行榜">
            <a-table
              :columns="rankColumns"
              :data="rankList"
              :pagination="false"
              row-key="userId"
            >
              <template #rank="{ record }">
                {{ record.rank ?? "-" }}
              </template>
              <template #user="{ record }">
                {{ record.userVO?.userName || `用户 ${record.userId}` }}
              </template>
              <template #penalty="{ record }">
                {{ formatPenalty(record.penalty) }}
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>

        <a-empty
          v-else
          description="比赛未开始，当前仅支持查看基本信息与报名。"
        />
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import {
  ContestControllerService,
  ContestQuestionControllerService,
  ContestRankControllerService,
  ContestSignupControllerService,
  ContestVO,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

type ContestQuestionRecord = any;
type ContestRankRecord = any;
type ContestSignupRecord = any;

const props = defineProps<{
  id: string;
}>();

const router = useRouter();
const store = useStore();

const loading = ref(false);
const contest = ref<ContestVO>();
const hasSignedUp = ref(false);
const signupRecordId = ref<number>();
const signupCount = ref(0);
const questionList = ref<ContestQuestionRecord[]>([]);
const rankList = ref<ContestRankRecord[]>([]);
const userQuestionStatus = ref<Record<number, number>>({});
const currentTime = ref(Date.now());
let timer: number | null = null;

const currentUserId = computed(() =>
  Number(store.state.user?.loginUser?.id || 0)
);

const contestStatus = computed(() => {
  if (!contest.value) {
    return -1;
  }
  const now = currentTime.value;
  const start = parseTimestamp(contest.value.startTime);
  const end = parseTimestamp(contest.value.endTime);
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return -1;
  }
  if (now < start) {
    return 0;
  }
  if (now > end) {
    return 2;
  }
  return 1;
});

const statusMeta = computed(() => {
  if (contestStatus.value === 0) {
    return { label: "未开始", color: "blue", alertType: "info" as const };
  }
  if (contestStatus.value === 1) {
    return { label: "进行中", color: "green", alertType: "success" as const };
  }
  if (contestStatus.value === 2) {
    return { label: "已结束", color: "gray", alertType: "warning" as const };
  }
  return { label: "未知", color: "gray", alertType: "info" as const };
});

const countdownText = computed(() => {
  if (!contest.value) {
    return "-";
  }
  const now = currentTime.value;
  const start = parseTimestamp(contest.value.startTime);
  const end = parseTimestamp(contest.value.endTime);
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return "-";
  }
  let target = 0;
  if (contestStatus.value === 0) {
    target = start;
  } else if (contestStatus.value === 1) {
    target = end;
  } else {
    return "00:00:00";
  }
  const diff = Math.max(target - now, 0);
  const duration = moment.duration(diff);
  const hours = Math.floor(duration.asHours()).toString().padStart(2, "0");
  const minutes = duration.minutes().toString().padStart(2, "0");
  const seconds = duration.seconds().toString().padStart(2, "0");
  return `${hours}:${minutes}:${seconds}`;
});

const durationText = computed(() => {
  if (!contest.value?.startTime || !contest.value?.endTime) {
    return "-";
  }
  const start = parseTimestamp(contest.value.startTime);
  const end = parseTimestamp(contest.value.endTime);
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return "-";
  }
  const diff = Math.max(end - start, 0);
  const duration = moment.duration(diff);
  const hours = Math.floor(duration.asHours());
  const minutes = duration.minutes();
  return `${hours} 小时 ${minutes} 分钟`;
});

const statusMessage = computed(() => {
  if (contestStatus.value === 0) {
    return `比赛尚未开始，距离开始还有 ${countdownText.value}。`;
  }
  if (contestStatus.value === 1) {
    return hasSignedUp.value
      ? `比赛进行中，距离结束还有 ${countdownText.value}。你已报名，可以进入比赛。`
      : `比赛进行中，距离结束还有 ${countdownText.value}。未报名用户不能正式参赛。`;
  }
  if (contestStatus.value === 2) {
    return "比赛已结束，当前可查看题目列表和最终排行榜。";
  }
  return "比赛信息加载中。";
});

const canViewContent = computed(() => {
  return (
    contestStatus.value === 2 ||
    (contestStatus.value === 1 && hasSignedUp.value)
  );
});

const showSignupButton = computed(
  () => contestStatus.value === 0 && !hasSignedUp.value
);
const showCancelButton = computed(
  () => contestStatus.value === 0 && hasSignedUp.value
);
const showEnterButton = computed(
  () =>
    contestStatus.value === 1 &&
    hasSignedUp.value &&
    questionList.value.length > 0
);

const questionColumns = [
  { title: "状态", slotName: "status", width: 100 },
  { title: "题目", slotName: "title" },
  { title: "分值", dataIndex: "score", width: 100 },
  { title: "操作", slotName: "action", width: 100 },
];

const rankColumns = [
  { title: "排名", slotName: "rank", width: 80 },
  { title: "用户", slotName: "user" },
  { title: "解决数", dataIndex: "solvedCount", width: 100 },
  { title: "总分", dataIndex: "totalScore", width: 100 },
  { title: "罚时", slotName: "penalty", width: 140 },
];

const parseTimestamp = (value: any): number => {
  if (value === null || value === undefined || value === "") {
    return Number.NaN;
  }
  if (typeof value === "number") {
    return value;
  }

  const raw = String(value).trim();
  const normalized = raw.includes("T") ? raw : raw.replace(" ", "T");

  const strictIso = moment(normalized, moment.ISO_8601, true);
  if (strictIso.isValid()) {
    return strictIso.valueOf();
  }

  const common = moment(raw, [
    "YYYY-MM-DD HH:mm:ss",
    "YYYY-MM-DD HH:mm",
    "YYYY/MM/DD HH:mm:ss",
    "YYYY/MM/DD HH:mm",
  ]);
  if (common.isValid()) {
    return common.valueOf();
  }

  return new Date(normalized).getTime();
};

const formatTime = (value: any) => {
  const timestamp = parseTimestamp(value);
  if (Number.isNaN(timestamp)) {
    return "-";
  }
  return moment(timestamp).format("YYYY-MM-DD HH:mm:ss");
};

const formatPenalty = (penalty?: number) => {
  if (!penalty) {
    return "0";
  }
  const minutes = Math.floor(penalty / 60);
  const seconds = penalty % 60;
  return `${minutes} 分 ${seconds} 秒`;
};

const loadContest = async () => {
  const res = await ContestControllerService.getContestVoByIdUsingGet(
    props.id as any
  );
  if (res.code !== 0) {
    throw new Error(res.message);
  }
  contest.value = res.data as any;
};

const loadSignupState = async () => {
  const signupRes =
    await ContestSignupControllerService.listContestSignupByPageUsingPost({
      contestId: props.id as any,
      pageSize: 200,
    } as any);
  if (signupRes.code !== 0) {
    throw new Error(signupRes.message);
  }
  const records = (signupRes.data?.records || []) as ContestSignupRecord[];
  const activeRecords = records.filter((item) => item.status === 1);
  signupCount.value = activeRecords.length;
  if (!currentUserId.value) {
    hasSignedUp.value = false;
    signupRecordId.value = undefined;
    return;
  }
  const mine = activeRecords.find(
    (item) => Number(item.userId) === currentUserId.value
  );
  hasSignedUp.value = Boolean(mine);
  signupRecordId.value = mine?.id;
};

const loadQuestions = async () => {
  if (!contest.value || !canViewContent.value) {
    questionList.value = [];
    return;
  }
  const res =
    await ContestQuestionControllerService.listContestQuestionByPageUsingPost({
      contestId: props.id as any,
      pageSize: 100,
    } as any);
  if (res.code !== 0) {
    throw new Error(res.message);
  }
  questionList.value = res.data?.records || [];
};

const loadRank = async () => {
  if (!contest.value || !canViewContent.value) {
    rankList.value = [];
    return;
  }
  const res = await ContestRankControllerService.listContestRankByPageUsingPost(
    {
      contestId: props.id as any,
      pageSize: 100,
    } as any
  );
  if (res.code !== 0) {
    throw new Error(res.message);
  }
  rankList.value = res.data?.records || [];
};

const loadUserQuestionStatus = async () => {
  if (!currentUserId.value || !canViewContent.value) {
    userQuestionStatus.value = {};
    return;
  }
  const res = await QuestionControllerService.listQuestionSubmitByPageUsingPost(
    {
      contestId: props.id as any,
      userId: currentUserId.value,
      pageSize: 1000,
    } as any
  );
  if (res.code !== 0) {
    return;
  }
  const statusMap: Record<number, number> = {};
  (res.data?.records || []).forEach((submit: any) => {
    const questionId = Number(submit.questionId);
    if (!questionId || statusMap[questionId] === 2) {
      return;
    }
    if (submit.status === 2 && submit.judgeInfo?.message === "Accepted") {
      statusMap[questionId] = 2;
      return;
    }
    if (
      submit.status === 3 ||
      (submit.status === 2 && submit.judgeInfo?.message !== "Accepted")
    ) {
      statusMap[questionId] = 3;
    }
  });
  userQuestionStatus.value = statusMap;
};

const loadVisibleData = async () => {
  await Promise.all([loadQuestions(), loadRank()]);
  await loadUserQuestionStatus();
};

const loadPage = async () => {
  loading.value = true;
  try {
    await loadContest();
    await loadSignupState();
    await loadVisibleData();
  } catch (error: any) {
    message.error(error?.message || "比赛信息加载失败");
  } finally {
    loading.value = false;
  }
};

const handleSignup = async () => {
  const res = await ContestSignupControllerService.signupUsingPost({
    contestId: props.id as any,
  });
  if (res.code !== 0) {
    message.error(res.message || "报名失败");
    return;
  }
  message.success("报名成功");
  await loadSignupState();
  await loadVisibleData();
};

const handleCancelSignup = async () => {
  if (!signupRecordId.value) {
    message.error("未找到报名记录");
    return;
  }
  const res = await ContestSignupControllerService.cancelContestSignupUsingPost(
    {
      id: signupRecordId.value,
    }
  );
  if (res.code !== 0) {
    message.error(res.message || "取消报名失败");
    return;
  }
  message.success("已取消报名");
  await loadSignupState();
  await loadVisibleData();
};

const toDoQuestion = (questionId: number) => {
  router.push({
    path: `/contest/${props.id}/solve/${questionId}`,
  });
};

const enterContest = () => {
  if (!questionList.value.length) {
    message.warning("比赛题目尚未加载完成");
    return;
  }
  toDoQuestion(questionList.value[0].questionId);
};

watch(
  () => contestStatus.value,
  async (nextStatus, prevStatus) => {
    if (nextStatus !== prevStatus) {
      await loadSignupState();
      await loadVisibleData();
    }
  }
);

onMounted(() => {
  loadPage();
  timer = window.setInterval(() => {
    currentTime.value = Date.now();
  }, 1000);
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
#contestDetailView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 12px 24px;
}

.text-stat-card {
  height: 100%;
}

.text-stat-title {
  color: #86909c;
  font-size: 14px;
  margin-bottom: 8px;
}

.text-stat-value {
  color: #1d2129;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.3;
  word-break: break-all;
}
</style>
