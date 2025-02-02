package com.example.stepcounterapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SensorEventListener stepListener;
    private TextView stepCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        stepCountText = findViewById(R.id.stepCountText);

        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Check if Step Counter Sensor is available
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        } else {
            Toast.makeText(this, "Step Counter sensor not available!", Toast.LENGTH_LONG).show();
            return;
        }

        // Request runtime permission for activity recognition (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        } else {
            startStepCounting();
        }
    }

    private void startStepCounting() {
        // Define the step listener
        stepListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Display the step count on the screen
                if (event != null) {
                    float stepCount = event.values[0];
                    stepCountText.setText("Steps: " + (int) stepCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not needed for this sensor
            }
        };

        // Register the listener
        sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the listener to save battery
        if (stepListener != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounting();
            } else {
                Toast.makeText(this, "Permission denied. Cannot track steps.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
