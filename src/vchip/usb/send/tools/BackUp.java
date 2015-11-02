package vchip.usb.send.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;


public class BackUp {

  private Context ctx;

  private byte[] lastWriteData;

  

  /*
   * 鑾峰彇SD鍗＄姸鎬侊紝浠ョ‘瀹氭槸鍚﹁兘灏嗘暟鎹粠鏁版嵁搴撲繚瀛樺埌SD鍗′腑
   */
  public static boolean getSDCradReadyStatus() {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return true;
    } else {
      return false;
    }
  }

  /*
   * 鏄剧ずSD鍗￠敊璇紝鎻愮ず鐢ㄦ埛
   */
  public void showSDCardError() {
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    builder.setTitle(ctx.getString(com.example.speextest.R.string.Toast));
    builder.setMessage(ctx.getString(com.example.speextest.R.string.setting_backup_sdcarderror));
    builder.setPositiveButton(ctx.getString(com.example.speextest.R.string.OK),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {

          }
        });
    builder.show();
  }

  /*
   * 鑾峰彇SD鍗＄殑缁濆璺緞
   */
  public static String getSDCardPath() {
    if (getSDCradReadyStatus()) {
      return Environment.getExternalStorageDirectory().toString();
    } else {
      return null;
    }

  }

  private boolean compareBytes(byte[] data1, byte[] data2) {
    if (data1 == null || data2 == null) {
      return false;
    }
    if (data1.length != data2.length) {
      return false;
    }
    for (int i = 0; i < data1.length; i++) {
      if (data1[i] != data2[i]) {
        return false;
      }
    }
    return true;
  }

  /*
   * 灏嗘暟鎹簱鍐呭鍐欏叆SD鍗�
   */
  public boolean writeDataToFiles() {
    try {
     
      File file = new File(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/");

      if (!file.exists()) {
        file.mkdirs();
      }

     
      FileOutputStream fileOutputStream =
          new FileOutputStream(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
              + SystemConfig.BACKUP_FILE);
      ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
      /*
       * 閲囩敤Java鐨勫簭鍒楀寲鏈哄埗锛屽皢鏁版嵁鍐欏叆鏂囦欢
       */
////      os.writeObject(LightDAO.getDAO(ctx).getAll());
////      os.writeObject(GroupDAO.getDAO(ctx).getAll());
////      os.writeObject(GroupLightDAO.getDAO(ctx).getAll());
////      os.writeObject(SceneDAO.getDAO(ctx).getAllInclueSchedule());
////      os.writeObject(SceneLightDAO.getDAO(ctx).getAll());
////      os.writeObject(FavouriteDAO.getDAO(ctx).getAll());
////      os.writeObject(ScheduleDAO.getDAO(ctx).getAll());
////      os.writeObject(ScheduleItemDAO.getDAO(ctx).getAll());
////      os.writeObject(RemoterDAO.getDAO(ctx).getAll());
////      os.writeObject(RemoterLightDAO.getDAO(ctx).getAll());
//      os.writeObject(LightDAO.getDAO(ctx).getAll());
//      os.writeObject(GroupDAO.getDAO(ctx).getAll());
//      os.writeObject(GroupLightDAO.getDAO(ctx).getAll());
//      os.writeObject(SceneDAO.getDAO(ctx).getAllInclueSchedule());
//      os.writeObject(SceneLightDAO.getDAO(ctx).getAll());
//      os.writeObject(FavouriteDAO.getDAO(ctx).getAll());
//      os.writeObject(ScheduleDAO.getDAO(ctx).getAll());
//      os.writeObject(ScheduleItemDAO.getDAO(ctx).getAll());
//      os.writeObject(RemoterDAO.getDAO(ctx).getAll());
//      os.writeObject(RemoterLightDAO.getDAO(ctx).getAll());
//      os.writeObject(AlarmDAO.getDAO(ctx).getAll());
//      os.writeObject(MusicDAO.getDAO(ctx).getAll());
//      os.writeObject(MusicHallItemDAO.getDAO(ctx).getAll());
//      os.writeObject(LightControlImageDAO.getDAO(ctx).getAll());
      os.flush();
      os.close();

      return true;

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      showSDCardError();
      return false;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      showSDCardError();
      return false;
    }
  }
  
  public boolean writeDataToFiles(String remoteName , int localLightsVersion ) {
	    try {
	     
	      File file = new File(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/");

	      if (!file.exists()) {
	        file.mkdirs();
	      }

	     
	      FileOutputStream fileOutputStream =
	          new FileOutputStream(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
	              + remoteName);
	      ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
	      /*
	       * 閲囩敤Java鐨勫簭鍒楀寲鏈哄埗锛屽皢鏁版嵁鍐欏叆鏂囦欢
	       */
	      os.writeInt(localLightsVersion);
//	      os.writeObject(LightDAO.getDAO(ctx).getAll());
//	      os.writeObject(GroupDAO.getDAO(ctx).getAll());
//	      os.writeObject(GroupLightDAO.getDAO(ctx).getAll());
//	      os.writeObject(SceneDAO.getDAO(ctx).getAllInclueSchedule());
//	      os.writeObject(SceneLightDAO.getDAO(ctx).getAll());
//	      os.writeObject(FavouriteDAO.getDAO(ctx).getAll());
//	      os.writeObject(ScheduleDAO.getDAO(ctx).getAll());
//	      os.writeObject(ScheduleItemDAO.getDAO(ctx).getAll());
//	      os.writeObject(RemoterDAO.getDAO(ctx).getAll());
//	      os.writeObject(RemoterLightDAO.getDAO(ctx).getAll());
//	      os.writeObject(AlarmDAO.getDAO(ctx).getAll());
//	      os.writeObject(MusicDAO.getDAO(ctx).getAll());
//	      os.writeObject(MusicHallItemDAO.getDAO(ctx).getAll());
//	      os.writeObject(LightControlImageDAO.getDAO(ctx).getAll());
	      os.flush();
	      os.close();

	      return true;

	    } catch (FileNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	      showSDCardError();
	      return false;
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	      showSDCardError();
	      return false;
	    }
	  }

  RandomAccessFile randomAccessFile = null;
  int cmdReadStep = 100;

  public long prepareRandomReadFromFiles(int readStep) {
    this.cmdReadStep = readStep;
    try {
      randomAccessFile =
          new RandomAccessFile(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
	                + SystemConfig.SPPEEX_ENCONDE_FILE , "r");
//    	 randomAccessFile =
//    	          new RandomAccessFile(SystemConfig.SDPATHString  , "r");

      return randomAccessFile.length();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return -1;
    }
  }

  public long prepareRandomWriteFiles() {
    try {
      randomAccessFile =
          new RandomAccessFile(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
              + SystemConfig.BACKUP_FILE, "rw");
      lastWriteData = null;
      return randomAccessFile.length();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return -1;
    }
  }

  public void writeRandomToFiles(byte[] content) {
    try {
      if (!compareBytes(content, lastWriteData)) {
        randomAccessFile.write(content);
        lastWriteData = new byte[content.length];
        System.arraycopy(content, 0, lastWriteData, 0, content.length);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void closeRandomWriteFiles() {
    try {
      randomAccessFile.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 淇澶囦唤鏁版嵁搴撳埌璺敱鍣ㄥけ璐ョ殑闂锛氶渶瑕佸厛鍒ゆ柇闀垮害锛屽啀淇濆瓨鏁版嵁
   * @param offset
   * @param length
   * @return
   */
  private byte[] readRandomFromFiles(int offset, int length) {
    try {
      if (randomAccessFile == null)
        return null;
      byte[] buffer = new byte[length];
      long i= randomAccessFile.getFilePointer();

      int byteRead = randomAccessFile.read(buffer, 0, length);
      // int byteRead = randomAccessFile.seek(offset);
      long j= randomAccessFile.getFilePointer();
      Log.i("Backup", "currentreadSUm: "+byteRead);

      if (byteRead <= 0) {
        randomAccessFile.close();
        randomAccessFile = null;
      }else if (buffer.length > byteRead) {
        byte[] bufferReturn = new byte[byteRead];
        System.arraycopy(buffer, 0, bufferReturn, 0, byteRead);
        return bufferReturn;
      }

      return buffer;
    } catch (Exception e) {
      e.printStackTrace();
      return null;

    }
  }

  public byte[] readFileBackUpFileStepByStep(int index) {
	  Log.i("Backup", "currentindex: "+index);
    byte[] buffer = readRandomFromFiles(index * cmdReadStep, cmdReadStep);

    return buffer;
  }

  /*
   * 浠嶴D鍗′腑璇诲彇鏂囦欢锛屽苟灏嗗唴瀹瑰啓鍏ユ暟鎹簱
   */
  public boolean readDataFromFiles() {
    FileInputStream fs;
    ObjectInputStream ois = null;
    try {
      // fs = new FileInputStream("/" + SDPATHString + "/" + APP_FOLDER
      // + "/"+BACKUP_FILE);
      /*
       * 璇诲彇澶囦唤鏂囦欢
       */
      fs =
          new FileInputStream(SystemConfig.SDPATHString + "/" + SystemConfig.APP_FOLDER + "/"
              + SystemConfig.BACKUP_FILE);
      ois = new ObjectInputStream(fs);


      /*
       * 閲囩敤Java鐨勫弽搴忓垪鍖栨満鍒讹紝浠庢枃浠朵腑璇诲彇瀵硅薄
       */
//      List<Light> lights = (List<Light>) ois.readObject();
//      List<Group> groups = (List<Group>) ois.readObject();
//      List<GroupLight> grouplighst = (List<GroupLight>) ois.readObject();
//      List<Scene> scenes = (List<Scene>) ois.readObject();
//      List<SceneLight> scenelights = (List<SceneLight>) ois.readObject();
//      List<Favourite> favourites = (List<Favourite>) ois.readObject();
//      List<Schedule> schedules = (List<Schedule>) ois.readObject();
//      List<ScheduleItem> scheduleItems = (List<ScheduleItem>) ois.readObject();
//      List<Remoter> remoters = (List<Remoter>) ois.readObject();
//      List<RemoterLight> remoterLights = (List<RemoterLight>) ois.readObject();
//      List<Light> lights = (List<Light>) ois.readObject();
//      List<Group> groups = (List<Group>) ois.readObject();
//      List<GroupLight> grouplighst = (List<GroupLight>) ois.readObject();
//      List<Scene> scenes = (List<Scene>) ois.readObject();
//      List<SceneLight> scenelights = (List<SceneLight>) ois.readObject();
//      List<Favourite> favourites = (List<Favourite>) ois.readObject();
//      List<Schedule> schedules = (List<Schedule>) ois.readObject();
//      List<ScheduleItem> scheduleItems = (List<ScheduleItem>) ois.readObject();
//      List<Remoter> remoters = (List<Remoter>) ois.readObject();
//      List<RemoterLight> remoterLights = (List<RemoterLight>) ois.readObject();
//      List<Alarm> alarms = (List<Alarm>) ois.readObject();
//      List<Music> muscis = (List<Music>) ois.readObject();
//      List<MusicHallItem> musicItems = (List<MusicHallItem>)ois.readObject();
//      List<LightControlImage> lightControlImages = (List<LightControlImage>)ois.readObject();
      ois.close();
      /*
       * 娓呯┖鏁版嵁搴�
       */
//      mDbHelper.resetDb();

      /*
       * 渚濇娣诲姞鍚勪釜鏁版嵁
       */
//      for (Light light : lights) {
//          LightDAO.getDAO(ctx).addWithId(light);
//        }
//      
//      for (Group group : groups) {
//        GroupDAO.getDAO(ctx).addWithId(group);
//      }
//
//      for (GroupLight groupLights : grouplighst) {
//        GroupLightDAO.getDAO(ctx).addWithId(groupLights);
//      }
//
//
//      for (Scene scene : scenes) {
//        SceneDAO.getDAO(ctx).addWithId(scene);
//      }
//
//      for (SceneLight sceneLight : scenelights) {
//        SceneLightDAO.getDAO(ctx).addWithId(sceneLight);
//      }
//
//      for (Favourite favourite : favourites) {
//        FavouriteDAO.getDAO(ctx).addWithId(favourite);
//      }
//      
//      for (Schedule schedule : schedules) {
//        ScheduleDAO.getDAO(ctx).addWithId(schedule);
//      }
//      
//      for (ScheduleItem scheduleItem : scheduleItems) {
//        ScheduleItemDAO.getDAO(ctx).addWithId(scheduleItem);
//      }
//      
//      for (Remoter remoter : remoters) {
//        RemoterDAO.getDAO(ctx).addWithId(remoter);
//      }
//      
//      for (RemoterLight remoterLight : remoterLights) {
//        RemoterLightDAO.getDAO(ctx).addWithId(remoterLight);
//      }
      
            
      return true;

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      showSDCardError();
      return false;

    } catch (StreamCorruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      showSDCardError();
      return false;

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      showSDCardError();
      return false;

    }
  }
  
  /*
   * 浠嶴D鍗′腑璇诲彇鏂囦欢锛屽苟灏嗗唴瀹瑰啓鍏ユ暟鎹簱
   */
}
