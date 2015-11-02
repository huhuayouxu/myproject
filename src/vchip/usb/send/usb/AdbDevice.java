/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vchip.usb.send.usb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import vchip.usb.send.tools.SystemConfig;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.util.SparseArray;

import com.example.speextest.MainActivity;


/* This class represents a USB device that supports the adb protocol. */
public class AdbDevice<VrUsbDevice> {

    private final MainActivity mActivity;
    private final UsbDeviceConnection mDeviceConnection;
    private final UsbEndpoint mEndpointOut;
    private final UsbEndpoint mEndpointIn;

    private long nowTime = 0L;
    private long preTime = 0L;
    private int rcvCounter = 0;
    private long rcvTime = 0L;
    private int sendTime = 0;
    private int delayMs = 0;
    private String mSerial;

    // pool of requests for the OUT endpoint
    private final LinkedList<UsbRequest> mOutRequestPool = new LinkedList<UsbRequest>();
    // pool of requests for the IN endpoint
    private final LinkedList<UsbRequest> mInRequestPool = new LinkedList<UsbRequest>();
    // list of currently opened sockets
    private final SparseArray<AdbSocket> mSockets = new SparseArray<AdbSocket>();
    private int mNextSocketId = 1;
    private VSControllerListner vsControllerListner = null;
    private AdbSocket tmpSocket = null;
    private final LoopSendThread mLoopSend = new LoopSendThread();
    private final WaiterThread mWaiterThread = new WaiterThread();
    private String filePath ;

    public AdbDevice(MainActivity activity, UsbDeviceConnection connection,
            UsbInterface intf , VSControllerListner paramVSControllerListner ,  String filePath ) {
        mActivity = activity;
        mDeviceConnection = connection;
        mSerial = connection.getSerial();
        this.filePath =filePath ;
        this.vsControllerListner = paramVSControllerListner;
        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;
    }
    
    public AdbDevice(MainActivity activity, UsbDeviceConnection connection,
            UsbInterface intf ) {
        mActivity = activity;
        mDeviceConnection = connection;
        mSerial = connection.getSerial();
        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;
    }

