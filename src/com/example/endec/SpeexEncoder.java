package com.example.endec;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.example.endec.Speex;
import com.example.network.Sender;

import android.util.Log;



/**
 * 
 * 
 * @author Gauss
 * 
 */
public class SpeexEncoder implements Runnable {

//	private Logger log = LoggerFactory.getLogger(SpeexEncoder.class);
	private final Object mutex = new Object();
	private Speex speex = new Speex();
	// private long ts;
	public static int encoder_packagesize = 1024;
	private byte[] processedData = new byte[encoder_packagesize];

	List<ReadData> list = null;
	private volatile boolean isRecording;
	private String fileName;
    private int compression = 0;
    private int timelen = 50;
    public Sender senderClient;
    
	public SpeexEncoder(String fileName, int compression, int timelen) {
		super();
		Log.v("qqq", "encoder set compression" + compression);
		speex.init(compression);
		list = Collections.synchronizedList(new LinkedList<ReadData>());
		this.fileName = fileName;
		this.compression = compression;
		this.timelen = timelen;
	    try {
			senderClient = new Sender();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    senderClient.setRecording(true);
	    
	}
	

	public void run() {

		com.example.writer.SpeexWriter fileWriter = new com.example.writer.SpeexWriter(fileName, timelen);
		Thread consumerThread = new Thread((Runnable) fileWriter);
		fileWriter.setRecording(true);
		consumerThread.start();
        Thread senderThread = new Thread((Runnable) senderClient);
        senderThread.start();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		int getSize = 0;
		while (this.isRecording()) {
			if (list.size() == 0) {
//				log.debug("no data need to do encode");
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (list.size() > 0) {
				synchronized (mutex) {
					ReadData rawdata = list.remove(0);
					getSize = speex.encode(rawdata.ready, 0, processedData, rawdata.size);
					Log.v("SpeexEncoder", "Encoder: before=" + rawdata.size + " after=" + processedData.length + " getsize="
     						+ getSize);
				}
				if (getSize > 0) {
					fileWriter.putData(processedData, getSize);
					senderClient.putData(processedData, getSize);
					
//					log.info("............clear....................");
					processedData = new byte[encoder_packagesize];
				}
			}
		}
//		log.debug("encode thread exit");
		fileWriter.setRecording(false);
		senderClient.setRecording(false);
	}

	/**
	 * ��Recorder�������������
	 * 
	 * @param data
	 * @param size
	 */
	public void putData(short[] data, int size) {
		
		ReadData rd = new ReadData();
		synchronized (mutex) {
			rd.size = size;
			System.arraycopy(data, 0, rd.ready, 0, size);
			list.add(rd);
		}
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

	class ReadData {
		private int size;
		private short[] ready = new short[encoder_packagesize];
	}
}

