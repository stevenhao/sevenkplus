package common;

import java.util.Collection;

public class Utils {
  public static String join(Collection<String> strings) {
    String result = "";
    for (String player : strings) {
      if (!result.equals("")) {
        result = result + ",";
      }
      result = result + player;
    }
    return result;
  }

  public static String getRatioString(int numerator, int denominator) {
    if (denominator == 0) {
      return numerator + " / " + denominator;
    }

    double ratio = numerator / ((double) denominator);
    return numerator + " / " + denominator + " (" + ratio * 100d + "%)";
  }
}
