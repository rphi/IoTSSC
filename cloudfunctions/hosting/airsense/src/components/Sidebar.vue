<template>
  <nav class="col-xl-1 col-md-2 d-none d-md-block bg-light sidebar">
    <div class="sidebar-sticky">
      <ul class="nav flex-column">
        <li class="nav-item">
          <b-link class="nav-link" to="/home">
            My dashboard
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/readings">
            My readings
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/account">
            My account
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/exposure">
            My exposure
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/alerts">
            My alerts
          </b-link>
        </li>
      </ul>

      <h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
        <span>AirSense community</span>
      </h6>
      <ul class="nav flex-column mb-2">
        <li class="nav-item">
          <b-link class="nav-link" to="/map">
            Global map
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/cleanest">
            <span data-feather="file-text"></span>
            Cleanest locations
          </b-link>
        </li>
        <li class="nav-item">
          <b-link class="nav-link" to="/blog">
            BreathEasy Newsdesk
          </b-link>
        </li>
      </ul>
    </div>
  </nav>
</template>