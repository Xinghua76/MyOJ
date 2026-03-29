<template>
  <div id="postDetailView">
    <a-card :title="post?.title">
      <template #extra>
        <a-space>
          <a-tag v-for="(tag, index) of post?.tags" :key="index" color="green">
            {{ tag }}
          </a-tag>
          <a-button type="outline" @click="router.back()">返回</a-button>
        </a-space>
      </template>
    </a-card>

    <a-divider />

    <a-card>
      <div class="content">
        <MdViewer :value="post?.content || ''" />
      </div>
    </a-card>

    <a-divider />

    <div class="post-actions">
      <span class="action" @click="doPostThumb">
        <icon-thumb-up :style="{ color: post?.hasThumb ? '#165dff' : '' }" />
        {{ post?.thumbNum || 0 }}
      </span>
      <span class="action" @click="doPostFavour">
        <icon-star :style="{ color: post?.hasFavour ? '#f7ba1e' : '' }" />
        {{ post?.favourNum || 0 }}
      </span>
    </div>

    <a-divider />

    <div class="comment-header">
      <h3>评论区（{{ totalCommentCount }}）</h3>
      <a-button type="text" :loading="commentLoading" @click="loadComments"
        >刷新</a-button
      >
    </div>

    <a-comment
      id="comment-input"
      align="right"
      :author="loginUserName"
      :avatar="loginUserAvatar"
    >
      <template #actions>
        <a-space>
          <a-button
            v-if="replyParentId"
            type="secondary"
            status="warning"
            size="small"
            @click="cancelReply"
          >
            取消回复
          </a-button>
          <a-button
            type="primary"
            :disabled="!isLogin"
            :loading="commentSubmitting"
            @click="doSubmitComment"
          >
            {{ isLogin ? "发布" : "请先登录" }}
          </a-button>
        </a-space>
      </template>
      <template #content>
        <div>
          <div v-if="replyParentId" class="reply-tip">
            正在回复 #{{ replyParentId }}
            {{ replyParentUserName ? `(@${replyParentUserName})` : "" }}
          </div>
          <a-textarea
            v-model="commentContent"
            :disabled="!isLogin"
            :max-length="500"
            show-word-limit
            placeholder="写下你的评论..."
          />
        </div>
      </template>
    </a-comment>

    <a-list
      :data="pagedCommentRows"
      :bordered="false"
      :loading="commentLoading"
    >
      <template #item="{ item }">
        <div
          class="comment-row"
          :class="{ 'child-row': item.level > 0 }"
          :style="{ marginLeft: `${Math.min(item.level, 4) * 24}px` }"
        >
          <a-comment
            :author="item.user?.userName"
            :content="item.content"
            :datetime="formatRelativeTime(item.createTime)"
          >
            <template #avatar>
              <a-avatar>
                <img
                  v-if="item.user?.userAvatar"
                  alt="avatar"
                  :src="item.user?.userAvatar"
                />
                <span v-else>{{ item.user?.userName?.charAt(0) }}</span>
              </a-avatar>
            </template>
            <template #actions>
              <span
                v-if="item.childCount > 0"
                class="action"
                @click="toggleCollapse(item.id)"
              >
                {{
                  isCollapsed(item.id)
                    ? `展开 ${item.childCount}`
                    : `折叠 ${item.childCount}`
                }}
              </span>
              <span
                class="action"
                :class="{ 'is-disabled': commentThumbLoadingMap[item.id || 0] }"
                @click="doThumb(item.id)"
              >
                <icon-thumb-up
                  :style="{ color: item.hasThumb ? '#165dff' : '' }"
                />
                {{ item.thumbNum || 0 }}
              </span>
              <span class="action" @click="replyComment(item)">
                <icon-message /> 回复
              </span>
              <a-popconfirm
                v-if="isOwnComment(item)"
                content="确定删除这条评论吗？"
                @ok="doDeleteComment(item.id)"
              >
                <span
                  class="action danger"
                  :class="{
                    'is-disabled': commentDeletingMap[item.id || 0],
                  }"
                >
                  删除
                </span>
              </a-popconfirm>
            </template>
          </a-comment>
        </div>
      </template>
      <template #empty>
        <a-empty description="暂无评论，来抢沙发吧" />
      </template>
    </a-list>

    <div class="comment-pagination" v-if="rootCommentTotal > commentPageSize">
      <a-pagination
        :current="commentCurrent"
        :page-size="commentPageSize"
        :total="rootCommentTotal"
        :show-total="true"
        :show-jumper="true"
        @change="onCommentPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onMounted,
  ref,
  watch,
  withDefaults,
  defineProps,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  PostVO,
  PostControllerService,
  PostCommentControllerService,
  PostCommentVO,
  PostThumbControllerService,
  PostFavourControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import MdViewer from "@/components/MdViewer.vue";
