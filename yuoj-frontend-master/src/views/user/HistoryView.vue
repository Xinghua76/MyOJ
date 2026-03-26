<template>
  <div class="history-view">
    <a-card :bordered="false" title="历史记录">
      <template #extra>
        <a-button type="text" @click="router.back()">返回</a-button>
      </template>
      <a-table
        :columns="columns"
        :data="submitDataList"
        :pagination="{
          showTotal: true,
          pageSize: submitSearchParams.pageSize,
          current: submitSearchParams.current,
          total: submitTotal,
        }"
        @page-change="onSubmitPageChange"
      >
        <template #judgeInfo="{ record }">
          <a-space>
            <a-tag
              v-if="record.judgeInfo?.message"
              :color="record.judgeInfo.message === 'Accepted' ? 'green' : 'red'"
              >{{ record.judgeInfo.message }}</a-tag
            >
            <a-tag v-if="record.judgeInfo?.time" color="cyan"
              >{{ record.judgeInfo.time }} ms</a-tag
            >
            <a-tag v-if="record.judgeInfo?.memory" color="orange"
              >{{ record.judgeInfo.memory }} KB</a-tag
            >
            <span v-if="!record.judgeInfo?.time && !record.judgeInfo?.memory">
              -
            </span>
          </a-space>
        </template>
        <template #status="{ record }">
          <a-tag v-if="record.status === 0" color="gray">等待中</a-tag>
          <a-tag v-else-if="record.status === 1" color="blue">判题中</a-tag>
          <a-tag v-else-if="record.status === 2" color="green">成功</a-tag>
          <a-tag v-else-if="record.status === 3" color="red">失败</a-tag>
          <a-tag v-else color="gray">未知</a-tag>
        </template>
        <template #question="{ record }">
          <a-link :href="`/view/question/${record.questionId}`">
            {{ record.questionVO?.title }}
          </a-link>
        </template>
        <template #submitTime="{ record }">
          {{ moment(record.submitTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import {
  QuestionControllerService,
  QuestionSubmitQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const store = useStore();
const router = useRouter();
const loginUser = computed(() => store.state.user.loginUser);

// 提交记录相关
const submitDataList = ref([]);
const submitTotal = ref(0);
const submitSearchParams = ref<QuestionSubmitQueryRequest>({
  pageSize: 10,
  current: 1,
});

const columns = [
  {
    title: "题目",
    slotName: "question",
  },
  {
    title: "编程语言",
    dataIndex: "language",
  },
  {
    title: "判题信息",
    slotName: "judgeInfo",
  },
  {
    title: "判题状态",
    slotName: "status",
  },
  {
    title: "提交时间",
    slotName: "submitTime",
  },
];

const loadSubmitData = async () => {
  if (!loginUser.value?.id) {
    return;
  }
  const res = await QuestionControllerService.listQuestionSubmitByPageUsingPost(
    {
      ...submitSearchParams.value,
      userId: loginUser.value.id,
      sortField: "create_time",
      sortOrder: "descend",
    }
  );
  if (res.code === 0) {
    submitDataList.value = res.data.records;
    submitTotal.value = res.data.total;
  } else {
    message.error("加载提交记录失败，" + res.message);
  }
};

const onSubmitPageChange = (page: number) => {
  submitSearchParams.value = {
    ...submitSearchParams.value,
    current: page,
  };
  loadSubmitData();
};

onMounted(() => {
  if (loginUser.value) {
    loadSubmitData();
  }
});
</script>

<style scoped>
.history-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
