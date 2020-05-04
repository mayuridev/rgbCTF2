package xyz.rgbsec.util;

public class TraceUtils {
  public static String getLineNumber() {
    String out = "";
    for (StackTraceElement n : new Throwable().getStackTrace()) {

      if (n.getClassName().contains("rgbsec")) {
        out +=
            String.format(
                "\n\t@ %s at function %s, line %d",
                n.getFileName(), n.getMethodName(), n.getLineNumber());
      }
    }
    return out;
  }
}
