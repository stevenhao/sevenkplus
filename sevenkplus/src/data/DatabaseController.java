package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.generated.tables.Hand;
import org.jooq.generated.tables.Play;
import org.jooq.generated.tables.Player;
import org.jooq.impl.DSL;

public class DatabaseController {
  // TODO: move these to config file
  private final static String userName = "root";
  private final static String password = "";
  private final static String url = "jdbc:mysql://localhost:3306/sevenkplus";
  private final DSLContext db;

  public DatabaseController() throws SQLException {
    db = getDatabaseContext();
  }

  private DSLContext getDatabaseContext() throws SQLException {
    // Connection is the only JDBC resource that we need
    // PreparedStatement and ResultSet are handled by jOOQ, internally
    Connection conn = DriverManager.getConnection(url, userName, password);

    System.out.println("Connected to database");
    return DSL.using(conn, SQLDialect.MYSQL);
  }

  public List<String> getPlayers() {
    List<String> list = new ArrayList<String>();

    Result<Record> result = db.select().from(Player.PLAYER).fetch();
    for (Record r : result) {
      String name = r.getValue(Player.PLAYER.NAME);
      list.add(name);
    }

    return list;
  }

  protected Integer getPlayer(String playerName) {
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

  protected Integer getOrInsertPlayer(String playerName) {
    // TODO: Represent player/hand data with local java objects and only update the DB at the end of
    // each file. Cleaner and significantly improves performance
    if (getPlayer(playerName) == null) {
      db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(playerName).execute();
      System.out.println("Added new player: " + playerName);
    }

    return getPlayer(playerName);
  }

  protected Integer getOrInsertHand(String handTag) {
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

  protected void addToHand(int playerId, int handId) {
    if (!db.select().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId))
        .and(Play.PLAY.HANDID.equal(handId)).fetch().isEmpty()) {
      return;
    }

    db.insertInto(Play.PLAY, Play.PLAY.PLAYERID, Play.PLAY.HANDID).values(playerId, handId)
        .execute();
  }

  protected void updateVPIP(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.VPIP, (byte) 1).where(Play.PLAY.PLAYERID.equal(playerId)
        .and(Play.PLAY.HANDID.equal(handId))).execute();
  }

  protected void updatePFR(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.PFR, (byte) 1).where(Play.PLAY.PLAYERID.equal(playerId)
        .and(Play.PLAY.HANDID.equal(handId))).execute();
  }
}
