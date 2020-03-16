<template>
  <nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 col-xl-1 mr-0" href="#">AirSense</a>
    <input class="form-control form-control-dark w-100" type="text" placeholder="Search for a location" aria-label="Search">
    <ul class="navbar-nav px-3">
      <li class="nav-item text-nowrap">
        <b-link v-if="user" class="nav-link" v-on:click="signout()">Hi {{ user.displayName }}!</b-link>
        <b-link v-else class="nav-link" to="/login">Sign in</b-link>
      </li>
    </ul>
  </nav>
</template>

<script>
import {Auth} from '../auth';

export default {
  data() {
    return {
      user: this.user
    }
  },
  mounted() {
    Auth.onAuthStateChanged((user) => {
      this.updateuser(user);
      console.log(user);
    });
  },
  methods: {
    updateuser(user) {
      this.user = user;
    },
    signout() {
      Auth.signOut();
    }
  }
}
</script>