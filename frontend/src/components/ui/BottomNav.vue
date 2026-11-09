<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CalendarDays, Bus, Palette } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

// Tab-ul Design System apare doar in modul de dezvoltare (npm run dev),
// nu si in build-ul final al aplicatiei.
const navItems = computed(() => [
  { label: 'Orar', path: '/schedule', icon: CalendarDays },
  { label: 'Transport', path: '/transit', icon: Bus },
  ...(import.meta.env.DEV
    ? [{ label: 'Design', path: '/design-system', icon: Palette }]
    : []),
])
</script>

<template>
  <div class="bottom-nav-wrapper">
    <nav class="bottom-dock" aria-label="Navigatie principala">
      <button
        v-for="item in navItems"
        :key="item.path"
        type="button"
        class="dock-tab"
        :class="{ active: route.path === item.path }"
        :aria-current="route.path === item.path ? 'page' : undefined"
        @click="router.push(item.path)"
      >
        <component :is="item.icon" class="tab-icon" :size="20" :stroke-width="2" />
        <span class="tab-label">{{ item.label }}</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
.bottom-nav-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding: 0 16px calc(14px + env(safe-area-inset-bottom)) 16px;
  pointer-events: none;
  z-index: 100;
}

.bottom-dock {
  pointer-events: auto;
  width: 100%;
  max-width: 400px;
  height: 60px;
  background-color: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: var(--shadow-floating);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 4px 8px;
}

.dock-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  min-height: 48px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-muted);
  font-size: 0.72rem;
  font-weight: 600;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  padding: 4px 12px;
  border-radius: var(--radius-full);
}

.tab-icon {
  transition: transform 0.2s ease;
}

.dock-tab:hover {
  color: var(--color-text-main);
}

.dock-tab.active {
  color: var(--color-text-main);
  background-color: var(--color-selected-bg);
}
</style>
