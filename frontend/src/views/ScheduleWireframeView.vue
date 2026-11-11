<script setup lang="ts">
import { computed, ref } from 'vue'
import { Clock, MapPin, User, Coffee } from 'lucide-vue-next'
import AppBadge from '../components/ui/AppBadge.vue'
import AppCard from '../components/ui/AppCard.vue'
import AppChip from '../components/ui/AppChip.vue'
import AppSegmented from '../components/ui/AppSegmented.vue'

type EventType = 'course' | 'seminar' | 'lab'
type Frequency = 'all' | 'week1' | 'week2'

interface ClassEvent {
  id: number
  day: string
  time: string
  type: EventType
  typeLabel: string
  subject: string
  room: string
  building: string
  teacher: string
  frequency: Frequency
}

const days = ['Luni', 'Marti', 'Miercuri', 'Joi', 'Vineri']

const getInitialDay = () => {
  const weekday = new Date().getDay()
  return days[weekday >= 1 && weekday <= 5 ? weekday - 1 : 0]
}

const selectedDay = ref(getInitialDay())
const selectedWeek = ref('all')

const weekOptions = [
  { value: 'all', label: 'Toate' },
  { value: 'week1', label: 'S1' },
  { value: 'week2', label: 'S2' },
]

const frequencyLabels: Record<Frequency, string> = {
  all: 'Saptamanal',
  week1: 'Sapt. 1',
  week2: 'Sapt. 2',
}

// Subset de evenimente reale pentru grupa 231, extrase din
// backend/src/main/resources/seed/schedule-seed.json (fara diacritice)
const mockClasses: ClassEvent[] = [
  {
    id: 1,
    day: 'Luni',
    time: '08:00 - 10:00',
    type: 'course',
    typeLabel: 'Curs',
    subject: 'Instruire asistata de calculator',
    room: 'DPPD-205',
    building: 'Campus UBB',
    teacher: 'Lect. MAIER Mariana',
    frequency: 'week2',
  },
  {
    id: 2,
    day: 'Luni',
    time: '10:00 - 12:00',
    type: 'lab',
    typeLabel: 'Laborator',
    subject: 'Aplicatii ale geometriei in informatica',
    room: 'L320',
    building: 'Campus FSEGA',
    teacher: 'Drd. ARON Mihai Alexandru',
    frequency: 'week2',
  },
  {
    id: 3,
    day: 'Luni',
    time: '12:00 - 15:00',
    type: 'seminar',
    typeLabel: 'Seminar',
    subject: 'Practica pedagogica observativa',
    room: 'DPPD-205',
    building: 'Campus UBB',
    teacher: 'Lect. MAIER Mariana',
    frequency: 'all',
  },
  {
    id: 4,
    day: 'Luni',
    time: '16:00 - 18:00',
    type: 'course',
    typeLabel: 'Curs',
    subject: 'Programare paralela si distribuita',
    room: '2/I',
    building: 'Cladirea Centrala UBB',
    teacher: 'Conf. NICULESCU Virginia',
    frequency: 'all',
  },
  {
    id: 5,
    day: 'Luni',
    time: '18:00 - 20:00',
    type: 'lab',
    typeLabel: 'Laborator',
    subject: 'Principiile implementarii orientate spre performanta',
    room: 'MOS-S16',
    building: 'Cladirea Centrala UBB',
    teacher: 'C.d.asociat MAJA Ovidiu',
    frequency: 'week1',
  },
  {
    id: 6,
    day: 'Marti',
    time: '08:00 - 10:00',
    type: 'lab',
    typeLabel: 'Laborator',
    subject: 'Arhitectura aplicatiilor cloud',
    room: 'L439',
    building: 'Campus FSEGA',
    teacher: 'C.d.asociat GABUDEAN Calin',
    frequency: 'week1',
  },
]

// Filtrare dupa zi si saptamana (orele saptamanale apar mereu)
const visibleClasses = computed(() =>
  mockClasses.filter(
    (c) =>
      c.day === selectedDay.value &&
      (selectedWeek.value === 'all' ||
        c.frequency === 'all' ||
        c.frequency === selectedWeek.value),
  ),
)

