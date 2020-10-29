package com.production.speedometr.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import rebus.permissionutils.AskagainCallback;
import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

import com.production.speedometr.R;
import com.production.speedometr.utils.DatabaseHelper;
import com.production.speedometr.utils.Speedometer;

public class ActivitySensor extends AppCompatActivity implements LocationListener {

    SensorManager sensorManager;
    Context ctx;
    Sensor sensor;
    TextView tvSensor;
    Button button1, button2, datas;
    Speedometer speedometer;
    private int _samplePeriod = 15;
    private long _lastTick = System.currentTimeMillis();

    Location locA;
    Location locB;
    TextView finalDist;
    String startTime, endTime, today;

    DatabaseHelper databaseHelper;
    List<Float> speedsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        initialiseElements();

        databaseHelper = new DatabaseHelper(this);

        button1.setOnClickListener(view -> {
            speedsArray.clear();
            today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "." +
                    (Calendar.getInstance().get(Calendar.MONTH) + 1) + "." +
                    Calendar.getInstance().get(Calendar.YEAR);

            startTime = Calendar.getInstance().getTime().getHours() + ":" +
                Calendar.getInstance().getTime().getMinutes() + ":" +
                Calendar.getInstance().getTime().getSeconds();

            finalDist.setVisibility(View.GONE);
            finalDist.setText("");
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locA = new Location("");
                locA.setLatitude(latitude);
                locA.setLongitude(longitude);
            }
        });
        button2.setOnClickListener(view -> {
            endTime = Calendar.getInstance().getTime().getHours() + ":" +
                    Calendar.getInstance().getTime().getMinutes() + ":" +
                    Calendar.getInstance().getTime().getSeconds();

            if (locA != null) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locB = new Location("");
                locB.setLatitude(latitude);
                locB.setLongitude(longitude);

                finalDist.setVisibility(View.VISIBLE);
                finalDist.setText(String.valueOf(locA.distanceTo(locB) + " метров"));
            }

            double speeds = 0;
            for (Float fl : speedsArray) {
                speeds += fl;
            }

            speeds /= speedsArray.size();
            speedsArray.clear();

            String newEntry = "Дата: " + today + ", время старта: " + startTime + ", время финиша: " + endTime;
            String newEntry1 = ", Дистанция: ";
            if (locA != null) {
                newEntry1 += locA.distanceTo(locB) + " метров";
            } else {
                newEntry1 = "Не удалось получить геоданные";
            }
            newEntry1 += ", Средняя скорость: " + speeds;
            addData(newEntry, newEntry1);
        });
        datas.setOnClickListener(view -> {
            startActivity(new Intent(ActivitySensor.this, ListDataActivity.class));
        });
    }

    public void addData(String newEntry, String newEntry1) {
        boolean insertData = databaseHelper.addData(newEntry, newEntry1);
        if (insertData) {
            Toast.makeText(this, "Сохранено", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Не сохранено", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionManager.with(ActivitySensor.this)
                .permission(PermissionEnum.ACCESS_COARSE_LOCATION, PermissionEnum.ACCESS_FINE_LOCATION)
                .askagain(true)
                .askagainCallback(new AskagainCallback() {
                    @Override
                    public void showRequestPermission(UserResponse response) {
                        showDialog(response);
                    }
                })
                .callback(new FullCallback() {
                    @Override
                    public void result(ArrayList<PermissionEnum> permissionsGranted, ArrayList<PermissionEnum> permissionsDenied, ArrayList<PermissionEnum> permissionsDeniedForever, ArrayList<PermissionEnum> permissionsAsked) {
                    }
                })
                .ask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(requestCode, permissions, grantResults);
    }

    private void showDialog(final AskagainCallback.UserResponse response) {
        new AlertDialog.Builder(ActivitySensor.this)
                .setTitle("Permission needed")
                .setMessage("This app realy need to use this permission, you wont to authorize it?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(true);
                    }
                })
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        response.result(false);
                    }
                })
                .show();
    }

    private void initialiseElements() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(accelListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        tvSensor = (TextView) findViewById(R.id.sensorX);
        speedometer = (Speedometer) findViewById(R.id.speedometer);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        datas = (Button) findViewById(R.id.datas);
        finalDist = (TextView) findViewById(R.id.finalDist);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelListener);
    }

    SensorEventListener accelListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            String vendor = event.sensor.getVendor();
           // Toast.makeText(ActivitySensor.this, vendor, Toast.LENGTH_SHORT).show();
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                long tick = System.currentTimeMillis();
                long localPeriod = tick - _lastTick;

                if (localPeriod > _samplePeriod) {
                    _lastTick = tick;
                    double motion = Math.sqrt(Math.pow(event.values[0], 2) +
                            Math.pow(event.values[1], 2) +
                            Math.pow(event.values[2], 2));

                    // Warn the activity that we sampled a new value.
                    tvSensor.setText(motion + "");
                    float speedKM=(float) (motion * 3.6);
                    speedometer.onSpeedChanged(speedKM);

                    speedsArray.add(speedKM);

                    if (speedKM>50)
                    {
                        speedometer.setBackgroundColor(getResources().getColor(R.color.red));
                        tvSensor.setText("HIGH");
                        tvSensor.setTextColor(getResources().getColor(R.color.red));
                    }
                    else if (speedKM>25)
                    {
                        tvSensor.setText("AVERAGE");
                        tvSensor.setTextColor(getResources().getColor(R.color.green));
                        speedometer.setBackgroundColor(getResources().getColor(R.color.green));

                    }
                    else
                    {
                        tvSensor.setText("LOW");
                        tvSensor.setTextColor(getResources().getColor(R.color.blue));
                        speedometer.setBackgroundColor(getResources().getColor(R.color.blue));
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}
