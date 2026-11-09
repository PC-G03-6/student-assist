<script setup lang="ts">
import { RouterView } from 'vue-router'
import BottomNav from './components/ui/BottomNav.vue'
</script>

<template>
  <div class="app-layout">
    <main class="main-content">
      <RouterView />
    </main>
    <BottomNav />
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>
