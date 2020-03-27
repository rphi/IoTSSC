<template>
  <div>
    <h1>Your Air Quality Dashboard:</h1>
    
    <div class="row">
      <div class="col">
        <b-card no-body class="mb-3">
          <b-row no-gutters>
            <b-col md="4" class="d-flex justify-content-center" :class="dailyexposure.class">
              <div class="p-4 text-center align-self-center">
                <h1>{{dailyexposure.score}}</h1>
                <h5>{{dailyexposure.rating}}</h5>
              </div>
            </b-col>
            <b-col md="8">
              <b-card-body title="Your exposure score">
                <b-card-text>
                  This score is calculated based on a rolling average of {{dailyexposure.basedOn}} of your sensors' readings over the past 24 hours.
                </b-card-text>
                <b-button-group>
                  <b-button variant="secondary" to="/exposure">View your exposure -></b-button>
                  <b-button variant="outline-secondary" to="/help">Learn more about scores -></b-button>
                </b-button-group>
              </b-card-body>
            </b-col>
          </b-row>
        </b-card>

        <b-card no-body class="mb-3">
          <b-row no-gutters>
            <b-col md="4" style="background-color: lightgray;" class="d-flex justify-content-center">
              <div class="p-4 text-center align-self-center" style="color: black;">
                <h5>Last reading</h5>
                <h1>{{lastReading}}</h1>
              </div>
            </b-col>
            <b-col md="8">
              <b-card-body title="Your air quality sensor">
                <b-card-text>
                  We use the AirSense sensor pack to monitor air quality around you, and sync readings to our cloud.
                  Make sure your sensor is powered on, paired with your phone and you have the AirSense app running
                  to keep your score up to date.
                </b-card-text>
                <b-button to="/readings">View your readings -></b-button>
              </b-card-body>
            </b-col>
          </b-row>
        </b-card>

        <b-card no-body class="mb-3">
          <b-row no-gutters>
            <b-col md="4" style="background-color: darkorange;" class="d-flex justify-content-center">
              <div class="p-4 text-center align-self-center" style="color: black;">
                <h5>Subscribed to</h5>
                <h1>{{numAlerts}} alerts</h1>
              </div>
            </b-col>
            <b-col md="8">
              <b-card-body title="Your alerts">
                <b-card-text>
                  You can register to recieve push notifications about pollution in places you care about through the app.
                </b-card-text>
                <b-button to="/alerts">View your alerts -></b-button>
              </b-card-body>
            </b-col>
          </b-row>
        </b-card>
      </div>

      <div class="col-md-4">
        <b-card title="UK Air Quality Forecast">
          <Timeline id="DefraUKAir" sourceType="profile" :options="{ tweetLimit: '3' }" error-message="This tweet could not be loaded" error-message-class="tweet--not-found"><div class="spinner"></div></Timeline>
        </b-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sensorscore-unknown {
  background-color: darkgray;
  color: black;
}

.sensorscore-good {
  background-color: green;
  color: whitesmoke;
}

.sensorscore-moderate {
  background-color: orange;
  color: black;
}

.sensorscore-bad {
  background-color: red;
  color: whitesmoke;
}

.sensorscore-verybad {
  background-color: purple;
  color: whitesmoke;
}
</style>

<script>
import { Timeline } from 'vue-tweet-embed'
import { Db } from '../db';

import TimeAgo from 'javascript-time-ago'
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'

export default {
  components: {
    Timeline
  },
  data() {
    return {
      dailyexposure: {
        score: "unknown",
        basedOn: "n/a",
        class: "sensorscore-unknown",
        rating: "unknown"
      },
      lastReading: null,
      numAlerts: "?"
    }
  },
  methods: {
    setupExposure() {
      let self = this;
      Db.collection("userexposure")
      .doc(this.uid)
      .get()
      .then(function(doc) {
        var data = doc.data()["d"];
        self.dailyexposure.score = data.score.toFixed(3);
        self.dailyexposure.basedOn = data.basedOn;
        
        var score = self.dailyexposure.score;
        if (score <= 50) {
          self.dailyexposure.class = "sensorscore-good";
          self.dailyexposure.rating = "Good";
        } else if (score <= 100) {
          self.dailyexposure.class = "sensorscore-moderate";
          self.dailyexposure.rating = "Moderate";
        } else if (score <= 150) {
          self.dailyexposure.class = "sensorscore-bad";
          self.dailyexposure.rating = "Bad";
        } else {
          self.dailyexposure.class = "sensorscore-verybad";
          self.dailyexposure.rating = "Very bad";
        }
      })
      .catch(function(error) {
          console.log("Error getting documents: ", error);
      });
    },
    setupLastReading() {
      let self = this;
      Db.collection("readings")
      .where("auth", "==", this.uid)
      .orderBy("t", "desc")
      .limit(1)
      .get()
      .then(function(doc) {
        if (doc.empty) {
          // no readings yet
          self.lastReading = "Never";
          return
        }
        console.log(doc.docs[0].data());
        var last = new Date(doc.docs[0].data().t.seconds * 1000);

        TimeAgo.addLocale(en);
        const timeAgo = new TimeAgo('en-GB');

        self.lastReading = timeAgo.format(last);

      })
      .catch(function(error) {
          console.log("Error getting documents: ", error);
      });
    },
    setupAlerts() {
      let self = this;
      Db.collection("notificationSubscribers")
      .where("d.auth", "==", this.uid)
      .get()
      .then(function(doc) {
        self.numAlerts = doc.docs.length;
      })
      .catch(function(error) {
          console.log("Error getting documents: ", error);
      });
    }
  },
  mounted() {
    this.setupExposure();
    this.setupLastReading();
    this.setupAlerts();
  },
  props: ['uid']
}
</script>