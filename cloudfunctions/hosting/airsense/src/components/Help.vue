<template>
  <div>
    <b-row>
      <b-col md=6>
        <h1>Help</h1>
        <p>
          Need a hand getting started with AirSense? Here's some FAQs that might be of use. Otherwise, drop a message to our support team.
        </p>
        <b-button onClick="alert('lol no')">Contact support -></b-button>
      </b-col>
    </b-row>

    <b-row class="mt-4">
      <b-col>
        <b-card title="About your AirSense sensor pack">
          <div class="text-center">
            <img :src="require('@/assets/airsense.png')" style="max-width: 200px" />
          </div>
          <p>
            The sensor pack is an IoT device that communicates to our companion Android app over Bluetooth, and uses its suite of sensors
            to monitor air quality using the following metrics:
          </p>
          <ul>
            <li>Equivalent CO2</li>
            <li>PM2.5 (Dust)</li>
            <li>Propane</li>
            <li>Carbon Monoxide</li>
            <li>Volatile Organic Compounds (VOCs)</li>
            <li>...and several more</li>
          </ul>
          <p>
            It has a rechargable battery, that you will need to charge once per day.
          </p>
        </b-card>
      </b-col>
      <b-col>
        <b-card title="Explaining Air Quality Scores">
          <p>
            Our app combines the scores from each of the sensor types inside your sensor pack to calculate your scores.
          </p>
          <p>
            Your <strong>exposure score</strong> is a rolling average of your exposure over the past 24 hours, and is a great
            way to check at a glance how you are doing. From this, we calculate your exposure rating:
          </p>
          <ul>
            <li>0-50 is Good</li>
            <li>50-100 is Moderate</li>
            <li>100-150 is Bad (if sensitive)</li>
            <li>200 is Very Bad</li>
          </ul>
          <p>
            On the "my exposure" page, you can see historic data from your sensor, based on cumulative exposure per 24 hour period.
          </p>
          <p>
            The leaderboard uses cumulative exposure to show how you compare to other users. The lower the score, the better you 
            are doing!
          </p>
        </b-card>
      </b-col>
      <b-col>
        <b-card title="Where do I start?">
          <p>
            To use the AirSense platform, you both need a sensor pack and our companion Android app.
          </p>
          <p>
            Once you have your sensor and app, follow the in-app instructions to get paired and begin syncing
            information to the AirSense cloud.
          </p>
          <div>
            <b-button variant="primary" class="mt-2">Buy AirSense sensor pack -></b-button>
            <b-button variant="success" class="mt-2">Download AirSense app -></b-button>
          </div>
        </b-card>
      </b-col>
    </b-row>
  </div>
</template>

<script>

export default {
  components: {
    
  },
}
</script>