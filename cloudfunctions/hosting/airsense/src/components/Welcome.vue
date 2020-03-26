<template>
  <div class="d-flex flex-column">
    <div style="background-color: rgba(255,255,255,0.8);">
      <h1>Welcome to Airsense</h1>
      <h4>your air quality monitoring system</h4>
      <p>
        AirSense is able to detect when your exposure to harmful airborne pollutants is high, and give you the help
        you need to make positive changes.
      </p>
      <p>
        This site works with our Bluetooth sensor package and companion app. To get started, create an account in-app
        then log in here.
      </p>
      <p class="text-center">
        <b-button size="lg" to="login">Login</b-button>
      </p>
    </div>
    <img class="mt-auto" :src="require('@/assets/undraw/undraw_environmental_study_skau.svg')" style="
            position: fixed;
            bottom: 0;
            right: 0;
            max-height: 40vh;
            z-index: -1;" />
  </div>
</template>