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
import org.jooq.impl.DSL;

import static org.jooq.generated.tables.Hand.HAND;
import static org.jooq.generated.tables.Play.PLAY;
import static org.jooq.generated.tables.Player.PLAYER;

public class DatabaseController {
  private final static Logger logger = Logger.getLogger(DatabaseController.class.getName());

  private final DSLContext db;
  private final Map<String, Player> players;

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

  public Collection<Player> getPlayers() {
    return players.values();
  }

  private Map<String, Player> getPlayersInternal() {
    Map<String, Player> players = new HashMap<>();

    Result<Record> result = db.select().from(PLAYER).fetch();
    for (Record r : result) {
      String name = r.getValue(PLAYER.NAME);
      Integer id = r.getValue(PLAYER.ID);

      if (players.containsKey(name)) {
        throw new IllegalArgumentException("More than one ID found for player name!");
      }
      players.put(name, new Player(name, id));
    }

    return players;
  }

  public Player getPlayer(String playerName) {
    if (players.containsKey(playerName)) {
      return players.get(playerName);
    }

    // No record for this player yet
    return null;
  }

  public Player getOrInsertPlayer(String playerName) {
    Player player = getPlayer(playerName);
    if (player == null) {
      db.insertInto(PLAYER, PLAYER.NAME).values(playerName).execute();
      logger.info("Added new player: " + playerName);

      Result<Record1<Integer>> playerIds =
          db.select(PLAYER.ID).from(PLAYER).where(PLAYER.NAME.equal(playerName)).fetch();

      Integer id = playerIds.get(0).value1();
      player = new Player(playerName, id);
      players.put(playerName, player);
    }

    return player;
  }

  public Integer getOrInsertHand(String handTag) {
    // TODO: Represent hand data with local java objects and only update the DB at the end of each
    // file. Cleaner and significantly improves performance
    Result<Record1<Integer>> handIds = db.select(HAND.ID).from(HAND)
        .where(HAND.TAG.equal(handTag)).fetch();
    if (handIds.isEmpty()) {
      db.insertInto(HAND, HAND.TAG).values(handTag).execute();
      logger.info("Added new hand: " + handTag);
      handIds = db.select(HAND.ID).from(HAND).where(HAND.TAG.equal(handTag)).fetch();
    }

    if (handIds.size() > 1) {
      throw new IllegalArgumentException("More than one ID found for hand tag!");
    }

    return handIds.get(0).value1();
  }

  public void addToHand(int playerId, int handId) {
    if (!db.select().from(PLAY).where(PLAY.PLAYERID.equal(playerId))
        .and(PLAY.HANDID.equal(handId)).fetch().isEmpty()) {
      return;
    }

    db.insertInto(PLAY, PLAY.PLAYERID, PLAY.HANDID).values(playerId, handId)
        .execute();
  }

  public void updateVPIP(int playerId, int handId) {
    db.update(PLAY).set(PLAY.VPIP, (byte) 1)
        .where(PLAY.PLAYERID.equal(playerId).and(PLAY.HANDID.equal(handId))).execute();
  }

  public void updatePFR(int playerId, int handId) {
    db.update(PLAY).set(PLAY.PFR, (byte) 1)
        .where(PLAY.PLAYERID.equal(playerId).and(PLAY.HANDID.equal(handId))).execute();
  }

  public int getHands(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(PLAY).where(PLAY.PLAYERID.equal(playerId)).fetchOne();
    return (int) record.getValue(0);
  }

  public int getHandsVPIP(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(PLAY).where(PLAY.PLAYERID.equal(playerId))
            .and(PLAY.VPIP.equal((byte) 1)).fetchOne();
    return (int) record.getValue(0);
  }

  public int getHandsPFR(int playerId) {
    Record1<Integer> record =
        db.selectCount().from(PLAY).where(PLAY.PLAYERID.equal(playerId))
            .and(PLAY.PFR.equal((byte) 1)).fetchOne();
    return (int) record.getValue(0);
  }
}
