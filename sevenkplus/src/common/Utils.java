package common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Utils {

  static final Map<String, String> secrets;
  static {
    secrets = new HashMap<>();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(
        new FileInputStream("prefs.conf")))) {
      String line = null;
      while ((line = in.readLine()) != null) {
        int commentStart = line.indexOf("#");
        if (commentStart != -1) {
          line = line.substring(0, commentStart);
        }
        if (line.isEmpty()) {
          continue;
        }

        String[] tokens = line.split("=");
        if (tokens.length == 2) {
          secrets.put(tokens[0].trim(), tokens[1].trim());
        } else if (tokens.length == 0) {
          // continue
        } else {
          System.err.println(line);
          throw new Exception("error parsing config file");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getSecret(String key) {
    if (secrets.containsKey(key)) {
      return secrets.get(key);
    } else {
      return "";
    }
  }

  public static String getMysqlUsername() {
    return secrets.get("mysql_username");
  }

  public static String getMysqlPassword() {
    return secrets.get("mysql_password");
  }

  public static String getMysqlUrl() {
    return secrets.get("mysql_url");
  }

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
    return numerator + " / " + denominator + " ("
        + String.format("%.1f", ratio * 100d) + "%)";
  }

}
