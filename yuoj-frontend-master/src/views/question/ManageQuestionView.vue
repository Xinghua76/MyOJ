<template>
  <div id="manageQuestionView">
    <a-card title="题目管理">
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 20px">
        <a-form-item field="title" label="标题" style="min-width: 240px">
          <a-input v-model="searchParams.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item field="tags" label="标签" style="min-width: 240px">
          <a-input-tag v-model="searchParams.tags" placeholder="请输入标签" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">搜索</a-button>
        </a-form-item>
      </a-form>

      <div style="margin-bottom: 16px">
        <a-space>
          <a-button type="primary" status="success" @click="doAdd"
            >创建题目</a-button
          >
          <a-upload
            action="/api/question/import"
            :show-file-list="false"
            name="file"
            @success="onImportSuccess"
            @error="onImportError"
            :with-credentials="true"
          >
            <template #upload-button>
              <a-button type="outline" status="success"
                >批量导入题目 (Excel)</a-button
              >
            </template>
          </a-upload>
          <a-button type="outline" status="warning" @click="doExport"
            >批量导出题目 (Excel)</a-button
          >
          <a-popconfirm
            content="确定要删除选中的题目吗?"
            @ok="handleBatchDelete"
            position="bottom"
          >
            <a-button
              type="outline"
              status="danger"
              :disabled="selectedKeys.length === 0"
            >
              批量删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
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
        row-key="id"
        :row-selection="rowSelection"
        v-model:selectedKeys="selectedKeys"
      >
        <template #difficulty="{ record }">
          <a-tag v-if="record.difficulty === 1" color="green"> 简单 </a-tag>
          <a-tag v-else-if="record.difficulty === 2" color="orange">
            中等
          </a-tag>
          <a-tag v-else-if="record.difficulty === 3" color="red"> 困难 </a-tag>
        </template>
        <template #tags="{ record }">
          <a-space wrap>
            <a-tag
              v-for="(tag, index) of JSON.parse(record.tags)"
              :key="index"
              color="green"
              >{{ tag }}
            </a-tag>
          </a-space>
        </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="primary" @click="doUpdate(record)"> 修改</a-button>
            <a-button status="danger" @click="doDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watchEffect } from "vue";
import {
  Page_Question_,
  Question,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  title: "",
  tags: [],
  pageSize: 10,
  current: 1,
});

// 行选择配置
const selectedKeys = ref([]);
const rowSelection = reactive({
  type: "checkbox",
  showCheckedAll: true,
  onlyCurrent: false,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

// {id: "1", title: "A+ D", content: "新的题目内容", tags: "["二叉树"]", answer: "新的答案", submitNum: 0,…}

const columns = [
  {
    title: "id",
    dataIndex: "id",
    width: 60,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "标题",
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
    title: "提交数",
    dataIndex: "submitNum",
    width: 80,
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
    width: 80,
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const doDelete = async (question: Question) => {
  const res = await QuestionControllerService.deleteQuestionUsingPost({
    id: question.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    loadData();
  } else {
    message.error("删除失败");
  }
};

// 批量删除处理函数
const handleBatchDelete = async () => {
  if (selectedKeys.value.length === 0) {
    message.warning("请选择要删除的题目");
    return;
  }
  const res = await QuestionControllerService.deleteBatchQuestionsUsingPost({
    ids: selectedKeys.value,
  });
  if (res.code === 0) {
    message.success("批量删除成功");
    selectedKeys.value = [];
    loadData();
  } else {
    message.error("批量删除失败: " + res.message);
  }
};

const router = useRouter();

const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};

const doAdd = () => {
  router.push({
    path: "/add/question",
  });
};

const onImportSuccess = (fileItem: any) => {
  if (fileItem.response && fileItem.response.code === 0) {
    message.success("导入成功");
    loadData();
  } else {
    message.error(
      "导入失败: " +
        (fileItem.response ? fileItem.response.message : "Unknown error")
    );
  }
};

const onImportError = (fileItem: any) => {
  message.error("导入请求失败");
};

const doExport = () => {
  // 构建查询参数
  const params = { ...searchParams.value };

  // 使用 URLSearchParams 替代 querystring
  const urlSearchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      // 处理数组情况，例如 tags
      value.forEach((v) => urlSearchParams.append(key, String(v)));
    } else if (value !== null && value !== undefined) {
      // 处理普通值
      urlSearchParams.append(key, String(value));
    }
  });

  // 如果有选中项，传递 ids 参数
  if (selectedKeys.value.length > 0) {
    selectedKeys.value.forEach((id) =>
      urlSearchParams.append("ids", String(id))
    );
  }

  const queryString = urlSearchParams.toString();
  // 直接下载
  window.location.href = `/api/question/export?${queryString}`;
};
</script>

<style scoped>
#manageQuestionView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
