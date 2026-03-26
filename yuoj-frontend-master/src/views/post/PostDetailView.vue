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
    <a-comment
      id="comment-input"
      align="right"
      :author="store.state.user.loginUser.userName"
      :avatar="store.state.user.loginUser.userAvatar"
    >
      <template #actions>
        <a-button type="primary" @click="doSubmitComment">发送</a-button>
      </template>
      <template #content>
        <a-textarea v-model="commentContent" placeholder="请输入评论..." />
      </template>
    </a-comment>
    <a-list :data="commentList" :bordered="false">
      <template #item="{ item }">
        <a-comment
          :author="item.user?.userName"
          :content="item.content"
          :datetime="moment(item.createTime).fromNow()"
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
            <span class="action" @click="doThumb(item)">
              <icon-thumb-up
                :style="{ color: item.hasThumb ? '#165dff' : '' }"
              />
              {{ item.thumbNum || 0 }}
            </span>
            <span class="action" @click="replyComment(item)">
              <icon-message /> 回复
            </span>
          </template>
        </a-comment>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, withDefaults, defineProps } from "vue";
import { useRouter } from "vue-router";
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

import {
  IconThumbUp,
  IconMessage,
  IconStar,
} from "@arco-design/web-vue/es/icon";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const router = useRouter();
const store = useStore();
const post = ref<PostVO>();
const commentContent = ref("");
const commentList = ref<PostCommentVO[]>([]);

const loadData = async () => {
  const res = await PostControllerService.getPostVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    post.value = res.data;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const loadComments = async () => {
  const res =
    await PostCommentControllerService.listPostCommentVoByPageUsingPost({
      postId: props.id as any,
      pageSize: 100,
    });
  if (res.code === 0) {
    commentList.value = res.data.records;
  } else {
    message.error("评论加载失败");
  }
};

const doPostThumb = async () => {
  if (!post.value?.id) {
    return;
  }
  const res = await PostThumbControllerService.doThumbUsingPost({
    postId: post.value.id,
  });
  if (res.code === 0) {
    const delta = res.data;
    if (delta === 1) {
      post.value.hasThumb = true;
      post.value.thumbNum = (post.value.thumbNum || 0) + 1;
    } else if (delta === -1) {
      post.value.hasThumb = false;
      post.value.thumbNum = Math.max(0, (post.value.thumbNum || 0) - 1);
    }
  } else {
    message.error("操作失败，" + res.message);
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
    const delta = res.data;
    if (delta === 1) {
      post.value.hasFavour = true;
      post.value.favourNum = (post.value.favourNum || 0) + 1;
    } else if (delta === -1) {
      post.value.hasFavour = false;
      post.value.favourNum = Math.max(0, (post.value.favourNum || 0) - 1);
    }
  } else {
    message.error("操作失败，" + res.message);
  }
};

const doSubmitComment = async () => {
  if (!commentContent.value) {
    message.error("请输入评论内容");
    return;
  }
  const res = await PostCommentControllerService.addPostCommentUsingPost({
    postId: props.id as any,
    content: commentContent.value,
  });
  if (res.code === 0) {
    message.success("评论成功");
    commentContent.value = "";
    loadComments();
  } else {
    message.error("评论失败，" + res.message);
  }
};

const doThumb = async (comment: PostCommentVO) => {
  const res = await PostCommentControllerService.doPostCommentThumbUsingPost({
    id: comment.id,
  });
  if (res.code === 0) {
    // 重新加载评论列表以更新点赞状态和数量
    loadComments();
  } else {
    message.error("操作失败，" + res.message);
  }
};

const replyComment = (comment: PostCommentVO) => {
  commentContent.value = `回复 @${comment.user?.userName} : `;
  // 滚动到输入框
  const inputElement = document.getElementById("comment-input");
  if (inputElement) {
    inputElement.scrollIntoView({ behavior: "smooth" });
  }
};

const data = ref<any[]>([]);

onMounted(() => {
  loadData();
  loadComments();
});
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

.action {
  display: inline-block;
  padding: 0 4px;
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
</style>
