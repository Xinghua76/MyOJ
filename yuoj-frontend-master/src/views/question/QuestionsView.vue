<template>
  <div id="questionsView">
    <!-- 公告栏 -->
    <a-card class="notice-card" v-if="noticeList && noticeList.length > 0">
      <template #title>
        <div style="display: flex; align-items: center">
          <icon-sound-fill style="margin-right: 8px; color: #165dff" />
          <span>系统公告</span>
        </div>
      </template>
      <a-carousel
        auto-play
        indicator-type="dot"
        show-arrow="hover"
        style="height: 200px; border-radius: 4px; overflow: hidden"
        animation-name="fade"
      >
        <a-carousel-item v-for="notice in noticeList" :key="notice.id">
          <div class="notice-content">
            <h3 class="notice-title">{{ notice.title }}</h3>
            <p class="notice-text">{{ notice.content }}</p>
            <div class="notice-time">
              发布于：{{
                moment(notice.publishTime).format("YYYY-MM-DD HH:mm")
              }}
            </div>
          </div>
        </a-carousel-item>
      </a-carousel>
    </a-card>
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
  NoticeControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import * as querystring from "querystring";
import { useRouter } from "vue-router";
import moment from "moment";
import { IconSoundFill } from "@arco-design/web-vue/es/icon";

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
const noticeList = ref([]);
const searchParams = ref<QuestionQueryRequest>({
  title: "",
  keyword: "",
  tags: [],
  pageSize: 8,
  current: 1,
});

const loadData = async () => {
  // 加载公告（只获取已发布的，这里复用 listPage 接口，status=1 为已发布）
  // 注意：后端 NoticeController.listPage 可能没有自动过滤 status，需要在请求参数中指定或者在后端处理
  // 这里假设前端传参过滤，或者后端默认不过滤，需要手动筛选
  const noticeRes = await NoticeControllerService.listPageUsingPost({
    pageSize: 5,
    current: 1,
    // 假设后端支持 status 查询，如果不支持可能需要后端改动
    // 根据之前的代码，NoticeQueryRequest 应该包含 status
  });
  if (noticeRes.code === 0) {
    // 简单过滤一下，确保只显示已发布的
    noticeList.value = noticeRes.data.records.filter(
      (item: any) => item.status === 1
    );
  }

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
.notice-card {
  margin-bottom: 20px;
  background: linear-gradient(to right, #e6f7ff, #ffffff);
}
.notice-content {
  padding: 20px 40px;
  text-align: center;
}
.notice-title {
  margin-bottom: 10px;
  color: #165dff;
  font-size: 18px;
}
.notice-text {
  margin-bottom: 10px;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
}
.notice-time {
  color: #999;
  font-size: 12px;
}
</style>
