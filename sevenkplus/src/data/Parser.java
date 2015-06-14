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
import org.jooq.generated.tables.Play;
import org.jooq.generated.tables.Player;
import org.jooq.impl.DSL;

public class Parser {
  private final File logsDir;
  private DSLContext db;

  public Parser(File logsDir) {
    this.logsDir = logsDir;
  }

  private DSLContext getDatabaseContext() throws SQLException {
    // TODO: move these to config file
    String userName = "root";
    String password = "";
    String url = "jdbc:mysql://localhost:3306/sevenkplus";

    // Connection is the only JDBC resource that we need
    // PreparedStatement and ResultSet are handled by jOOQ, internally
    Connection conn = DriverManager.getConnection(url, userName, password);
    return DSL.using(conn, SQLDialect.MYSQL);
  }

  private Integer getOrInsertHand(String handTag) {
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

  private Integer getPlayer(String playerName) {
    Result<Record1<Integer>> playerIds = db.select(Player.PLAYER.ID).from(Player.PLAYER).where(
        Player.PLAYER.NAME.equal(playerName)).fetch();

    if (playerIds.isEmpty()) {
      // No record in the DB for this player yet
      return null;
    } else if (playerIds.size() > 1) {
      throw new IllegalArgumentException("More than one ID found for player name!");
    }

    return playerIds.get(0).value1();
  }

  private Integer getOrInsertPlayer(String playerName) {
    if (getPlayer(playerName) == null) {
      db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(playerName).execute();
      System.out.println("Added new player: " + playerName);
    }

    return getPlayer(playerName);
  }

  private void addToHand(int playerId, int handId) {
    if (!db.select().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId))
        .and(Play.PLAY.HANDID.equal(handId)).fetch().isEmpty()) {
      return;
    }

    db.insertInto(Play.PLAY, Play.PLAY.PLAYERID, Play.PLAY.HANDID).values(playerId, handId)
        .execute();
  }

  private void updateVPIP(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.VPIP, (byte) 1).where(Play.PLAY.PLAYERID.equal(playerId)
        .and(Play.PLAY.HANDID.equal(handId))).execute();
  }

  private void updatePFR(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.PFR, (byte) 1).where(Play.PLAY.PLAYERID.equal(playerId)
        .and(Play.PLAY.HANDID.equal(handId))).execute();
  }

  private void parseFile(BufferedReader in) throws IOException {
    String nextLine = in.readLine();
    String currentStreet = ""; // hole (PF), flop, turn, river, main/side/pot (showdown)
    Integer currentHand = null;

    while (nextLine != null) {
      String[] tokens = nextLine.split("\\s+");

      if ("".equals(tokens[0])) {
        // Blank lines between hands, skip

      } else if ("Hand".equals(tokens[0])) {
        // Starting a new hand

        String hand = tokens[1];
        currentHand = getOrInsertHand(hand);
        currentStreet = "";
      } else if ("Game:".equals(tokens[0])) {
        // Game type, buyins, blinds

      } else if ("Site:".equals(tokens[0])) {
        // SWC

      } else if ("Table:".equals(tokens[0])) {
        // Game type, table size, blinds, table index

      } else if ("**".equals(tokens[0])) {
        // Indicates start of next street
        currentStreet = tokens[1];
      } else if ("Dealt".equals(tokens[0])) {
        // Cards given to current player

      } else if ("Rake".equals(tokens[0])) {
        // Rake taken from total pot

      } else if ("Seat".equals(tokens[0])) {
        // Info on a player at the table

        if (currentHand == null) {
          throw new IllegalArgumentException("No hand tag found before player mentioned");
        }

        // Ignore players at the table who are sitting out or waiting for a blind
        if (!"out".equals(tokens[tokens.length - 1]) &&
            !"blind".equals(tokens[tokens.length - 1])) {
          String player = tokens[2]; // Format: "Seat #: (username)"
          Integer playerId = getOrInsertPlayer(player);

          // Add this pair to the plays table
          addToHand(playerId, currentHand);
        }
      } else {
        // Player actions (post, show, win, act)
        String player = tokens[0];
        Integer playerId;

        if ("posts".equals(tokens[1])) {
          // We should have seen this player before in "Seat: ". However, it's possible for
          // the player to be waiting for the big blind or sitting out and then to post.
          playerId = getOrInsertPlayer(player);
        } else {
          playerId = getPlayer(player);
        }

        if ("Hole".equals(currentStreet)) {
          if (currentHand == null || playerId == null) {
            throw new IllegalArgumentException("Bad parse or action from player not in data: "
                + nextLine);
          }

          String action = tokens[1];
          if ("calls".equals(action)) {
            updateVPIP(playerId, currentHand);
          } else if ("raises".equals(action)) {
            updateVPIP(playerId, currentHand);
            updatePFR(playerId, currentHand);
          }
        }
      }

      nextLine = in.readLine();
    }
  }

  public void run() throws IOException {
    System.out.println("Searching in " + logsDir.getPath());
    try {
      db = getDatabaseContext();
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }

    File[] listOfFiles = logsDir.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        System.out.println("Parsing file " + file.getName());

        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
          parseFile(in);
        } finally {
          in.close();
        }
      }
    }
  }

  public static void main(String[] args) {
    Parser parser = new Parser(new File("../logs"));
    try {
      parser.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
