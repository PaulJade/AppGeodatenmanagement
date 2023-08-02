package com.example.appgeodatenmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
    private Button myAusgabeButton;
    private String name = "Name";
    private static final int REQUEST_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    Boolean erfolgreich = null;
    String fehler = "";
    private double latitude = 0;
    private double longitude = 0;
    private String zeit = "2023-07-28 12:34:56+02";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myTextView = findViewById(R.id.nameTextfeld);
        myButton = findViewById(R.id.button);
        zugriff = findViewById((R.id.standortZugriffText));


        myButton.setOnClickListener(new View.OnClickListener() {
        Boolean erfolgreich = null;
            @Override
            public void onClick(View v) {

                System.out.println(myTextView.getText());
                name = myTextView.getText().toString();
                System.out.println(name);
                //sendLocation();
                getLastLocation();
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void sendLocation(){
        System.out.println("Status");
        System.out.println(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
    }

    public void getLastLocation(){
        System.out.println("Methode startet");
        sendLocation();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
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
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                send2Postgis(longitude, latitude, zeit);
                                erfolgreich = true;
                                //myTextView.setText("Erfolgreich");
                            } catch (SQLException throwables) {
                                System.out.println("Verbindung fehlgeschlagen");
                                erfolgreich = false;
                                //myTextView.setText("Verbindung fehlgeschlagen");
                                fehler = throwables.getMessage();
                                throwables.printStackTrace();
                            }
                            // A potentially time consuming task.


                        }
                    }).start();



                }else {
                    System.out.println("location = null");
                }

            });
        } else            {
            zugriff.setVisibility(View.VISIBLE);
            System.out.println("hallo");
        }
    }

    public void send2Postgis (double lat ,double lon , String zeit) throws SQLException {
        Connection db = DriverManager.getConnection(
                "jdbc:postgresql://psql-t-01.fbbgg.hs-woe.de:5435/pnolte",
                "pnolte",
                "x"
                );

        System.out.println("Verbindung erfolgreich");
    }

}


