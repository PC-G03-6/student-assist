<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'outline'
  active?: boolean
}>()
</script>

<template>
  <button
    type="button"
    class="app-btn"
    :class="[variant || 'primary', { 'is-active': active }]"
  >
    <slot />
  </button>
</template>

<style scoped>
.app-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  padding: 8px 18px;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: -0.01em;
  white-space: nowrap;
}

/* Primar: actiunea principala a unui ecran (ex: Salveaza grupa) */
.primary {
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.12);
}
.primary:hover {
  background-color: var(--color-primary-light);
}

/* Secundar */
.secondary {
  background-color: var(--color-surface);
  color: var(--color-text-muted);
  border-color: var(--color-border);
  box-shadow: var(--shadow-subtle);
}
.secondary:hover {
  background-color: var(--color-bg);
  color: var(--color-text-main);
  border-color: var(--color-border-dark);
}
.secondary.is-active {
  background-color: var(--color-selected-bg);
  color: var(--color-text-main);
  border-color: var(--color-selected-border);
  font-weight: 700;
  box-shadow: none;
}

/* Contur */
.outline {
  background-color: transparent;
  color: var(--color-text-main);
  border-color: var(--color-border-dark);
}
.outline:hover {
  background-color: rgba(15, 23, 42, 0.04);
}
</style>
