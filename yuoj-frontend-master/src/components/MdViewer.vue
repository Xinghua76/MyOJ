<template>
  <MdPreview :editorId="id" :modelValue="value" />
</template>

<script setup lang="ts">
import { MdPreview } from "md-editor-v3";
import "md-editor-v3/lib/style.css";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
}

/**
 * 给组件指定初始值
 */
withDefaults(defineProps<Props>(), {
  value: () => "",
});

const id = "preview-only";
</script>

<style></style>
