<template>
  <div class="favour-view">
    <a-card :bordered="false" title="我的收藏">
      <template #extra>
        <a-button type="text" @click="router.back()">返回</a-button>
      </template>
      <a-input-search
        v-model="favourSearchParams.title"
        placeholder="搜索收藏的题目（标题/标签）"
        style="margin-bottom: 20px; max-width: 400px"
        @search="loadFavourData"
      />
      <a-table
        :columns="favourColumns"
        :data="favourDataList"
        :pagination="{
          showTotal: true,
          pageSize: favourSearchParams.pageSize,
          current: favourSearchParams.current,
          total: favourTotal,
        }"
        @page-change="onFavourPageChange"
      >
        <template #title="{ record }">
          <a-link :href="`/view/question/${record.id}`">{{
            record.title
          }}</a-link>
        </template>
        <template #tags="{ record }">
          <a-space wrap>
            <a-tag v-for="tag in record.tags" :key="tag" color="green">{{
              tag
            }}</a-tag>
          </a-space>
        </template>
        <template #action="{ record }">
          <a-button type="text" status="danger" @click="doUnFavour(record)">
            取消收藏
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { QuestionQueryRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import axios from "axios";

const store = useStore();
const router = useRouter();
const loginUser = computed(() => store.state.user.loginUser);

// 收藏相关
const favourDataList = ref([]);
const favourTotal = ref(0);
const favourSearchParams = ref<QuestionQueryRequest>({
  pageSize: 10,
  current: 1,
  title: "",
});

const favourColumns = [
  {
    title: "题目",
    slotName: "title",
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "操作",
    slotName: "action",
  },
];

const loadFavourData = async () => {
  const res = await axios.post(
    "/api/question_favour/my/list/page",
    favourSearchParams.value
  );
  if (res.data.code === 0) {
    favourDataList.value = res.data.data.records;
    favourTotal.value = res.data.data.total;
  } else {
    message.error("加载收藏失败，" + res.data.message);
  }
};

const onFavourPageChange = (page: number) => {
  favourSearchParams.value = {
    ...favourSearchParams.value,
    current: page,
  };
  loadFavourData();
};

const doUnFavour = async (question: any) => {
  const res = await axios.post("/api/question_favour/", {
    questionId: question.id,
  });
  if (res.data.code === 0) {
    message.success("取消收藏成功");
    loadFavourData();
  } else {
    message.error("操作失败，" + res.data.message);
  }
};

onMounted(() => {
  if (loginUser.value) {
    loadFavourData();
  }
});
</script>

<style scoped>
.favour-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
