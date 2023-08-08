package com.example.appgeodatenmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private TextView zugriff;
    private Button myButton;
    private String name = "Name";
    private static final int REQUEST_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    Boolean erfolgreich = null;
    String fehler = "";
    private double latitude = 0;
    private double longitude = 0;
    private String zeit = "2023-07-28 12:34:56+02";

    //Textviews
    private TextView longiLast;
    private TextView latiiLast;
    private TextView datiLast;

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private LocationManager locationManager;




    Handler uiHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        uiHandler = new Handler(Looper.getMainLooper());

        myTextView = findViewById(R.id.nameTextfeld);
        myButton = findViewById(R.id.button);
        zugriff = findViewById((R.id.textView2));
        zugriff.setVisibility(View.INVISIBLE);

        longiLast = findViewById(R.id.longi);
        latiiLast = findViewById(R.id.lati);
        datiLast = findViewById(R.id.datum);



        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    startLocationUpdates();
                }

                //System.out.println(myTextView.getText());

                //getLastLocationAPI();

            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void sendLocation() {
        System.out.println("Status");
        System.out.println(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
    }


    public void getLastLocationAPI(){
        for (int i = 0; i < 3; i++) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {

                        System.out.println(location.getLatitude());
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();



                        longiLast.setText(" " + longitude);
                        latiiLast.setText(" " + latitude);
                        datiLast.setText(" " + zeit);

                        System.out.println("JSON Aufbau");
                        JsonObject insertDB = bouildJSON(latitude, longitude, zeit);
                        sendJsonToAPI(insertDB);

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("location = null");
                    }

                });
            } else {
                zugriff.setVisibility(View.VISIBLE);
                System.out.println("hallo");
            }
        }


    }

    public void getLastLocation() {
        for (int i = 0; i < 3; i++) {

            System.out.println("Methode startet");

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Methode geht in if");
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {

                        System.out.println(location.getLatitude());
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Date currentDate = new Date();


                        // Gewünschtes Datumsformat definieren
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        // Zeitpunkt im gewünschten Format erhalten
                        String formattedDate = dateFormat.format(currentDate);
                        zeit = formattedDate;

                        System.out.println(latitude);
                        System.out.println(longitude);
                        System.out.println(zeit);
                        longiLast.setText(" " + longitude);
                        latiiLast.setText(" " + latitude);
                        datiLast.setText(" " + zeit);


                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    send2Postgis(longitude, latitude, zeit);
                                    erfolgreich = true;
                                    //Das hier müsste eig dafür sorgen, dass bei jedem Durchlauf


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            System.out.println("geht in RunUI");
                                            longiLast.setText(" " + longitude);
                                            latiiLast.setText(" " + latitude);
                                            datiLast.setText(" " + zeit);
                                        }
                                    });


                                    //myTextView.setText("Erfolgreich");
                                } catch (SQLException throwables) {

                                    erfolgreich = false;
                                    //myTextView.setText("Verbindung fehlgeschlagen");
                                    fehler = throwables.getMessage();
                                    throwables.printStackTrace();
                                }

                                // A potentially time consuming task.


                            }
                        }).start();


                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        System.out.println("Text sollte gesetzt werden");
                        longiLast.setText(" " + longitude);
                        latiiLast.setText(" " + latitude);
                        datiLast.setText(" " + zeit);


                    } else {
                        System.out.println("location = null");
                    }

                });
            } else {
                zugriff.setVisibility(View.VISIBLE);
                System.out.println("hallo");
            }
        }
    }

    public void send2Postgis(double lat, double lon, String zeit) throws SQLException {
        Connection db = DriverManager.getConnection(
                "jdbc:postgresql://psql-t-01.fbbgg.hs-woe.de:5435/pnolte",
                "pnolte",
                "x"
        );

        Statement anweisung = db.createStatement();
        String sql = "INSERT INTO spatiotemporal_data (geom, timestamp, name) VALUES (ST_SetSRID(ST_MakePoint(" + longitude + "," + latitude + "), 4326), '" + zeit + "', '" + name + "')";
        System.out.println(sql);
        int erg = anweisung.executeUpdate(sql);

        anweisung.close();

        System.out.println("Verbindung erfolgreich");
    }

    public JsonObject bouildJSON(double lat, double lon, String zeit){

        name = myTextView.getText().toString();
        System.out.println(zeit + " " + lon + " " + lat + " " + name);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp_with_timezone",zeit);
        jsonObject.addProperty("geom","POINT("+lon+" "+lat+")");
        jsonObject.addProperty("name", name);

        return jsonObject;
    }

    public void sendJsonToAPI(JsonObject jsonObject) {


        new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Thread gestartet ..");
                try {
                    URL url = new URL("http://192.168.0.126:5000/api/room");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                    httpURLConnection.setRequestProperty("Accept", "application/json");
                    httpURLConnection.setDoOutput(true);

                    // Sende das JSON-Objekt an die API
                    try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                        byte[] input = jsonObject.toString().getBytes("utf-8");
                        outputStream.write(input, 0, input.length);
                    }
                    System.out.println("Erfolgreich die Daten");

                    // Lese die Antwort von der API
                    int responseCode = httpURLConnection.getResponseCode();
                    // Hier kannst du den Response-Code und die Response-Body verarbeiten
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void startLocationUpdates() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }
            }
        };

        handler.post(runnable);
    }


    public String getDateTime(){
        Date currentDate = new Date();
        // Gewünschtes Datumsformat definieren
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Zeitpunkt im gewünschten Format erhalten
        String formattedDate = dateFormat.format(currentDate);
        zeit = formattedDate;
        return zeit;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(MainActivity.this, "Position: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();


            System.out.println("10Sek");
            JsonObject insertDB = bouildJSON(location.getLatitude(), location.getLongitude(), getDateTime());
            sendJsonToAPI(insertDB);

        }

        // Implement other required methods of LocationListener if needed
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

}


