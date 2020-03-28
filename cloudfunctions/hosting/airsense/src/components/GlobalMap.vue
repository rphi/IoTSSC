<template>
  <div>
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
      <h1 class="h2">Global Air Quality Heatmap</h1>
      <div class="btn-toolbar mb-2 mb-md-0">
        <div class="btn-group mr-2">
          <button class="btn btn-sm btn-outline-secondary">Share</button>
          <button onClick="window.open(geojsonlink,'_blank');" class="btn btn-sm btn-outline-secondary">Export</button>
        </div>
        <button type="button" class="btn btn-sm btn-outline-secondary dropdown-toggle">
          <span data-feather="calendar"></span>
          This week
        </button>
      </div>
    </div>
    <div>
      <div style="height: 80vh; width: 100%" id="map"></div>
    </div>
  </div>
</template>

<style>
@import url("https://api.mapbox.com/mapbox-gl-js/v1.8.1/mapbox-gl.css");
@import url("https://api.mapbox.com/mapbox-gl-js/plugins/mapbox-gl-geocoder/v4.4.2/mapbox-gl-geocoder.css");
</style>

<script>
import mapboxgl from 'mapbox-gl';
var MapboxGeocoder = require('@mapbox/mapbox-gl-geocoder');

var geojsonlink = "";

function getDistanceFromLatLonInKm(lat1, lon1, lat2, lon2) {
  var R = 6371; // Radius of the earth in km
  var dLat = deg2rad(lat2-lat1);  // deg2rad below
  var dLon = deg2rad(lon2-lon1); 
  var a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
    Math.sin(dLon/2) * Math.sin(dLon/2)
    ; 
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  var d = R * c; // Distance in km
  return d;
}

function deg2rad(deg) {
  return deg * (Math.PI/180)
}

function formatDataString(center, boundingbox) {
  console.debug("Creating data query string");
  var lnglimit = Math.abs(boundingbox._ne.lng-boundingbox._sw.lng)/0.0005
  var latlimit = Math.abs(boundingbox._ne.lat-boundingbox._sw.lat)/0.0005
  var limit = Math.floor(Math.max(lnglimit, latlimit));
  var radius = Math.ceil(getDistanceFromLatLonInKm(boundingbox._ne.lat, boundingbox._ne.lng, boundingbox._sw.lat, boundingbox._sw.lng)/2);
  geojsonlink = `https://us-central1-iotssc-aqm.cloudfunctions.net/api/points/?lat=${center.lat}&long=${center.lng}&radius=${radius}&limit=${limit}`;
  return geojsonlink;
}

function startmap(location) {
  console.info("Starting MapBox");

  mapboxgl.accessToken = 'pk.eyJ1IjoicnBoaSIsImEiOiJjam52bDE0bjQwYWx3M3BxdDlxeWR6ZXJ4In0.xYvhKxiSm3f858diHlu3lA';
  var map = new mapboxgl.Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/light-v10',
      center: [location.long, location.lat],
      zoom: 13
  });

  map.addControl(
    new MapboxGeocoder({
      accessToken: mapboxgl.accessToken,
      mapboxgl: mapboxgl
    })
  );

  map.on('load', function() {
    // Add geolocate control to the map.
    map.addControl(
      new mapboxgl.GeolocateControl({
        positionOptions: {
          enableHighAccuracy: true
        },
        trackUserLocation: true,
        showUserLocation: true
      })
    );

    // Add a geojson point source.
    // Heatmap layers also work with a vector tile source.
    map.addSource('reads', {
        'type': 'geojson',
        'data': formatDataString(map.getCenter(), map.getBounds())
    });
    console.debug("added map datasource");

    map.addLayer(
        {
            'id': 'reads-heat',
            'type': 'heatmap',
            'source': 'reads',
            paint: {
              // increase weight as diameter breast height increases
              'heatmap-weight': {
                property: 'score',
                type: 'exponential',
                stops: [
                  [0, 0],
                  [40, 1]
                ]
              },
              // increase intensity as zoom level increases
              'heatmap-intensity': {
                stops: [
                  [11, 1],
                  [15, 3]
                ]
              },
              // assign color values be applied to points depending on their density
              'heatmap-color': [
                'interpolate',
                ['linear'],
                ['heatmap-density'],
                0, 'rgba(0, 0, 0, 0.0)',
                1, 'rgba(211,0,8,0.73)',
                //10, 'rgba(255, 47, 47, 0.90)'
              ],
              // increase radius as zoom increases
              'heatmap-radius': {
                stops: [
                  [11, 20],
                  [15, 40]
                ]
              },
              // decrease opacity to transition into the circle layer
              'heatmap-opacity': {
                default: 1,
                stops: [
                  [14, 1],
                  [15, 1]
                ]
              },
            }
        },
        'waterway-label'
    );

    map.addLayer(
        {
            'id': 'readings-point',
            'type': 'circle',
            'source': 'reads',
            'minzoom': 15,
            'paint': {
                // Size circle radius by earthquake magnitude and zoom level
                'circle-radius': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    7,
                    ['interpolate', ['linear'], ['get', 'mag'], 1, 1, 6, 4],
                    16,
                    ['interpolate', ['linear'], ['get', 'mag'], 1, 5, 6, 50]
                ],
                // Color circle by earthquake magnitude
                'circle-color': [
                    'interpolate',
                    ['linear'],
                    ['get', 'mag'],
                    1,
                    'rgba(33,102,172,0)',
                    2,
                    'rgb(103,169,207)',
                    3,
                    'rgb(209,229,240)',
                    4,
                    'rgb(253,219,199)',
                    5,
                    'rgb(239,138,98)',
                    6,
                    'rgb(178,24,43)'
                ],
                'circle-stroke-color': 'white',
                'circle-stroke-width': 1,
                // Transition from heatmap to circle layer by zoom level
                'circle-opacity': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    7,
                    0,
                    8,
                    1
                ]
            }
        },
        'waterway-label'
    );

    // When a click event occurs on a feature in
    // the unclustered-point layer, open a popup at
    // the location of the feature, with
    // description HTML from its properties.
    map.on('click', 'readings-point', function(e) {
    
      var coordinates = e.features[0].geometry.coordinates.slice();
      
      // Ensure that if the map is zoomed out such that
      // multiple copies of the feature are visible, the
      // popup appears over the copy being pointed to.
      while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
        coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
      }
      
      new mapboxgl.Popup()
      .setLngLat(coordinates)
      .setHTML("Hi this is a test")
      .addTo(map);
    });

    var nav = new mapboxgl.NavigationControl();
    map.addControl(nav, 'top-left');
  });

  // Change it back to a pointer when it leaves.
  map.on('moveend', function() {
    console.debug("Updating map data source")
    map.getSource('reads').setData(
      formatDataString(map.getCenter(), map.getBounds())
    );
  });
}

export default {
  mounted() {
    this.$nextTick(function () {
      this.getLocation()
    })
  },
  data() {
    return {
      location: null
    }
  },
  methods: {
    getLocation() {
      if ("geolocation" in navigator) { 
        navigator.geolocation.getCurrentPosition(position => { 
          if (position.coords.accuracy > 500) {
            console.info("Terrible GPS accuracy, centering on Edinburgh.")
            this.location = {"lat": 55.953, "long": -3.189};
          } else {
            console.info("Using device location.")
            this.location = {"lat": position.coords.latitude, "long": position.coords.longitude};
          }
          startmap(this.location);
        }); 
      } else {
        console.info("No geolocation, centering on Edinburgh.")
        this.location = {"lat": 55.953, "long": -3.189};
        startmap(this.location);
      }
    }
  }
}
</script>