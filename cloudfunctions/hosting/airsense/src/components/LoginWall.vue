<template>
  <div>
    <b-alert v-if="showBanner" variant="warning" show><strong>You're not signed in.</strong> For the best experience, sign into AirSense.</b-alert>
    <component v-if="authed" v-bind:is="protectedComponent" v-bind:uid="uid" />
  </div>
</template>

<script>
import {Auth} from '../auth'

export default {
  mounted() {
    this.init();
  },
  methods: {
    init() {
      Auth.onAuthStateChanged((user) => {
      if (!user || user.isAnonymous) {
        if (this.toWelcome) {
          // redirect to welcome page
          this.$router.push('/welcome');
          return;
        }
        else if (this.justBanner) {
          this.showBanner = true;
          this.authed = true;
          return;
        }
        else {
          // user not logged in
          this.$router.push('/login?needauth=1');
          return;
        }
        
      }
      //console.log(user.uid);
      this.uid = user.uid;
      this.authed = true;
    });
    }
  },
  props: ['protectedComponent', 'toWelcome', 'justBanner'],
  data() {
    return {
      showBanner: false,
      authed: false,
      uid: null
    }
  },
  watch: {
    protectedComponent: function() { // watch it
          this.init();
        }
  }
}
</script>