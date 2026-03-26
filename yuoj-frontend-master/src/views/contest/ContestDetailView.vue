<template>
  <div id="contestDetailView">
    <a-page-header
      :title="contest?.title"
      @back="router.back()"
      style="margin-bottom: 16px; padding: 0"
    >
    </a-page-header>
    <a-card v-if="contest" :bordered="false">
      <template #extra>
        <a-tag v-if="contestStatus === 0" color="blue">未开始</a-tag>
        <a-tag v-else-if="contestStatus === 1" color="green">进行中</a-tag>
        <a-tag v-else color="gray">已结束</a-tag>
      </template>

      <!-- 倒计时/状态提示区域 -->
      <a-alert
        v-if="contestStatus === 0"
        type="info"
        style="margin-bottom: 20px"
      >
        距离比赛开始还有：{{ countdownText }}
      </a-alert>
      <a-alert
        v-else-if="contestStatus === 1"
        type="success"
        style="margin-bottom: 20px"
      >
        比赛进行中！距离结束还有：{{ countdownText }}
      </a-alert>
      <a-alert v-else type="warning" style="margin-bottom: 20px">
        比赛已结束
      </a-alert>

      <a-descriptions :column="2">
        <a-descriptions-item label="开始时间">
          {{ moment(contest.startTime).format("YYYY-MM-DD HH:mm:ss") }}
        </a-descriptions-item>
        <a-descriptions-item label="结束时间">
          {{ moment(contest.endTime).format("YYYY-MM-DD HH:mm:ss") }}
        </a-descriptions-item>
        <a-descriptions-item label="创建人">
          {{ contest.creatorVO?.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="描述">
          {{ contest.description }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div
        v-if="hasSignedUp"
        style="
          text-align: center;
          margin: 20px 0;
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 10px;
        "
      >
        <span style="color: green; font-size: 16px">已报名</span>
        <a-popconfirm
          content="确定要取消报名吗？"
          @ok="handleCancelSignup"
          v-if="contestStatus === 0"
        >
          <a-button type="outline" status="danger" size="small"
            >取消报名</a-button
          >
        </a-popconfirm>
      </div>
      <div
        v-else-if="!hasSignedUp && contestStatus === 0"
        style="text-align: center; margin: 20px 0"
      >
        <a-button type="primary" size="large" @click="handleSignup"
          >报名参赛</a-button
        >
      </div>

      <a-alert
        v-else-if="!hasSignedUp && contestStatus !== 0"
        type="warning"
        style="margin-bottom: 20px"
      >
        比赛已开始或已结束，无法报名。
      </a-alert>

      <!-- 题目列表与排行榜 -->
      <a-tabs
        v-if="(hasSignedUp && contestStatus !== 0) || contestStatus === 2"
        default-active-key="1"
      >
        <a-tab-pane key="1" title="题目列表">
          <a-table
            :columns="questionColumns"
            :data="questionList"
            :pagination="false"
          >
            <template #status="{ record }">
              <span v-if="userQuestionStatus[record.questionId] === 2">
                <icon-check-circle-fill style="color: green; font-size: 20px" />
              </span>
              <span v-else-if="userQuestionStatus[record.questionId] === 3">
                <icon-close-circle-fill style="color: red; font-size: 20px" />
              </span>
              <span v-else>
                <!-- 未开始 -->
              </span>
            </template>
            <template #title="{ record }">
              <a-link @click="toDoQuestion(record.questionId)"
                >{{ record.questionId }} -
                {{ record.questionVO?.title || "题目" }}</a-link
              >
            </template>
            <template #score="{ record }">
              {{ record.score }}
            </template>
            <template #action="{ record }">
              <a-button
                type="primary"
                size="small"
                @click="toDoQuestion(record.questionId)"
                >做题</a-button
              >
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="2" title="排行榜">
          <a-table :columns="rankColumns" :data="rankList" :pagination="false">
            <template #rank="{ rowIndex }">
              {{ rowIndex + 1 }}
            </template>
            <template #user="{ record }">
              {{ record.userVO?.userName }}
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, onMounted, onUnmounted, ref, watch } from "vue";
import {
  ContestControllerService,
  ContestQuestionControllerService,
  ContestSignupControllerService,
  ContestRankControllerService,
  ContestVO,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import {
  IconCheckCircleFill,
  IconCloseCircleFill,
} from "@arco-design/web-vue/es/icon";

const props = defineProps<{
  id: string;
}>();

const router = useRouter();
const store = useStore();
const contest = ref<ContestVO>();
const hasSignedUp = ref(false);
const questionList = ref([]);
const rankList = ref([]);
const currentTime = ref(Date.now());
// 存储每个题目的用户状态: questionId -> status (2: Accepted, 3: Failed)
const userQuestionStatus = ref<Record<number, number>>({});
let timer: any = null;

// 计算比赛状态：0-未开始，1-进行中，2-已结束
const contestStatus = computed(() => {
  if (!contest.value) return -1;
  const now = currentTime.value;
  const start = new Date(contest.value.startTime).getTime();
  const end = new Date(contest.value.endTime).getTime();
  if (now < start) return 0;
  if (now > end) return 2;
  return 1;
});

// 计算倒计时文本
const countdownText = computed(() => {
  if (!contest.value) return "";
  const now = currentTime.value;
  const start = new Date(contest.value.startTime).getTime();
  const end = new Date(contest.value.endTime).getTime();

  let targetTime = 0;
  if (now < start) {
    targetTime = start;
  } else if (now < end) {
    targetTime = end;
  } else {
    return "";
  }

  const diff = targetTime - now;
  const duration = moment.duration(diff);
  const hours = Math.floor(duration.asHours());
  const minutes = duration.minutes();
  const seconds = duration.seconds();
  return `${hours}小时 ${minutes}分 ${seconds}秒`;
});

const questionColumns = [
  {
    title: "状态",
    slotName: "status",
    width: 80,
  },
  {
    title: "题目",
    slotName: "title",
  },
  {
    title: "分值",
    slotName: "score",
  },
  {
    title: "操作",
    slotName: "action",
  },
];

const rankColumns = [
  {
    title: "排名",
    slotName: "rank",
  },
  {
    title: "用户",
    slotName: "user",
  },
  {
    title: "解决数",
    dataIndex: "solvedCount",
  },
  {
    title: "总分",
    dataIndex: "totalScore",
  },
  {
    title: "罚时",
    dataIndex: "penalty",
  },
];

watch(
  () => contestStatus.value,
  (newStatus, oldStatus) => {
    if (newStatus === 1 && oldStatus === 0) {
      if (hasSignedUp.value) {
        message.success("比赛已开始，正在加载题目...");
        loadQuestions();
        loadRank();
      }
    } else if (newStatus === 2 && oldStatus === 1) {
      message.info("比赛已结束");
    }
  }
);

const loadData = async () => {
  const res = await ContestControllerService.getContestVoByIdUsingGet(props.id);
  if (res.code === 0) {
    contest.value = res.data;
    await checkSignup();
    // 如果已报名且比赛已开始，或者比赛已结束，加载题目和排行榜
    if (
      (hasSignedUp.value && contestStatus.value !== 0) ||
      contestStatus.value === 2
    ) {
      loadQuestions();
      loadRank();
    }
  } else {
    message.error("加载失败: " + res.message);
  }
};

const checkSignup = async () => {
  const loginUser = store.state.user.loginUser;
  if (!loginUser.id) return;

  try {
    const signupRes =
      await ContestSignupControllerService.listContestSignupByPageUsingPost({
        contestId: props.id,
        userId: loginUser.id,
      });
    if (
      signupRes.code === 0 &&
      signupRes.data.records.length > 0 &&
      signupRes.data.records[0].status === 1
    ) {
      hasSignedUp.value = true;
    }
  } catch (e) {
    console.error("Check signup failed", e);
  }
};

const loadQuestions = async () => {
  const res =
    await ContestQuestionControllerService.listContestQuestionByPageUsingPost({
      contestId: props.id,
      pageSize: 100,
    });
  if (res.code === 0) {
    questionList.value = res.data.records;
    // 加载完题目后，加载用户的提交状态
    loadUserQuestionStatus();
  }
};

const loadUserQuestionStatus = async () => {
  const loginUser = store.state.user.loginUser;
  if (!loginUser.id) return;

  try {
    const res =
      await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        contestId: props.id,
        userId: loginUser.id,
        pageSize: 1000, // 获取足够多的记录
      });
    if (res.code === 0 && res.data.records) {
      const statusMap: Record<number, number> = {};
      res.data.records.forEach((submit: any) => {
        const qId = submit.questionId;
        // 如果已经 Accepted，则保持 Accepted
        if (statusMap[qId] === 2) return;

        // 检查状态
        if (submit.status === 2 && submit.judgeInfo?.message === "Accepted") {
          statusMap[qId] = 2; // Accepted
        } else if (
          submit.status === 3 ||
          (submit.status === 2 && submit.judgeInfo?.message !== "Accepted")
        ) {
          // 如果还没 AC，记录为失败（尝试过）
          if (statusMap[qId] !== 2) {
            statusMap[qId] = 3;
          }
        }
      });
      userQuestionStatus.value = statusMap;
    }
  } catch (e) {
    console.error("加载做题状态失败", e);
  }
};

const handleSignup = async () => {
  const res = await ContestSignupControllerService.signupUsingPost({
    contestId: props.id,
  });
  if (res.code === 0) {
    message.success("报名成功");
    hasSignedUp.value = true;
    // 如果比赛已经开始或已结束，才加载题目和排行榜
    if (contestStatus.value !== 0) {
      loadQuestions();
      loadRank();
    }
  } else {
    message.error("报名失败: " + res.message);
  }
};

const handleCancelSignup = async () => {
  console.log("Cancelling signup...");
  // 查找报名记录的ID
  try {
    const loginUser = store.state.user.loginUser;
    const listRes =
      await ContestSignupControllerService.listContestSignupByPageUsingPost({
        contestId: props.id,
        userId: loginUser.id,
      });

    if (
      listRes.code === 0 &&
      listRes.data.records &&
      listRes.data.records.length > 0
    ) {
      const signupId = listRes.data.records[0].id;
      const res =
        await ContestSignupControllerService.cancelContestSignupUsingPost({
          id: signupId,
        });
      if (res.code === 0) {
        message.success("已取消报名");
        hasSignedUp.value = false;
        // 清空题目和排行榜数据
        questionList.value = [];
        rankList.value = [];
      } else {
        message.error("取消报名失败: " + res.message);
      }
    } else {
      message.error("未找到报名记录");
    }
  } catch (e) {
    message.error("操作失败: " + e.message);
  }
};

const loadRank = async () => {
  const res = await ContestRankControllerService.listContestRankByPageUsingPost(
    {
      contestId: props.id,
      pageSize: 100,
    }
  );
  if (res.code === 0) {
    rankList.value = res.data.records;
  } else {
    message.error("加载排行榜失败: " + res.message);
  }
};

const toDoQuestion = (questionId: number) => {
  router.push({
    path: `/contest/${props.id}/solve/${questionId}`,
  });
};

onMounted(() => {
  loadData();
  // 启动定时器，每秒更新当前时间
  timer = setInterval(() => {
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
}
</style>
