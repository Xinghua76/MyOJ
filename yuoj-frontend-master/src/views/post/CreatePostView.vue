<template>
  <div id="createPostView">
    <a-card title="创建帖子">
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
            >发布</a-button
          >
          <a-button
            type="outline"
            status="danger"
            style="min-width: 150px; margin-left: 20px"
            @click="router.back()"
            >取消</a-button
          >
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { PostAddRequest, PostControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import MdEditor from "@/components/MdEditor.vue";

const router = useRouter();

const form = ref<PostAddRequest>({
  title: "",
  content: "",
  tags: [],
});

const onContentChange = (v: string) => {
  form.value.content = v;
};

const formRef = ref();

const handleSubmit = async () => {
  // 手动触发校验
  // validate 方法返回 undefined 表示校验通过，返回错误对象表示失败（Arco Design Vue 2.x）
  // 或者 validate((errors) => {})
  // 查阅文档：validate() returns Promise<Record<string, ValidatedError> | undefined>
  const errors = await formRef.value?.validate();
  if (errors) {
    return;
  }
  if (!form.value.title || !form.value.content) {
    message.error("请填写标题和内容");
    return;
  }
  const res = await PostControllerService.addPostUsingPost(form.value);
  if (res.code === 0) {
    message.success("发布成功");
    router.push(`/post/view/${res.data}`);
  } else {
    message.error("发布失败，" + res.message);
  }
};
</script>

<style scoped>
#createPostView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
