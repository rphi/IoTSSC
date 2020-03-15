import 'firebase/firestore';
import * as firebase from 'firebase/app';
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

import * as GeoJSON from 'geojson';
import { GeoCollectionReference, GeoFirestore, GeoQuery, GeoQuerySnapshot } from 'geofirestore';

admin.initializeApp(functions.config().firebase);

const db = admin.firestore();

// Create a GeoFirestore reference
const geofirestore: GeoFirestore = new GeoFirestore(db);

// Create a GeoCollection reference
const geocollection: GeoCollectionReference = geofirestore.collection('georeadings');

import * as express from 'express';

const app = express();

// Express middleware that validates Firebase ID Tokens passed in the Authorization HTTP header.
// The Firebase ID token needs to be passed as a Bearer token in the Authorization HTTP header like this:
// `Authorization: Bearer <Firebase ID Token>`.
// when decoded successfully, the ID Token content will be added as `req.user`.
const authenticate = async (req, res, next) => {
    if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
        res.status(403).send('Unauthorized');
        return;
    }
    const idToken = req.headers.authorization.split('Bearer ')[1];
    try {
        const decodedIdToken = await admin.auth().verifyIdToken(idToken);
        req.user = decodedIdToken;
        next();
        return;
    } catch (e) {
        res.status(403).send('Unauthorized');
        return;
    }
};

//app.use(authenticate);

app.get('/', async (req, res) => {
    res.send("Hi there");
});

app.get('/points', async (req, res) => {
  let lat = Number(req.query.lat);
  let long = Number(req.query.long);
  let radius = Number(req.query.radius);
  let limit = Number(req.query.limit);

  res.set('Access-Control-Allow-Origin', '*');

  // Create a GeoQuery based on a location
  const query: GeoQuery = geocollection.near({ center: new admin.firestore.GeoPoint(lat, long), radius: radius }).limit(limit);
  query.get().then((value: GeoQuerySnapshot) => {
    // All GeoDocument returned by GeoQuery, like the GeoDocument added above
    var reads = [];
    value.docs.forEach(doc => {
      let data = doc.data();
      console.log(data);
      reads.push({
        name: data.name,
        score: data.score,
        lat: data.coordinates._latitude,
        long: data.coordinates._longitude
      })
    });
    let gjson = GeoJSON.parse(reads, {Point: ['lat', 'long']});
    res.send(gjson);
  }).catch((reason) => {
    res.send(`Error: ${reason}`)
  });
});


// Expose the API as a function
exports.api = functions.https.onRequest(app);

function limit_precision(value) {
  let inflated_value = value * 2 * 1000;
  return Math.round(inflated_value) / (2 * 1000)
}

function calculateAQI(latestReading, oldValue) {
  if (oldValue == null || !('basedOn' in oldValue.d)){
    return [latestReading.aqi, 1]
  } else {
    return [(latestReading.aqi + oldValue.d.basedOn)/(oldValue.d.basedOn + 1), Math.min(oldValue.d.basedOn + 1,4)]
  }
}

exports.updateReading = functions.firestore
    .document('readings/{readingId}')
    .onCreate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const newValue = snap.data();

      // access a particular field as you would any JS property
      const lat = limit_precision(newValue.lat);
      const long = limit_precision(newValue.long);

      let geopoint = new admin.firestore.GeoPoint(lat, long);

      let current_date_string = new Date().toJSON().slice(0,10).replace(/-/g,'/');
      let user_score_doc_reference = db.collection('userscores').doc(`${context.auth.uid}@${current_date_string}`)
      user_score_doc_reference.get().then(documentSnapshot => {
        console.log(documentSnapshot)
        if (!documentSnapshot.exists) {
          user_score_doc_reference.create({score: 500-newValue.aqi}).catch(e=> {console.log("failed to start new score: ", e)})
        } else {
          user_score_doc_reference.set({score: (500-newValue.aqi) + documentSnapshot.get('score')}).catch(e=> {console.log("failed to update score: ", e)})
        }

      return db.collection('georeadings').where("l", "==", geopoint).get().then(querySnapshot => {
        console.log(querySnapshot)
        if (!querySnapshot.empty) {
          querySnapshot.forEach(documentSnapshot => {
            console.log(`Found document at ${documentSnapshot.id}`);
            console.log(documentSnapshot.data)
            let values = calculateAQI(newValue, documentSnapshot.data)
            db.collection('georeadings').doc(documentSnapshot.id).update({'d.score': values[0], 'd.basedOn': values[1]}).catch(e=> {throw e})
          });
        } else {
          geocollection.add({
            name: 'Geofirestore',
            score: calculateAQI(newValue, null)[0],
            basedOn: 1,
            // The coordinates field must be a GeoPoint!
            coordinates: geopoint
          }).catch(e=> {throw e});
        }
      }).catch(e=> {throw e});
      
      /*geocollection.add({
            name: 'Geofirestore',
            score: calculateAQI(newValue, null),
            // The coordinates field must be a GeoPoint!
            coordinates: geopoint
      }).catch(e=> {throw e});
      */
        /*db.collection("heatmapdata")
            .where("lat", "==", newValue.lat)
            .where("long", "==", newValue.long)
            .get()
            .then(querySnapshot => {
              if (querySnapshot.length > 0) {
                querySnapshot.forEach(documentSnapshot => {
                  console.log(`Found document at ${documentSnapshot.ref.path}`);
                  documentSnapshot.set({aqi: calculateAQI(newValue)})
                });
              } else {
                db.collection("heatmapdata").doc().set({aqi:})
              }
        });
        db.doc('heatmap').set({ ... });
        */
    });
