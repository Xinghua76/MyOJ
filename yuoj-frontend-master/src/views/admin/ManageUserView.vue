<template>
  <div class="manage-user">
    <a-card title="用户管理">
      <a-form :model="searchParams" layout="inline" style="margin-bottom: 20px">
        <a-form-item field="userName" label="用户名">
          <a-input v-model="searchParams.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-table
        :data="dataList"
        :pagination="{
          showTotal: true,
          pageSize: searchParams.pageSize,
          current: searchParams.current,
          total: total,
        }"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" />
          <a-table-column title="账号" data-index="userAccount" />
          <a-table-column title="用户名" data-index="userName" />
          <a-table-column title="角色" data-index="userRole">
            <template #cell="{ record }">
              <a-tag v-if="record.userRole === 'admin'" color="red">
                管理员
              </a-tag>
              <a-tag v-else-if="record.userRole === 'ban'" color="gray">
                已封号
              </a-tag>
              <a-tag v-else color="green"> 普通用户 </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime">
            <template #cell="{ record }">
              {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
            </template>
          </a-table-column>
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-button
                type="primary"
                size="small"
                style="margin-left: 8px"
                @click="openEditModal(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                content="确定要删除该用户吗？"
                @ok="handleDelete(record)"
              >
                <a-button status="danger" size="small" style="margin-left: 8px">
                  删除
                </a-button>
              </a-popconfirm>
              <a-button
                v-if="record.userRole !== 'admin' && record.userRole !== 'ban'"
                status="warning"
                size="small"
                style="margin-left: 8px"
                @click="handleBan(record)"
              >
                封号
              </a-button>
              <a-button
                v-if="record.userRole === 'ban'"
                status="success"
                size="small"
                style="margin-left: 8px"
                @click="handleUnban(record)"
              >
                解封
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑用户弹窗 -->
    <a-modal
      v-model:visible="editVisible"
      title="编辑用户"
      @ok="handleEditSubmit"
    >
      <a-form :model="editForm">
        <a-form-item field="userName" label="用户名">
          <a-input v-model="editForm.userName" />
        </a-form-item>
        <a-form-item field="userProfile" label="简介">
          <a-textarea v-model="editForm.userProfile" />
        </a-form-item>
        <a-form-item field="userRole" label="角色">
          <a-select v-model="editForm.userRole">
            <a-option value="user">普通用户</a-option>
            <a-option value="admin">管理员</a-option>
            <a-option value="ban">封号</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  UserControllerService,
  UserQueryRequest,
  User,
  UserUpdateRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const dataList = ref<User[]>([]);
const total = ref(0);
const searchParams = ref<UserQueryRequest>({
  userName: "",
  pageSize: 10,
  current: 1,
});

// 编辑相关
const editVisible = ref(false);
const editForm = ref<UserUpdateRequest>({});

const loadData = async () => {
  const res = await UserControllerService.listUserByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadData();
};

const openEditModal = (user: User) => {
  editForm.value = {
    id: user.id,
    userName: user.userName,
    userProfile: user.userProfile,
    userRole: user.userRole,
  };
  editVisible.value = true;
};

const handleEditSubmit = async () => {
  const res = await UserControllerService.updateUserUsingPost(editForm.value);
  if (res.code === 0) {
    message.success("修改成功");
    editVisible.value = false;
    loadData();
  } else {
    message.error("修改失败: " + res.message);
  }
};

const handleDelete = async (user: User) => {
  const res = await UserControllerService.deleteUserUsingPost({ id: user.id });
  if (res.code === 0) {
    message.success("删除成功");
    loadData();
  } else {
    message.error("删除失败，" + res.message);
  }
};

const handleBan = async (user: User) => {
  const res = await UserControllerService.updateUserUsingPost({
    id: user.id,
    userRole: "ban",
  });
  if (res.code === 0) {
    message.success("封号成功");
    loadData();
  } else {
    message.error("操作失败: " + res.message);
  }
};

const handleUnban = async (user: User) => {
  const res = await UserControllerService.updateUserUsingPost({
    id: user.id,
    userRole: "user",
  });
  if (res.code === 0) {
    message.success("解封成功");
    loadData();
  } else {
    message.error("操作失败: " + res.message);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.manage-user {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
