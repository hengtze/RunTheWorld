package edu.cmu.runtheworld;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassListener implements SensorEventListener {
	
	private SensorManager sensorManager;
	
	// Sensors
	private Sensor compass;
	
	// Sensor Values
	private double[] orientation;
	
	public CompassListener(Context c) {
		sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		orientation = new double [3];
	}
	
	public void startListening() {
		sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stopListening() {
		try {
			sensorManager.unregisterListener(this);
		}
		catch(IllegalArgumentException e) {			
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		orientation[0] = event.values[0];
		orientation[1] = event.values[1];
		orientation[2] = event.values[2];
		RunTheWorldMainActivity.setOutputCompassData(orientation);
	}
	public double [] getData() {
		// TODO Auto-generated method stub
		return orientation;
	}
}