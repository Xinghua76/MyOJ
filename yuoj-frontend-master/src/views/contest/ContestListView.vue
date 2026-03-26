<template>
  <div id="contestListView">
    <a-card title="比赛列表">
      <template #extra>
        <a-radio-group
          v-model="searchParams.status"
          type="button"
          @change="loadData"
        >
          <a-radio :value="undefined">全部</a-radio>
          <a-radio :value="0">未开始</a-radio>
          <a-radio :value="1">进行中</a-radio>
          <a-radio :value="2">已结束</a-radio>
        </a-radio-group>
      </template>
      <a-table
        :columns="columns"
        :data="dataList"
        :pagination="{
          showTotal: true,
          pageSize: searchParams.pageSize,
          current: searchParams.current,
          total: total,
        }"
        @page-change="onPageChange"
      >
        <template #status="{ record }">
          <a-tag v-if="record.status === 0" color="blue">未开始</a-tag>
          <a-tag v-else-if="record.status === 1" color="green">进行中</a-tag>
          <a-tag v-else color="gray">已结束</a-tag>
        </template>
        <template #startTime="{ record }">
          {{ moment(record.startTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
        <template #endTime="{ record }">
          {{ moment(record.endTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
        <template #action="{ record }">
          <a-button type="primary" @click="toDetail(record.id)"
            >查看详情</a-button
          >
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { ContestControllerService, ContestVO } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

const router = useRouter();
const dataList = ref<ContestVO[]>([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 10,
  current: 1,
  status: undefined as number | undefined,
});

const columns = [
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "状态",
    slotName: "status",
  },
  {
    title: "开始时间",
    slotName: "startTime",
  },
  {
    title: "结束时间",
    slotName: "endTime",
  },
  {
    title: "创建人",
    dataIndex: "creatorVO.userName",
  },
  {
    title: "操作",
    slotName: "action",
  },
];

const loadData = async () => {
  const res = await ContestControllerService.listContestVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败: " + res.message);
  }
};

const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadData();
};

const toDetail = (id: number) => {
  router.push(`/contest/detail/${id}`);
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
#contestListView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
