package vchip.usb.send.tools;

/*
 * 命令封装，主要封装了命令的两个字�?
 */
public class CMD {
  public byte cmd1;
  public byte cmd2;

  public CMD(byte cmd1, byte cmd2) {
    this.cmd1 = cmd1;
    this.cmd2 = cmd2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + cmd1;
    result = prime * result + cmd2;
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
    CMD other = (CMD) obj;
    if (cmd1 != other.cmd1)
      return false;
    if (cmd2 != other.cmd2)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CMD [cmd1=0x" + Integer.toHexString((0xff & cmd1)) + ", cmd2=0x"
        + Integer.toHexString((0xff & cmd2)) + "]";
  }
}
