package com.example.network;

import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.example.endec.Speex;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Message;
import android.util.Log;

public class Receiver implements Runnable{
	private ServerSocket socketServer;
	private Socket socket;
	private AudioTrack track;
	private Speex speexDec;
	
    public Receiver() throws IOException{
    	int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    	track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				minBufferSize, AudioTrack.MODE_STREAM);
    	speexDec = new Speex();
		speexDec.init();
		
		socketServer = new ServerSocket(10000);

	}
    
    public void run(){
    	while(true){
    	    try {
		    	socket=socketServer.accept();
		    	Log.v("socket","number number number");
		    	new Thread(new ServerThread(socket)).start();
		    } catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }
		}
    	
	}
    
    private class ServerThread extends Thread
	{
    	private InputStream inputStreamServer;
    	private OutputStream outputStreamServer;
    	
		private Socket s = null;
		
		public ServerThread(Socket s)
		{
			this.s = s;
			try 
			{
				inputStreamServer = s.getInputStream();
				outputStreamServer = s.getOutputStream();
				
			} catch (IOException e) 
			{
				e.printStackTrace();
			}	
		}
		
		public void run()
		{
			byte  [] tmp = new byte[1024];
			int count = 0;
			int counter = 0;
			int timelen = 0;
			while(true){
				timelen = timelen + 1;
				if(timelen > 3000)
				    continue;
				try {
				    count = inputStreamServer.read(tmp);
					if(count != 0){ 
						counter = counter + 1;
						int decsize = 0;
					    short[] decoded = new short[160];
					    if ((decsize = speexDec.decode(tmp, decoded, 160)) > 0) {
						   	track.write(decoded, 0, decsize);
						    track.setStereoVolume(0.7f, 0.7f);
						    track.play();
					    }
				    }
				    else
				 	    counter = 0;
			    	    if(counter == 1024)
						    break;
					
				    } catch (IOException e) {
					// TODO Auto-generated catch block
					    e.printStackTrace();
				    }
	     	}//while
			track.stop();
			track.release();
		}
	}

}
