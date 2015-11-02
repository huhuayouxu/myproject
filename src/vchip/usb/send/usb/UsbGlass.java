package vchip.usb.send.usb;


import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.speextest.MainActivity;

public class UsbGlass
{
  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
  public static final String BTLOG_FILE_PATH = "/Vchip/";
  public static final int STATE_CONNECTED = 14;
  public static final int STATE_CONNECTING = 13;
  public static final int STATE_NONE = 12;
  public static final int STATE_SCANNING = 15;
  private static final String TAG = "usbwqGlass";
  public static final String VRGlass_VERSION_ID = "v1.0";
  public Context mContext = null;
  private UsbDevice mDevice;
  private UsbDeviceConnection mDeviceConnection;
  public Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
      super.handleMessage(paramMessage);
    }
  };
  private UsbInterface mInterface;
  private int mLeState = 0;
  private UsbManager mManager;
  private PendingIntent mPermissionIntent = null;
  BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
  {

      public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();

          if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
              UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
              UsbInterface intf = findAdbInterface(device);
              if (intf != null) {
            	  if(!mManager.hasPermission(device)){
            		  setState(11);
            		  String str2; 
            		  GlassConLog.saveLog("usbwqGlass", "ATTACHED requestPermission");
                      mManager.requestPermission(device, mPermissionIntent);
                      GlassConLog.saveLog("usbwqGlass", "after ATTACHED requestPermission");
            	  }else{
            		  GlassConLog.saveLog("usbwqGlass", "Found adb interface " + intf);
                      setAdbInterface(device, intf); 
            	  }
            	 
              }
          } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
              UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
              String deviceName = device.getDeviceName();
              if (mDevice != null && mDevice.equals(deviceName)) {
            	  GlassConLog.saveLog("usbwqGlass", "adb interface removed");
                  setAdbInterface(null, null);
              }
          }else if ("com.android.example.USB_PERMISSION".equals(action)){
        	  UsbDevice localUsbDevice1;
        	  localUsbDevice1 = (UsbDevice)intent.getParcelableExtra("device");
        	  if (intent.getBooleanExtra("permission", false))
              {
                GlassConLog.saveLog("usbwqGlass", "EXTRA_PERMISSION_GRANTED " + localUsbDevice1);
                if (localUsbDevice1 != null)
                  UsbGlass.this.setAdbInterface(UsbGlass.this.tmpDevice, UsbGlass.this.tmpInterface);
               
              }
          }
          
          
      }
  
  };
  private VSControllerListner mVSControllerListner = null;
  private AdbDevice mVrUsbDevice;
  private UsbDevice tmpDevice;
  private UsbInterface tmpInterface;
  private String filePath ;

  public UsbGlass(Context paramContext, VSControllerListner paramVSControllerListner , String filePath)
  {
    this.mVSControllerListner = paramVSControllerListner;
    this.mContext = paramContext;
    this.mManager = ((UsbManager)paramContext.getSystemService("usb"));
    this.filePath =filePath ;
    registerReceiver();
    this.mLeState = 12;
    setState(this.mLeState);
    this.mPermissionIntent = PendingIntent.getBroadcast(paramContext, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
    GlassConLog.saveLog("usbwqGlass", "onCreate getDeviceList " + this.mManager.getDeviceList().size());
  }

   public boolean foundDevice(UsbDevice device){
       boolean flag = false;
       UsbInterface intf = findAdbInterface(device);
       if (intf != null) {
     	  if(!mManager.hasPermission(device)){
     		  setState(11);
     		  GlassConLog.saveLog("usbwqGlass", "ATTACHED requestPermission");
               mManager.requestPermission(device, mPermissionIntent);
               GlassConLog.saveLog("usbwqGlass", "after ATTACHED requestPermission");
               flag = true ;
     	  }else{
     		  GlassConLog.saveLog("usbwqGlass", "Found adb interface " + intf);
               setAdbInterface(device, intf); 
               flag = true ;
     	  }
     	 
       }else{
    	   flag = false ;
       }
       return flag;
   
   }
  
  private UsbInterface findAdbInterface(UsbDevice paramUsbDevice)
  {
    setState(13);
    Log.d("usbwqGlass", "findAdbInterface " + paramUsbDevice);
    GlassConLog.saveLog("usbwqGlass", "VID " + paramUsbDevice.getProductId() + ", PID " + paramUsbDevice.getVendorId());
    int i = paramUsbDevice.getInterfaceCount();
//    for (int j = 0; j < i ; j++) {
//    	  UsbInterface intf = paramUsbDevice.getInterface(i);
//          if (intf.getInterfaceClass() == 3  && intf.getInterfaceSubclass() == 0  && intf.getEndpointCount() >1) {
//            this.tmpDevice = paramUsbDevice;
//            this.tmpInterface = intf;
//              return intf;
//          }
//    }
    UsbInterface intf = paramUsbDevice.getInterface(0);
  this.tmpDevice = paramUsbDevice;
  this.tmpInterface = intf;
  return intf;
//    return null; 
  }

  private void registerReceiver()
  {
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
    localIntentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
    localIntentFilter.addAction("com.android.example.USB_PERMISSION");
    this.mContext.registerReceiver(this.mUsbReceiver, localIntentFilter);
  }

  public AdbSocket getScocket (){
	  if(mVrUsbDevice != null){
		  return mVrUsbDevice.getAdbSocket();
	  }
	  return null;
  }
  
  public void openFile (){
	  if(mVrUsbDevice != null){
		   mVrUsbDevice.openFile();
	  }
  }
  public void closeFile (){
	  if(mVrUsbDevice != null){
		   mVrUsbDevice.closeFile();
	  }
  }
  
  private boolean setAdbInterface(UsbDevice paramUsbDevice, UsbInterface paramUsbInterface)
  {
	 
    if (this.mDeviceConnection != null)
    {
      if (this.mInterface != null)
      {
        this.mDeviceConnection.releaseInterface(this.mInterface);
        this.mInterface = null;
      }
      this.mDeviceConnection.close();
      this.mDevice = null;
      this.mDeviceConnection = null;
    }
    
    if ((paramUsbDevice != null) && (paramUsbInterface != null))
    {
      UsbDeviceConnection localUsbDeviceConnection = this.mManager.openDevice(paramUsbDevice);
      GlassConLog.saveLog("usbwqGlass", "UsbDeviceConnection " + localUsbDeviceConnection);
      if (localUsbDeviceConnection == null){
    	  GlassConLog.saveLog("usbwqGlass", "open failed");
      }else{
    	  GlassConLog.saveLog("usbwqGlass", "open succeeded");
          if (localUsbDeviceConnection.claimInterface(paramUsbInterface, true))
          {
            GlassConLog.saveLog("usbwqGlass", "claim interface succeed");
            setState(14);
            this.mDevice = paramUsbDevice;
            this.mDeviceConnection = localUsbDeviceConnection;
            this.mInterface = paramUsbInterface;
            this.mVrUsbDevice = new AdbDevice((MainActivity) this.mContext, this.mDeviceConnection, paramUsbInterface, this.mVSControllerListner ,  filePath);
            GlassConLog.saveLog("usbwqGlass", "call start");
            this.mVrUsbDevice.startALL();
//            this.this.mVrUsbDevice.openSocket("test");
            this.mVrUsbDevice.startReceive();
            return true;
          }else{
        	  GlassConLog.saveLog("usbwqGlass", "claim interface failed");
        	  localUsbDeviceConnection.close();
        	  return true;
          }
      }
    	 
      
      
    }

        if ((this.mDeviceConnection == null) && (this.mVrUsbDevice != null))
        {
          this.mVrUsbDevice.stopALL();
          this.mVrUsbDevice = null;
        }
        return false;
       
      
    
   
  }

  private void setState(int paramInt)
  {
    this.mLeState = paramInt;
    this.mVSControllerListner.onBaseStateChange(this.mLeState);
  }

  public void destroyVRGlass()
  {
    stopVRGlass();
    this.mContext.unregisterReceiver(this.mUsbReceiver);
  }

  public int getState()
  {
    return this.mLeState;
  }

  public boolean startVRGlass(boolean paramBoolean)
  {
    setState(13);
    Iterator localIterator = this.mManager.getDeviceList().values().iterator();
    if (localIterator.hasNext())
  
    {
      
      UsbDevice localUsbDevice = (UsbDevice)localIterator.next();
      UsbInterface localUsbInterface = findAdbInterface(localUsbDevice);
      if (localUsbInterface != null){
    	  
      
       if(this.mManager.hasPermission(localUsbDevice))
      {
        GlassConLog.saveLog("usbwqGlass", "onCreate hasPermission");
        setAdbInterface(localUsbDevice, localUsbInterface);
        return true ;
      }else{
    	  GlassConLog.saveLog("usbwqGlass", "onCreate requestPermission");
          this.mManager.requestPermission(localUsbDevice, this.mPermissionIntent);
      }
      } 
    }
    return false ;
  }

  public void stop()
  {
    setState(12);
    if (this.mVrUsbDevice != null)
      this.mVrUsbDevice.stopALL();
     setAdbInterface(null, null);
  }
  
  public void startSend() {
	  if (this.mVrUsbDevice != null)
	      this.mVrUsbDevice.startSend();
  }
  
  public void stopSend() {
	  if (this.mVrUsbDevice != null)
	      this.mVrUsbDevice.stopSend();
	  
  }
  public void stopVRGlass()
  {
    setState(12);
    if (this.mVrUsbDevice != null)
      this.mVrUsbDevice.stopALL();
     setAdbInterface(null, null);
  }

//  public int write(byte[] paramArrayOfByte)
//  {
//    if (this.mVrUsbDevice != null)
//      return this.mVrUsbDevice.sendEventPacket(paramArrayOfByte, paramArrayOfByte.length);
//    return -1;
//  }
}