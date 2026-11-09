<script setup lang="ts">
defineProps<{
  variant?: 'course' | 'seminar' | 'lab' | 'bus' | 'live' | 'neutral'
  dot?: boolean
}>()
</script>

<template>
  <span class="app-badge" :class="variant || 'neutral'">
    <span v-if="dot" class="badge-dot" />
    <slot />
  </span>
</template>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  line-height: 1;
  letter-spacing: -0.01em;
  border: 1px solid transparent;
  white-space: nowrap;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
  flex-shrink: 0;
}

.neutral {
  background-color: var(--color-surface-hover);
  color: var(--color-text-secondary);
  border-color: var(--color-border);
}

.course {
  background-color: var(--badge-curs-bg);
  color: var(--badge-curs-text);
  border-color: var(--badge-curs-border);
}

.seminar {
  background-color: var(--badge-seminar-bg);
  color: var(--badge-seminar-text);
  border-color: var(--badge-seminar-border);
}

.lab {
  background-color: var(--badge-lab-bg);
  color: var(--badge-lab-text);
  border-color: var(--badge-lab-border);
}

.bus {
  background-color: var(--badge-bus-bg);
  color: var(--badge-bus-text);
  border-color: var(--badge-bus-border);
}

.live {
  background-color: var(--status-live-bg);
  color: var(--status-live-text);
  border-color: var(--badge-curs-border);
}

.live .badge-dot {
  background-color: var(--status-live-dot);
  box-shadow: 0 0 8px var(--status-live-dot);
  animation: pulse-glow 2s infinite ease-in-out;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.85); }
}

@media (prefers-reduced-motion: reduce) {
  .live .badge-dot {
    animation: none;
  }
}
</style>
