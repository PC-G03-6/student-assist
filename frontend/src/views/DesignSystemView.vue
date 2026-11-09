<script setup lang="ts">
import { ref } from 'vue'
import AppButton from '../components/ui/AppButton.vue'
import AppBadge from '../components/ui/AppBadge.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppChip from '../components/ui/AppChip.vue'
import AppSegmented from '../components/ui/AppSegmented.vue'

const demoChip = ref('Luni')
const demoWeek = ref('all')
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1 class="main-title">Design System</h1>
      <p class="subtitle">Componente si tokens reutilizabili (SCRUM-22)</p>
    </header>

    <section class="ds-section">
      <div class="section-header">
        <span class="section-number">01</span>
        <h2>Orar & Activitati</h2>
      </div>
      <p class="section-desc">Badge-uri pentru tipul orei si frecventa (AppBadge):</p>
      <div class="showcase-row">
        <AppBadge variant="lab" dot>Laborator</AppBadge>
        <AppBadge variant="seminar" dot>Seminar</AppBadge>
        <AppBadge variant="course" dot>Curs</AppBadge>
        <AppBadge variant="neutral">Saptamanal</AppBadge>
      </div>
    </section>

    <section class="ds-section">
      <div class="section-header">
        <span class="section-number">02</span>
        <h2>Transport Public</h2>
      </div>
      <p class="section-desc">Linii de autobuz si status de sosire:</p>
      <div class="showcase-row">
        <AppBadge variant="bus">Linia 24B</AppBadge>
        <AppBadge variant="live" dot>Live GPS</AppBadge>
        <AppBadge variant="neutral">Orar programat</AppBadge>
      </div>
    </section>

    <section class="ds-section">
      <div class="section-header">
        <span class="section-number">03</span>
        <h2>Butoane</h2>
      </div>
      <p class="section-desc">Pentru actiuni (AppButton). Butonul primar se foloseste o singura data pe ecran:</p>
      <div class="showcase-row">
        <AppButton variant="primary">Salveaza grupa</AppButton>
        <AppButton variant="secondary">Anuleaza</AppButton>
        <AppButton variant="outline">Vezi pe harta</AppButton>
      </div>
    </section>

    <section class="ds-section">
      <div class="section-header">
        <span class="section-number">04</span>
        <h2>Filtre & Selectie</h2>
      </div>
      <p class="section-desc">Pastile de filtru (AppChip) si comutator compact (AppSegmented):</p>
      <div class="showcase-row">
        <AppChip
          v-for="d in ['Luni', 'Marti', 'Miercuri']"
          :key="d"
          :active="demoChip === d"
          @click="demoChip = d"
        >
          {{ d }}
        </AppChip>
      </div>
      <div class="showcase-row spaced">
        <AppSegmented
          v-model="demoWeek"
          :options="[
            { value: 'all', label: 'Toate' },
            { value: 'week1', label: 'S1' },
            { value: 'week2', label: 'S2' },
          ]"
        />
      </div>
    </section>

    <section class="ds-section">
      <div class="section-header">
        <span class="section-number">05</span>
        <h2>Carduri cu dunga de accent</h2>
      </div>
      <p class="section-desc">AppCard cu prop-ul accent:</p>
      <div class="cards-showcase">
        <AppCard hoverable accent="lab" class="sample-card">
          <h3>Card Laborator</h3>
          <p>Dunga verticala pe stanga pentru recunoastere rapida.</p>
        </AppCard>
        <AppCard hoverable accent="seminar" class="sample-card">
          <h3>Card Seminar</h3>
          <p>Dunga verticala chihlimbar pe stanga.</p>
        </AppCard>
        <AppCard hoverable accent="course" class="sample-card">
          <h3>Card Curs</h3>
          <p>Dunga verticala verde pe stanga.</p>
        </AppCard>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  max-width: 580px;
  margin: 0 auto;
  padding: 18px 16px;
  padding-bottom: 104px;
}

.page-header {
  margin-bottom: 14px;
}

.main-title {
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-main);
  line-height: 1.2;
}

.subtitle {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-top: 2px;
  font-weight: 500;
}

.ds-section {
  margin-bottom: 14px;
  background: var(--color-surface);
  padding: 14px 16px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.section-number {
  font-size: 0.68rem;
  font-weight: 800;
  color: var(--color-text-secondary);
  background: var(--color-surface-hover);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  border: 1px solid var(--color-border);
  font-variant-numeric: tabular-nums;
}

.section-header h2 {
  font-size: 0.98rem;
  font-weight: 700;
  color: var(--color-text-main);
  letter-spacing: -0.01em;
}

.section-desc {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.showcase-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.showcase-row.spaced {
  margin-top: 12px;
}

.cards-showcase {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sample-card {
  padding: 12px 14px;
}

.sample-card h3 {
  font-size: 0.88rem;
  font-weight: 700;
  color: var(--color-text-main);
  margin-bottom: 2px;
}

.sample-card p {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}
</style>
