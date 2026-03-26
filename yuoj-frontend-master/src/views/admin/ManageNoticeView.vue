<template>
  <div class="manage-notice">
    <a-card title="公告管理">
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 20px">
        <a-form-item field="title" label="标题">
          <a-input v-model="searchParams.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">搜索</a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" status="success" @click="doAdd"
            >发布公告</a-button
          >
        </a-form-item>
      </a-form>
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
          <a-tag v-if="record.status === 0" color="gray">草稿</a-tag>
          <a-tag v-else-if="record.status === 1" color="green">已发布</a-tag>
          <a-tag v-else color="red">已下架</a-tag>
        </template>
        <template #action="{ record }">
          <a-button type="text" size="small" @click="doUpdate(record)"
            >修改</a-button
          >
          <a-popconfirm content="确定要删除吗？" @ok="handleDelete(record)">
            <a-button type="text" status="danger" size="small">删除</a-button>
          </a-popconfirm>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="visible"
      :title="form.id ? '修改公告' : '发布公告'"
      @ok="handleSubmit"
    >
      <a-form :model="form">
        <a-form-item field="title" label="标题">
          <a-input v-model="form.title" />
        </a-form-item>
        <a-form-item field="content" label="内容">
          <a-textarea v-model="form.content" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status">
            <a-option :value="0">草稿</a-option>
            <a-option :value="1">已发布</a-option>
            <a-option :value="2">已下架</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { NoticeControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 10,
  current: 1,
  title: "",
});
const visible = ref(false);
const form = ref({
  id: undefined,
  title: "",
  content: "",
  status: 0,
});

const columns = [
  { title: "ID", dataIndex: "id" },
  { title: "标题", dataIndex: "title" },
  { title: "状态", slotName: "status" },
  { title: "发布时间", dataIndex: "publishTime" },
  { title: "操作", slotName: "action" },
];

const loadData = async () => {
  const res = await NoticeControllerService.listPageUsingPost(
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

const doAdd = () => {
  form.value = { id: undefined, title: "", content: "", status: 0 };
  visible.value = true;
};

const doUpdate = (record: any) => {
  form.value = { ...record };
  visible.value = true;
};

const handleSubmit = async () => {
  let res;
  if (form.value.id) {
    res = await NoticeControllerService.updateUsingPost(form.value);
  } else {
    res = await NoticeControllerService.addUsingPost(form.value);
  }
  if (res.code === 0) {
    message.success("操作成功");
    visible.value = false;
    loadData();
  } else {
    message.error("操作失败: " + res.message);
  }
};

const handleDelete = async (record: any) => {
  const res = await NoticeControllerService.deleteUsingPost({ id: record.id });
  if (res.code === 0) {
    message.success("删除成功");
    loadData();
  } else {
    message.error("删除失败: " + res.message);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.manage-notice {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
