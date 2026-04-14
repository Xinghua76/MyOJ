<template>
  <div id="questionSolutionList">
    <div class="header">
      <a-button type="primary" @click="openAddSolutionModal">发布题解</a-button>
    </div>
    <a-divider />
    <a-list
      :data="dataList"
      :pagination-props="paginationProps"
      @page-change="onPageChange"
    >
      <template #item="{ item }">
        <a-list-item action-layout="vertical" @click="toSolutionDetail(item)">
          <template #actions>
            <span class="action" @click.stop="handleThumb(item)">
              <icon-heart-fill v-if="item.hasThumb" style="color: #f53f3f" />
              <icon-heart v-else />
              {{ item.likeNum || 0 }}
            </span>
            <span class="action">
              <icon-message /> {{ item.commentNum || 0 }}
            </span>
          </template>
          <a-list-item-meta :title="item.title">
            <template #avatar>
              <a-avatar shape="square">
                <img alt="avatar" :src="item.user?.userAvatar" />
              </a-avatar>
            </template>
            <template #description>
              <div>
                <a-tag
                  v-for="tag in getSolutionTags(item)"
                  :key="tag"
                  color="green"
                  >{{ tag }}</a-tag
                >
                <span style="margin-left: 8px; color: #888">
                  {{ moment(item.createTime).format("YYYY-MM-DD HH:mm") }}
                </span>
                <div
                  style="
                    margin-top: 8px;
                    color: #555;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    width: 100%;
                    max-width: none;
                  "
                >
                  {{
                    item.content?.length > 100
                      ? item.content.substring(0, 100) + "..."
                      : item.content
                  }}
                </div>
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>

    <!-- Add Solution Modal -->
    <a-modal
      v-model:visible="showAddModal"
      :title="isEditSolutionMode ? '编辑题解' : '发布题解'"
      @ok="handleSubmitSolution"
      fullscreen
      class="solution-modal"
    >
      <a-form :model="addForm" layout="vertical" class="solution-form">
        <a-form-item field="title" label="标题">
          <a-input v-model="addForm.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item field="tags" label="标签">
          <a-input-tag
            v-model="addForm.tags"
            placeholder="请输入标签 (回车添加)"
          />
        </a-form-item>
        <a-form-item field="content" label="内容">
          <div class="solution-editor">
            <MdEditor
              class="solution-md-editor"
              :value="addForm.content"
              :handle-change="(v) => (addForm.content = v)"
            />
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Solution Detail Drawer -->
    <a-drawer
      :width="800"
      v-model:visible="showDetailDrawer"
      title="题解详情"
      :footer="false"
      class="solution-drawer"
    >
      <div v-if="currentSolution">
        <h2>{{ currentSolution.title }}</h2>
        <div
          class="user-info"
          style="display: flex; align-items: center; margin-bottom: 16px"
        >
          <a-avatar :size="24">
            <img
              v-if="currentSolution.user?.userAvatar"
              alt="avatar"
              :src="currentSolution.user?.userAvatar"
            />
            <span v-else>{{ currentSolution.user?.userName?.charAt(0) }}</span>
          </a-avatar>
          <span style="margin-left: 8px; font-weight: bold">{{
            currentSolution.user?.userName
          }}</span>
          <span style="margin-left: auto; color: #888">{{
            moment(currentSolution.createTime).format("YYYY-MM-DD HH:mm")
          }}</span>
        </div>
        <a-space v-if="canManageCurrentSolution" style="margin-bottom: 16px">
          <a-button type="primary" @click="goEditSolution">编辑题解</a-button>
          <a-popconfirm content="确定删除这篇题解吗？" @ok="deleteSolution">
            <a-button
              type="outline"
              status="danger"
              :loading="deletingSolution"
            >
              删除题解
            </a-button>
          </a-popconfirm>
        </a-space>
        <a-divider />
        <MdViewer :value="currentSolution.content || ''" />
        <a-divider />
        <div
          style="display: flex; justify-content: center; margin-bottom: 24px"
        >
          <a-button
            type="primary"
            shape="circle"
            size="large"
            @click="handleThumb(currentSolution)"
          >
            <template #icon>
              <icon-heart-fill v-if="currentSolution.hasThumb" />
              <icon-heart v-else />
            </template>
          </a-button>
          <span style="margin-left: 8px; line-height: 40px; font-size: 16px">{{
            currentSolution.likeNum
          }}</span>
        </div>
        <a-divider />
        <!-- Comments Section -->
        <h3>评论</h3>
        <a-list :data="commentList" :bordered="false">
          <template #item="{ item }">
            <a-comment
              :author="item.userVO?.userName"
              :datetime="moment(item.createTime).fromNow()"
            >
              <template #avatar>
                <a-avatar>
                  <img
                    v-if="item.userVO?.userAvatar"
                    alt="avatar"
                    :src="item.userVO?.userAvatar"
                  />
                  <span v-else>{{ item.userVO?.userName?.charAt(0) }}</span>
                </a-avatar>
              </template>
              <template #content>
                {{ item.content }}
              </template>
              <template #actions>
                <span class="action" @click="handleCommentThumb(item)">
                  <icon-heart-fill
                    v-if="item.hasThumb"
                    style="color: #f53f3f"
                  />
                  <icon-heart v-else />
                  {{ item.likeNum || 0 }}
                </span>
              </template>
            </a-comment>
          </template>
        </a-list>
        <div class="comment-input" style="margin-top: 20px">
          <a-textarea
            v-model="newComment"
            placeholder="写下你的评论..."
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
          <a-button
            type="primary"
            style="margin-top: 10px"
            @click="handleAddComment"
            >发送</a-button
          >
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, onMounted, ref, watch } from "vue";
import { useStore } from "vuex";
import { PostControllerService, PostVO } from "../../../generated";
import MdEditor from "@/components/MdEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import axios from "axios";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  IconHeart,
  IconMessage,
  IconHeartFill,
} from "@arco-design/web-vue/es/icon";
import { PostThumbControllerService } from "../../../generated";

