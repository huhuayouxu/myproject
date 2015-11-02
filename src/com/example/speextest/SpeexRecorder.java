package com.example.speextest;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by Alice on 2015/9/13.
 */
public class SpeexRecorder implements Runnable {
    private boolean isRecording = true;
    private static final int frequency = 8000;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static int packagesize = 160;
    private String fileName = null;
    private int compression = 0;
    private int timelen = 50;
	private Callback callback;

    public SpeexRecorder(String fileName, int compression, int timelen) {
        super();
        this.fileName = fileName;
        this.compression = compression;
        this.timelen = timelen;
    }
    public interface Callback {
	    public void callback(int values);
	  }
  
  public void setCallback(Callback callback) {
	this.callback = callback;
}

    public void run() {

        com.example.endec.SpeexEncoder encoder = new com.example.endec.SpeexEncoder(this.fileName, compression, timelen);
        Thread encodeThread = new Thread(encoder);
        encoder.setRecording(true);
        encodeThread.start();

        Log.i("SpeexRecord","running spxrecorder thread");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int bufferRead = 0;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding);
        short[] tempBuffer = new short[packagesize];

        AudioRecord recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding,
                bufferSize);

        recordInstance.startRecording();

        while (this.isRecording) {
//			log.debug("start to recording.........");
            bufferRead = recordInstance.read(tempBuffer, 0, packagesize);
            // bufferRead = recordInstance.read(tempBuffer, 0, 320);
            Log.i("SpeexRecord", "  bufferRead: "+ bufferRead +" Data :"+ getCmdString(tempBuffer));
            if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
            } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
                throw new IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
            } else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
            }
			//Log.v("debug","put data into encoder collector....");
           
            encoder.putData(tempBuffer, bufferRead);
            

        }
        recordInstance.stop();
        recordInstance.release();
        recordInstance = null ;
        encoder.setRecording(false);

    }
    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public boolean isRecording() {
        return isRecording;
    }
    
    private String getCmdString(short[] tempBuffer)
    {
      StringBuilder localStringBuilder = new StringBuilder();
      int size = tempBuffer.length;
      for (int i = 0; i < size  ; ++i)
      {
        localStringBuilder.append(tempBuffer[i]+ " ");
      }
      return localStringBuilder.toString();
    }
}
