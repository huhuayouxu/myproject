package com.example.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;



public class Sender implements Runnable{
	private final Object mutex = new Object();
	private volatile boolean isRecording;
	private processedData pData;
	private List<processedData> list;
	public static int write_packageSize = 1024;
	private Socket socketClient = null;
	private InputStream inputStreamClient;
	private OutputStream outputStreamClient;
	
	public Sender() throws UnknownHostException, IOException{
		
		socketClient = new Socket("127.0.0.1", 10000);
		inputStreamClient = socketClient.getInputStream();
		outputStreamClient = socketClient.getOutputStream();
		list = Collections.synchronizedList(new LinkedList<processedData>());
	}

	public void run(){
		
		//这里采用tcp收发数据，替换成需要的方式
		//发送起始信号,字符串“aabbccdd”
		//String header = "aabbccdd";
		//try {
		//	outputStreamClient.write(header.getBytes(), 0, 8);
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		
		while (this.isRecording() || list.size() > 0) {
            
			if (list.size() > 0) {
				pData = list.remove(0);
                try {
					outputStreamClient.write(pData.processed, 0, pData.size);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
		
	}

	public void putData(final byte[] buf, int size) {
        Log.i("Sender", "  length: "+ size +" Data :"+ getCmdString(buf));
		processedData data = new processedData();
		//data.ts = ts;
		data.size = size;
		System.arraycopy(buf, 0, data.processed, 0, size);
		list.add(data);
	}

	public void setRecording(boolean isRecording) {
		synchronized (mutex) {
			this.isRecording = isRecording;
			if (this.isRecording) {
				mutex.notify();
			}
		}
	}

	public boolean isRecording() {
		synchronized (mutex) {
			return isRecording;
		}
	}

	class processedData {
		//private long ts;
		private int size;
		private byte[] processed = new byte[write_packageSize];
	}
	
	 private String getCmdString(byte[] data)
	    {
	      StringBuilder localStringBuilder = new StringBuilder();
	      int size = data.length;
	      for (int i = 0; i < size  ; ++i)
	      {
	        localStringBuilder.append(String.format("%02X ", data[i])+ " ");
	      }
	      return localStringBuilder.toString();
	    }
	
	
	

}

