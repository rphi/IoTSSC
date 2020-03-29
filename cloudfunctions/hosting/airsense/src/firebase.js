import firebase from "firebase/app"
import 'firebase/app'

// setup Firebase
const firebaseConfig = {
  apiKey: "AIzaSyB7MokhA3ABPHi9Ku5ABNV6ngXKYYZTKag",
  authDomain: "iotssc-aqm.firebaseapp.com",
  databaseURL: "https://iotssc-aqm.firebaseio.com",
  projectId: "iotssc-aqm",
  storageBucket: "iotssc-aqm.appspot.com",
  messagingSenderId: "927998709219",
  appId: "1:927998709219:web:fc98cfbd3f3f3766ad64d6"
};
// Initialize Firebase
export const FbApp = firebase.initializeApp(firebaseConfig);
