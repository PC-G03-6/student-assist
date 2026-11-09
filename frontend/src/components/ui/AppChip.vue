<script setup lang="ts">
// Pastila de filtru / selectie (zile, linii de autobuz etc.)
defineProps<{
  active?: boolean
}>()
</script>

<template>
  <button
    type="button"
    class="app-chip"
    :class="{ active }"
    :aria-pressed="active ? 'true' : 'false'"
  >
    <slot />
  </button>
</template>

<style scoped>
.app-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 38px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-subtle);
}

.app-chip:hover {
  background: var(--color-bg);
  color: var(--color-text-main);
  border-color: var(--color-border-dark);
}

.app-chip.active {
  background: var(--color-selected-bg);
  color: var(--color-text-main);
  border-color: var(--color-selected-border);
  font-weight: 700;
  box-shadow: none;
}
</style>
