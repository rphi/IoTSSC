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

Vue.config.productionTip = false

// Install BootstrapVue
Vue.use(BootstrapVue)
// Optionally install the BootstrapVue icon components plugin
Vue.use(IconsPlugin)

Vue.use(VueRouter)

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', component: Dashboard },
  { path: '/map', component: GlobalMap },
  { path: '/login', component: Login },
  { path: '/account', component: Account}
]

const router = new VueRouter({
  routes,
  linkActiveClass: "active"
})

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