import { useStore } from "vuex";
import moment from "moment";
import "moment/locale/zh-cn";

import {
  IconThumbUp,
  IconMessage,
  IconStar,
} from "@arco-design/web-vue/es/icon";

moment.locale("zh-cn");

interface Props {
  id: string;
}

interface CommentNode extends PostCommentVO {
  children: CommentNode[];
}

interface CommentRow extends PostCommentVO {
  level: number;
  childCount: number;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const route = useRoute();
const router = useRouter();
const store = useStore();

const post = ref<PostVO>();
const commentContent = ref("");
const comments = ref<PostCommentVO[]>([]);

const commentCurrent = ref(1);
const commentPageSize = ref(10);
const commentLoading = ref(false);
const commentSubmitting = ref(false);
const commentThumbLoadingMap = ref<Record<number, boolean>>({});
const commentCollapseMap = ref<Record<number, boolean>>({});
const commentDeletingMap = ref<Record<number, boolean>>({});

const replyParentId = ref<number | undefined>(undefined);
const replyParentUserName = ref<string>("");

const loginUser = computed(() => store.state.user.loginUser || {});
const isLogin = computed(() => Boolean(loginUser.value?.id));
const loginUserName = computed(() => loginUser.value?.userName || "Guest");
const loginUserAvatar = computed(() => loginUser.value?.userAvatar || "");

const resolvedPostId = computed<string | undefined>(() => {
  const rawId =
    props.id || (route.params.id as string) || (route.query.id as string);
  const postId = String(rawId || "").trim();
  if (!/^\d+$/.test(postId)) {
    return undefined;
  }
  return postId;
});

const totalCommentCount = computed(() => comments.value.length);

const commentTree = computed<CommentNode[]>(() => {
  const nodeMap = new Map<number, CommentNode>();
  const roots: CommentNode[] = [];

  comments.value.forEach((item) => {
    if (!item.id) {
      return;
    }
    nodeMap.set(item.id, { ...item, children: [] });
  });

  nodeMap.forEach((node) => {
    const parentId = node.parentId;
    if (parentId && nodeMap.has(parentId)) {
      nodeMap.get(parentId)?.children.push(node);
      return;
    }
    roots.push(node);
  });

  const sortByTimeAsc = (a: PostCommentVO, b: PostCommentVO) => {
    const aTime = a.createTime ? new Date(a.createTime).getTime() : 0;
    const bTime = b.createTime ? new Date(b.createTime).getTime() : 0;
    return aTime - bTime;
  };

  const sortTree = (nodes: CommentNode[]) => {
    nodes.sort(sortByTimeAsc);
    nodes.forEach((node) => sortTree(node.children));
  };

  sortTree(roots);
  return roots;
});

const rootCommentTotal = computed(() => commentTree.value.length);

const pagedCommentRows = computed<CommentRow[]>(() => {
  const start = (commentCurrent.value - 1) * commentPageSize.value;
  const roots = commentTree.value.slice(start, start + commentPageSize.value);
  const rows: CommentRow[] = [];

  const append = (node: CommentNode, level: number) => {
    const nodeId = node.id || 0;
    const childCount = node.children.length;
    const collapsed = Boolean(commentCollapseMap.value[nodeId]);
    rows.push({ ...node, level, childCount });
    if (collapsed) {
      return;
    }
    node.children.forEach((child) => append(child, level + 1));
  };

  roots.forEach((root) => append(root, 0));
  return rows;
});

const formatRelativeTime = (time?: string) => {
  if (!time) {
    return "";
  }
  const utcTime = moment.utc(time, "YYYY-MM-DD HH:mm:ss", true);
  if (utcTime.isValid()) {
    return utcTime.local().fromNow();
  }
  return moment(time).fromNow();
};

const loadData = async () => {
  if (!resolvedPostId.value) {
    return;
  }
  const res = await PostControllerService.getPostVoByIdUsingGet(
    resolvedPostId.value as unknown as number
  );
  if (res.code === 0) {
    post.value = res.data;
  } else {
    message.error("帖子加载失败：" + res.message);
  }
};

const loadComments = async () => {
  if (!resolvedPostId.value) {
    comments.value = [];
    return;
  }
  commentLoading.value = true;
  try {
    const res =
      await PostCommentControllerService.listPostCommentVoByPageUsingPost({
        postId: resolvedPostId.value as unknown as number,
        current: 1,
        pageSize: 1000,
        sortField: "create_time",
        sortOrder: "ascend",
      });
    if (res.code === 0) {
      comments.value = res.data?.records || [];
      const validIds = new Set(
        comments.value
          .map((item) => item.id)
          .filter((id): id is number => Boolean(id))
      );
      Object.keys(commentCollapseMap.value).forEach((key) => {
        const id = Number(key);
        if (!validIds.has(id)) {
          delete commentCollapseMap.value[id];
        }
      });
      if (
        commentCurrent.value > rootCommentTotal.value &&
        rootCommentTotal.value > 0
      ) {
        commentCurrent.value = 1;
      }
    } else {
      message.error("评论加载失败：" + res.message);
    }
  } finally {
    commentLoading.value = false;
  }
};

const onCommentPageChange = (page: number) => {
  commentCurrent.value = page;
};

const isCollapsed = (commentId?: number) => {
  if (!commentId) {
    return false;
  }
  return Boolean(commentCollapseMap.value[commentId]);
};

const toggleCollapse = (commentId?: number) => {
  if (!commentId) {
    return;
  }
  commentCollapseMap.value[commentId] = !commentCollapseMap.value[commentId];
};

const isOwnComment = (comment: PostCommentVO) => {
  const loginUserId = loginUser.value?.id;
  if (!loginUserId || !comment.userId) {
    return false;
  }
  return String(loginUserId) === String(comment.userId);
};

const doPostThumb = async () => {
  if (!post.value?.id) {
    return;
  }
  const res = await PostThumbControllerService.doThumbUsingPost({
    postId: post.value.id,
  });
  if (res.code === 0) {
    const delta = Number(res.data || 0);
    if (delta === 1) {
      post.value.hasThumb = true;
      post.value.thumbNum = (post.value.thumbNum || 0) + 1;
    } else if (delta === -1) {
      post.value.hasThumb = false;
      post.value.thumbNum = Math.max(0, (post.value.thumbNum || 0) - 1);
    }
  } else {
    message.error("操作失败：" + res.message);
  }
};

const doPostFavour = async () => {
  if (!post.value?.id) {
    return;
  }
  const res = await PostFavourControllerService.doPostFavourUsingPost({
    postId: post.value.id,
  });
  if (res.code === 0) {
    const delta = Number(res.data || 0);
    if (delta === 1) {
      post.value.hasFavour = true;
      post.value.favourNum = (post.value.favourNum || 0) + 1;
    } else if (delta === -1) {
      post.value.hasFavour = false;
      post.value.favourNum = Math.max(0, (post.value.favourNum || 0) - 1);
    }
  } else {
    message.error("操作失败：" + res.message);
  }
};

const doSubmitComment = async () => {
  if (!resolvedPostId.value) {
    message.error("帖子参数无效");
    return;
  }
  if (!isLogin.value) {
    message.warning("请先登录后再评论");
    return;
  }
  const content = commentContent.value.trim();
  if (!content) {
    message.error("请输入评论内容");
    return;
  }

  commentSubmitting.value = true;
  try {
    const res = await PostCommentControllerService.addPostCommentUsingPost({
      postId: resolvedPostId.value as unknown as number,
      content,
      parentId: replyParentId.value,
    });
    if (res.code === 0) {
      message.success("评论成功");
      commentContent.value = "";
      cancelReply();
      await loadComments();
    } else {
      message.error("评论失败：" + res.message);
    }
  } finally {
    commentSubmitting.value = false;
  }
};

const doThumb = async (commentId?: number) => {
  if (!commentId) {
    return;
  }
  commentThumbLoadingMap.value[commentId] = true;
  try {
    const res = await PostCommentControllerService.doPostCommentThumbUsingPost({
      id: commentId,
    });
    if (res.code === 0) {
      const target = comments.value.find((item) => item.id === commentId);
      if (!target) {
        return;
      }
      const delta = Number(res.data || 0);
      if (delta === 1) {
        target.hasThumb = true;
        target.thumbNum = (target.thumbNum || 0) + 1;
      } else if (delta === -1) {
        target.hasThumb = false;
        target.thumbNum = Math.max(0, (target.thumbNum || 0) - 1);
      }
    } else {
      message.error("操作失败：" + res.message);
    }
  } finally {
    commentThumbLoadingMap.value[commentId] = false;
  }
};

const doDeleteComment = async (commentId?: number) => {
  if (!commentId) {
    return;
  }
  commentDeletingMap.value[commentId] = true;
  try {
    const res = await PostCommentControllerService.deletePostCommentUsingPost({
      id: commentId,
    });
    if (res.code === 0) {
      message.success("删除成功");
      await loadComments();
    } else {
      message.error("删除失败：" + res.message);
    }
  } finally {
    commentDeletingMap.value[commentId] = false;
  }
};

const replyComment = (comment: PostCommentVO) => {
  replyParentId.value = comment.id;
  replyParentUserName.value = comment.user?.userName || "";
  commentContent.value = `@${comment.user?.userName || "user"} `;

  const inputElement = document.getElementById("comment-input");
  if (inputElement) {
    inputElement.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  nextTick(() => {
    const textarea = document.querySelector(
      "#comment-input textarea"
    ) as HTMLTextAreaElement | null;
    textarea?.focus();
  });
};

const cancelReply = () => {
  replyParentId.value = undefined;
  replyParentUserName.value = "";
};

onMounted(() => {
  if (!resolvedPostId.value) {
    message.error("帖子参数无效");
    return;
  }
  loadData();
  loadComments();
});

watch(
  () => resolvedPostId.value,
  (newPostId, oldPostId) => {
    if (!newPostId || newPostId === oldPostId) {
      return;
    }
    commentCurrent.value = 1;
    cancelReply();
    loadData();
    loadComments();
  }
);
</script>

<style scoped>
#postDetailView {
  max-width: 1280px;
  margin: 0 auto;
}

.post-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-header h3 {
  margin: 0;
  font-size: 18px;
}

.reply-tip {
  margin-bottom: 8px;
  color: var(--color-text-2);
  font-size: 12px;
}

.comment-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comment-row {
  width: 100%;
}

.child-row {
  border-left: 2px solid var(--color-border-2);
  padding-left: 10px;
}

.action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 6px;
  color: var(--color-text-1);
  line-height: 24px;
  background: transparent;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.action:hover {
  background: var(--color-fill-3);
}

.action.is-disabled {
  opacity: 0.55;
  cursor: not-allowed;
  pointer-events: none;
}

.action.danger {
  color: rgb(var(--danger-6));
}

:deep(.arco-comment) {
  position: relative;
}

:deep(.arco-comment) {
  margin-left: 0;
}

:deep(.arco-comment .arco-comment-inner) {
  border-radius: 8px;
}

:deep(.arco-list-item) {
  border-bottom: none;
}

:deep(.arco-list-item .arco-comment) {
  margin-left: 0;
}

:deep(.arco-list-item) {
  padding-left: 0;
}

:deep(.arco-list-item) > * {
  width: 100%;
}
</style>
