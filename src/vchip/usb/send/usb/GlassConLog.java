package vchip.usb.send.usb;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class GlassConLog
{
  private static final Boolean D = Boolean.valueOf(true);
  private static final String TAG = "GlassConLog";

  private boolean isFileExist(String paramString)
  {
    return new File(paramString).exists();
  }

  public static String readFileFromSD(String paramString)
  {
    Object localObject = "";
    File localFile = new File(paramString);
    if (localFile.isDirectory()){

        Log.d("GlassConLog", "The File doesn't not exist.");
        return (String) localObject;
    }
    String str1 = null;
      try
      {
        FileInputStream localFileInputStream = new FileInputStream(localFile);
        if (localFileInputStream != null){
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localFileInputStream));
        str1 = localBufferedReader.readLine();
        if (str1 == null)
        {
          localFileInputStream.close();
          return (String) localObject;
        }
      }
      }
      catch (FileNotFoundException localFileNotFoundException)
      {

          String str2 = localObject + str1;
          localObject = str2;
            Log.d("GlassConLog", "The File doesn't not exist.");
            return (String)localObject;

          
      }
      catch (IOException localIOException)
      {
        Log.d("GlassConLog", localIOException.getMessage());
      }
    
    return (String) localObject;
  }

  public static void saveLog(String paramString1, String paramString2)
  {
//      boolean bool = D.booleanValue();
//      if (bool)
//      {
//        Log.d(paramString1, paramString2);
//        writeFileToSD("VSUSBLog.log", new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ").format(new Date(System.currentTimeMillis())) + paramString1 + "  \t" + paramString2 + "\n", true);
//      }
	  writeFileToSD("VSUSBLog.log", new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ").format(new Date(System.currentTimeMillis())) + paramString1 + "  \t" + paramString2 + "\n", true);
    
  }

  private static void writeFileToSD(String paramString1, String paramString2, boolean paramBoolean)
  {
    if (!Environment.getExternalStorageState().equals("mounted"))
    {
    	throw new IllegalArgumentException("");
//      Log.d("GlassConLog", "SD card is not avaiable/writeable right now.");
//      return;
    }
    try
    {
      String str = Environment.getExternalStorageDirectory().toString();
      File localFile1 = new File(str + "/VSUSB/");
      File localFile2 = new File(str + "/VSUSB/" + paramString1);
      if (!localFile1.exists())
      {
        Log.d("GlassConLog", "Create the path:" + "/VSUSB/");
        localFile1.mkdir();
      }
      if (!localFile2.exists())
      {
        Log.d("GlassConLog", "Create the file:" + paramString1);
        localFile2.createNewFile();
      }
      FileOutputStream localFileOutputStream = new FileOutputStream(localFile2, paramBoolean);
      byte[] arrayOfByte = paramString2.getBytes();
      localFileOutputStream.getFD();
      localFileOutputStream.write(arrayOfByte);
      localFileOutputStream.write("\r\n".getBytes());
      localFileOutputStream.close();
      return;
    }
    catch (Exception localException)
    {
      Log.e("GlassConLog", "Error on writeFilToSD.");
      localException.printStackTrace();
    }
  }
}