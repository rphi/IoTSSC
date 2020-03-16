<template>
  <nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 col-xl-1 mr-0" href="#">AirSense</a>
    <ul class="navbar-nav px-3 ">
      <li v-if="user" class="nav-item text-nowrap">
        <b-link class="nav-link" v-on:click="signout()">Hi {{ user.displayName }}! Sign out?</b-link>
      </li>
      <li v-else class="nav-item text-nowrap" style="display:inline;">
        <b-link class="nav-link" to="/login">Sign in</b-link>
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