const props = defineProps<{
  questionId: string;
}>();

type SolutionItem = PostVO & {
  tagList?: string[];
  tags?: string[];
};

const dataList = ref<SolutionItem[]>([]);
const total = ref(0);
const showAddModal = ref(false);
const showDetailDrawer = ref(false);
const currentSolution = ref<SolutionItem>();
const commentList = ref([]);
const newComment = ref("");
const deletingSolution = ref(false);
const editingSolutionId = ref<number>();

const store = useStore();
const loginUser = computed(() => store.state.user.loginUser || {});
const isEditSolutionMode = computed(() => Boolean(editingSolutionId.value));

const isAdmin = computed(() => loginUser.value?.userRole === ACCESS_ENUM.ADMIN);
const canManageCurrentSolution = computed(() => {
  const currentUserId = loginUser.value?.id;
  const solutionUserId = currentSolution.value?.userId;
  if (!currentUserId || !solutionUserId) {
    return false;
  }
  return isAdmin.value || String(currentUserId) === String(solutionUserId);
});

const searchParams = ref({
  questionId: props.questionId,
  postType: "solution",
  pageSize: 10,
  current: 1,
  sortField: "createTime",
  sortOrder: "descend",
});

const addForm = ref({
  title: "",
  content: "",
  tags: [] as string[],
});

const resetSolutionForm = () => {
  editingSolutionId.value = undefined;
  addForm.value = {
    title: "",
    content: "",
    tags: [],
  };
};

const getSolutionTags = (solution?: SolutionItem) => {
  if (!solution) {
    return [] as string[];
  }
  const tags = solution.tagList || solution.tags || [];
  return Array.isArray(tags) ? tags : [];
};

const paginationProps = ref({
  pageSize: 10,
  current: 1,
  total: 0,
  showTotal: true,
});

const loadData = async () => {
  if (!props.questionId) return;
  const res = await PostControllerService.listPostVoByPageUsingPost({
    ...searchParams.value,
    postType: "solution",
    questionId: parseInt(props.questionId),
    sortField: "create_time",
  });
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
    paginationProps.value.total = res.data.total;
  }
};

const onPageChange = (page: number) => {
  searchParams.value.current = page;
  paginationProps.value.current = page;
  loadData();
};

const openAddSolutionModal = () => {
  resetSolutionForm();
  showAddModal.value = true;
};

const handleSubmitSolution = async () => {
  if (!addForm.value.title?.trim() || !addForm.value.content?.trim()) {
    message.error("请输入标题和内容");
    return;
  }
  if (isEditSolutionMode.value && editingSolutionId.value) {
    const res = await PostControllerService.editPostUsingPost({
      id: editingSolutionId.value,
      title: addForm.value.title,
      content: addForm.value.content,
      tags: addForm.value.tags,
      postType: "solution",
      questionId: parseInt(props.questionId),
    });
    if (res.code === 0) {
      message.success("题解更新成功");
      showAddModal.value = false;
      if (currentSolution.value?.id === editingSolutionId.value) {
        currentSolution.value = {
          ...currentSolution.value,
          title: addForm.value.title,
          content: addForm.value.content,
          tagList: addForm.value.tags,
          tags: addForm.value.tags,
        };
      }
      await loadData();
      return;
    }
    message.error("题解更新失败: " + res.message);
    return;
  }

  const res = await PostControllerService.addPostUsingPost({
    ...addForm.value,
    postType: "solution",
    questionId: parseInt(props.questionId),
  });
  if (res.code === 0) {
    message.success("发布成功");
    showAddModal.value = false;
    resetSolutionForm();
    loadData();
  } else {
    message.error("发布失败: " + res.message);
  }
};

