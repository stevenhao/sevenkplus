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

  // Identifier for indication of a player. Username follows immediately after this string
  private static final String PLAYER_STRING = "Seat #: ";

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

  public static void main(String[] args) throws IOException, SQLException {
    System.out.println(logsDir.getName());
    db = getDatabaseContext();

    File[] listOfFiles = logsDir.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        System.out.println("Parsing " + file.getName());
        BufferedReader in = new BufferedReader(new FileReader(file));

        String nextLine = in.readLine();
        while (nextLine != null) {
          if (nextLine.startsWith(PLAYER_STRING)) {
            String player = "";
            int cloc = PLAYER_STRING.length();
            while (nextLine.charAt(cloc) != ' ') {
              player += nextLine.charAt(cloc);
              cloc++;
            }

            if (db.select().from(Player.PLAYER).where(Player.PLAYER.NAME.equal(player)).fetch()
                .isEmpty()) {
              db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(player).execute();
              System.out.println("Added new player: " + player);
            }
          }

          nextLine = in.readLine();
        }

        in.close();
      }
    }
  }
}
