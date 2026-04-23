import { RouteRecordRaw } from "vue-router";
import UserLayout from "@/layouts/UserLayout.vue";
import UserLoginView from "@/views/user/UserLoginView.vue";
import UserRegisterView from "@/views/user/UserRegisterView.vue";
import UserResetPasswordView from "@/views/user/UserResetPasswordView.vue";
import NoAuthView from "@/views/NoAuthView.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import AddQuestionView from "@/views/question/AddQuestionView.vue";
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import QuestionsView from "@/views/question/QuestionsView.vue";
import QuestionSubmitView from "@/views/question/QuestionSubmitView.vue";
import ViewQuestionView from "@/views/question/ViewQuestionView.vue";
import PostListView from "@/views/post/PostListView.vue";
import UserCenterView from "@/views/user/UserCenterView.vue";
import HistoryView from "@/views/user/HistoryView.vue";
import FavourView from "@/views/user/FavourView.vue";
import ManageUserView from "@/views/admin/ManageUserView.vue";
import ManagePostView from "@/views/admin/ManagePostView.vue";
import PostDetailView from "@/views/post/PostDetailView.vue";
import HomeView from "@/views/HomeView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "/user/login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "/user/register",
        name: "用户注册",
        component: UserRegisterView,
      },
      {
        path: "/user/reset-password",
        name: "找回密码",
        component: UserResetPasswordView,
      },
      {
        path: "/user/center",
        name: "个人中心",
        component: UserCenterView,
        meta: {
          access: ACCESS_ENUM.USER,
        },
      },
      {
        path: "/user/history",
        name: "历史记录",
        component: HistoryView,
        meta: {
          access: ACCESS_ENUM.USER,
        },
      },
      {
        path: "/user/favour",
        name: "我的收藏",
        component: FavourView,
        meta: {
          access: ACCESS_ENUM.USER,
        },
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/contests",
    name: "比赛列表",
    component: () => import("@/views/contest/ContestListView.vue"),
  },
  {
    path: "/contest/detail/:id",
    name: "比赛详情",
    component: () => import("@/views/contest/ContestDetailView.vue"),
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/contest/:contestId/solve/:questionId?",
    name: "比赛做题",
    component: () => import("@/views/contest/ContestDoQuestionView.vue"),
    props: true,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    },
  },
  {
    path: "/questions",
    name: "浏览题目",
    component: QuestionsView,
  },
  {
    path: "/question_submit",
    name: "浏览题目提交",
    component: QuestionSubmitView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/posts",
    name: "讨论区",
    component: PostListView,
  },
  {
    path: "/post/create",
    name: "创建帖子",
    component: () => import("@/views/post/CreatePostView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/post/view/:id",
    name: "帖子详情",
    component: PostDetailView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/view/question/:id",
    name: "在线做题",
    component: ViewQuestionView,
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/add/question",
    name: "创建题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/update/question",
    name: "更新题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
      hideInMenu: true,
    },
  },
  {
    path: "/manage/question/",
    name: "管理题目",
    component: ManageQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/manage/user",
    name: "用户管理",
    component: ManageUserView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/manage/post",
    name: "讨论管理",
    component: ManagePostView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/manage/contest",
    name: "竞赛管理",
    component: () => import("@/views/admin/ManageContestView.vue"),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/manage/notice",
    name: "公告管理",
    component: () => import("@/views/admin/ManageNoticeView.vue"),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/op_log",
    name: "操作日志",
    component: () => import("@/views/admin/AdminOpLogView.vue"),
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/",
    name: "主页",
    component: HomeView,
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    },
  },
];