const summaryText = computed(() => {
  const n = visibleClasses.value.length
  if (n === 0) return 'nicio activitate'
  return n === 1 ? '1 activitate' : `${n} activitati`
})
</script>

<template>
  <div class="page">
    <header class="page-header">
      <div class="header-top">
        <div>
          <h1 class="main-title">Orar • Grupa 231</h1>
          <p class="subtitle">Informatica Romana • Anul 3</p>
        </div>
        <AppSegmented v-model="selectedWeek" :options="weekOptions" />
      </div>
    </header>

    <div class="chip-row">
      <AppChip
        v-for="day in days"
        :key="day"
        :active="selectedDay === day"
        @click="selectedDay = day"
      >
        {{ day }}
      </AppChip>
    </div>

    <div class="day-summary">
      <span class="summary-day">{{ selectedDay }}</span>
      <span class="summary-count">{{ summaryText }}</span>
    </div>

    <div v-if="visibleClasses.length" class="classes-list">
      <AppCard
        v-for="item in visibleClasses"
        :key="item.id"
        hoverable
        :accent="item.type"
        class="class-card"
      >
        <div class="card-top-row">
          <div class="time-container">
            <Clock :size="15" :stroke-width="2.25" class="muted-icon" />
            <span class="time-text">{{ item.time }}</span>
          </div>
          <div class="badges-group">
            <AppBadge :variant="item.type" dot>{{ item.typeLabel }}</AppBadge>
            <AppBadge variant="neutral">{{ frequencyLabels[item.frequency] }}</AppBadge>
          </div>
        </div>

        <h3 class="subject-name">{{ item.subject }}</h3>

        <div class="card-footer-meta">
          <div class="meta-row">
            <MapPin :size="14" :stroke-width="2" class="muted-icon" />
            <span><strong>{{ item.room }}</strong> • {{ item.building }}</span>
          </div>
          <div class="meta-row">
            <User :size="14" :stroke-width="2" class="muted-icon" />
            <span>{{ item.teacher }}</span>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- Stare goala -->
    <AppCard v-else class="empty-state">
      <Coffee :size="28" :stroke-width="1.75" class="muted-icon" />
      <p class="empty-title">Nicio activitate</p>
      <p class="empty-text">Nu ai ore programate in aceasta zi pentru saptamana selectata.</p>
    </AppCard>
  </div>
</template>

<style scoped>
.page {
  max-width: 580px;
  margin: 0 auto;
  padding: 18px 16px;
  padding-bottom: 104px;
}

/* Header */
.page-header {
  margin-bottom: 14px;
}

.header-top {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
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

/* Zile */
.chip-row {
  display: flex;
  gap: 8px;
  margin: 0 -16px 16px;
  padding: 2px 16px;
  overflow-x: auto;
  scrollbar-width: none;
}
.chip-row::-webkit-scrollbar {
  display: none;
}

/* Sumar zi */
.day-summary {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
  padding: 0 2px;
}

.summary-day {
  font-size: 0.98rem;
  font-weight: 800;
  color: var(--color-text-main);
  letter-spacing: -0.01em;
}

.summary-count {
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

/* Carduri */
.classes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.class-card {
  padding: 14px 16px;
}

.card-top-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.time-container {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time-text {
  font-size: 0.9rem;
  font-weight: 800;
  color: var(--color-text-main);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.badges-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.subject-name {
  font-size: 1.02rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--color-text-main);
  margin-bottom: 12px;
  letter-spacing: -0.01em;
}

.card-footer-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-subtle);
  font-size: 0.78rem;
  color: var(--color-text-muted);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-row strong {
  color: var(--color-text-main);
}

.muted-icon {
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

/* Stare goala */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 6px;
  padding: 28px 20px;
}

.empty-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--color-text-main);
}

.empty-text {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  max-width: 260px;
}
</style>
