<template>
  <div>
    <h1>Your account</h1>
    <p>Username: {{ display_name }}</p>
    <p>Email: {{ email }}</p>
    <p>Uid: {{ uid }}</p>
  </div>
</template>

<script>
import {Auth} from '../auth';

export default {
  mounted() {
    Auth.onAuthStateChanged((user) => {
      this.display_name = user.displayName;
      this.email = user.email;
      this.uid = user.uid;
    });
  },
  data() {
    return { display_name: this.display_name, email: this.email, uid: this.uid };
  }
}
</script>