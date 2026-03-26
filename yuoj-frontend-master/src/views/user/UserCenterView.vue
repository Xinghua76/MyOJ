<template>
  <div class="user-center">
    <a-tabs default-active-key="info">
      <a-tab-pane key="info" title="个人信息">
        <a-row :gutter="24" justify="center">
          <a-col :span="12">
            <a-card title="个人信息">
              <template #extra>
                <a-button type="text" @click="router.back()">返回</a-button>
              </template>
              <div class="avatar-container">
                <a-upload
                  action="/"
                  :custom-request="uploadAvatar"
                  :show-file-list="false"
                >
                  <template #upload-button>
                    <div class="avatar-wrapper">
                      <a-avatar :size="100" shape="circle" class="avatar-hover">
                        <img
                          v-if="form.userAvatar"
                          :src="form.userAvatar"
                          alt="Avatar"
                        />
                        <span v-else>{{ loginUser.userName?.charAt(0) }}</span>
                        <div class="avatar-mask">
                          <icon-camera />
                        </div>
                      </a-avatar>
                    </div>
                  </template>
                </a-upload>
              </div>
              <a-divider />
              <a-form
                :model="form"
                @submit="handleUpdate"
                auto-label-width
                layout="vertical"
              >
                <a-form-item field="userName" label="昵称">
                  <a-input v-model="form.userName" placeholder="请输入昵称" />
                </a-form-item>
                <a-form-item field="gender" label="性别">
                  <a-radio-group v-model="form.gender">
                    <a-radio :value="1">男</a-radio>
                    <a-radio :value="2">女</a-radio>
                    <a-radio :value="0">保密</a-radio>
                  </a-radio-group>
                </a-form-item>
                <a-form-item field="userProfile" label="个人简介">
                  <a-textarea
                    v-model="form.userProfile"
                    placeholder="请输入个人简介"
                    :auto-size="{ minRows: 3, maxRows: 5 }"
                  />
                </a-form-item>
                <a-form-item>
                  <a-space fill style="width: 100%">
                    <a-button type="primary" html-type="submit" long
                      >保存修改</a-button
                    >
                    <a-button
                      type="outline"
                      long
                      @click="passwordVisible = true"
                      >修改密码</a-button
                    >
                  </a-space>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>
      <a-tab-pane key="solutions" title="我的讨论">
        <a-list
          :data="mySolutions"
          :pagination-props="paginationProps"
          @page-change="onPageChange"
        >
          <template #item="{ item }">
            <a-list-item
              action-layout="vertical"
              class="clickable"
              @click="toPostDetail(item)"
            >
              <a-list-item-meta
                :title="item.title"
                :description="item.content.substring(0, 100) + '...'"
              >
                <template #description>
                  <div>
                    <a-tag v-for="tag in item.tags" :key="tag" color="green">{{
                      tag
                    }}</a-tag>
                    <span style="margin-left: 8px; color: #888">
                      {{ moment(item.createTime).format("YYYY-MM-DD HH:mm") }}
                    </span>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
    </a-tabs>
    <a-modal
      v-model:visible="passwordVisible"
      title="修改密码"
      @ok="handlePasswordUpdate"
      @cancel="passwordVisible = false"
    >
      <a-form :model="passwordForm">
        <a-form-item field="userPassword" label="原密码">
          <a-input-password
            v-model="passwordForm.userPassword"
            placeholder="请输入原密码"
          />
        </a-form-item>
        <a-form-item field="newPassword" label="新密码">
          <a-input-password
            v-model="passwordForm.newPassword"
            placeholder="请输入新密码"
          />
        </a-form-item>
        <a-form-item field="confirmPassword" label="确认密码">
          <a-input-password
            v-model="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import {
  FileControllerService,
  UserControllerService,
  UserUpdateMyRequest,
  PostVO,
  PostControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { IconCamera } from "@arco-design/web-vue/es/icon";
import moment from "moment";
import axios from "axios";

const router = useRouter();
const store = useStore();
const loginUser = computed(() => store.state.user.loginUser);

const mySolutions = ref<PostVO[]>([]);
const paginationProps = ref({
  pageSize: 10,
  current: 1,
  total: 0,
  showTotal: true,
});

const loadMySolutions = async () => {
  const res = await PostControllerService.listMyPostVoByPageUsingPost({
    current: paginationProps.value.current,
    pageSize: paginationProps.value.pageSize,
  });
  if (res.code === 0) {
    mySolutions.value = res.data.records;
    paginationProps.value.total = res.data.total;
  }
};

const toPostDetail = (post: PostVO) => {
  router.push({
    path: `/post/view/${post.id}`,
  });
};

const onPageChange = (page: number) => {
  paginationProps.value.current = page;
  loadMySolutions();
};

const form = ref<UserUpdateMyRequest>({
  userName: "",
  userProfile: "",
  userAvatar: "",
  gender: 0,
});

const passwordVisible = ref(false);
const passwordForm = reactive({
  userPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const handlePasswordUpdate = async () => {
  if (
    !passwordForm.userPassword ||
    !passwordForm.newPassword ||
    !passwordForm.confirmPassword
  ) {
    message.error("请输入所有密码字段");
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error("两次输入的新密码不一致");
    return;
  }
  try {
    const res = await axios.post("/api/user/update/password", passwordForm);
    if (res.data.code === 0) {
      message.success("密码修改成功");
      passwordVisible.value = false;
      // clear form
      passwordForm.userPassword = "";
      passwordForm.newPassword = "";
      passwordForm.confirmPassword = "";
    } else {
      message.error("修改失败，" + res.data.message);
    }
  } catch (e: any) {
    message.error("修改失败，" + (e.response?.data?.message || e.message));
  }
};

onMounted(() => {
  // 初始化表单数据
  if (loginUser.value) {
    form.value.userName = loginUser.value.userName;
    form.value.userProfile = loginUser.value.userProfile;
    form.value.userAvatar = loginUser.value.userAvatar;
    form.value.gender = loginUser.value.gender ?? 0;
  }
  loadMySolutions();
});

const uploadAvatar = async (option: any) => {
  const { onError, onSuccess, fileItem } = option;
  try {
    const res = await FileControllerService.uploadFileUsingPost(
      "user_avatar",
      fileItem.file
    );
    if (res.code === 0) {
      form.value.userAvatar = res.data;
      onSuccess(res);
    } else {
      message.error("上传失败，" + res.message);
      onError(res);
    }
  } catch (error) {
    onError(error);
  }
};

const handleUpdate = async () => {
  const res = await UserControllerService.updateMyUserUsingPost(form.value);
  if (res.code === 0) {
    message.success("更新成功");
    await store.dispatch("user/getLoginUser");
  } else {
    message.error("更新失败，" + res.message);
  }
};
</script>

<style scoped>
.clickable {
  cursor: pointer;
}
</style>

<style scoped>
.user-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
}
.avatar-hover:hover .avatar-mask {
  opacity: 1;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.3s;
}
</style>
