<template>
  <div class="manage-post">
    <a-card title="讨论管理">
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 20px">
        <a-form-item field="searchText" label="关键词" style="min-width: 240px">
          <a-input
            v-model="searchParams.searchText"
            placeholder="搜索标题或内容"
          />
        </a-form-item>
        <a-form-item field="title" label="标题" style="min-width: 240px">
          <a-input v-model="searchParams.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item field="userId" label="作者ID" style="min-width: 200px">
          <a-input-number
            v-model="searchParams.userId"
            placeholder="请输入作者ID"
          />
        </a-form-item>
        <a-form-item field="tags" label="标签" style="min-width: 240px">
          <a-input-tag v-model="searchParams.tags" placeholder="请输入标签" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-table
        :data="dataList"
        :pagination="{
          showTotal: true,
          pageSize: searchParams.pageSize,
          current: searchParams.current,
          total: total,
        }"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="90" />
          <a-table-column
            title="标题"
            data-index="title"
            :ellipsis="true"
            :tooltip="true"
          />
          <a-table-column title="作者" data-index="user.userName" :width="160">
            <template #cell="{ record }">
              <span>{{ record.user?.userName || "-" }}</span>
            </template>
          </a-table-column>
          <a-table-column title="点赞" data-index="thumbNum" :width="90" />
          <a-table-column title="收藏" data-index="favourNum" :width="90" />
          <a-table-column title="创建时间" data-index="createTime" :width="190">
            <template #cell="{ record }">
              {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="220">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  type="primary"
                  size="small"
                  @click="toPostDetail(record)"
                >
                  查看
                </a-button>
                <a-popconfirm
                  content="确定要删除该讨论吗？"
                  @ok="handleDelete(record)"
                >
                  <a-button status="danger" size="small">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  PostControllerService,
  PostQueryRequest,
  PostVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const router = useRouter();

const dataList = ref<PostVO[]>([]);
const total = ref(0);
const searchParams = ref<PostQueryRequest>({
  searchText: "",
  title: "",
  userId: undefined,
  tags: [],
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await PostControllerService.listPostVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadData();
};

const toPostDetail = (post: PostVO) => {
  router.push({
    path: `/post/view/${post.id}`,
  });
};

const handleDelete = async (post: PostVO) => {
  const res = await PostControllerService.deletePostUsingPost({
    id: post.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    loadData();
  } else {
    message.error("删除失败，" + res.message);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.manage-post {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
