package com.example.speextest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vchip.usb.send.tools.BackUp;
import vchip.usb.send.tools.SystemConfig;
import vchip.usb.send.usb.UsbGlass;
import vchip.usb.send.usb.VSControllerListner;

import com.example.network.Receiver;
import com.example.speextest.SpeexRecorder.Callback;
import com.nickming.view.AudioRecordButton;
import com.nickming.view.AudioRecordButton.AudioFinishRecorderListener;



public class MainActivity extends Activity implements VSControllerListner ,Callback {
	    private TextView mLog;
	    private UsbGlass usbGlass = null ;
	    private Handler handle;	    
	    private UsbManager mManager;
	    private SpeexRecorder spxRecorder = null;
	    private String reFilsePath;
	    private String deFilesPath;
	    private Context ctx;
		private String stringShow =null ;
    public static final int STOPPED = 0;
    public static final int RECORDING = 1;
    Button switchButton = null;
    Button setComButton = null;
    Button setTimButton = null;
    EditText setComTxt = null;
    EditText setTimTxt = null;
    String outputString;
    String file;
    SpeexPlayer splayer = null;
    AudioRecordButton button;
    private Receiver  socketReceiver;
    public int compression = 0;
    public int timlen = 250;
    private ListView mlistview;
    private ArrayAdapter<Recorder> mAdapter;
	private View viewanim;
	private List<Recorder> mDatas = new ArrayList<MainActivity.Recorder>();
	private int totalPackage = 0 ;
    public int getCompression(){
    	return compression;
    }    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        setTitle("Taikie");
        mLog = (TextView)findViewById(R.id.textview);
        ctx = this;
        try {
			socketReceiver = new Receiver();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        new Thread(socketReceiver).start();
        if(this.isExternalStorageWritable())
        	SystemConfig.SDPATHString = ctx.getExternalFilesDir(null)+"" ;
        else
        	SystemConfig.SDPATHString = ctx.getFilesDir() +"";
        //this.setContentView(layout);
        mLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        stringShow ="Begin Receive Data:" + "\n";
        mLog.setText(stringShow);
        SystemConfig.SDPATHString = BackUp.getSDCardPath();
        reFilsePath =SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
                + SystemConfig.SPPEEX_ENCONDE_FILE ;
        deFilesPath =SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
                + SystemConfig.SPPEEX_DECONDE_FILE ;
       
        mlistview = (ListView) findViewById(R.id.listview);
        button = (AudioRecordButton) findViewById(R.id.recordButton);
        switchButton = (Button) findViewById(R.id.swButton);
        setComButton = (Button) findViewById(R.id.setComButton);
        setTimButton = (Button) findViewById(R.id.setTimButton);
        setComTxt = (EditText) findViewById(R.id.comTxt);
        setTimTxt = (EditText) findViewById(R.id.timTxt);
        setComButton.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
        		compression = Integer.parseInt(setComTxt.getText().toString());
        		if(compression < 0)
        			compression = 0;
        		else if(compression > 10)
        			compression = 10;
        		button.setCompression(compression);
        	}
        });
        setTimButton.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
        		timlen = Integer.parseInt(setTimTxt.getText().toString())/10;
        		if(timlen <= 0)
        			timlen = 1;
        		else if(timlen >= 255)
        			timlen = 250;
        		button.setTimeLen(timlen);
        	}
        });
        switchButton.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View v){
        		if(setComButton.getVisibility() == 8){
                    setComButton.setVisibility(0);
                    setTimButton.setVisibility(0);
                    setComTxt.setVisibility(0);
                    setTimTxt.setVisibility(0);
                    Log.v("aaa", "switch to activity 2");
        		}
        		else{
        			setComButton.setVisibility(8);
        			 setTimButton.setVisibility(8);
                     setComTxt.setVisibility(8);
                     setTimTxt.setVisibility(8);
        		}
        	}
        });
      
		button.setAudioFinishRecorderListener(new AudioFinishRecorderListener() {

			@Override
			public void onFinished(float seconds, String filePath) {
				// TODO Auto-generated method stub
				Recorder recorder = new Recorder(seconds, filePath);
				mDatas.add(recorder);
				mAdapter.notifyDataSetChanged();
				mlistview.setSelection(mDatas.size() - 1);
			}
		});
		
		
		mAdapter = new RecorderAdapter(this, mDatas);
		mlistview.setAdapter(mAdapter);

		mlistview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// 播放动画
				//if (viewanim!=null) {//让第二个播放的时候第一个停止播放
				//	viewanim.setBackgroundResource(R.id.id_recorder_anim);
				//	viewanim=null;
				//}
				//viewanim = view.findViewById(R.id.id_recorder_anim);
				//viewanim.setBackgroundResource(R.drawable.play);
				//AnimationDrawable drawable = (AnimationDrawable) viewanim
				//		.getBackground();
				//drawable.start();
				splayer = new SpeexPlayer(mDatas.get(position).filePathString);
				Log.v("111111111", mDatas.get(position).filePathString);
	            splayer.startPlay();
	            
	            //viewanim.setBackgroundResource(R.id.id_recorder_anim);
	            splayer = null;
	            
				// 播放音频
				//MediaManager.playSound(mDatas.get(position).filePathString,
				//		new MediaPlayer.OnCompletionListener() {
