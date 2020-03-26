<template>
  <div>
    <h1>My Exposure</h1>
    <h3>Today's score: {{dailyExposure}}</h3>
    <div class="chart-container" style="position: relative; height:40vh;">
      <canvas id="exposurechart"></canvas>
    </div>
    <b-overlay :show="show" rounded="sm">
      <b-table striped hover :items="exposure"></b-table>
    </b-overlay>
  </div>
</template>

<script>
import { Db } from '../db';
import Chart from 'chart.js';


export default {
  methods: {
    createChart() {
      var ctx = document.getElementById('exposurechart').getContext('2d');
      new Chart(ctx, {
          // The type of chart we want to create
          type: 'line',

          // The data for our dataset
          data: this.chartdata,

          // Configuration options go here
          options: {
            responsive: true,
            maintainAspectRatio: false
          }
      });
    }
  },
  mounted() {
    let self = this;
    Db.collection("userexposure")
    .doc(this.uid)
    .get()
    .then(function(doc) {
      for (const [key, value] of Object.entries(doc.data())) {
        if (key == "d") {
          // daily exposure score
          self.dailyExposure = value.score;
          self.dailyBasedOn = value.basedOn;
        } else {
          self.exposure.push({
            "Date": key,
            "Exposure": value.exposureTotal
          });
          self.chartdata.labels.push(key);
          self.chartdata.datasets[0].data.push(value.exposureTotal);
        }
      }
      self.createChart();
      self.show = false;
    })
    .catch(function(error) {
        console.log("Error getting documents: ", error);
    });
  },
  props: ['uid'],
  data() {
    return {
      exposure: [],
      chartdata: {
        labels: [],
        datasets: [{
          label: "Exposure",
          backgroundColor: "#f87979",
          data: []
        }]
      },
      dailyExposure: null,
      dailyBasedOn: null,
      show: true
    }
  },
  components: {
    
  }
}
</script>