<template>
  <div>
    <h1>Leaderboard (today)</h1>
    <b-table striped hover :items="scores"></b-table>
  </div>
</template>

<script>
import {Db} from '../db';

export default {
  mounted() {
    let self = this;
    Db.collection("userscores")
    .doc(`${new Date().toISOString().split('T')[0]}`)
    .collection('scores')
    .orderBy('score', 'desc')
    .get()
    .then(function(querySnapshot) {
      var count = 1;
      querySnapshot.forEach(function(doc) {
        // doc.data() is never undefined for query doc snapshots
        console.log(doc.id, " => ", doc.data());
        self.scores.push({
          Position: count,
          Name: doc.data()["name"],
          Score: doc.data()["score"]
        })
        count++;
      });
    })
    .catch(function(error) {
        console.log("Error getting documents: ", error);
    });
  },
  data() {
    return {
      scores: []
    }
  }
}
</script>