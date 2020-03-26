import Vue from 'vue'
import VueRouter from 'vue-router';
import { BootstrapVue, IconsPlugin } from 'bootstrap-vue'

// CSS
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import './assets/custom.scss'

import App from './App.vue'
import Dashboard from './components/Dashboard.vue'
import GlobalMap from './components/GlobalMap.vue'
import Login from './components/Login.vue'
import Account from './components/Account.vue'
import Help from './components/Help.vue'
import Readings from './components/Readings.vue'
import Leaderboard from './components/Leaderboard.vue'
import Cleanspots from './components/Cleanspots.vue'
import LoginWall from './components/LoginWall.vue'
import Welcome from './components/Welcome.vue'
import Exposure from './components/Exposure.vue'

Vue.config.productionTip = false

// Install BootstrapVue
Vue.use(BootstrapVue)
// Optionally install the BootstrapVue icon components plugin
Vue.use(IconsPlugin)

//Vue.use(TwitterFeed)

Vue.use(VueRouter)

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/welcome', component: Welcome },
  { path: '/home', component: LoginWall, props: { protectedComponent: Dashboard, toWelcome: true } },
  { path: '/map', component: LoginWall, props: { protectedComponent: GlobalMap, justBanner: true } },
  { path: '/login', component: Login },
  { path: '/account', component: LoginWall, props: { protectedComponent: Account } },
  { path: '/help', component: Help },
  { path: '/readings', component: LoginWall, props: { protectedComponent: Readings } },
  { path: '/exposure', component: LoginWall, props: { protectedComponent: Exposure } },
  { path: '/leaderboard', component: LoginWall, props: { protectedComponent: Leaderboard, justBanner: true } },
  { path: '/cleanlocations', component: Cleanspots }
]

const router = new VueRouter({
  routes,
  linkActiveClass: "active"
})

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
