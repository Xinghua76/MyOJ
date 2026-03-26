<template>
  <div class="manage-contest">
    <a-card title="竞赛管理">
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 20px">
        <a-form-item field="title" label="标题">
          <a-input v-model="searchParams.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">搜索</a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" status="success" @click="doAdd"
            >创建竞赛</a-button
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
          <a-tag v-if="getContestStatus(record) === 0" color="blue"
            >未开始</a-tag
          >
          <a-tag v-else-if="getContestStatus(record) === 1" color="green"
            >进行中</a-tag
          >
          <a-tag v-else color="gray">已结束</a-tag>
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
      :title="form.id ? '修改竞赛' : '创建竞赛'"
      width="1000px"
      @ok="handleSubmit"
    >
      <a-form :model="form">
        <a-form-item field="title" label="标题">
          <a-input v-model="form.title" />
        </a-form-item>
        <a-form-item field="description" label="描述">
          <a-textarea v-model="form.description" />
        </a-form-item>
        <a-form-item field="startTime" label="开始时间">
          <a-date-picker show-time v-model="form.startTime" />
        </a-form-item>
        <a-form-item field="endTime" label="结束时间">
          <a-date-picker show-time v-model="form.endTime" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status">
            <a-option :value="0">未开始</a-option>
            <a-option :value="1">进行中</a-option>
            <a-option :value="2">已结束</a-option>
          </a-select>
        </a-form-item>
      </a-form>

      <a-divider />

      <div class="question-manage">
        <h3>题目选择</h3>
        <a-row :gutter="24">
          <a-col :span="12">
            <h4>可选题目</h4>
            <a-form :model="innerSearchParams" layout="inline">
              <a-form-item field="title" label="搜索">
                <a-input
                  v-model="innerSearchParams.title"
                  placeholder="输入标题搜索"
                  @press-enter="loadQuestions"
                />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" size="small" @click="loadQuestions"
                  >搜索</a-button
                >
              </a-form-item>
            </a-form>
            <a-table
              :columns="questionColumns"
              :data="questionList"
              :pagination="{
                showTotal: true,
                pageSize: innerSearchParams.pageSize,
                current: innerSearchParams.current,
                total: totalQuestions,
                simple: true,
              }"
              @page-change="onInnerPageChange"
              size="small"
            >
              <template #action="{ record }">
                <a-button
                  type="text"
                  size="small"
                  status="success"
                  @click="addQuestionToSelection(record)"
                  :disabled="isQuestionSelected(record.id)"
                >
                  添加
                </a-button>
              </template>
            </a-table>
          </a-col>
          <a-col :span="12">
            <h4>已选题目</h4>
            <a-table
              :columns="selectedQuestionColumns"
              :data="selectedQuestionList"
              :pagination="false"
              size="small"
            >
              <template #action="{ record }">
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="removeQuestionFromSelection(record)"
                >
                  移除
                </a-button>
              </template>
            </a-table>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  ContestControllerService,
  ContestQueryRequest,
  QuestionControllerService,
  QuestionQueryRequest,
  ContestQuestionControllerService,
  ContestQuestionVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<ContestQueryRequest>({
  pageSize: 10,
  current: 1,
});
const visible = ref(false);
const form = ref({
  id: undefined,
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  status: 0,
});

const columns = [
  { title: "ID", dataIndex: "id" },
  { title: "标题", dataIndex: "title" },
  { title: "状态", slotName: "status" },
  { title: "开始时间", dataIndex: "startTime" },
  { title: "结束时间", dataIndex: "endTime" },
  { title: "操作", slotName: "action" },
];

// --- 题目选择相关逻辑 ---
const innerSearchParams = ref<QuestionQueryRequest>({
  pageSize: 10,
  current: 1,
  title: "",
});
const questionList = ref([]);
const totalQuestions = ref(0);
const selectedQuestionList = ref<any[]>([]); // 存储已选的题目信息 { id, title }
const originalContestQuestions = ref<ContestQuestionVO[]>([]); // 存储编辑时的原始题目关联

const questionColumns = [
  { title: "标题", dataIndex: "title" },
  { title: "操作", slotName: "action", width: 80 },
];

const selectedQuestionColumns = [
  { title: "标题", dataIndex: "title" },
  { title: "操作", slotName: "action", width: 80 },
];

const loadQuestions = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    innerSearchParams.value
  );
  if (res.code === 0) {
    questionList.value = res.data.records;
    totalQuestions.value = res.data.total;
  } else {
    message.error("加载题目失败: " + res.message);
  }
};

