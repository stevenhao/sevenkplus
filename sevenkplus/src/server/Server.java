package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import static org.jooq.generated.Tables.PLAYER;

public class Server {
  private final int port;
  private Socket clientSocket;
  private ServerSocket serverSocket;
  private PrintWriter out;
  private BufferedReader in;

  public Server(int port) {
    this.port = port;
  }

  private void connectToDB() {
    // TODO: move these to config file
    String userName = "root";
    String password = "";
    String url = "jdbc:mysql://localhost:3306/sevenkplus";

    // Connection is the only JDBC resource that we need
    // PreparedStatement and ResultSet are handled by jOOQ, internally
    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
      Result<Record> result = create.select().from(PLAYER).fetch();
      for (Record r : result) {
        Integer id = r.getValue(PLAYER.ID);
        String name = r.getValue(PLAYER.NAME);
        System.out.println("ID: " + id + " title: " + name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void connectToClient() {
    try {
      serverSocket = new ServerSocket(port);
      clientSocket = serverSocket.accept();
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    out.println("Sup Client.");
    out.flush();
  }

  public static void main(String[] args) {
    int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
    System.out.println("Serving at port " + portNumber);
    Server server = new Server(portNumber);
    System.out.println("Server started.");
    server.connectToDB();
    System.out.println("Connected to database.");
    server.connectToClient();
    System.out.println("Connected to client.");
  }
}
