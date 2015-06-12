package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
  private final String host;
  private final int port;
  private Socket clientSocket;
  private ServerSocket serverSocket;
  private PrintWriter out;
  private BufferedReader in;

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  private void connectToServer() {
    try {
      System.out.println("connecting to port " + port);
      Socket socket = new Socket(host, port);
      System.out.println("connected.");
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(
          socket.getInputStream()));
      System.out.println(in.readLine());
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  public static void main(String[] args) {
    int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
    String hostName = "localhost";
  }
}
