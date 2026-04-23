<template>
  <div id="homeView">
    <section class="hero">
      <div class="hero-content">
        <div class="hero-title">码上练编程学习平台</div>
        <div class="hero-subtitle">
          刷题、竞赛、讨论一体化，专注算法训练与实战成长。
        </div>
        <a-space wrap>
          <a-button type="primary" size="large" @click="goTo('/questions')">
            开始刷题
          </a-button>
          <a-button size="large" @click="goTo('/contests')">查看竞赛</a-button>
        </a-space>
      </div>
      <div class="hero-stats">
        <div class="stat-card">
          <div class="stat-label">已发布公告</div>
          <div class="stat-value">{{ noticeList.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">快捷模块</div>
          <div class="stat-value">3</div>
        </div>
      </div>
    </section>

    <section class="main-grid">
      <a-card class="notice-card" :bordered="false">
        <template #title>
          <div class="section-title-wrap">
            <span class="section-title">系统公告</span>
            <span class="section-subtitle">仅展示已发布内容</span>
          </div>
        </template>

        <a-empty v-if="noticeList.length === 0" description="暂无公告" />

        <a-timeline v-else>
          <a-timeline-item v-for="notice in noticeList" :key="notice.id">
            <div class="notice-item-title">{{ notice.title }}</div>
            <div class="notice-item-content">{{ notice.content }}</div>
            <div class="notice-item-time">
              {{ moment(notice.publishTime).format("YYYY-MM-DD HH:mm") }}
            </div>
          </a-timeline-item>
        </a-timeline>
      </a-card>

      <div class="side-panel">
        <a-card class="quick-card" :bordered="false">
          <template #title>
            <div class="section-title-wrap">
              <span class="section-title">快速入口</span>
              <span class="section-subtitle">常用功能直达</span>
            </div>
          </template>

          <div class="quick-list">
            <button class="quick-item" @click="goTo('/questions')">
              <div class="quick-item-title">浏览题目</div>
              <div class="quick-item-desc">按标签、难度筛选题目并在线提交</div>
            </button>
            <button class="quick-item" @click="goTo('/contests')">
              <div class="quick-item-title">比赛列表</div>
              <div class="quick-item-desc">查看进行中与历史竞赛，参与排名</div>
            </button>
            <button class="quick-item" @click="goTo('/posts')">
              <div class="quick-item-title">讨论区</div>
              <div class="quick-item-desc">分享题解、交流思路与常见陷阱</div>
            </button>
          </div>
        </a-card>

        <a-card class="tips-card" :bordered="false" title="今日建议">
          <ul class="tips-list">
            <li>先做 1 道简单题热身，再挑战中等难度。</li>
            <li>提交失败后先看判题信息，再针对性修改。</li>
            <li>遇到卡点可在讨论区搜索同类题解思路。</li>
          </ul>
        </a-card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { NoticeControllerService } from "../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { useRouter } from "vue-router";

const router = useRouter();
const noticeList = ref<any[]>([]);

const goTo = (path: string) => {
  router.push(path);
};

const loadNotice = async () => {
  const res = await NoticeControllerService.listPageUsingPost({
    pageSize: 6,
    current: 1,
  });
  if (res.code === 0) {
    noticeList.value = (res.data.records || []).filter(
      (item: any) => item.status === 1
    );
  } else {
    message.error("公告加载失败，" + res.message);
  }
};

onMounted(() => {
  loadNotice();
});
</script>

<style scoped>
#homeView {
  --surface: #ffffff;
  --line: #e5e6eb;
  --text-main: #1d2129;
  --text-sub: #4e5969;
  --text-light: #86909c;
  max-width: 1200px;
  margin: 0 auto;
  padding: 8px 4px 20px;
}

.hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.hero-content {
  background: linear-gradient(120deg, #e8f3ff 0%, #f6fbff 45%, #ffffff 100%);
  border: 1px solid #dbe8ff;
  border-radius: 14px;
  padding: 28px 24px;
}

.hero-title {
  color: var(--text-main);
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 10px;
}

.hero-subtitle {
  color: var(--text-sub);
  font-size: 15px;
  margin-bottom: 18px;
}

.hero-stats {
  display: grid;
  gap: 12px;
}

.stat-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-label {
  color: var(--text-light);
  font-size: 13px;
  margin-bottom: 8px;
}

.stat-value {
  color: #165dff;
  font-size: 34px;
  font-weight: 700;
}

.main-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.notice-card {
  border-radius: 14px;
  background: var(--surface);
}

.section-title-wrap {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  width: 100%;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-main);
}

.section-subtitle {
  font-size: 12px;
  color: var(--text-light);
}

.notice-item-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
}

.notice-item-content {
  color: #4e5969;
  line-height: 1.6;
  margin-bottom: 6px;
  white-space: pre-wrap;
}

.notice-item-time {
  color: var(--text-light);
  font-size: 12px;
}

.side-panel {
  display: grid;
  gap: 16px;
}

.quick-card,
.tips-card {
  border-radius: 14px;
}

.quick-list {
  display: grid;
  gap: 10px;
}

.quick-item {
  text-align: left;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-item:hover {
  border-color: #91b4ff;
  background: #f5f9ff;
  transform: translateY(-1px);
}

.quick-item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-main);
  margin-bottom: 4px;
}

.quick-item-desc {
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.5;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-sub);
  line-height: 1.8;
}

@media (max-width: 992px) {
  .hero,
  .main-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 24px;
  }
}
</style>
