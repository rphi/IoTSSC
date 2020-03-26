<template>
  <div v-if="login_needed" class="text-center">
    <div class="m-5">
      <h1>Well hey there!</h1>
      <h3>Nice to meet you :)</h3>
      <b-alert v-if="$route.query.needauth" variant="danger" class="mt-4" show>You need to be signed in to do this.</b-alert>
    </div>
    <p>You can use the following methods to sign in or sign up.</p>
    <div id="firebaseui-auth-container"></div>
  </div>
</template>

<style scoped>
@import url("https://cdn.firebase.com/libs/firebaseui/3.5.2/firebaseui.css");
</style>

<script>
import * as firebase from 'firebase';
import {Auth} from '../auth';
import * as firebaseui from "firebaseui";

// Initialize the FirebaseUI Widget using Firebase.
var ui = new firebaseui.auth.AuthUI(Auth);

var uiConfig = {
  callbacks: {
    signInSuccessWithAuthResult: function() {
      // User successfully signed in.
      // Return type determines whether we continue the redirect automatically
      // or whether we leave that to developer to handle.
      return true;
    },
    uiShown: function() {
      // The widget is rendered.
      // Hide the loader.
      document.getElementById('loader').style.display = 'none';
    }
  },
  // Will use popup for IDP Providers sign-in flow instead of the default, redirect.
  //signInFlow: 'popup',
  signInSuccessUrl: '#/',
  signInOptions: [
    // Leave the lines as is for the providers you want to offer your users.
    firebase.auth.GoogleAuthProvider.PROVIDER_ID,
    firebase.auth.EmailAuthProvider.PROVIDER_ID,
    firebase.auth.PhoneAuthProvider.PROVIDER_ID
  ]
};

export default {
  mounted() {
    if (Auth.currentUser) {
      // already logged in
      this.$router.push('/');
      return;
    }
    Auth.onAuthStateChanged((user) => {
      if (user) {
        // already logged in
        this.$router.push('/');
        return;
      } else {
        // not logged in
        this.login_needed = true;
        this.$nextTick(function () {
          ui.start('#firebaseui-auth-container', uiConfig);
        })
      }
    });
  },
  data() {
    return {
      login_needed: false
    }
  }
}
</script>