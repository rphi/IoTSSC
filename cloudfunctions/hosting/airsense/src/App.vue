<template>
  <div id="app" style="min-height: 100vh;">
    <Nav />
    <div class="container-fluid">
      <div class="row">
        <Sidebar />
        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 col-xl-11 px-4">
          <router-view></router-view>
        </main>
      </div>
    </div>
  </div>
</template>

<script>
//import HelloWorld from './components/HelloWorld.vue'
import Nav from './components/Nav.vue'
import Sidebar from './components/Sidebar.vue'

export default {
  name: 'App',
  components: {
    Nav,
    Sidebar
  }
}
</script>

<style>

</style>
