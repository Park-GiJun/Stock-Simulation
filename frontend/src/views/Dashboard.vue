<template>
  <div class="min-h-screen bg-white">
    <Sidebar />
    <div class="ml-64 p-8">
      <h1 class="text-2xl font-bold text-gray-800 mb-6">Dashboard</h1>
      <!-- Add dashboard content here -->
    </div>
  </div>
</template>

<script>
import Sidebar from '../components/Sidebar.vue';

export default {
  components: {
    Sidebar
  }
};
</script>