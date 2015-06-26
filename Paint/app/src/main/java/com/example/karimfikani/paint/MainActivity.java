package com.example.karimfikani.paint;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, SensorEventListener {

    private MenuFragment menuFragment;
    private DrawingFragment drawingFragment;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagnetometer;
    private float[] accelerationCoords = new float[3];
    private float[] geomagneticCoords = new float[3];
    private Boolean rotation = false;
    private Button rotateButton;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the fragments to set a communication between them
        menuFragment = (MenuFragment) getFragmentManager().findFragmentById(R.id.menu_fragment);
        drawingFragment = (DrawingFragment) getFragmentManager().findFragmentById(R.id.drawing_fragment);

        // setting up the buttons for the menu fragment
        Button clearDrawingButton = (Button) menuFragment.getView().findViewById(R.id.clearButton);
        clearDrawingButton.setOnClickListener(this);

        Button toggleColorButton = (Button) menuFragment.getView().findViewById(R.id.toggleColorButton);
        toggleColorButton.setOnClickListener(this);

        Button undoButton = (Button) menuFragment.getView().findViewById(R.id.undoButton);
        undoButton.setOnClickListener(this);

        Button redoButton = (Button) menuFragment.getView().findViewById(R.id.redoButton);
        redoButton.setOnClickListener(this);

        rotateButton = (Button) menuFragment.getView().findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(this);

        // sensor setup
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearButton:
                drawingFragment.clearDrawing();
                break;

            case R.id.toggleColorButton:
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, 0xFF00FF00, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        // color is the color selected by the user.
                        drawingFragment.setColor(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                });
                dialog.show();
                break;

            case R.id.undoButton:
                drawingFragment.undo();
                break;

            case R.id.redoButton:
                drawingFragment.redo();
                break;

            case R.id.rotateButton:
                rotation = !rotation;
                rotateButton.setText(rotation ? R.string.rotateOff : R.string.rotateOn);

                break;

            default:
                Log.v("MenuFragment", "Button onClick not handled!");
        }
    }

    /**
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!rotation) return;

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerationCoords = sensorEvent.values;
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticCoords = sensorEvent.values;
        }
        if (accelerationCoords != null && geomagneticCoords != null) {
            float R[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, null, accelerationCoords, geomagneticCoords);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation); // orientation contains: yaw, pitch and roll
                float yaw = (float)(-orientation[0]*360.0f / (2.0f*Math.PI));
                drawingFragment.rotate(yaw);
            }
        }
    }

    /**
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     *
     */
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /**
     *
     */
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
