import { createRouter, createWebHistory } from 'vue-router'
import ScheduleWireframeView from '../views/ScheduleWireframeView.vue'
import TransitWireframeView from '../views/TransitWireframeView.vue'
import DesignSystemView from '../views/DesignSystemView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/schedule',
    },
    {
      path: '/schedule',
      name: 'schedule',
      component: ScheduleWireframeView,
    },
    {
      path: '/transit',
      name: 'transit',
      component: TransitWireframeView,
    },
    {
      path: '/design-system',
      name: 'design-system',
      component: DesignSystemView,
    },
  ],
})

export default router
