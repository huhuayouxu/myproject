package vchip.usb.send.usb;

public abstract interface VSControllerListner {
	
		  public abstract void onBaseStateChange(int paramInt);

		  public abstract void onGetData(byte[] paramArrayOfByte);

		  public abstract void onScanResult(String paramString);
		
}
