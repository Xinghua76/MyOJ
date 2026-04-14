<template>
  <MdEditor
    :model-value="value"
    :theme="theme"
    @onChange="handleChange"
    @onUploadImg="onUploadImg"
  />
</template>

<script setup lang="ts">
import { MdEditor } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import { FileControllerService } from "../../generated";
import message from "@arco-design/web-vue/es/message";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  mode?: string;
  handleChange: (v: string) => void;
  theme?: string;
}

/**
 * 给组件指定初始值
 */
withDefaults(defineProps<Props>(), {
  value: () => "",
  mode: () => "split",
  handleChange: (v: string) => {
    console.log(v);
  },
  theme: "light",
});

/**
 * 上传图片
 * @param files
 * @param callback
 */
const onUploadImg = async (
  files: Array<File>,
  callback: (urls: Array<string>) => void
) => {
  const res = await Promise.all(
    files.map((file) => {
      return FileControllerService.uploadFileUsingPost("post_content", file);
    })
  );
  const urls = res.map((item) => {
    if (item.code === 0) {
      return item.data;
    } else {
      message.error("上传失败，" + item.message);
      return "";
    }
  });
  callback(urls.filter((url) => !!url));
};
</script>

<style>
/* 可以在这里覆盖 md-editor-v3 的样式 */
</style>
