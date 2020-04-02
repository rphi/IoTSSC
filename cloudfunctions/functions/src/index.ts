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
  let limit = Math.min(10000, Number(req.query.limit));

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

function calculateAQI(latestReading, oldValue, max_based_on = 4) {
  if (oldValue == null || oldValue.d.basedOn == undefined){
    return [latestReading.aqi, 1]
  } else {
    return [(latestReading.aqi + oldValue.d.score * oldValue.d.basedOn)/(oldValue.d.basedOn + 1), Math.min(oldValue.d.basedOn + 1,max_based_on)]
  }
}

function notify_subscribers(point: admin.firestore.GeoPoint, newValue){
  //get all subscribers in 100m with a lower threshold than recorded value
  const query: GeoQuery = geofirestore.collection('notificationSubscribers').near({ center: point, radius: 0.100 });

  // Get query (as Promise)
  query.get().then((value: GeoQuerySnapshot) => {
    // All GeoDocument returned by GeoQuery, like the GeoDocument added above
    value.docs.forEach(subscriberDocument => {
      const data = subscriberDocument.data();
      console.log(data)
      if (data.limit <= newValue) {
        const message = {
          data: {
            name: data['name'],
            alertingValue: String(newValue)
          },
          token: data['registrationToken']
        };

        // Send a message to the device corresponding to the provided
        // registration token.
        admin.messaging().send(message)
          .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
          })
          .catch((error) => {
            console.log('Error sending message:', error);
          });
      }
      
    });
  }).catch(e=> {throw e});
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
      snap.ref.update({'l':geopoint, 't': new Date()}).catch(e=> {console.log("failed to add geopoint to reading: ", e)});
      notify_subscribers(geopoint, newValue.aqi);

      let current_date_string = new Date().toJSON().slice(0,10).replace(/-/g,'-');
      console.log(newValue)
      if (newValue.auth != undefined){
        let user_score_doc_reference = db.collection('userscores').doc(`${current_date_string}/scores/${newValue.auth}`)
        user_score_doc_reference.get().then(documentSnapshot => {
          console.log(documentSnapshot)
          if (!documentSnapshot.exists) {
            user_score_doc_reference.create({score: 500-newValue.aqi, name: newValue.username}).catch(e=> {console.log("failed to start new score: ", e)})
          } else {
            user_score_doc_reference.set({score: (500-newValue.aqi) + documentSnapshot.get('score'), name: newValue.username}).catch(e=> {console.log("failed to update score: ", e)})
          }
        }).catch(e=> {throw e});
        //.log("before user exposure block")
          let user_exposure = db.collection('userexposure').doc(`${newValue.auth}`)
          user_exposure.get().then(documentSnapshot => {
              //console.log("in user exposure block")
              console.log(documentSnapshot)
              if (!documentSnapshot.exists) {
                  let new_data = {d:{basedOn:1, score: newValue.aqi}};
                  new_data[current_date_string] = {exposureTotal: newValue.aqi};
                  user_exposure.create(new_data).catch(e=> {console.log("failed to start new exposure tracker: ", e)});
              } else {
                  let todays_data = documentSnapshot.get(current_date_string);
                  let updateData = {d: {basedOn:0, score:0}};
                  updateData[current_date_string] = {exposureTotal: 0};
                  if (todays_data == undefined) {
                      updateData[current_date_string].exposureTotal = newValue.aqi
                  } else {
                      updateData[current_date_string].exposureTotal = newValue.aqi + documentSnapshot.get(current_date_string + ".exposureTotal")
                  }
                  let x = calculateAQI(newValue, documentSnapshot.data(), 60*10);
                  updateData.d.basedOn = x[1];
                  updateData.d.score = x[0];
                  console.log(updateData);
                  user_exposure.set(updateData, {merge: true}).catch(e=> {console.log("failed to update score: ", e)})
              }
          }).catch(e=> {throw e});
      }

      return db.collection('georeadings').where("l", "==", geopoint).get().then(querySnapshot => {
        console.log(querySnapshot)
        if (!querySnapshot.empty) {
          querySnapshot.forEach(documentSnapshot => {
            console.log(`Found document at ${documentSnapshot.id}`);
            console.log(documentSnapshot.data())
            let values = calculateAQI(newValue, documentSnapshot.data())
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

exports.addMessage = functions.https.onCall((data, context) => {
  console.log(data);
  geofirestore.collection('notificationSubscribers').add({
    name: data.name,
    registrationToken: data.registrationToken,
    limit: data.limit,
    // The coordinates field must be a GeoPoint!
    coordinates: new admin.firestore.GeoPoint(data.lat, data.long),
    auth: context.auth.uid
  }).catch(e=> {throw e});
});
