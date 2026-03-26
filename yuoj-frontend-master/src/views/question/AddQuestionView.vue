<template>
  <div id="addQuestionView">
    <a-page-header
      :title="updatePage ? '更新题目' : '创建题目'"
      @back="router.back()"
      :show-back="updatePage"
      style="margin-bottom: 24px; padding: 0"
    ></a-page-header>
    <a-form :model="form" layout="vertical" auto-label-width>
      <a-space direction="vertical" size="large" fill>
        <a-card title="基本信息" :bordered="false" class="section-card">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item field="title" label="题目名称">
                <a-input v-model="form.title" placeholder="请输入标题" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item field="tags" label="题目标签">
                <a-select
                  v-model="form.tags"
                  :options="TAG_OPTIONS"
                  placeholder="请选择标签"
                  multiple
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-form-item field="content" label="题目描述">
            <MdEditor :value="form.content" :handle-change="onContentChange" />
          </a-form-item>
          <a-form-item field="answer" label="参考答案">
            <MdEditor :value="form.answer" :handle-change="onAnswerChange" />
          </a-form-item>
        </a-card>

        <a-card title="判题配置" :bordered="false" class="section-card">
          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item field="judgeConfig.timeLimit" label="时间限制 (ms)">
                <a-input-number
                  v-model="form.judgeConfig.timeLimit"
                  placeholder="请输入时间限制"
                  mode="button"
                  min="0"
                  size="large"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                field="judgeConfig.memoryLimit"
                label="内存限制 (KB)"
              >
                <a-input-number
                  v-model="form.judgeConfig.memoryLimit"
                  placeholder="请输入内存限制"
                  mode="button"
                  min="0"
                  size="large"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item field="judgeConfig.stackLimit" label="堆栈限制 (KB)">
                <a-input-number
                  v-model="form.judgeConfig.stackLimit"
                  placeholder="请输入堆栈限制"
                  mode="button"
                  min="0"
                  size="large"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>

        <a-card title="测试用例配置" :bordered="false" class="section-card">
          <div
            v-for="(judgeCaseItem, index) of form.judgeCase"
            :key="index"
            class="judge-case-item"
          >
            <a-space direction="vertical" fill style="margin-bottom: 16px">
              <a-row :gutter="24" align="center">
                <a-col :span="10">
                  <a-form-item
                    :field="`form.judgeCase[${index}].input`"
                    :label="`输入用例 ${index + 1}`"
                    :key="index"
                  >
                    <a-textarea
                      v-model="judgeCaseItem.input"
                      placeholder="请输入测试输入用例"
                      :auto-size="{ minRows: 2, maxRows: 5 }"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="10">
                  <a-form-item
                    :field="`form.judgeCase[${index}].output`"
                    :label="`输出用例 ${index + 1}`"
                    :key="index"
                  >
                    <a-textarea
                      v-model="judgeCaseItem.output"
                      placeholder="请输入测试输出用例"
                      :auto-size="{ minRows: 2, maxRows: 5 }"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="4">
                  <a-button
                    status="danger"
                    @click="handleDelete(index)"
                    shape="circle"
                  >
                    <icon-delete />
                  </a-button>
                </a-col>
              </a-row>
            </a-space>
          </div>
          <a-button @click="handleAdd" type="dashed" status="success" long>
            <icon-plus /> 新增测试用例
          </a-button>
        </a-card>

        <div style="margin-top: 16px; text-align: center">
          <a-button
            type="primary"
            size="large"
            style="min-width: 200px"
            @click="doSubmit"
          >
            提交
          </a-button>
        </div>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";
import { IconDelete, IconPlus } from "@arco-design/web-vue/es/icon";

const route = useRoute();
const router = useRouter();

const TAG_OPTIONS = [
  { label: "数组", value: "数组" },
  { label: "字符串", value: "字符串" },
  { label: "哈希表", value: "哈希表" },
  { label: "栈", value: "栈" },
  { label: "队列", value: "队列" },
  { label: "链表", value: "链表" },
  { label: "二叉树", value: "二叉树" },
  { label: "图", value: "图" },
  { label: "DFS", value: "DFS" },
  { label: "BFS", value: "BFS" },
  { label: "动态规划", value: "动态规划" },
  { label: "贪心", value: "贪心" },
  { label: "双指针", value: "双指针" },
  { label: "排序", value: "排序" },
  { label: "数学", value: "数学" },
];

// 如果页面地址包含 update，则视为更新页面
const updatePage = route.path.includes("update");

let form = ref({
  title: "",
  tags: [],
  answer: "",
  content: "",
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
});

const loadData = async () => {
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as any
  );
  if (res.code === 0) {
    form.value = res.data as any;
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
      };
    } else {
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      form.value.tags = JSON.parse(form.value.tags as any);
    }
  } else {
    message.error("加载失败，" + res.message);
  }
};

onMounted(() => {
  loadData();
});

const doSubmit = async () => {
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("更新成功");
      router.push({
        path: `/view/question/${form.value.id}`,
        replace: true,
      });
    } else {
      message.error("更新失败，" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("创建成功");
      // 跳转到做题页面
      router.push({
        path: `/view/question/${res.data}`,
        replace: true,
      });
    } else {
      message.error("创建失败，" + res.message);
    }
  }
};

const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
</script>

<style scoped>
#addQuestionView {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