    // return device serial number
    public String getSerial() {
        return mSerial;
    }
  public AdbSocket getAdbSocket(){
	  if(this.tmpSocket !=null)
		  return tmpSocket ;
	  else
		  tmpSocket =  openSocket("test");
	  
	  return tmpSocket;
  }
    // get an OUT request from our pool
    public UsbRequest getOutRequest() {
        synchronized(mOutRequestPool) {
            if (mOutRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointOut);
                return request;
            } else {
                return mOutRequestPool.removeFirst();
            }
        }
    }

    // return an OUT request to the pool
    public void releaseOutRequest(UsbRequest request) {
        synchronized (mOutRequestPool) {
            mOutRequestPool.add(request);
        }
    }

    // get an IN request from the pool
    public UsbRequest getInRequest() {
        synchronized(mInRequestPool) {
            if (mInRequestPool.isEmpty()) {
                UsbRequest request = new UsbRequest();
                request.initialize(mDeviceConnection, mEndpointIn);
                return request;
            } else {
                return mInRequestPool.removeFirst();
            }
        }
    }

    public void startALL() {
    	  this.tmpSocket = openSocket("test");
//    	    this.mLoopSend.start();
        mWaiterThread.start();
        openFile();
    }
    

    public AdbSocket openSocket(String destination) {
        AdbSocket socket;
        synchronized (mSockets) {
            int id = mNextSocketId++;
            socket = new AdbSocket(this, id);
            mSockets.put(id, socket);
        }
        if (socket.sendMsg(null, 0)) {
            return socket;
        } else {
            return null;
        }
    }

    private AdbSocket getSocket(int id) {
        synchronized (mSockets) {
            return mSockets.get(id);
        }
    }

    public void socketClosed(AdbSocket socket) {
        synchronized (mSockets) {
            mSockets.remove(socket.getId());
        }
    }

    // send a connect command
    private void connect() {
        AdbMessage message = new AdbMessage();
        message.set(AdbMessage.A_CNXN, AdbMessage.A_VERSION, AdbMessage.MAX_PAYLOAD, "host::\0");
        message.write(this);
    }

    // handle connect response
    private void handleConnect(AdbMessage message) {
//        if (message.getDataString().startsWith("device:")) {
//            log("connected");
//            mActivity.deviceOnline(this);
//        }
    }

    public void stopALL() {
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = true;
            mLoopSend.mSendStop = true ;
        }
    }
    
    public void stopSend() {
        synchronized (mLoopSend) {
        	mLoopSend.mSendStop = true;
        }
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = false;
        }
    }
    
    public void stopReceive() {
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = true;
        }
    }
    
    public void startSend() {
        synchronized (mLoopSend) {
        	mLoopSend.mSendStop = false;
        }
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = true;
        }
    }
    
    public void startReceive()  {
        synchronized (mWaiterThread) {
            mWaiterThread.mStop = false;
            openFile();
        }
    }

    // dispatch a message from the device
    void dispatchMessage(AdbMessage message) {
        int command = message.getCommand();
        switch (command) {
            case AdbMessage.A_SYNC:
                log("got A_SYNC");
                break;
            case AdbMessage.A_CNXN:
                handleConnect(message);
                break;
            case AdbMessage.A_OPEN:
            case AdbMessage.A_OKAY:
            case AdbMessage.A_CLSE:
            case AdbMessage.A_WRTE:
                AdbSocket socket = getSocket(message.getArg1());
                if (socket == null) {
                    log("ERROR socket not found");
                } else {
                    socket.handleMessage(message);
                }
                break;
        }
    }

    void log(String s) {
        mActivity.sendData(s);
    }


    private class LoopSendThread extends Thread
    {
      private LoopSendThread()
      {
      }
     
      public boolean mSendStop;
      public boolean mFlush = false;
      public void run()
      {
        while (true)
        {
        	
        	synchronized (this) {
                if (mSendStop) {
                    return;
                }
            }
          if (tmpSocket != null){
        	  boolean success = tmpSocket.sendMsg(null, 0);
          }
          
          try
          {
            Thread.sleep(100L);
          }
          catch (Exception localException)
          {
            localException.printStackTrace();
          }
        }
      }
    }
  private class WaiterThread extends Thread {
  public boolean mStop;
  
  public void run() {
      // start out with a command read
      AdbMessage localVrSensorMessage1 ;
      AdbMessage localVrSensorMessage2 = null ;
      UsbRequest localUsbRequest;
      // FIXME error checking
    

      while (true) {
          synchronized (this) {
              if (mStop) {
                  return;
              }
          }
         try {
			Thread.sleep(2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
          localVrSensorMessage1 = new AdbMessage();
          localVrSensorMessage1.readCommand(getInRequest());
          localUsbRequest = mDeviceConnection.requestWait();
          if (localUsbRequest == null) {
        	  
              break;
          }
          if (localUsbRequest.getEndpoint() != mEndpointOut){
        	   localVrSensorMessage2 = (AdbMessage)localUsbRequest.getClientData();
        	  localUsbRequest.setClientData(null);
        	  rcvTime = System.currentTimeMillis();
        	  if(out ==null ){
        		  openFile();
        	  }
        	  byte[] arrayOfByte = localVrSensorMessage2.getRcvData();
        	  Log.i("AdbDecive", "Receive Data :"+getCmdString(arrayOfByte));
        	  vsControllerListner.onGetData(arrayOfByte);
        	  if(arrayOfByte.length < 20)
        		  return ;
        	  if((arrayOfByte[5]&0xff ) !=0   && (arrayOfByte[5]& 0xff )< 65 ){
        		  
        		  if((arrayOfByte[6]&0xff ) != ((arrayOfByte[7]&0xff ) +1)){
        			  byte[] data = new byte[arrayOfByte[5]&0xff ];
        			  System.arraycopy(arrayOfByte, 8, data, 0, data.length);
//        			  vsControllerListner.onGetData(arrayOfByte);
        			  try {
        				  if(out !=null)
						      out.write(data);
        				  else
        					  Log.i("AdbDevice", "wirite data failure. outString ==null");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.i("AdbDevice", "wirite data failure.");
						break ;
					}
        		  }else{
        			 closeFile();
        		  }
        		  
        	  }
        	 
//        	  sendTime = (0xFF000000 & arrayOfByte[10] << 24 | 0xFF0000 & arrayOfByte[9] << 16 | 0xFF00 & arrayOfByte[8] << 8 | 0xFF & arrayOfByte[7]);
//        	  delayMs = ((int)(0xFFFFFFFF & rcvTime) - sendTime);
//              GlassConLog.saveLog("usbwqDevice", "recv ack, delayMs one way ," + delayMs / 2);
          }
          
        

          // put request back into the appropriate pool
          if (localVrSensorMessage2 == localVrSensorMessage1)  {
              synchronized (mInRequestPool) {
                  mInRequestPool.add(localUsbRequest);
              }
          }
      }
  }
}
  private FileOutputStream out;
  public void openFile()  {
	  
	  File file = new File(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/");

      if (!file.exists()) {
        file.mkdirs();
      }
      
		try {
			out =new FileOutputStream(filePath);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

  public void closeFile(){
		if(out!=null){
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				out = null;
				Log.i("AdbDevice", "closeFile");
				if(vsControllerListner !=null)
					vsControllerListner.onScanResult("Play");
			}
		}
			
	}
	
//    private class WaiterThread extends Thread {
//        public boolean mStop;
//
//        public void run() {
//            // start out with a command read
//            AdbMessage currentCommand = new AdbMessage();
//            AdbMessage currentData = null;
//            // FIXME error checking
//            currentCommand.readCommand(getInRequest());
//
//            while (true) {
//                synchronized (this) {
//                    if (mStop) {
//                        return;
//                    }
//                }
//                UsbRequest request = mDeviceConnection.requestWait();
//                if (request == null) {
//                    break;
//                }
//
//                AdbMessage message = (AdbMessage)request.getClientData();
//                request.setClientData(null);
//                AdbMessage messageToDispatch = null;
//
//                if (message == currentCommand) {
//                    int dataLength = message.getDataLength();
//                    // read data if length > 0
//                    if (dataLength > 0) {
//                        message.readData(getInRequest(), dataLength);
//                        currentData = message;
//                    } else {
//                        messageToDispatch = message;
//                    }
//                    currentCommand = null;
//                } else if (message == currentData) {
//                    messageToDispatch = message;
//                    currentData = null;
//                }
//
//                if (messageToDispatch != null) {
//                    // queue another read first
//                    currentCommand = new AdbMessage();
//                    currentCommand.readCommand(getInRequest());
//
//                    // then dispatch the current message
//                    dispatchMessage(messageToDispatch);
//                }
//
//                // put request back into the appropriate pool
//                if (request.getEndpoint() == mEndpointOut) {
//                    releaseOutRequest(request);
//                } else {
//                    synchronized (mInRequestPool) {
//                        mInRequestPool.add(request);
//                    }
//                }
//            }
//        }
//    }
    
    public int sendEventPacket(byte[] paramArrayOfByte, int paramInt)
    {
      synchronized (this.mWaiterThread)
      {
        GlassConLog.saveLog("usbwqDevice", "mWaiterThread.mStop " + this.mWaiterThread.mStop);
        this.mWaiterThread.mStop = false;
        return -1;
      }
    }
    
    public String getCmdString(byte[] data)
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
