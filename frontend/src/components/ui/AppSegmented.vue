<script setup lang="ts">
// Comutator compact intre 2-4 optiuni (ex: Toate / S1 / S2)
defineProps<{
  options: { value: string; label: string }[]
}>()

const model = defineModel<string>({ required: true })
</script>

<template>
  <div class="segmented" role="radiogroup">
    <button
      v-for="opt in options"
      :key="opt.value"
      type="button"
      role="radio"
      class="seg-btn"
      :class="{ active: model === opt.value }"
      :aria-checked="model === opt.value ? 'true' : 'false'"
      @click="model = opt.value"
    >
      {{ opt.label }}
    </button>
  </div>
</template>

<style scoped>
.segmented {
  display: inline-flex;
  background-color: var(--color-surface-hover);
  padding: 3px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  gap: 2px;
  flex-shrink: 0;
}

.seg-btn {
  background: none;
  border: none;
  min-height: 30px;
  padding: 5px 11px;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-xs);
  transition: all 0.2s ease;
}

.seg-btn:hover {
  color: var(--color-text-main);
}

.seg-btn.active {
  background: var(--color-surface);
  color: var(--color-text-main);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}
</style>
