package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
  private static File folder = new File("../logs");

  public static void main(String[] args) throws IOException {
    System.out.println(folder.getName());

    File[] listOfFiles = folder.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        System.out.println("Parsing " + file.getName());
        BufferedReader in = new BufferedReader(new FileReader(file));
        System.out.println(in.readLine());
      }
    }
  }
}
