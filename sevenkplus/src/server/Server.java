package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.DatabaseController;
import common.Utils;

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

  private String executePlayerListCommand(String[] params) {
    if (params.length == 0) {
      return Utils.join(db.getPlayers());
    }

    return "wrong number of tokens";
  }

  private String executeStatCommand(String[] params) {
    String statName = params[1];
    if ("VPIP".equals(statName)) {
      if (params.length == 3) {
        Integer playerId = db.getPlayer(params[2]);

        if (playerId != null) {
          int hands = db.getHands(playerId);
          int handsVPIP = db.getHandsVPIP(playerId);

          return Utils.getRatioString(handsVPIP, hands);
        }
      } else {
        return "wrong number of tokens";
      }
    } else if ("PFR".equals(statName)) {
      if (params.length == 3) {
        Integer playerId = db.getPlayer(params[2]);
        if (playerId != null) {
          int hands = db.getHands(playerId);
          int handsPFR = db.getHandsPFR(playerId);

          return Utils.getRatioString(handsPFR, hands);
        }
      } else {
        return "wrong number of tokens";
      }
    }

    logger.warning("Stat not found: " + statName);
    return "invalid stat";
  }

  private String executeServerCall(String query) {
    String[] tokens = query.split("\\s+");
    String command = tokens[0];
    String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

    if ("getPlayerList".equals(command)) {
      return executePlayerListCommand(params);
    } else if ("getStat".equals(command)) {
      return executeStatCommand(params);
    }

    logger.warning("Invalid command from client: " + command);
    return "invalid command";
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
