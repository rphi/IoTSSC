<template>
  <div>
    <h1>My alerts</h1>
    <p>You can view and delete alerts you have setup from the app here.</p>
    <b-table striped hover :busy="busy" :items="alerts" show-empty>

      <template v-slot:table-busy>
        <div class="text-center text-danger my-2">
          <b-spinner class="align-middle"></b-spinner>
          <strong>Loading...</strong>
        </div>
      </template>

      <template v-slot:cell(delete)="data">
        <b-button variant="danger" v-on:click="deleteAlert(data.value)">Delete</b-button>
      </template>

      <template v-slot:cell(coordinates)="data">
        {{data.value.F}}, {{data.value.V}}
      </template>

    </b-table>
  </div>
</template>

<script>
import {Db} from '../db';

export default {
  mounted() {
    this.updateAlerts();
  },
  props: ['uid'],
  data() {
    return {
      alerts: [],
      busy: true
    }
  },
  methods: {
    updateAlerts() {
      this.alerts = [];
      let self = this;
      Db.collection("notificationSubscribers")
      .where("d.auth", "==", this.uid)
      .get()
      .then(function(querySnapshot) {
        querySnapshot.forEach(function(doc) {
          // doc.data() is never undefined for query doc snapshots
          console.log(doc.data());
          var d = doc.data();
          self.alerts.push({
            name: d.d.name,
            coordinates: d.d.coordinates,
            limit: d.d.limit,
            delete: doc.id
          })
        });
        self.busy = false;
        //console.log(self.readings);
      })
      .catch(function(error) {
          console.log("Error getting documents: ", error);
      });
    },
    deleteAlert(id) {
      this.busy = true;
      console.log(id);

      let self = this;
      Db.collection("notificationSubscribers")
      .doc(id)
      .delete()
      .then(function() {
          self.updateAlerts();
      }).catch(function(error) {
          alert("Error removing document.");
          console.error("Error removing document: ", error);
          self.updateAlerts();
      });

      return;
    }
  }
}
</script>