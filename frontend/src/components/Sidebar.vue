<!-- src/components/Sidebar.vue -->
<template>
  <div class="h-screen w-64 bg-white border-r border-gray-200 fixed left-0 top-0">
    <div class="p-4 flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-800">Stock Trading</h1>
    </div>

    <!-- User Info -->
    <div class="px-4 py-3 border-b border-gray-200">
      <p class="text-sm text-gray-600">Welcome,</p>
      <p class="font-medium text-gray-800">{{ userStore.userId }}</p>
    </div>

    <nav class="mt-8">
      <!-- Dashboard -->
      <router-link
          to="/dashboard"
          class="block px-4 py-2 text-gray-600 hover:bg-gray-100 transition-colors duration-200"
          :class="{ 'bg-gray-100': $route.path === '/dashboard' }"
      >
        <div class="flex items-center space-x-2">
          <svg
              class="w-5 h-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
          <span>Dashboard</span>
        </div>
      </router-link>

      <!-- Stock View -->
      <router-link
          to="/stockview"
          class="block px-4 py-2 text-gray-600 hover:bg-gray-100 transition-colors duration-200"
          :class="{ 'bg-gray-100': $route.path === '/stockview' }"
      >
        <div class="flex items-center space-x-2">
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M3 3h18M3 9h18M3 15h18M3 21h18"
            />
          </svg>
          <span>Stock View</span>
        </div>
      </router-link>

      <!-- Logout Button -->
      <div
          @click="handleLogout"
          class="block px-4 py-2 text-gray-600 hover:bg-gray-100 cursor-pointer transition-colors duration-200 mt-auto absolute bottom-8 w-full"
      >
        <div class="flex items-center space-x-2">
          <svg
              class="w-5 h-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
            />
          </svg>
          <span>Logout</span>
        </div>
      </div>
    </nav>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';

export default {
  setup() {
    const router = useRouter();
    const userStore = useUserStore();

    const handleLogout = () => {
      localStorage.removeItem('accessToken');
      userStore.clearUserId();
      router.push('/login');
    };

    return {
      userStore,
      handleLogout
    };
  }
};
</script>