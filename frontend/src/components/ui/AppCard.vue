<script setup lang="ts">
defineProps<{
  hoverable?: boolean
  accent?: 'course' | 'seminar' | 'lab' | 'bus' | 'primary' | 'none'
}>()
</script>

<template>
  <div
    class="app-card"
    :class="[
      { 'is-hoverable': hoverable },
      accent && accent !== 'none' ? `accent-${accent}` : ''
    ]"
  >
    <slot />
  </div>
</template>

<style scoped>
.app-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px 18px;
  box-shadow: var(--shadow-card);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.25s ease;
  position: relative;
  overflow: hidden;
}

.is-hoverable:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  /* doar 3 laturi, ca dunga de accent din stanga sa ramana colorata */
  border-top-color: var(--color-border-dark);
  border-right-color: var(--color-border-dark);
  border-bottom-color: var(--color-border-dark);
}

/* Dunga verticala de accent pe marginea stanga */
.accent-lab {
  border-left: 4px solid var(--badge-lab-text);
}

.accent-seminar {
  border-left: 4px solid var(--badge-seminar-text);
}

.accent-course {
  border-left: 4px solid var(--badge-curs-text);
}

.accent-bus {
  border-left: 4px solid var(--badge-bus-text);
}

.accent-primary {
  border-left: 4px solid var(--color-primary);
}

@media (prefers-reduced-motion: reduce) {
  .is-hoverable:hover {
    transform: none;
  }
}
</style>
