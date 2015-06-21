package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import data.DatabaseController;
import org.apache.commons.lang3.StringUtils;

public class Server {
  private final int port;
  private Socket clientSocket;
  private ServerSocket serverSocket;
  private PrintWriter out;
  private BufferedReader in;
  private final DatabaseController db;

  public Server(int port) throws SQLException {
    this.port = port;
    this.db = new DatabaseController();
  }

  private String executeServerCall(String command) {
    if ("getPlayerList".equals(command)) {
      return StringUtils.join(db.getPlayers(), ',');
    } else {
      return "invalid command";
    }
  }

  private boolean acceptClient() {
    try {
      clientSocket = serverSocket.accept();
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(
          clientSocket.getInputStream()));
    } catch (IOException e) {
      return false;
    }
    System.out.println("Connected to a client.");
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          String line;
          while ((line = in.readLine()) != null) {
            String result = executeServerCall(line);
            out.println(result);
            out.flush();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    return true;
  }

  private boolean connectToClients() {
    try {
      serverSocket = new ServerSocket(port);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    while (acceptClient()) {
      // AC
    }
    return true;
  }

  public static void main(String[] args) {
    int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
    System.out.println("Serving at port " + portNumber);

    Server server = null;
    try {
      server = new Server(portNumber);
    } catch (SQLException e) {
      System.out.println("ERROR: Failed to connect to database.");
      e.printStackTrace();
    }
    System.out.println("Server started.");

    server.connectToClients();
  }
}
