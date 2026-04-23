<template>
  <div id="questionsView">
    <a-divider size="0" />

    <a-form :model="searchParams" layout="inline">
      <a-form-item field="title" label="关键词" style="min-width: 240px">
        <a-input v-model="searchParams.keyword" placeholder="请输入关键词" />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="min-width: 240px">
        <a-select
          v-model="searchParams.tags"
          :options="TAG_OPTIONS"
          placeholder="请选择标签"
          multiple
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">提交</a-button>
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
      <template #difficulty="{ record }">
        <a-tag v-if="record.difficulty === 1" color="green"> 简单 </a-tag>
        <a-tag v-else-if="record.difficulty === 2" color="orange"> 中等 </a-tag>
        <a-tag v-else-if="record.difficulty === 3" color="red"> 困难 </a-tag>
      </template>
      <template #tags="{ record }">
        <a-space wrap>
          <a-tag v-for="(tag, index) of record.tags" :key="index" color="green">
            {{ tag }}
          </a-tag>
        </a-space>
      </template>
      <template #acceptedRate="{ record }">
        {{
          `${
            record.submitNum
              ? ((record.acceptedNum / record.submitNum) * 100).toFixed(2)
              : "0.00"
          }% (${record.acceptedNum}/${record.submitNum})`
        }}
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD") }}
      </template>
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="toQuestionPage(record)">
            做题
          </a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Page_Question_,
  Question,
  QuestionControllerService,
  QuestionQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import * as querystring from "querystring";
import { useRouter } from "vue-router";
import moment from "moment";

const TAG_OPTIONS = [
  { label: "数组", value: "数组" },
  { label: "字符串", value: "字符串" },
  { label: "哈希表", value: "哈希表" },
  { label: "栈", value: "栈" },
  { label: "队列", value: "队列" },
  { label: "链表", value: "链表" },
  { label: "二叉树", value: "二叉树" },
  { label: "图", value: "图" },
  { label: "DFS", value: "DFS" },
  { label: "BFS", value: "BFS" },
  { label: "动态规划", value: "动态规划" },
  { label: "贪心", value: "贪心" },
  { label: "双指针", value: "双指针" },
  { label: "排序", value: "排序" },
  { label: "数学", value: "数学" },
];

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionQueryRequest>({
  title: "",
  keyword: "",
  tags: [],
  pageSize: 8,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

watchEffect(() => {
  loadData();
});

onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "题号",
    dataIndex: "id",
  },
  {
    title: "题目名称",
    dataIndex: "title",
  },
  {
    title: "难度",
    slotName: "difficulty",
    width: 80,
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "通过率",
    slotName: "acceptedRate",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();

const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

const doSubmit = () => {
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};
</script>

<style scoped>
#questionsView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
