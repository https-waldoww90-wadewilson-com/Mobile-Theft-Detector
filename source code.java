import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginScreenActivity extends Activity {
	
	EditText et_uname;
	EditText et_pwd;
	Button btn_login;
	Button btn_signup;
	
	DatabaseHelper helper;
	SQLiteDatabase db;
	Cursor cursor;
	
	String str_uName;
	
	private static final String fields[] = {"emailid", BaseColumns._ID };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
        
        try{
        
        et_uname = (EditText)findViewById(R.id.et_uname);
        et_pwd = (EditText)findViewById(R.id.et_pwd);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_signup = (Button)findViewById(R.id.btn_signup);
        
        helper  = new DatabaseHelper(this);

        btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(et_uname.getText().toString().trim().equals("") ||
						et_uname.getText().toString().trim().length()==0){
					
					Toast.makeText(getApplicationContext(), "Please enter Username to proceed", Toast.LENGTH_SHORT).show();
				}
				else if(et_pwd.getText().toString().trim().equals("") ||
						et_pwd.getText().toString().trim().length()==0){
					
					Toast.makeText(getApplicationContext(), "Please enter Password to proceed", Toast.LENGTH_SHORT).show();
				}
				else{
					
					myAsyncTask myRequest = new myAsyncTask();
				    myRequest.execute();
					
				}
				
				
			}
		});
Capture image
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CaptureImageSendMail extends Activity implements SurfaceHolder.Callback{
	
	Camera camera = null;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	Bitmap resizedBitmap;
	String encodedImage;
	byte[] bytes;
	Uri sdCardUri;
	String path;
	
	Mail m;
//	String[] toArr = {"sandeshk779@gmail.com"}; 
	
	DatabaseHelper helper;
	SQLiteDatabase db;
	Cursor cursor;
	
	String str_email;
	String str_alternate_email;
	
	LocationManager mlocManager;

	String src_latitude;
	String src_longitude;

	boolean gps_enabled=false;
	boolean network_enabled=false;
	String reqURL; 
	static double latitude_src;
	static double longitude_src;
	static double latitude_dest;
	static double longitude_dest;
	String addressText;
	static String str_address="";
	String str_location;
	static double src_lat;
	static double src_long;
	static double dest_lat;
	static double dest_long;
	double current_loc_lat;
	double current_loc_long;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.captureimagescreen);
		
		 getWindow().setFormat(PixelFormat.UNKNOWN);
	     surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
	     surfaceHolder = surfaceView.getHolder();
	     surfaceHolder.addCallback(this);
	     surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	     
	     helper  = new DatabaseHelper(getApplicationContext());
     	 db = helper.getReadableDatabase(); 
     	
     	 get_location_details();
     	 
	     sendData();
	 
	}
	
	public void sendData(){
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		  @Override
		  public void run() {

			
			  try{

				     // TODO Auto-generated method stub
				     if(!previewing){
				
				     	try{
				  	Toast.makeText(getApplicationContext(), "no of cameras: " + Camera.getNumberOfCameras(), Toast.LENGTH_SHORT).show();
//				      Log.d("No of cameras",Camera.getNumberOfCameras()+"");
				      for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
				  //    	Toast.makeText(getApplicationContext(), "in for", Toast.LENGTH_SHORT).show();	
				      	
				          CameraInfo camInfo = new CameraInfo();
				          Camera.getCameraInfo(camNo, camInfo);
				         
				          if (camInfo.facing==(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
				         
				         	 camera = Camera.open(camNo);
				          }
				      
				      }
				    
				      if (camera != null){
				       try {
				        camera.setPreviewDisplay(surfaceHolder);
				        camera.startPreview();
				        previewing = true;
				        camera.takePicture(null, mPictureCallback, mPictureCallback);
				       } catch (IOException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
				       }
				      }
				     	}
				     	catch(Exception e){
				     		Toast.makeText(getApplicationContext(), "catch is: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				     	}
				 	   
				     }
				 	   
				// 	   preview();
				 	   
				     }
				     catch(Exception e){
				    	  Toast.makeText(getApplicationContext(), "error is: " + e.getMessage(), Toast.LENGTH_SHORT).show();  
				      }
			
		  }
		}, 5000);

		}
	
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

	 	/*Called when image data is available after a picture is taken. The format of the data depends on the context of the callback and Camera.Parameters settings.

	 	Parameters
	 	data	a byte array of the picture data
	 	camera	the Camera service object*/
	     @Override
			public void onPictureTaken(byte[] data, Camera camera) {
	         // TODO Auto-generated method stub
	         if (data != null){
	           
	             camera.stopPreview();
	             previewing = false;
				 camera.release();
			 }


