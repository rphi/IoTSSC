<template>
  <div>
    <h1>My readings</h1>
    <b-overlay :show="show" rounded="sm">
      <b-table striped hover :items="readings"></b-table>
    </b-overlay>
  </div>
</template>

<script>
import {Db} from '../db';

export default {
  mounted() {
    let self = this;
    Db.collection("readings")
    .where("auth", "==", this.uid)
    .get()
    .then(function(querySnapshot) {
      querySnapshot.forEach(function(doc) {
        // doc.data() is never undefined for query doc snapshots
        console.log(doc.id, " => ", doc.data());
        self.readings.push({
          id: doc.id,
          data: doc.data()
        })
      });
      self.show = false;
      console.log(self.readings);
    })
    .catch(function(error) {
        console.log("Error getting documents: ", error);
    });
  },
  props: ['uid'],
  data() {
    return {
      readings: [],
      show: true
    }
  }
}
</script>