package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.generated.tables.Hand;
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

  private static int getOrInsertHand(String handTag) {
    Result<Record1<Integer>> handIds =
        db.select(Hand.HAND.ID).from(Hand.HAND).where(Hand.HAND.TAG.equal(handTag)).fetch();
    if (handIds.isEmpty()) {
      db.insertInto(Hand.HAND, Hand.HAND.TAG).values(handTag).execute();
      System.out.println("Added new hand: " + handTag);
      handIds = db.select(Hand.HAND.ID).from(Hand.HAND).where(Hand.HAND.TAG.equal(handTag)).fetch();
    }

    if (handIds.size() > 1) {
      throw new IllegalArgumentException("More than one ID found for hand tag!");
    }

    return handIds.get(0).value1();
  }

  private static int getOrInsertPlayer(String playerName) {
    Result<Record1<Integer>> playerIds = db.select(Player.PLAYER.ID).from(Player.PLAYER).where(
        Player.PLAYER.NAME.equal(playerName)).fetch();
    if (playerIds.isEmpty()) {
      db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(playerName).execute();
      System.out.println("Added new player: " + playerName);
      playerIds = db.select(Player.PLAYER.ID).from(Player.PLAYER).where(
          Player.PLAYER.NAME.equal(playerName)).fetch();
    }

    if (playerIds.size() > 1) {
      throw new IllegalArgumentException("More than one ID found for player name!");
    }

    return playerIds.get(0).value1();
  }

  private static void parseFile(BufferedReader in) throws IOException {
    String nextLine = in.readLine();
    int currentHand = -1;

    while (nextLine != null) {
      String[] tokens = nextLine.split("\\s+");

      if ("Hand".equals(tokens[0])) {
        // Starting a new hand

        String hand = tokens[1];
        currentHand = getOrInsertHand(hand);
      } else if ("Seat".equals(tokens[0])) {
        // Info on a player at the table

        if (currentHand == -1) {
          throw new IllegalArgumentException("No hand tag found before player mentioned!");
        }
        String player = tokens[2]; // Format: "Seat #: (username)"
        getOrInsertPlayer(player);
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
