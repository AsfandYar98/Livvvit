package com.app.livit.utils;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rémi OLLIVIER on 22/05/2018.
 */

public class MapUtils {

    /**
     * This static method formats the url to request from Google API directions
     * @param origin the origin position
     * @param dest the destination position
     * @return the url to use to make this request
     */
    private static String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = strOrigin + "&" + strDest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?key=AIzaSyAq6E0BZetEIFTLc-1GPKvv_dRt4RSjmX0&" + parameters;
    }

    /**
     * This static method calls the url and returns the result as a string
     * @param origin the origin place
     * @param dest the destination place
     * @return the result as a string
     * @throws IOException if a network error occurred
     */
    public static String getDeliveryPath(LatLng origin, LatLng dest) throws IOException {
        String data = "";
        HttpURLConnection urlConnection;
        URL url = new URL(getDirectionsUrl(origin, dest));

        Log.d("URL for Path", url.toString());
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.connect();

        try (InputStream iStream = urlConnection.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * This asynctask is used to get the path between two points on the map
     */
    public static class GetPathTask extends AsyncTask<LatLng, Void, String> {

        private PathTaskResponse response;

        public GetPathTask(PathTaskResponse response) {
            this.response = response;
        }

        @Override
        protected String doInBackground(LatLng... positions) {

            String data = "";

            try {
                data = getDeliveryPath(positions[0], positions[1]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask(response);
            parserTask.execute(result);
        }
    }

    /**
     * This asynctask is used to get the results we need from Google directions API's response
     */
    private static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        private PathTaskResponse response;

        ParserTask(PathTaskResponse response) {
            this.response = response;
        }

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            for (String aJsonData : jsonData) Log.d("jsonData", aJsonData);

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (result == null) {
                response.onFailed();
                return;
            }

            ArrayList<LatLng> points;
            PolylineOptions currentLine = new PolylineOptions();

            String distance = "";
            if (!result.isEmpty() && !result.get(0).isEmpty()) {
                distance = result.get(0).get(0).get("distance");
                Log.d("Distance", distance);
                result.get(0).remove(0);
            }

            for (int i = 0; i < result.size(); i ++) {
                points = new ArrayList<>();

                List<HashMap<String, String>> path = result.get(i);

                //loop to get every position of the path
                for (int j = 0; j < path.size(); j ++) {
                    HashMap point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat").toString());
                    double lng = Double.parseDouble(point.get("lng").toString());
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                currentLine.addAll(points);
                currentLine.width(12);
                currentLine.color(Color.BLUE);
                currentLine.geodesic(true);
            }
            response.onResult(currentLine, distance);
        }
    }

    /**
     * This static method is used to get the address from a position
     * @param latitude the latitude
     * @param longitude the longitude
     * @return the address
     */
    public static Address getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(Utils.getContext(), Locale.getDefault());
        try {
            return geocoder.getFromLocation(latitude, longitude, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Interface used to return the result or the failure
     */
    public interface PathTaskResponse {
        void onResult(PolylineOptions currentLine, String distance);
        void onFailed();
    }
}
