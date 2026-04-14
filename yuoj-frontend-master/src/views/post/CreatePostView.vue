<template>
  <div id="createPostView">
    <a-card :title="isEditMode ? '编辑帖子' : '创建帖子'">
      <a-form ref="formRef" :model="form" @submit="handleSubmit">
        <a-form-item
          field="title"
          label="标题"
          :rules="[{ required: true, message: '请输入标题' }]"
        >
          <a-input v-model="form.title" placeholder="请输入帖子标题" />
        </a-form-item>
        <a-form-item field="tags" label="标签">
          <a-input-tag
            v-model="form.tags"
            placeholder="请输入标签，按回车确认"
            allow-clear
          />
        </a-form-item>
        <a-form-item
          field="content"
          label="内容"
          :rules="[{ required: true, message: '请输入内容' }]"
        >
          <MdEditor :value="form.content" :handle-change="onContentChange" />
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            @click="handleSubmit"
            style="min-width: 150px"
          >
            {{ isEditMode ? "保存" : "发布" }}
          </a-button>
          <a-button
            type="outline"
            status="danger"
            style="min-width: 150px; margin-left: 20px"
            @click="router.back()"
          >
            取消
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  PostAddRequest,
  PostControllerService,
  PostEditRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import MdEditor from "@/components/MdEditor.vue";

const route = useRoute();
const router = useRouter();

const form = ref<PostAddRequest>({
  title: "",
  content: "",
  tags: [],
});
const editPostId = ref<string>();

const isEditMode = computed(() => Boolean(editPostId.value));

const onContentChange = (v: string) => {
  form.value.content = v;
};

const formRef = ref();

const loadPostForEdit = async () => {
  const rawId = String(route.query.id || "").trim();
  if (!/^\d+$/.test(rawId)) {
    return;
  }
  editPostId.value = rawId;
  const res = await PostControllerService.getPostVoByIdUsingGet(
    editPostId.value as unknown as number
  );
  if (res.code !== 0 || !res.data) {
    message.error("帖子加载失败：" + res.message);
    return;
  }
  const data = res.data as Record<string, unknown>;
  const tags = (data.tags as string[]) || (data.tagList as string[]) || [];
  form.value = {
    title: (data.title as string) || "",
    content: (data.content as string) || "",
    tags: Array.isArray(tags) ? tags : [],
  };
};

const handleSubmit = async () => {
  const errors = await formRef.value?.validate();
  if (errors) {
    return;
  }
  if (!form.value.title || !form.value.content) {
    message.error("请填写标题和内容");
    return;
  }

  if (isEditMode.value && editPostId.value) {
    const editPayload: PostEditRequest = {
      id: editPostId.value as unknown as number,
      title: form.value.title,
      content: form.value.content,
      tags: form.value.tags,
    };
    const editRes = await PostControllerService.editPostUsingPost(editPayload);
    if (editRes.code === 0) {
      message.success("保存成功");
      router.push(`/post/view/${editPostId.value}`);
    } else {
      message.error("保存失败：" + editRes.message);
    }
    return;
  }

  const addRes = await PostControllerService.addPostUsingPost(form.value);
  if (addRes.code === 0) {
    message.success("发布成功");
    router.push(`/post/view/${addRes.data}`);
  } else {
    message.error("发布失败：" + addRes.message);
  }
};

onMounted(() => {
  loadPostForEdit();
});
</script>

<style scoped>
#createPostView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
