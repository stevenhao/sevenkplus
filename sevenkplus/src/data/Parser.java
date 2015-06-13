package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.generated.tables.Player;
import org.jooq.impl.DSL;

public class Parser {
  private static final File logsDir = new File("../logs");
  private static DSLContext db;

  private static DSLContext getDatabaseContext() throws SQLException {
    // TODO: move these to config file
    String userName = "root";
    String password = "";
    String url = "jdbc:mysql://localhost:3306/sevenkplus";

    // Connection is the only JDBC resource that we need
    // PreparedStatement and ResultSet are handled by jOOQ, internally
    Connection conn = DriverManager.getConnection(url, userName, password);
    return DSL.using(conn, SQLDialect.MYSQL);
  }

  private static void parseFile(BufferedReader in) throws IOException {
    String nextLine = in.readLine();
    while (nextLine != null) {
      String[] tokens = nextLine.split("\\s+");

      if ("Hand".equals(tokens[0])) {
        // Starting a new hand

        System.out.println("Reading hand " + tokens[1]);
      } else if ("Seat".equals(tokens[0])) {
        // Info on a player at the table

        String player = tokens[2]; // Format: "Seat #: (username)"

        if (db.select().from(Player.PLAYER).where(Player.PLAYER.NAME.equal(player)).fetch()
            .isEmpty()) {
          db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(player).execute();
          System.out.println("Added new player: " + player);
        }
      }

      nextLine = in.readLine();
    }
  }

  public static void main(String[] args) throws IOException, SQLException {
    System.out.println(logsDir.getName());
    db = getDatabaseContext();

    File[] listOfFiles = logsDir.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        System.out.println("Parsing " + file.getName());

        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
          parseFile(in);
        } finally {
          in.close();
        }
      }
    }
  }
}
