<script setup lang="ts">
import { computed, ref } from 'vue'
import { Bus, MapPin, Map as MapIcon, Navigation } from 'lucide-vue-next'
import AppBadge from '../components/ui/AppBadge.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppChip from '../components/ui/AppChip.vue'

const selectedLine = ref('all')

const lines = ['all', '24B', '25', '4']

// Date ilustrative pentru wireframe. Liniile, directiile si timpii reali
// vor veni din Tranzy API + GTFS CTP (vezi docs/tranzy-spike.md).
const mockBuses = [
  { line: '24B', dest: 'Spre Polus / Vivo', stop: 'Memorandumului Sud', eta: '3 min', isLive: true },
  { line: '25', dest: 'Spre Bucium (Manastur)', stop: 'Memorandumului Sud', eta: '7 min', isLive: true },
  { line: '4', dest: 'Spre Gara', stop: 'Piata Unirii', eta: '12 min', isLive: false },
]

const visibleBuses = computed(() =>
  selectedLine.value === 'all'
    ? mockBuses
    : mockBuses.filter((b) => b.line === selectedLine.value),
)
</script>

<template>
  <div class="page">
    <header class="page-header">
      <h1 class="main-title">Transport in Comun</h1>
      <p class="subtitle">Cluj-Napoca • Tranzy & CTP</p>
    </header>

    <div class="chip-row">
      <AppChip
        v-for="l in lines"
        :key="l"
        :active="selectedLine === l"
        @click="selectedLine = l"
      >
        {{ l === 'all' ? 'Toate' : `Linia ${l}` }}
      </AppChip>
    </div>

    <!-- Zona harta (placeholder pentru Leaflet) -->
    <div class="map-container">
      <div class="map-top-dock">
        <span class="map-badge">
          <MapIcon :size="13" :stroke-width="2.25" /> Harta Leaflet
        </span>
        <span class="map-badge">
          <span class="pulse-green" /> Live GPS (20s)
        </span>
      </div>

      <div class="map-marker vehicle v1">
        <Bus :size="13" :stroke-width="2.5" />
        <strong>24B</strong>
        <span class="radar-ping" />
      </div>

      <div class="map-marker vehicle v2">
        <Bus :size="13" :stroke-width="2.5" />
        <strong>25</strong>
      </div>

      <div class="map-marker stop s1">
        <MapPin :size="13" :stroke-width="2.25" /> Memorandumului
      </div>

      <div class="map-marker stop s2">
        <MapPin :size="13" :stroke-width="2.25" /> Piata Unirii
      </div>

      <div class="map-info-float">
        <strong>Flota Live CTP</strong>
        <p>Atinge o statie sau un vehicul pentru detalii de sosire</p>
      </div>
    </div>

    <!-- Ruta recomandata spre urmatorul curs (curs real din seed, grupa 231) -->
    <AppCard accent="primary" class="route-card">
      <div class="route-header">
        <div class="route-icon">
          <Navigation :size="16" :stroke-width="2.25" />
        </div>
        <div class="route-title-box">
          <span class="route-label">Ruta recomandata spre curs</span>
          <h4 class="route-event">Laborator • L320, Campus FSEGA (10:00)</h4>
        </div>
      </div>

      <div class="journey-timeline">
        <div class="timeline-step">
          <span class="step-dot" />
          <span class="step-time">09:30</span>
          <span class="step-desc">Pe jos spre <strong>Statia Memorandumului</strong> (4 min)</span>
        </div>
        <div class="timeline-step">
          <span class="step-dot bus" />
          <span class="step-time">09:34</span>
          <span class="step-desc">Autobuzul <strong>24B</strong></span>
        </div>
        <div class="timeline-step">
          <span class="step-dot finish" />
          <span class="step-time">09:51</span>
          <span class="step-desc">Sosire <strong>FSEGA</strong> (cu 9 min inainte)</span>
        </div>
      </div>
    </AppCard>

    <!-- Sosiri -->
    <section>
      <h3 class="section-heading">Sosiri in Timp Real</h3>
      <div class="arrivals-list">
        <AppCard
          v-for="bus in visibleBuses"
          :key="bus.line"
          hoverable
          accent="bus"
          class="bus-card"
        >
          <div class="bus-left">
            <div class="bus-num-box">{{ bus.line }}</div>
            <div class="bus-meta">
              <span class="bus-dest">{{ bus.dest }}</span>
              <span class="bus-stop">Statia {{ bus.stop }}</span>
            </div>
          </div>
          <div class="bus-right">
            <span class="eta-text">{{ bus.eta }}</span>
            <AppBadge :variant="bus.isLive ? 'live' : 'neutral'" :dot="bus.isLive">
              {{ bus.isLive ? 'Live GPS' : 'Orar' }}
            </AppBadge>
          </div>
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
  margin-bottom: 12px;
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

