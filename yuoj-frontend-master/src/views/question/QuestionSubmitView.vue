<template>
  <div id="questionSubmitView">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="questionId" label="题号" style="min-width: 240px">
        <a-input v-model="searchParams.questionId" placeholder="请输入" />
      </a-form-item>
      <a-form-item field="language" label="编程语言" style="min-width: 240px">
        <a-select
          v-model="searchParams.language"
          :style="{ width: '320px' }"
          placeholder="选择编程语言"
        >
          <a-option>java</a-option>
          <a-option>cpp</a-option>
          <a-option>go</a-option>
          <a-option>html</a-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #judgeInfo="{ record }">
        <a-space>
          <a-tag
            v-if="record.judgeInfo?.message"
            :color="
              isAcceptedJudgeMessage(record.judgeInfo.message) ? 'green' : 'red'
            "
            >{{ localizeJudgeMessage(record.judgeInfo.message) }}</a-tag
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
        <!-- 判题状态标签 -->
        <a-tag v-if="record.status === 0" color="gray">等待中</a-tag>
        <a-tag v-else-if="record.status === 1" color="blue">判题中</a-tag>
        <a-tag v-else-if="record.status === 2" color="green">成功</a-tag>
        <a-tag v-else-if="record.status === 3" color="red">失败</a-tag>
        <a-tag v-else color="gray">未知</a-tag>
      </template>
      <template #question="{ record }">
        {{ record.questionVO?.title }}
      </template>
      <template #submitter="{ record }">
        {{ record.userVO?.userName }}
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watchEffect } from "vue";
import {
  QuestionControllerService,
  QuestionSubmitQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { useRoute } from "vue-router";
import {
  isAcceptedJudgeMessage,
  localizeJudgeMessage,
} from "@/utils/judgeMessage";

const route = useRoute();
const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const refreshTimer = ref<number | undefined>(undefined);
const searchParams = ref<QuestionSubmitQueryRequest>({
  questionId: undefined,
  language: undefined,
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionSubmitByPageUsingPost(
    {
      ...searchParams.value,
      sortField: "create_time",
      sortOrder: "descend",
    }
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变化，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  // 初始化搜索参数
  if (route.query.questionId) {
    searchParams.value.questionId = Number(route.query.questionId);
  }
  loadData();
  if (route.query.refresh === "1") {
    refreshTimer.value = window.setTimeout(() => {
      loadData();
    }, 5000);
  }
});

onBeforeUnmount(() => {
  if (refreshTimer.value) {
    window.clearTimeout(refreshTimer.value);
    refreshTimer.value = undefined;
  }
});

const columns = [
  {
    title: "提交号",
    dataIndex: "id",
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
    title: "题目",
    slotName: "question",
  },
  {
    title: "提交者",
    slotName: "submitter",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};
</script>

<style scoped>
#questionSubmitView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
