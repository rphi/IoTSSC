<template>
  <div>
    <h1>My readings</h1>
    <p>View the last {{readings.length}} sensor readings from our API</p>
    <b-table striped hover :busy="busy" :items="readings" show-empty>

      <template v-slot:table-busy>
        <div class="text-center text-danger my-2">
          <b-spinner class="align-middle"></b-spinner>
          <strong>Loading...</strong>
        </div>
      </template>

      <template v-slot:cell(location)="data">
        {{data.value.F}}, {{data.value.V}}
      </template>

      <template v-slot:cell(aqi)="data">
        <strong>{{data.value}}</strong>
      </template>

    </b-table>
  </div>
</template>

<script>
import {Db} from '../db';

export default {
  mounted() {
    let self = this;
    Db.collection("readings")
    .where("auth", "==", this.uid)
    .orderBy("t", "desc")
    .limit(300)
    .get()
    .then(function(querySnapshot) {
      querySnapshot.forEach(function(doc) {
        // doc.data() is never undefined for query doc snapshots
        //console.log(doc.id, " => ", doc.data());
        var d = doc.data();
        self.readings.push({
          time: new Date(d.t.seconds * 1000).toLocaleString(),
          aqi: d.aqi.toFixed(3),
          c2h5oh: d.c2h5oh.toFixed(3),
          c3h8: d.c3h8.toFixed(3),
          ch4: d.ch4.toFixed(3),
          ch4h10: d.ch4h10.toFixed(3),
          co: d.co.toFixed(3),
          dust: d.dust.toFixed(3),
          eco2: d.eco2.toFixed(3),
          h2: d.h2.toFixed(3),
          voc: d.voc.toFixed(3),
          location: d.l
        })
      });
      console.debug("Rendering table");
      self.busy = false;
      //console.log(self.readings);
    })
    .catch(function(error) {
        console.log("Error getting documents: ", error);
        return;
    });
  },
  props: ['uid'],
  data() {
    return {
      readings: [],
      busy: true
    }
  }
}
</script>