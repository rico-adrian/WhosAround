package com.example.ryan.whosaround;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.SearchQuery;
import com.cloudmine.api.db.LocallySavableCMObject;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mLocationClient;
    private GPSTracker mGPSTracker;
    private double mLatitude, mLongitude;
    LatLng currentLocation;
    private Bundle mBundle;
    public static final String TAG = MainActivity.class.getSimpleName();

    // Find this in your developer console
    private static final String APP_ID = "0dfe9797f15da547b66830f30a6b58c0";
    // Find this in your developer console
    private static final String API_KEY = "e49a29df1d2e442985d841f23d9e412e";

    static {
        ClassNameRegistry.register(Person.CLASS_NAME, Person.class);
    }

    ArrayList<Person> people;
    //api key for Google distancematrix
    private static final String apiKey = "AIzaSyAsN80BS-4iMWTpxW1KcjigGgeq4zzHGyQ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //initialize map
        if (initMap()) {
            //get current location from GPS
            mGPSTracker = new GPSTracker(this);
            if (mGPSTracker.canGetLocation()) {
                mLatitude = mGPSTracker.getLatitude();
                mLongitude = mGPSTracker.getLongitude();
                //Drexel lat: 38.479459, long: -94.608566
                Toast.makeText(
                        getApplicationContext(),
                        "Your Location is -\nLat: " + mLatitude + "\nLong: "
                                + mLongitude, Toast.LENGTH_LONG).show();
                createMarker(mLatitude, mLongitude); //create a marker of current location
            } else {
                mGPSTracker.showSettingsAlert();
            }
        } else {
            Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
        }

        //date formatter
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        //get current date & time
        Date date = new Date();
//        System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48

        // initialize CloudMine library
        CMApiCredentials.initialize(APP_ID, API_KEY, getApplicationContext());

//        //Save single Objects to Cloudmine
        // .. initialization code ... & create person
        final Person person1= new Person("Ryan", "Efendy");
        CMGeoPoint location = new CMGeoPoint(mLongitude, mLatitude); // lon, lat
        person1.setLocation(location);
        person1.setLastSeen(dateFormat.format(date));
        person1.save(this, new Response.Listener<ObjectModificationResponse>() {
            @Override
            public void onResponse(ObjectModificationResponse modificationResponse) {
                Toast.makeText(getApplicationContext(), "Person was saved: "+ modificationResponse.getCreatedObjectIds(), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Failed saving person", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed saving car", volleyError);
            }
        });

        //Geo search, query for objects near current location
        LocallySavableCMObject.searchObjects(this, SearchQuery.filter("location").near(mLongitude, mLatitude).searchQuery(),
                new Response.Listener<CMObjectResponse>() {
                    @Override
                    public void onResponse(CMObjectResponse objectResponse) {
                        //store objects to arraylist of person
                        people = (ArrayList<Person>) objectResponse.getObjects (Person.class);
                        //create a market for every person
                        for(Person person : people){
                            createMarker(person.getLocation().getLatitude(), person.getLocation().getLongitude(), person.getFirstName() + " " + person.getLastName());
                            //use Google Distance Matrix api to get distance in mile
                            MyBigTask myBigTask = new MyBigTask(person.getLocation().getLatitude(), person.getLocation().getLongitude());
                            try {
                                //set mile
                                person.setMiles(myBigTask.execute().get());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //create marker for current location
    private void createMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("My location");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    //overloaded method to create marker for people in cloudmine database
    private void createMarker(double latitude, double longitude, String locality) {
        //(double latitude, double longitude)
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(latLng);
        mMap.addMarker(options);
    }

    //display menu on the top right hand side
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //when menu is clicked, use intent to go to the next activity and pass an arraylist of person
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Add menu handling code
        switch (id) {
            case R.id.item1:
                Toast.makeText(getApplicationContext(), "item1", Toast.LENGTH_SHORT).show();
                this.parcelable();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //passing object by Intent via parcelable
    public void parcelable() {
        Intent mIntent = new Intent(this, MainActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelableArrayList("mylist", people);
        mIntent.putExtras(mBundle);
        startActivity(mIntent);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }
        return (mMap != null);
    }

    //create a new thread to perfrom network call so UI remains responsive, make a call to google distance matrix API
    //using user's current location and the people lot, lang in the database to get the distance in mile
    public class MyBigTask extends AsyncTask<Void, Void, String> {

        Double latitude, longitude;

        public MyBigTask(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(Void... params) {
            //1. create okHttp Client object
            OkHttpClient client = new OkHttpClient();

            //2. Define reqest being sent to the server
            RequestBody postData = new FormBody.Builder()
                    .add("type", "json")
                    .build();

            Request request = new Request.Builder()
//                    .url("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=39.956821%20-75.189804&destinations=39.925599%20-75.176236%20&key=AIzaSyAsN80BS-4iMWTpxW1KcjigGgeq4zzHGyQ")
                    .url("https://maps.googleapis.com/maps/api/distancematrix/json?units=" +
                            "imperial&origins="+mLatitude+"%20"+mLongitude+"%20&destinations="+latitude+"%20"+longitude+"%20&" +
                            "key=AIzaSyAsN80BS-4iMWTpxW1KcjigGgeq4zzHGyQ")
                    .post(postData)
                    .build();

            //3. Transport the request and wait for response to process next
            okhttp3.Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result = "";
            try {
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonParser jp = new JsonParser(); //create the Json
            JsonElement root = jp.parse(result); //read the data using InputStreamReader
            JsonObject rootobj = root.getAsJsonObject();
            String distance = rootobj.get("rows").getAsJsonArray().get(0).getAsJsonObject().get("elements").getAsJsonArray().get(0).getAsJsonObject().get("distance").getAsJsonObject().get("text").getAsString();
            System.out.println(distance);
            return distance;
        }
    }
}
