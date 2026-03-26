<template>
  <div id="userResetPasswordView">
    <h2 style="margin-bottom: 16px">找回密码</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-tabs type="capsule" v-model:active-key="type">
        <a-tab-pane key="email" title="邮箱找回">
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
        <a-tab-pane key="phone" title="手机号找回">
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

      <a-form-item field="newPassword" tooltip="密码不少于 8 位" label="新密码">
        <a-input-password
          v-model="form.newPassword"
          placeholder="请输入新密码"
        />
      </a-form-item>
      <a-form-item
        field="confirmPassword"
        tooltip="确认密码不少于 8 位"
        label="确认密码"
      >
        <a-input-password
          v-model="form.confirmPassword"
          placeholder="请再次输入新密码"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          重置密码
        </a-button>
        <a-button type="outline" href="/user/login" style="margin-left: 20px">
          返回登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import axios from "axios";

const form = reactive({
  email: "",
  phone: "",
  code: "",
  newPassword: "",
  confirmPassword: "",
});

const router = useRouter();
const countDown = ref(0);
const type = ref("email");

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
      scene: "reset",
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
      scene: "reset",
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
  if (type.value === "email") {
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
  if (!form.newPassword || !form.confirmPassword) {
    message.error("请输入新密码");
    return;
  }
  if (form.newPassword.length < 8) {
    message.error("密码不少于 8 位");
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    message.error("两次输入的密码不一致");
    return;
  }

  let res;
  if (type.value === "email") {
    res = await axios.post("/api/user/password/reset/email", {
      email: form.email,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    });
  } else {
    res = await axios.post("/api/user/password/reset/phone", {
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    });
  }

  if (res.data?.code === 0) {
    message.success("密码重置成功，请登录");
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    message.error("重置失败，" + res.data?.message);
  }
};
</script>

<style scoped>
#userResetPasswordView {
  max-width: 480px;
  margin: 0 auto;
}
</style>