//
	//						@Override
		//					public void onCompletion(MediaPlayer mp) {
			//					viewanim.setBackgroundResource(R.id.id_recorder_anim);
//
	//						}
		//				});
			}
		});
		
		
    }
    
	@Override
	public void onBaseStateChange(int paramInt) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		switch (paramInt)
	    {
	    case 11:
		      setStatus("test");
	    case 14:
	      setStatus("connected");
	      Log.d("usbwqConFragment", "connected");
	      break ;
	    case 13:
	      setStatus("connecting");
	      Log.d("usbwqConFragment", "connecting");
	      break ;
	    case 12:
	      setStatus("none");
	      Log.d("usbwqConFragment", "none");
	      break ;
	    default: 
	    	break;
	    }
	   
	   
	
	}
	
	@Override
	public void onGetData(byte[] paramArrayOfByte) {
		// TODO Auto-generated method stub
		  StringBuilder localStringBuilder = new StringBuilder();
		  int size = paramArrayOfByte.length;
		  if(size < 10)
			  return ;
		    for (int i = 0; i < size ; ++i)
		    {
		      if (i >= 8)
		      {
		    	  totalPackage = totalPackage +1 ;
		    	  stringShow =  stringShow +"Receive Data:" + localStringBuilder.toString() + "\n";
		    	  handle.sendEmptyMessage(1000);
		        return;
		      }
		      Object[] arrayOfObject = new Object[1];
		      arrayOfObject[0] = Byte.valueOf(paramArrayOfByte[i]);
		      localStringBuilder.append(String.format("%02X ", arrayOfObject));
		     
		    }
	}


	  private void setStatus(CharSequence paramCharSequence)
	  {
	    if (this.ctx == null)
	    return;
	    
	    ActionBar  localActionBar = ((Activity) this.ctx).getActionBar();
	    
	    localActionBar.setSubtitle(paramCharSequence);
	  }
	
	@Override
	public void onScanResult(String paramString) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(paramString .equals("Play")){
			     if(splayer !=null)
				   splayer = new SpeexPlayer(deFilesPath);
		            splayer.startPlay(); 
		}
	}
	
	private String getString(byte[] content) {
		StringBuilder sb = new StringBuilder();
	    for (byte tmp : content) {
	      String tmpString = Integer.toHexString(tmp & 0xFF).toUpperCase();
	      sb.append(tmpString.length() < 2 ? "0" + tmpString : tmpString);
	      sb.append(" ");
	    }
	    return sb.deleteCharAt(sb.length()-1).toString();
	}


	public void sendData(String s) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void callback(int values) {
		// TODO Auto-generated method stub
				if(false){
					final int size = values ;
					if(size < 5)
						handle.sendEmptyMessage(0);
					else if(size < 15)
						handle.sendEmptyMessage(1);
					else if(size < 35)
						handle.sendEmptyMessage(2);
					else if(size < 55)
						handle.sendEmptyMessage(3);
					else if(size < 90)
						handle.sendEmptyMessage(4);
					else 
						handle.sendEmptyMessage(5);
				
					
					
					Log.i("callback:", "Callback values :"+ values);
				}
				
		 
	
	}
    
    class Recorder {
		float time;
		String filePathString;

		public Recorder(float time, String filePathString) {
			super();
			this.time = time;
			this.filePathString = filePathString;
		}

		public float getTime() {
			return time;
		}

		public void setTime(float time) {
			this.time = time;
		}

		public String getFilePathString() {
			return filePathString;
		}

		public void setFilePathString(String filePathString) {
			this.filePathString = filePathString;
		}

	}
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
   
}
