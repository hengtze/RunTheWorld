package edu.cmu.runtheworld;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerListener implements SensorEventListener {
	
	private Context mainContext;
	private SensorManager sensorManager;
	//
	// Sensors
	private Sensor accelerometer;
	
	// Sensor Values
	private double [] 	acceleration;
	private double 		accelMagnitude;
	private double [] 	accelMagnitudeHistory;
	private double		accelMagnitudeMean;
	int					historyWriteIdx;
	int 				HISTORY_WINDOWSIZE = 10;
	
	public AccelerometerListener(Context c) {
		mainContext = c;
		sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		acceleration = new double [3];
		accelMagnitudeHistory = new double [HISTORY_WINDOWSIZE];
		historyWriteIdx = 0;
	}
	
	public void startListening() {
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
		acceleration[0] = event.values[0];
		acceleration[1] = event.values[1];
		acceleration[2] = event.values[2];
		RunTheWorldMainActivity.setOutputAccelData(acceleration);
		
		accelMagnitude = Math.sqrt( acceleration[0]*acceleration[0]
					+ acceleration[1]*acceleration[1] + acceleration[2]*acceleration[2] );		
		
		accelMagnitudeHistory[historyWriteIdx] = accelMagnitude;
		historyWriteIdx++;
		if (historyWriteIdx >= HISTORY_WINDOWSIZE) {
			historyWriteIdx = 0;
		}
		
//		RunTheWorldMainActivity.setOutputMotionClass(getMotionClass());
	}
	public double [] getData() {
		// TODO Auto-generated method stub
		return acceleration;
	}
	public double getAccelMagnitudeMean() {
		// TODO Auto-generated method stub
		return accelMagnitudeMean;
	}
	
	public int getMotionClass() {
		double sum = 0;
		
		for (int i=0; i < HISTORY_WINDOWSIZE; i++){
			sum += accelMagnitudeHistory[i];
		}
		accelMagnitudeMean = sum / HISTORY_WINDOWSIZE;
				
		if ( accelMagnitudeMean >= 3.5 ) return 2;
		if ( accelMagnitudeMean >= 1.5 && accelMagnitudeMean < 3.5 ) return 1;
		else return 0;
	}
}
