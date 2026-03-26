<template>
  <div id="userLoginView">
    <h2 style="margin-bottom: 16px">用户登录</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-tabs type="capsule" v-model:active-key="type">
        <a-tab-pane key="account" title="账号登录">
          <a-form-item field="userAccount" label="账号">
            <a-input v-model="form.userAccount" placeholder="请输入账号" />
          </a-form-item>
          <a-form-item
            field="userPassword"
            tooltip="密码不少于 8 位"
            label="密码"
          >
            <a-input-password
              v-model="form.userPassword"
              placeholder="请输入密码"
            />
          </a-form-item>
        </a-tab-pane>
        <a-tab-pane key="email" title="邮箱登录">
          <a-form-item field="email" label="邮箱">
            <a-input v-model="form.email" placeholder="请输入邮箱" />
          </a-form-item>
          <a-form-item field="code" label="验证码">
            <a-input v-model="form.code" placeholder="请输入验证码" />
            <a-button
              type="primary"
              :disabled="!!countDown"
              @click="sendCode"
              style="margin-left: 10px; width: 120px"
            >
              {{ countDown ? `${countDown}s 后重试` : "获取验证码" }}
            </a-button>
          </a-form-item>
        </a-tab-pane>
        <a-tab-pane key="phone" title="手机号登录">
          <a-form-item field="phone" label="手机号">
            <a-input v-model="form.phone" placeholder="请输入手机号" />
          </a-form-item>
          <a-form-item field="code" label="验证码">
            <a-input v-model="form.code" placeholder="请输入验证码" />
            <a-button
              type="primary"
              :disabled="!!countDown"
              @click="sendSmsCode"
              style="margin-left: 10px; width: 120px"
            >
              {{ countDown ? `${countDown}s 后重试` : "获取验证码" }}
            </a-button>
          </a-form-item>
        </a-tab-pane>
      </a-tabs>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          登录
        </a-button>
        <a-button
          type="outline"
          href="/user/register"
          style="margin-left: 20px"
        >
          没有账号？去注册
        </a-button>
        <a-button
          type="text"
          href="/user/reset-password"
          style="margin-left: 10px"
        >
          忘记密码？
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import axios from "axios";

const form = reactive({
  userAccount: "",
  userPassword: "",
  email: "",
  phone: "",
  code: "",
} as any);

const type = ref("account");
const router = useRouter();
const store = useStore();
const countDown = ref(0);

const sendCode = async () => {
  if (!form.email) {
    message.error("请输入邮箱");
    return;
  }
  countDown.value = 60;
  const intervalId = setInterval(() => {
    countDown.value--;
    if (countDown.value <= 0) {
      clearInterval(intervalId);
    }
  }, 1000);
  try {
    const res = await axios.post("/api/user/email/code/send", {
      email: form.email,
      scene: "login",
    });
    if (res.data?.code === 0) {
      message.success("验证码发送成功");
    } else {
      message.error("发送失败，" + res.data?.message);
      countDown.value = 0;
    }
  } catch (e) {
    message.error("发送失败");
    countDown.value = 0;
  }
};

const sendSmsCode = async () => {
  if (!form.phone) {
    message.error("请输入手机号");
    return;
  }
  countDown.value = 60;
  const intervalId = setInterval(() => {
    countDown.value--;
    if (countDown.value <= 0) {
      clearInterval(intervalId);
    }
  }, 1000);
  try {
    const res = await axios.post("/api/user/sms/code/send", {
      phone: form.phone,
      scene: "login",
    });
    if (res.data?.code === 0) {
      message.success("验证码发送成功");
    } else {
      message.error("发送失败，" + res.data?.message);
      countDown.value = 0;
    }
  } catch (e) {
    message.error("发送失败");
    countDown.value = 0;
  }
};

const handleSubmit = async () => {
  if (type.value === "account") {
    if (!form.userAccount || !form.userPassword) {
      message.error("请输入账号和密码");
      return;
    }
    const res = await UserControllerService.userLoginUsingPost({
      userAccount: form.userAccount,
      userPassword: form.userPassword,
    } as UserLoginRequest);
    if (res.code === 0) {
      await store.dispatch("user/getLoginUser");
      router.push({ path: "/", replace: true });
    } else {
      message.error("登录失败，" + res.message);
    }
    return;
  }

  if (type.value === "email") {
    if (!form.email || !form.code) {
      message.error("请输入邮箱和验证码");
      return;
    }
    const res = await axios.post("/api/user/login/email", {
      email: form.email,
      code: form.code,
    });
    if (res.data?.code === 0) {
      await store.dispatch("user/getLoginUser");
      router.push({ path: "/", replace: true });
    } else {
      message.error("登录失败，" + res.data?.message);
    }
    return;
  }

  if (type.value === "phone") {
    if (!form.phone || !form.code) {
      message.error("请输入手机号和验证码");
      return;
    }
    const res = await axios.post("/api/user/login/phone", {
      phone: form.phone,
      code: form.code,
    });
    if (res.data?.code === 0) {
      await store.dispatch("user/getLoginUser");
      router.push({ path: "/", replace: true });
    } else {
      message.error("登录失败，" + res.data?.message);
    }
  }
};
</script>
