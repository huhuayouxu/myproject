package vchip.usb.send.tools;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdMessage {
  public static byte PACKAGE_HEAD = (byte) 0xFD;
  public static byte PACKAGE_TAIL = (byte) 0xFE;

  public static byte DEVICE_REMOTE_CONTROL = (byte) 0x20;
  public static byte DEVICE_TERMINAL = (byte) 0x21;

  public static byte[] ANDROID_IP = new byte[4];
  public static byte[] ROUTER_IP = new byte[4];
  public static byte[] REMOTE_CONTROL_IP = new byte[4];
  public static byte[] HEAD= new byte[]{(byte)0x5A ,(byte)0x5A ,(byte)0xA5,(byte)0xA5,(byte)0x3B};

  public static byte DEVICE_TYPE = DEVICE_REMOTE_CONTROL;
  public static byte REMOTE_CONTROL_TYPE = (byte) 0x00;

  // private ArrayList<byte []> ligthLists = new ArrayList<byte[]>();

  public static byte[] getIPBytes(String ip) {
    // 验证ip地址的正则表达式
    String[] tmpStrings = ip.split("\\.");
    byte[] tmp = new byte[4];

    tmp[0] = (byte) Integer.parseInt(tmpStrings[0]);
    tmp[1] = (byte) Integer.parseInt(tmpStrings[1]);
    tmp[2] = (byte) Integer.parseInt(tmpStrings[2]);
    tmp[3] = (byte) Integer.parseInt(tmpStrings[3]);

    return tmp;
  }

  public static void setAndroidIP(String IP) {
    byte[] tmp = getIPBytes(IP);
    if (tmp == null)
      return;
    ANDROID_IP[0] = tmp[0];
    ANDROID_IP[1] = tmp[1];
    ANDROID_IP[2] = tmp[2];
    ANDROID_IP[3] = tmp[3];
  }

  /**
   * 检测判断IP地址是否与主机相同
   * 
   * @param ip:需要判断的IP
   * @return true：是本机的数据包；false：不是本机的数据包
   */
  public static boolean checkIPOwner(byte[] ip) {
    if (ip.length != ANDROID_IP.length) {
      return false;
    }
    for (int i = 0; i < ANDROID_IP.length; i++) {
      if (ANDROID_IP[i] != ip[i]) {
        return false;
      }
    }
    return true;
  }

  public static void setRouterIP(String IP) {
    byte[] tmp = getIPBytes(IP);
    if (tmp == null)
      return;
    ROUTER_IP[0] = tmp[0];
    ROUTER_IP[1] = tmp[1];
    ROUTER_IP[2] = tmp[2];
    ROUTER_IP[3] = tmp[3];
  }

  public static boolean setRouterIPBytes(byte[] routerIP) {
    if (routerIP.length != 4) {
      return false;
    }
    ROUTER_IP[0] = routerIP[0];
    ROUTER_IP[1] = routerIP[1];
    ROUTER_IP[2] = routerIP[2];
    ROUTER_IP[3] = routerIP[3];
    return true;
  }
 
  private static CmdMessage cmdMessage = null;

  private CmdMessage() {
    ANDROID_IP[0] = (byte) 0xC0;
    ANDROID_IP[1] = (byte) 0xA8;
    ANDROID_IP[2] = (byte) 0;
    ANDROID_IP[3] = (byte) 0x01;

    ROUTER_IP[0] = (byte) 0x01;
    ROUTER_IP[1] = (byte) 0;
    ROUTER_IP[2] = (byte) 0;
    ROUTER_IP[3] = (byte) 0x4D;

    REMOTE_CONTROL_IP[0] = (byte) 0x01;
    REMOTE_CONTROL_IP[1] = (byte) 0;
    REMOTE_CONTROL_IP[2] = (byte) 0;
    REMOTE_CONTROL_IP[3] = (byte) 0x52;

  }

  public static CmdMessage getSharedCmdMessage() {
    if (cmdMessage == null)
      cmdMessage = new CmdMessage();
    return cmdMessage;
  }

  /*
   * public ArrayList<byte []> getRemoteIPList(){ return ligthLists; }
   * 
   * public void clearLights(){ ligthLists.clear(); }
   * 
   * public void addLight(byte[] light){ ligthLists.add(light); }
   * 
   * public void removeLightIP(byte[] lightIP){ ligthLists.remove(lightIP); }
   */

  /*
   * 从数据数据中读取命令。
   */
  public CMDTransfer getCMDTransferFromBytes(byte[] data) {
    /*
     * 检查数据的开头和结尾
     */
    if (data[0] != PACKAGE_HEAD || data[data.length - 1] != PACKAGE_TAIL)
      return null;

    /*
     * 创建命令对象，用来保存
     */
    CMD cmd = new CMD(data[1], data[2]);
    int length = (int) data[3];
    /*
     * 读取并保存命令中的IP地址
     */
    byte[] IP1 = new byte[4];
    byte[] IP2 = new byte[4];
    for (int i = 0; i < 4; i++) {
      IP1[i] = data[4 + i];
      IP2[i] = data[8 + i];
    }
    /*
     * 获取命令具体数据
     */
    byte[] content = null;
    if (length > 14) {
      content = new byte[length - 14];
      for (int i = 12; i < data.length - 2; i++) {
        content[i - 12] = data[i];
      }
    }

    byte sum = content[content.length - 2];
    /*
     * 创建命令传输对象
     */
    CMDTransfer cmdTransfer = new CMDTransfer(cmd, IP1, IP2, content, sum);

    return cmdTransfer;
  }

  /*
   * 将命令封装为字节流，以在Socket上传输
   */
  public static byte[] getPackageMessage(CMD cmd, byte[] IP1, byte[] IP2, byte[] content) {
    int packageLength = 10 + 4;
    if (content != null)
      packageLength += content.length;
    byte[] message = new byte[64];
    /*
     * 数据头
     */
    message[0] = PACKAGE_HEAD;
    // 数据长度
    message[1] = (byte) (packageLength - 4);
    // 命令1
    message[2] = cmd.cmd1;
    // 命令2
    message[3] = cmd.cmd2;
    int i = 4;
    // IP1
    for (byte data : IP1) {
      message[i] = data;
      i++;
    }
    // IP2
    for (byte data : IP2) {
      message[i] = data;
      i++;
    }
    // 具体内容
    if (content != null)
      for (byte data : content) {
        message[i] = data;
        i++;
      }
    // 获取校验码
    message[i] = getEOR(cmd, IP1, IP2, content);
    i++;
    // 数据结尾标志
    message[i] = PACKAGE_TAIL;
    return message;
  }

  public static byte[] getPackageMessage( byte[] content) {
	    byte[] message = new byte[64];
	    /*
	     * 数据头
	     */
	   System.arraycopy(CmdMessage.HEAD, 0, message, 0, 5);
	  
	   
	    if (content != null)
	    	 System.arraycopy(content, 0, message, 6, content.length);
	   
	    message[63] =  getEOR(message);
	    return message;
	  }
  // 校验码计算
  public static byte getEOR(CMD cmd, byte[] IP1, byte[] IP2, byte[] message) {
    int res = 0;

    res = cmd.cmd1 + cmd.cmd2;

    if (message != null)
      for (byte msg : message) {
        res = msg + res;
      }

    for (byte msg : IP1) {
      res = msg + res;
    }

    for (byte msg : IP2) {
      res = msg + res;
    }

    return (byte) (res & 0xFF);
  }

  // 校验码计算
  public static byte getEOR( byte[] message) {
    int res = 0;


    if (message != null)
      for (byte msg : message) {
        res = msg + res;
      }


    return (byte) (res & 0xFF);
  }
  public static byte getEOR(CMDTransfer cmdTransfer) {
    return getEOR(cmdTransfer.cmd, cmdTransfer.IP1, cmdTransfer.IP2, cmdTransfer.content);
  }

  public static boolean checkEOR(CMDTransfer cmdTransfer) {
    return getEOR(cmdTransfer) == cmdTransfer.sum;
  }

  // 设置IP地址
  public void setRouterIP(byte a, byte b, byte c, byte d) {
    ROUTER_IP[0] = a;
    ROUTER_IP[1] = b;
    ROUTER_IP[2] = c;
    ROUTER_IP[3] = d;
  }
  public static final String getRemoteAddressString() {
	  byte[] bArray = CmdMessage.ROUTER_IP;
	  StringBuffer sb = new StringBuffer(bArray.length);
	  String sTemp;
	  for (int i = 0; i < bArray.length; i++) {
	   sTemp = Integer.toHexString(0xFF & bArray[i]);
	   if (sTemp.length() < 2)
	    sb.append(0);
	   sb.append(sTemp.toUpperCase());
	  }
	  return sb.toString();
	 }
  // 对象简单封装
  public CMD getNewCMD(byte cmd1, byte cmd2) {
    // TODO Auto-generated method stub
    return new CMD(cmd1, cmd2);
  }

}
