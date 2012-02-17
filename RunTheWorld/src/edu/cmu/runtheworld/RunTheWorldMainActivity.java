package edu.cmu.runtheworld;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RunTheWorldMainActivity extends Activity implements OnClickListener {
	
	private static TextView textDebug;
	private static TextView textAccelX;
	private static TextView textAccelY;
	private static TextView textAccelZ;
	private static TextView textAzimuth;
	private static TextView textMotionClass;
	private static TextView textMeanAccel;
	private static ImageView imgMotionClass;
//	private static Button buttonUpload;
	private static ToggleButton buttonDoUpload;
	
	private AccelerometerListener accelerometerListener;
	private CompassListener compassListener;
	
	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost;
//	String serverUrl = "http://mcpr.stanford.edu/GoogleHackthon/getDataFromAndroid.php";
	String serverUrl = "http://192.168.187.60/GoogleHackthon/getDataFromAndroid.php";
		
	private boolean TO_UPLOAD = false;
	
	// Thread for Data Upload
	private final Handler mHandler = new Handler();
	private static Thread threadUploadData;
	private static Runnable runUploadData;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initUI();   
        
        // Sensor Listeners
        accelerometerListener = new AccelerometerListener(this);
        compassListener = new CompassListener(this);
        
        initThreadUploadData(); 
    }
        
    @Override
    protected void onResume() {
		super.onResume();
		accelerometerListener.startListening();
		compassListener.startListening();
		if (TO_UPLOAD == true) {
			startThreadUploadData();
		}
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	accelerometerListener.stopListening();
    	compassListener.stopListening();
    	TO_UPLOAD = false;
    	buttonDoUpload.setChecked(false);
    	stopThreadUploadData();
    }
    
    @Override
    protected void onDestroy() {
    	stopThreadUploadData();
    	super.onDestroy();
    }
	
    public void onClick (View clickedButton) {
//    	if(clickedButton.equals(buttonUpload)) {
//    		uploadData();
//		}
    	if(clickedButton.equals(buttonDoUpload)) {
    		if (buttonDoUpload.isChecked()) {
    			TO_UPLOAD = true;
    			startThreadUploadData();
    		}
    		else {
    			TO_UPLOAD = false;    			
    			stopThreadUploadData();
    		}	
		}
    }
    
    private int getMotionClass() {
    	int motionClass = accelerometerListener.getMotionClass();
    	textMotionClass.setText("Motion Class = " + String.valueOf(motionClass));
    	textMeanAccel.setText("Mean Acc = " + String.valueOf(accelerometerListener.getAccelMagnitudeMean())); 	
		
    	if ( motionClass == 0 )
    		imgMotionClass.setImageResource(R.drawable.stop_sign);
    	else if ( motionClass == 1 )
    		imgMotionClass.setImageResource(R.drawable.speed35_sign);
    	else
    		imgMotionClass.setImageResource(R.drawable.speed500_sign);    	
    	
    	return motionClass;
    }
    private double getAzimuth() {
    	double [] orientation = new double [3]; 
		orientation = compassListener.getData();
		return orientation[0];
    }
    
    private boolean uploadData() {
    	try {
		    // Add your data
//		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//		    nameValuePairs.add(new BasicNameValuePair("motionClass", "1"));
//		    nameValuePairs.add(new BasicNameValuePair("azimuth", String.valueOf(orientation[0])));
//		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			String url = new StringBuilder()
				.append(serverUrl)
                .append("?")
                .append("motionClass=")
                .append(String.valueOf(getMotionClass()))
                .append("&")
                .append("azimuth=")
                .append(String.valueOf(getAzimuth()))
                .toString();
                
			httppost = new HttpPost(url);
			
		    // Execute HTTP Post Request
		    HttpResponse response = httpclient.execute(httppost);
		    textDebug.setText(response.toString());
		    return true;
		    
		} catch (ClientProtocolException e) {
			textDebug.setText("HTTP Response: " + "ClientProtocolException");
			return false;
		    // TODO Auto-generated catch block
		} catch (IOException e) {
			textDebug.setText("HTTP Response: " + "IOException");
			return false;
		    // TODO Auto-generated catch block
		}
    }
    
    public static void setOutputMotionClass (int y) {
    	textDebug.setText("Motion Class: " + String.valueOf(y));
    }
    public static void setOutputAccelData(double [] data){
    	textAccelX.setText("AccelX = " + String.valueOf(data[0]));
    	textAccelY.setText("AccelY = " + String.valueOf(data[1]));
    	textAccelZ.setText("AccelZ = " + String.valueOf(data[2]));
    }
    public static void setOutputCompassData(double [] data){
    	textAzimuth.setText("Azimuth = " + String.valueOf(data[0]));
    }
    
    private void initThreadUploadData() {
		// TODO Auto-generated method stub
		runUploadData = new Runnable() {
        	@Override
	        public void run() {
	            // TODO Auto-generated method stub
	            while (TO_UPLOAD == true) {
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                mHandler.post(uploadDataRunnable);
	            }
			}
        };
	}
	
    public synchronized void startThreadUploadData(){
    	if (uploadData()) {
		  	if(threadUploadData == null){
		  		threadUploadData = new Thread(runUploadData);
		  		threadUploadData.start();
		  	}
    	}
    	else {
    		TO_UPLOAD = false;
    		buttonDoUpload.setChecked(false);
    		stopThreadUploadData();
    	}
	}

	public synchronized void stopThreadUploadData(){
		if(threadUploadData != null){
			Thread t = threadUploadData;
			threadUploadData = null;
			t.interrupt();
		}
	}
	
	private final Runnable uploadDataRunnable = new Runnable() 
    {
		@Override
        public void run() 
        {
            try 
            {
            	uploadData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    
	private void initUI() {
    	textDebug = (TextView) findViewById(R.id.textDebug);
    	textAccelX = (TextView) findViewById(R.id.textAccelX);
    	textAccelY = (TextView) findViewById(R.id.textAccelY);
    	textAccelZ = (TextView) findViewById(R.id.textAccelZ);
    	textAzimuth = (TextView) findViewById(R.id.textAzimuth);
    	textMotionClass = (TextView) findViewById(R.id.textMotionClass);
    	textMeanAccel = (TextView) findViewById(R.id.textMeanAccel);
    	imgMotionClass = (ImageView) findViewById(R.id.imgMotionClass);
//    	buttonUpload = (Button) findViewById(R.id.buttonUpload);
//    	buttonUpload.setOnClickListener(this);
    	buttonDoUpload = (ToggleButton) findViewById(R.id.buttonDoUpload);
    	buttonDoUpload.setOnClickListener(this);
    }
}