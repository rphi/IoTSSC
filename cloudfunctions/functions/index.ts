import * as firebase from 'firebase/app';
import 'firebase/firestore';
const admin = require('firebase-admin');
const functions = require('firebase-functions');
import { GeoCollectionReference, GeoFirestore, GeoQuery, GeoQuerySnapshot } from 'geofirestore';


admin.initializeApp(functions.config().firebase);

let db = admin.firestore();

// Create a GeoFirestore reference
const geofirestore: GeoFirestore = new GeoFirestore(db);

// Create a GeoCollection reference
const geocollection: GeoCollectionReference = geofirestore.collection('georeadings');

const express = require('express');
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
  } catch(e) {
    res.status(403).send('Unauthorized');
    return;
  }
};

app.use(authenticate);

app.post('/api/georeading', async (req, res) => {
  const loc = req.body.location;
  const airquality = req.body.quality;
  const time = new Date(req.body.timestamp);
  const user = req.body.user;

  try {
    // Add a GeoDocument to a GeoCollection
    geocollection.add({
      user: user,
      airquality: airquality,
      time: time,
      // The coordinates field must be a GeoPoint!
      coordinates: new firebase.firestore.GeoPoint(loc[0], loc[1])
    })

    res.status(201);
  } catch(error) {
    console.log('Error writing data to geofirestore', error.message);
    res.sendStatus(500);
  }
});

// Expose the API as a function
exports.api = functions.https.onRequest(app);