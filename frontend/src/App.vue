<template>
  <div class="flex h-screen">
    <!-- Sidebar: requiresAuth가 true일 때만 표시 -->
    <Sidebar v-if="$route.meta.requiresAuth" />

    <!-- 메인 콘텐츠 영역 -->
    <div :class="{ 'flex-1 ml-64': $route.meta.requiresAuth, 'w-full': !$route.meta.requiresAuth }">
      <router-view />
    </div>
  </div>
</template>

<script>
import Sidebar from './components/Sidebar.vue';

export default {
  name: 'App',
  components: {
    Sidebar,
  },
};
</script>

<style>
/* TailwindCSS를 사용하여 스타일 적용 */
</style>
