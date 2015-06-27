package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
  private final static Logger logger = Logger.getLogger(DatabaseController.class.getName());

  private final DSLContext db;
  private final Map<String, PPlayer> players;

  public DatabaseController() throws SQLException {
    db = getDatabaseContext();
    players = getPlayersInternal();
  }

  private DSLContext getDatabaseContext() throws SQLException {
    String username = Utils.getMysqlUsername();
    String password = Utils.getMysqlPassword();
    String url = Utils.getMysqlUrl();
    Connection conn = DriverManager.getConnection(url, username, password);

    logger.info("Connected to database");
    return DSL.using(conn, SQLDialect.MYSQL);
  }

  public Collection<PPlayer> getPlayers() {
    return players.values();
  }

  private Map<String, PPlayer> getPlayersInternal() {
    Map<String, PPlayer> players = new HashMap<>();

    Result<Record> result = db.select().from(Player.PLAYER).fetch();
    for (Record r : result) {
      String name = r.getValue(Player.PLAYER.NAME);
      Integer id = r.getValue(Player.PLAYER.ID);

      if (players.containsKey(name)) {
        throw new IllegalArgumentException("More than one ID found for player name!");
      }
      players.put(name, new PPlayer(name, id));
    }

    return players;
  }

  public PPlayer getPlayer(String playerName) {
    if (players.containsKey(playerName)) {
      return players.get(playerName);
    }

    // No record for this player yet
    return null;
  }

  public PPlayer getOrInsertPlayer(String playerName) {
    PPlayer player = getPlayer(playerName);
    if (player == null) {
      db.insertInto(Player.PLAYER, Player.PLAYER.NAME).values(playerName).execute();
      logger.info("Added new player: " + playerName);

      Result<Record1<Integer>> playerIds = db.select(Player.PLAYER.ID).from(Player.PLAYER)
          .where(Player.PLAYER.NAME.equal(playerName)).fetch();

      Integer id = playerIds.get(0).value1();
      player = new PPlayer(playerName, id);
      players.put(playerName, player);
    }

    return player;
  }

  public Integer getOrInsertHand(String handTag) {
    // TODO: Represent hand data with local java objects and only update the DB at the end of each
    // file. Cleaner and significantly improves performance
    Result<Record1<Integer>> handIds = db.select(Hand.HAND.ID).from(Hand.HAND)
        .where(Hand.HAND.TAG.equal(handTag)).fetch();
    if (handIds.isEmpty()) {
      db.insertInto(Hand.HAND, Hand.HAND.TAG).values(handTag).execute();
      logger.info("Added new hand: " + handTag);
      handIds = db.select(Hand.HAND.ID).from(Hand.HAND).where(Hand.HAND.TAG.equal(handTag)).fetch();
    }

    if (handIds.size() > 1) {
      throw new IllegalArgumentException("More than one ID found for hand tag!");
    }

    return handIds.get(0).value1();
  }

  public void addToHand(int playerId, int handId) {
    if (!db.select().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId))
        .and(Play.PLAY.HANDID.equal(handId)).fetch().isEmpty()) {
      return;
    }

    db.insertInto(Play.PLAY, Play.PLAY.PLAYERID, Play.PLAY.HANDID).values(playerId, handId)
        .execute();
  }

  public void updateVPIP(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.VPIP, (byte) 1)
        .where(Play.PLAY.PLAYERID.equal(playerId).and(Play.PLAY.HANDID.equal(handId))).execute();
  }

  public void updatePFR(int playerId, int handId) {
    db.update(Play.PLAY).set(Play.PLAY.PFR, (byte) 1)
        .where(Play.PLAY.PLAYERID.equal(playerId).and(Play.PLAY.HANDID.equal(handId))).execute();
  }

  public int getHands(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId)).fetchOne();
    return (int) record.getValue(0);
  }

  public int getHandsVPIP(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId))
            .and(Play.PLAY.VPIP.equal((byte) 1)).fetchOne();
    return (int) record.getValue(0);
  }

  public int getHandsPFR(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(Play.PLAY).where(Play.PLAY.PLAYERID.equal(playerId))
            .and(Play.PLAY.PFR.equal((byte) 1)).fetchOne();
    return (int) record.getValue(0);
  }
}
