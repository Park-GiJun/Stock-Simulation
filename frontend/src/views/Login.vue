// src/views/Login.vue
<template>
  <div class="min-h-screen bg-white flex items-center justify-center">
    <div class="max-w-md w-full p-8">
      <h2 class="text-3xl font-bold text-center text-gray-800 mb-8">Login</h2>
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div>
          <input
              type="text"
              v-model="userId"
              placeholder="User ID"
              class="input-field"
          >
        </div>
        <div>
          <input
              type="password"
              v-model="password"
              placeholder="Password"
              class="input-field"
          >
        </div>
        <button type="submit" class="btn btn-primary w-full">
          Login
        </button>
      </form>
      <p class="mt-4 text-center text-gray-600">
        Don't have an account?
        <router-link to="/signup" class="text-primary hover:underline">
          Sign up
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/axios';
import { useUserStore } from '../store/user';

export default {
  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const userId = ref('');
    const password = ref('');

    const handleSubmit = async () => {
      try {
        const response = await api.post('/auth/login', {
          userId: userId.value,
          password: password.value,
        });

        if (response.success) {
          localStorage.setItem('accessToken', response.data.accessToken);
          userStore.setUserId(userId.value);
          router.push('/dashboard');
        }
      } catch (error) {
        console.error('Login failed:', error);
      }
    };

    return {
      userId,
      password,
      handleSubmit,
    };
  }
};
</script>