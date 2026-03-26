<template>
  <div id="postListView">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="searchText" label="搜索" style="min-width: 240px">
        <a-input
          v-model="searchParams.searchText"
          placeholder="搜索标题或内容"
        />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="min-width: 240px">
        <a-input-tag v-model="searchParams.tags" placeholder="请输入标签" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">搜索</a-button>
        <a-button
          type="primary"
          status="success"
          @click="toCreatePost"
          style="margin-left: 10px"
          >创建帖子</a-button
        >
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-list
      class="post-list"
      :gridProps="{ gutter: [20, 20], sm: 24, md: 12, lg: 8, xl: 6 }"
      :data="dataList"
      :pagination-props="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #item="{ item }">
        <a-list-item class="post-item" @click="toPostPage(item)">
          <a-card hoverable class="post-card">
            <template #cover>
              <div class="card-cover">
                <img
                  v-if="extractImage(item.content)"
                  :src="extractImage(item.content)"
                  alt="cover"
                />
                <div v-else class="no-image-cover">
                  <icon-file-image style="font-size: 32px; color: #c9cdd4" />
                </div>
              </div>
            </template>
            <a-card-meta :title="item.title">
              <template #description>
                <div class="card-desc">
                  {{ item.content?.replace(/[#*`>]/g, "").slice(0, 50) }}...
                </div>
                <div class="card-tags">
                  <a-tag
                    v-for="(tag, index) of item.tags"
                    :key="index"
                    color="green"
                    size="small"
                    style="margin-right: 4px; margin-top: 4px"
                  >
                    {{ tag }}
                  </a-tag>
                </div>
                <div class="card-footer">
                  <span class="footer-item">
                    <icon-thumb-up /> {{ item.thumbNum }}
                  </span>
                  <span class="footer-item">
                    <icon-star /> {{ item.favourNum }}
                  </span>
                  <span class="footer-item time">
                    {{ moment(item.createTime).format("MM-DD") }}
                  </span>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  PostVO,
  PostControllerService,
  PostQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

import {
  IconThumbUp,
  IconStar,
  IconFileImage,
} from "@arco-design/web-vue/es/icon";

const tableRef = ref();

const extractImage = (content: string) => {
  const imgReg = /!\[.*?\]\((.*?)\)/;
  const match = content?.match(imgReg);
  return match ? match[1] : null;
};

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<PostQueryRequest>({
  searchText: "",
  tags: [],
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  // 使用 Search 接口
  const res = await PostControllerService.searchPostVoByPageUsingPost(
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

const columns = [
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "内容",
    dataIndex: "content",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "标签",
    slotName: "tagList",
  },
  {
    title: "点赞数",
    dataIndex: "thumbNum",
  },
  {
    title: "收藏数",
    dataIndex: "favourNum",
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

const router = useRouter();

/**
 * 跳转到帖子详情页
 * @param post
 */
const toPostPage = (post: PostVO) => {
  router.push({
    path: `/post/view/${post.id}`,
  });
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

const toCreatePost = () => {
  router.push("/post/create");
};
</script>

<style scoped>
#postListView {
  max-width: 1280px;
  margin: 0 auto;
}

.post-list {
  margin-top: 16px;
}

.post-card {
  height: 100%;
  transition: all 0.3s;
  cursor: pointer;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-cover {
  height: 160px;
  overflow: hidden;
  position: relative;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f3f5;
}

.card-desc {
  margin-top: 8px;
  color: #86909c;
  font-size: 13px;
  line-height: 22px;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-tags {
  margin-top: 12px;
  height: 28px;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  margin-top: 16px;
  color: #86909c;
  font-size: 12px;
}

.footer-item {
  display: flex;
  align-items: center;
  margin-right: 16px;
}

.footer-item .arco-icon {
  margin-right: 4px;
}

.time {
  margin-left: auto;
  margin-right: 0;
}
</style>