.chip-row {
  display: flex;
  gap: 8px;
  margin: 0 -16px 14px;
  padding: 2px 16px;
  overflow-x: auto;
  scrollbar-width: none;
}
.chip-row::-webkit-scrollbar {
  display: none;
}

/* Harta */
.map-container {
  position: relative;
  height: 230px;
  background: var(--color-surface-hover);
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 18px 18px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.map-top-dock {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  z-index: 5;
}

.map-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(8px);
  padding: 4px 9px;
  border-radius: var(--radius-full);
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--color-text-main);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-subtle);
  white-space: nowrap;
}

.pulse-green {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--status-live-dot);
  box-shadow: 0 0 6px var(--status-live-dot);
}

.map-marker {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  font-size: 0.72rem;
  font-weight: 700;
  box-shadow: var(--shadow-card);
  z-index: 3;
  white-space: nowrap;
}

.map-marker.vehicle {
  background: var(--badge-bus-text);
  color: white;
}

.map-marker.stop {
  background: white;
  color: var(--color-text-main);
  border: 1px solid var(--color-border);
}

.v1 { top: 46px; left: 14px; }
.v2 { bottom: 54px; right: 14px; }
.s1 { top: 46px; right: 14px; }
.s2 { bottom: 54px; left: 14px; }

.radar-ping {
  position: absolute;
  inset: -4px;
  border: 2px solid var(--badge-bus-text);
  border-radius: var(--radius-full);
  animation: radar-expand 2s infinite ease-out;
}

@keyframes radar-expand {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.4); opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .radar-ping {
    animation: none;
    opacity: 0;
  }
}

.map-info-float {
  position: absolute;
  bottom: 8px;
  left: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
  padding: 6px 10px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.72rem;
  color: var(--color-text-muted);
  z-index: 2;
  text-align: center;
}

.map-info-float strong {
  color: var(--color-text-main);
  display: block;
}

/* Ruta */
.route-card {
  margin-bottom: 18px;
  padding: 14px 16px;
}

.route-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.route-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-hover);
  color: var(--color-text-main);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.route-label {
  display: block;
  font-size: 0.68rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-muted);
}

.route-event {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.journey-timeline {
  display: flex;
  flex-direction: column;
  gap: 9px;
  position: relative;
  padding-left: 16px;
}

.journey-timeline::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  background: var(--color-border-dark);
  opacity: 0.6;
}

.timeline-step {
  position: relative;
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.step-dot {
  position: absolute;
  left: -16px;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: white;
  border: 2px solid var(--color-text-tertiary);
}

.step-dot.bus {
  border-color: var(--badge-bus-text);
  background: var(--badge-bus-text);
}

.step-dot.finish {
  border-color: var(--badge-curs-text);
  background: var(--badge-curs-text);
}

.step-time {
  font-weight: 800;
  color: var(--color-text-main);
  min-width: 40px;
  font-variant-numeric: tabular-nums;
}

.step-desc strong {
  color: var(--color-text-main);
}

/* Sosiri */
.section-heading {
  font-size: 0.98rem;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--color-text-main);
  margin-bottom: 10px;
}

.arrivals-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bus-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
}

.bus-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.bus-num-box {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  background: var(--badge-bus-bg);
  color: var(--badge-bus-text);
  border: 1px solid var(--badge-bus-border);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.bus-meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.bus-dest,
.bus-stop {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bus-dest {
  font-weight: 700;
  font-size: 0.88rem;
  color: var(--color-text-main);
}

.bus-stop {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.bus-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.eta-text {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--status-live-text);
  letter-spacing: -0.02em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
</style>
