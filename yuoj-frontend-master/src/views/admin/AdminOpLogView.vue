<template>
  <div class="admin-op-log">
    <a-card title="管理员操作日志">
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
        <template #opData="{ record }">
          <a-popover title="操作数据" trigger="click">
            <a-button type="text" size="small">查看详情</a-button>
            <template #content>
              <div
                style="
                  max-width: 400px;
                  max-height: 300px;
                  overflow: auto;
                  white-space: pre-wrap;
                  font-family: monospace;
                "
              >
                {{ record.opData }}
              </div>
            </template>
          </a-popover>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { AdminOpLogControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 20,
  current: 1,
});

const columns = [
  { title: "ID", dataIndex: "id" },
  { title: "操作人ID", dataIndex: "userId" },
  { title: "类型", dataIndex: "opType" },
  { title: "描述", dataIndex: "opDesc" },
  { title: "数据", slotName: "opData" },
  { title: "时间", dataIndex: "createTime" },
];

const loadData = async () => {
  const res = await AdminOpLogControllerService.listAdminOpLogByPageUsingPost(
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

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.admin-op-log {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
