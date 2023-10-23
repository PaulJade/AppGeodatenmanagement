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

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //Überpruefung der einzelnen Rechte um auf die Position zugreifen zu koennen
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    //Aufruf der Methode um die Position zu erfassen
                    startLocationUpdates();
                }



            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public JsonObject buildJSON(double lat, double lon, String zeit){

        name = myTextView.getText().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp_with_timezone",zeit);
        jsonObject.addProperty("geom","POINT("+lon+" "+lat+")");
        jsonObject.addProperty("name", name);

        return jsonObject;
    }

    /**
     * Methode um JSON-Objekt an API zu senden
     * @param jsonObject
     */
    public void sendJsonToAPI(JsonObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread gestartet ..");
                //Verbindung mit API herstellen
                try {
                    URL url = new URL("http://192.168.0.126:8000/api/addPosition");
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

                    // Lese die Antwort von der APIs
                    int responseCode = httpURLConnection.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Methode zum erfassen der aktuellen Position
     */
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


    /**
     * Methode um aktuellen Zeitpunkt zu erfassen
     * @return
     */
    public String getDateTime(){
        Date currentDate = new Date();
        // Gewünschtes Datumsformat definieren
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Zeitpunkt im gewünschten Format erhalten
        String formattedDate = dateFormat.format(currentDate);
        zeit = formattedDate;
        return zeit;
    }

    /**
     * Überschriebene Methode des LocationListeners um konkreten Positionen zu erfassen und zu senden
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Methode zum erstellen eines JSON-Objektes mit den benötigten Parametern
            JsonObject insertDB = buildJSON(location.getLatitude(), location.getLongitude(), getDateTime());
            //Senden des JSON-Objektes an die API
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


