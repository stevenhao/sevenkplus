package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import data.DatabaseController;

public class Server {
  private final int port;
  private Socket clientSocket;
  private ServerSocket serverSocket;
  private PrintWriter out;
  private BufferedReader in;
  private final DatabaseController db;
  private final static Logger logger = Logger.getLogger(Server.class.getName());

  public Server(int port) throws SQLException {
    this.port = port;
    this.db = new DatabaseController();
  }

  private static String join(Collection<String> strings) {
    String result = "";
    for (String player : strings) {
      if (!result.equals("")) {
        result = result + ",";
      }
      result = result + player;
    }
    return result;
  }

  private String executeServerCall(String command) {
    if ("getPlayerList".equals(command)) {
      return join(db.getPlayers());
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

    logger.info("Connected to a client.");
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
    logger.info("Serving at port " + portNumber);

    Server server = null;
    try {
      server = new Server(portNumber);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "ERROR: Failed to connect to database.");
      e.printStackTrace();
    }
    logger.info("Server started.");

    server.connectToClients();
  }
}
