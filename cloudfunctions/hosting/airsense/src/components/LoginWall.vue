<template>
  <component v-if="authed" v-bind:is="protectedComponent" v-bind:uid="uid" />
</template>

<script>
import {Auth} from '../auth'

export default {
  mounted() {
    Auth.onAuthStateChanged((user) => {
      if (!user) {
        // user not logged in
        this.$router.push('/login');
        return;
      }
      console.log(user.uid);
      this.uid = user.uid;
      this.authed = true;
    });

    
  },
  props: ['protectedComponent'],
  data() {
    return {
      authed: false,
      uid: null
    }
  }
}
</script>