const toSolutionDetail = (item: PostVO) => {
  currentSolution.value = item;
  showDetailDrawer.value = true;
  loadComments(item.id);
};

const goEditSolution = () => {
  if (!currentSolution.value?.id || !canManageCurrentSolution.value) {
    return;
  }
  editingSolutionId.value = currentSolution.value.id;
  addForm.value = {
    title: currentSolution.value.title || "",
    content: currentSolution.value.content || "",
    tags: getSolutionTags(currentSolution.value),
  };
  showAddModal.value = true;
};

const deleteSolution = async () => {
  if (!currentSolution.value?.id) {
    return;
  }
  deletingSolution.value = true;
  try {
    const res = await PostControllerService.deletePostUsingPost({
      id: currentSolution.value.id,
    });
    if (res.code === 0) {
      message.success("题解删除成功");
      showDetailDrawer.value = false;
      currentSolution.value = undefined;
      await loadData();
    } else {
      message.error("题解删除失败: " + res.message);
    }
  } finally {
    deletingSolution.value = false;
  }
};

const loadComments = async (solutionId: number) => {
  const res = await axios.post("/api/solution_comment/list/page/vo", {
    solutionId: solutionId,
    current: 1,
    pageSize: 20,
  });
  if (res.data.code === 0) {
    commentList.value = res.data.data.records;
  }
};

const handleAddComment = async () => {
  if (!newComment.value || !currentSolution.value) return;
  const res = await axios.post("/api/solution_comment/add", {
    solutionId: currentSolution.value.id,
    content: newComment.value,
  });
  if (res.data.code === 0) {
    message.success("评论成功");
    newComment.value = "";
    loadComments(currentSolution.value.id);
  } else {
    message.error("评论失败: " + res.data.message);
  }
};

const handleCommentThumb = async (item: any) => {
  const res = await axios.post("/api/solution_comment_thumb/", {
    commentId: item.id,
  });
  if (res.data.code === 0) {
    const result = res.data.data;
    if (result === 1) {
      item.hasThumb = true;
      item.likeNum = (item.likeNum || 0) + 1;
    } else {
      item.hasThumb = false;
      item.likeNum = (item.likeNum || 0) - 1;
    }
  } else {
    message.error("操作失败: " + res.data.message);
  }
};

const handleThumb = async (item: PostVO) => {
  const res = await PostThumbControllerService.doThumbUsingPost({
    postId: item.id,
  });
  if (res.code === 0) {
    const result = res.data; // 1: thumb, -1: cancel
    if (result === 1) {
      item.hasThumb = true;
      item.likeNum = (item.likeNum || 0) + 1;
    } else {
      item.hasThumb = false;
      item.likeNum = (item.likeNum || 0) - 1;
    }
  } else {
    message.error("操作失败：" + res.message);
  }
};

watch(
  () => props.questionId,
  () => {
    if (props.questionId) {
      searchParams.value.questionId = props.questionId;
      loadData();
    }
  }
);

watch(showAddModal, (visible) => {
  if (!visible) {
    resetSolutionForm();
  }
});

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.solution-form {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  overflow: hidden;
}

.solution-form :deep(.arco-form-item) {
  flex-shrink: 0;
}

.solution-form :deep(.arco-form-item:last-child) {
  flex: 1;
  min-height: 0;
  margin-bottom: 0;
}

.solution-editor {
  height: 100%;
  min-height: 0;
}

.solution-editor :deep(.md-editor) {
  height: 100%;
}

.solution-modal :global(.arco-modal-body) {
  height: calc(100vh - 120px);
  overflow: hidden;
}

.solution-modal :global(.arco-modal-content) {
  height: 100%;
}

.solution-drawer :global(.arco-drawer-body) {
  padding-right: 24px;
}

.action {
  display: inline-block;
  padding: 0 4px;
  color: var(--color-text-1);
  line-height: 24px;
  background: transparent;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.1s ease;
  margin-right: 16px;
}
.action:hover {
  background: var(--color-fill-3);
}

:deep(.arco-list-item) {
  cursor: pointer;
}
:deep(.arco-list-item:hover) {
  background-color: var(--color-fill-2);
}
</style>
