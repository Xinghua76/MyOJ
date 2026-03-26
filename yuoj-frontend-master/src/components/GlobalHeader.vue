<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img class="logo" src="../assets/oj-logo.svg" />
            <div class="title">MY OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="120px">
      <div v-if="store.state.user?.loginUser?.userName !== '未登录'">
        <a-dropdown trigger="hover">
          <div style="cursor: pointer; display: flex; align-items: center">
            <a-avatar
              :size="24"
              :style="{ marginRight: '8px' }"
              v-if="store.state.user.loginUser.userAvatar"
            >
              <img :src="store.state.user.loginUser.userAvatar" />
            </a-avatar>
            <a-avatar :size="24" :style="{ marginRight: '8px' }" v-else>
              {{ store.state.user.loginUser.userName?.charAt(0) }}
            </a-avatar>
            {{ store.state.user.loginUser.userName }}
          </div>
          <template #content>
            <a-doption @click="toUserCenter">
              <template #icon><icon-user /></template>
              个人中心
            </a-doption>
            <a-doption @click="toHistory">
              <template #icon><icon-history /></template>
              历史记录
            </a-doption>
            <a-doption @click="toFavour">
              <template #icon><icon-star /></template>
              我的收藏
            </a-doption>
            <a-doption @click="handleLogout">
              <template #icon><icon-export /></template>
              退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
      <div v-else style="display: flex; align-items: center">
        <a-button type="primary" href="/user/login" style="margin-right: 8px">
          登录
        </a-button>
        <a-button type="outline" href="/user/register">注册</a-button>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "../router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import { UserControllerService } from "../../generated";
import message from "@arco-design/web-vue/es/message";
import {
  IconUser,
  IconExport,
  IconHistory,
  IconStar,
} from "@arco-design/web-vue/es/icon";

const router = useRouter();
const store = useStore();

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  const loginUser = store.state.user.loginUser;
  return routes.filter((item) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (!checkAccess(loginUser, item?.meta?.access as string)) {
      return false;
    }
    // 如果是管理员，仅展示管理相关的菜单（path 包含 /manage/ 或 /admin/），或者是“我的”相关页面（如果在路由中没有被 hideInMenu 过滤）
    // 这里简单约定：管理员只看 /manage/ 或 /admin/ 开头的路由，以及主页等公共路由？
    // 用户需求是“管理员用户登录后专注系统管理，去除与管理无关的菜单”
    // 假设管理员不需要看“浏览题目”、“比赛列表”等普通用户功能
    if (loginUser && loginUser.userRole === "admin") {
      // 保留管理相关路由
      if (item.path.startsWith("/manage") || item.path.startsWith("/admin")) {
        return true;
      }
      // 过滤掉普通用户的功能路由（如 /questions, /contests, /add/question 等）
      // 但可能需要保留一些公共页面？比如主页？
      // 如果主页是 /questions，那么管理员登录后主页变成什么？
      // 暂时策略：管理员只显示管理菜单。
      return false;
    }

    return true;
  });
});

// 默认主页
const selectedKeys = ref(["/"]);

// 路由跳转后，更新选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const toUserCenter = () => {
  router.push("/user/center");
};

const toHistory = () => {
  router.push("/user/history");
};

const toFavour = () => {
  router.push("/user/favour");
};

const handleLogout = async () => {
  const res = await UserControllerService.userLogoutUsingPost();
  if (res.code === 0) {
    message.success("已退出登录");
    store.dispatch("user/getLoginUser");
    router.push("/user/login");
  } else {
    message.error("退出失败，" + res.message);
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #444;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
