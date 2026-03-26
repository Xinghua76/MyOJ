import { createApp } from "vue";
import App from "./App.vue";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import router from "./router";
import store from "./store";
import "@/plugins/axios";
import "@/access";
import "bytemd/dist/index.css";
import "github-markdown-css/github-markdown.css";

const debounce = (fn: any, delay: number) => {
  let timer: any = null;
  return (...args: any[]) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn(...args);
    }, delay);
  };
};

const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver {
  constructor(callback: any) {
    callback = debounce(callback, 16);
    super(callback);
  }
};

createApp(App).use(ArcoVue).use(store).use(router).mount("#app");
