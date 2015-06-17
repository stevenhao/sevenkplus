package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.generated.tables.Player;
import org.jooq.impl.DSL;

public class Server {
  private final int port;
  private Socket clientSocket;
  private ServerSocket serverSocket;
  private PrintWriter out;
  private BufferedReader in;
  private DSLContext db;

  public Server(int port) {
    this.port = port;
  }

  public ArrayList<String> getPlayerList() {
    String userName = "root";
    String password = "";
    String url = "jdbc:mysql://localhost:3306/sevenkplus";
    ArrayList<String> list = new ArrayList<String>();

    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      db = DSL.using(conn, SQLDialect.MYSQL);
      Result<Record> result = db.select().from(Player.PLAYER).fetch();
      for (Record r : result) {
        String name = r.getValue(Player.PLAYER.NAME);
        list.add(name);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return list;
  }

  private void connectToDB() {
    // TODO: move these to config file
    String userName = "root";
    String password = "";
    String url = "jdbc:mysql://localhost:3306/sevenkplus";

    // Connection is the only JDBC resource that we need
    // PreparedStatement and ResultSet are handled by jOOQ, internally
    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      db = DSL.using(conn, SQLDialect.MYSQL);
      Result<Record> result = db.select().from(Player.PLAYER).fetch();
      for (Record r : result) {
        Integer id = r.getValue(Player.PLAYER.ID);
        String name = r.getValue(Player.PLAYER.NAME);
        System.out.println("ID: " + id + " title: " + name);
      }

      System.out.println("Connected to database.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String executeServerCall(String command) {
    if ("getPlayerList".equals(command)) {
      return StringUtils.join(getPlayerList(), ',');
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

    while (acceptClient())
      ;
    return true;
  }

  public static void main(String[] args) {
    int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
    System.out.println("Serving at port " + portNumber);

    Server server = new Server(portNumber);
    System.out.println("Server started.");

    server.connectToDB();
    server.connectToClients();
  }
}
