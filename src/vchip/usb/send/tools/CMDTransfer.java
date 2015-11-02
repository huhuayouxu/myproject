package vchip.usb.send.tools;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * ����Ϊ���ݴ��������Ҫ��Socket��������֮�󣬽���ΪCMDTransfer�������ö���ͨ��Handler������UI����
 */
public class CMDTransfer {

  // �������
  public CMD cmd;
  // IP��ַ
  public byte[] IP1;
  // IP��ַ
  public byte[] IP2;
  // ��������
  public byte[] content;
  // У��
  public byte sum;

  public CMDTransfer(CMD cmd, byte[] IP1, byte[] IP2, byte[] content, byte sum) {
    this.cmd = new CMD(cmd.cmd1, cmd.cmd2);
    for (int i = 0; i < 4; i++) {
      this.IP1[i] = IP1[i];
      this.IP2[i] = IP2[i];
    }
    this.content = new byte[content.length];
    for (int i = 0; i < content.length; i++) {
      this.content[i] = content[i];
    }
    this.sum = sum;
  }

  public CMDTransfer(ArrayList<Byte> bufferReadTmp) {

    cmd = new CMD((byte) (0xff & bufferReadTmp.get(2)), (byte) (0xff & bufferReadTmp.get(3)));
    IP1 = new byte[4];
    IP2 = new byte[4];
    for (int i = 0; i < 4; i++) {
      IP1[i] = (byte) (0xff & bufferReadTmp.get(i + 4));
      IP2[i] = (byte) (0xff & bufferReadTmp.get(i + 8));
    }
    if (bufferReadTmp.size() - 14 > 0) {
      content = new byte[bufferReadTmp.size() - 14];
      for (int i = 0; i < content.length; i++)
        content[i] = (byte) (0xff & bufferReadTmp.get(i + 12));
    } else {
      content = new byte[0];
    }
    sum = bufferReadTmp.get(bufferReadTmp.size() - 2);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(IP1);
    result = prime * result + Arrays.hashCode(IP2);
    result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
    result = prime * result + Arrays.hashCode(content);
    result = prime * result + sum;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CMDTransfer other = (CMDTransfer) obj;
    if (!Arrays.equals(IP1, other.IP1))
      return false;
    if (!Arrays.equals(IP2, other.IP2))
      return false;
    if (cmd == null) {
      if (other.cmd != null)
        return false;
    } else if (!cmd.equals(other.cmd))
      return false;
    if (!Arrays.equals(content, other.content))
      return false;
    if (sum != other.sum)
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte tmp : content) {
      String tmpString = Integer.toHexString(tmp & 0xFF).toUpperCase();
      sb.append(" ");
      sb.append(tmpString.length() < 2 ? "0" + tmpString : tmpString);
    }
    return "CMDTransfer [cmd=" + cmd + ", content=" + sb.toString() + "]";
  }

}
