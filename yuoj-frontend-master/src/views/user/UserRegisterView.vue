<template>
  <div id="userRegisterView">
    <h2 style="margin-bottom: 16px">用户注册</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-tabs type="capsule" v-model:active-key="type">
        <a-tab-pane key="account" title="账号注册">
          <a-form-item field="userAccount" label="账号">
            <a-input v-model="form.userAccount" placeholder="请输入账号" />
          </a-form-item>
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
        <a-tab-pane key="phone" title="手机号注册">
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

      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        tooltip="确认密码不少于 8 位"
        label="确认密码"
      >
        <a-input-password
          v-model="form.checkPassword"
          placeholder="请再次输入密码"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          注册
        </a-button>
        <a-button type="outline" href="/user/login" style="margin-left: 20px">
          已有账号？去登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { UserControllerService, UserRegisterRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import axios from "axios";

const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  email: "",
  phone: "",
  code: "",
} as any);

const router = useRouter();
const countDown = ref(0);
const type = ref("account");

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
      scene: "register",
    });
    if (res.data?.code === 0) {
      message.success("验证码发送成功");
    } else {
      message.error("发送失败，" + res.data?.message);
      countDown.value = 0;
    }
  } catch (error) {
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
      scene: "register",
    });
    if (res.data?.code === 0) {
      message.success("验证码发送成功");
    } else {
      message.error("发送失败，" + res.data?.message);
      countDown.value = 0;
    }
  } catch (error) {
    message.error("发送失败");
    countDown.value = 0;
  }
};

const handleSubmit = async () => {
  if (type.value === "account") {
    if (!form.userAccount) {
      message.error("请输入账号");
      return;
    }
    if (!form.email) {
      message.error("请输入邮箱");
      return;
    }
  } else {
    if (!form.phone) {
      message.error("请输入手机号");
      return;
    }
  }

  if (!form.code) {
    message.error("请输入验证码");
    return;
  }
  if (!form.userPassword || !form.checkPassword) {
    message.error("请输入密码");
    return;
  }
  if (form.userPassword.length < 8 || form.checkPassword.length < 8) {
    message.error("密码不少于 8 位");
    return;
  }
  if (form.userPassword !== form.checkPassword) {
    message.error("两次输入的密码不一致");
    return;
  }

  let res;
  if (type.value === "account") {
    res = await UserControllerService.userRegisterUsingPost(form);
  } else {
    res = await axios.post("/api/user/register/phone", {
      phone: form.phone,
      code: form.code,
      userPassword: form.userPassword,
      checkPassword: form.checkPassword,
    });
    // 适配 axios 返回结构
    res = res.data;
  }

  if (res.code === 0) {
    message.success("注册成功，请登录");
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    message.error("注册失败，" + res.message);
  }
};
</script>