const onInnerPageChange = (page: number) => {
  innerSearchParams.value.current = page;
  loadQuestions();
};

const isQuestionSelected = (questionId: number) => {
  return selectedQuestionList.value.some((q) => q.id === questionId);
};

const addQuestionToSelection = (question: any) => {
  if (!isQuestionSelected(question.id)) {
    selectedQuestionList.value.push({
      id: question.id,
      title: question.title,
    });
  }
};

const removeQuestionFromSelection = (question: any) => {
  selectedQuestionList.value = selectedQuestionList.value.filter(
    (q) => q.id !== question.id
  );
};

// 加载现有竞赛的题目
const loadContestQuestions = async (contestId: number) => {
  const res =
    await ContestQuestionControllerService.listContestQuestionByPageUsingPost({
      contestId: contestId,
      current: 1,
      pageSize: 1000, // 假设一个竞赛题目不会太多，一次拉取
    });
  if (res.code === 0) {
    originalContestQuestions.value = res.data.records;
    // 映射到 selectedQuestionList
    selectedQuestionList.value = res.data.records.map((cq) => ({
      id: cq.questionId,
      title: cq.questionVO?.title || `题目ID: ${cq.questionId}`,
    }));
  } else {
    message.error("加载竞赛题目失败: " + res.message);
  }
};

const getContestStatus = (contest: any) => {
  const now = new Date().getTime();
  const start = new Date(contest.startTime).getTime();
  const end = new Date(contest.endTime).getTime();

  if (now < start) return 0;
  if (now > end) return 2;
  return 1;
};

// --- 主逻辑 ---

const loadData = async () => {
  const res = await ContestControllerService.listContestByPageUsingPost(
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
  form.value = {
    id: undefined,
    title: "",
    description: "",
    startTime: "",
    endTime: "",
    status: 0,
  };
  // 重置题目选择
  selectedQuestionList.value = [];
  originalContestQuestions.value = [];
  loadQuestions(); // 预加载题目列表
  visible.value = true;
};

const doUpdate = async (record: any) => {
  form.value = { ...record };
  // 加载已关联题目
  await loadContestQuestions(record.id);
  loadQuestions(); // 预加载题目列表
  visible.value = true;
};

const handleSubmit = async () => {
  if (!form.value.title) {
    message.error("请输入标题");
    return;
  }
  if (!form.value.startTime || !form.value.endTime) {
    message.error("请选择开始和结束时间");
    return;
  }

  let contestId = form.value.id;
  let res;

  // 1. 保存/更新竞赛基本信息
  const updateData = {
    ...form.value,
    startTime: moment(form.value.startTime).format("YYYY-MM-DD HH:mm:ss"),
    endTime: moment(form.value.endTime).format("YYYY-MM-DD HH:mm:ss"),
  };

  if (contestId) {
    res = await ContestControllerService.updateUsingPost(updateData);
  } else {
    res = await ContestControllerService.addUsingPost(updateData);
    if (res.code === 0) {
      contestId = res.data; // 获取新创建的 ID
    }
  }

  if (res.code === 0) {
    // 2. 处理题目关联
    try {
      await handleQuestionRelations(contestId);
      message.success("操作成功");
      visible.value = false;
      loadData();
    } catch (error: any) {
      message.error("竞赛信息保存成功，但题目关联失败: " + error.message);
    }
  } else {
    message.error("操作失败: " + res.message);
  }
};

const handleQuestionRelations = async (contestId: any) => {
  // 需要添加的：在 selected 中，但不在 original 中
  const toAdd = selectedQuestionList.value.filter(
    (q) => !originalContestQuestions.value.some((cq) => cq.questionId === q.id)
  );

  // 需要删除的：在 original 中，但不在 selected 中
  const toDelete = originalContestQuestions.value.filter(
    (cq) => !selectedQuestionList.value.some((q) => q.id === cq.questionId)
  );

  // 执行添加
  for (const q of toAdd) {
    await ContestQuestionControllerService.addContestQuestionUsingPost({
      contestId: contestId,
      questionId: q.id,
    });
  }

  // 执行删除
  for (const cq of toDelete) {
    await ContestQuestionControllerService.deleteContestQuestionUsingPost({
      id: cq.id, // 注意这里用的是 ContestQuestion 的 id
    });
  }
};

const handleDelete = async (record: any) => {
  const res = await ContestControllerService.deleteUsingPost({ id: record.id });
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
.manage-contest {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.question-manage {
  margin-top: 20px;
}
</style>
