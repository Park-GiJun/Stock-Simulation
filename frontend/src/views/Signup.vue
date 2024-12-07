<template>
  <div class="min-h-screen bg-white flex items-center justify-center">
    <div class="max-w-md w-full p-8">
      <h2 class="text-3xl font-bold text-center text-gray-800 mb-8">Sign Up</h2>
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
              type="text"
              v-model="username"
              placeholder="Username"
              class="input-field"
          >
        </div>
        <div>
          <input
              type="email"
              v-model="email"
              placeholder="Email"
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
          Sign Up
        </button>
      </form>
      <p class="mt-4 text-center text-gray-600">
        Already have an account?
        <router-link to="/login" class="text-primary hover:underline">
          Login
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/axios';

export default {
  setup() {
    const router = useRouter();
    const userId = ref('');
    const username = ref('');
    const email = ref('');
    const password = ref('');

    const handleSubmit = async () => {
      try {
        const response = await api.post('/auth/signup', {
          userId: userId.value,
          username: username.value,
          email: email.value,
          password: password.value,
        });

        if (response.success) {
          router.push('/login');
        }
      } catch (error) {
        console.error('Signup failed:', error);
      }
    };

    return {
      userId,
      username,
      email,
      password,
      handleSubmit,
    };
  }
};
</script>
