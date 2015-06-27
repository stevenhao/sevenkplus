package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import common.DatabaseController;
import common.Player;

public class Parser {
  private final File logsDir;
  private final DatabaseController db;
  private final static Logger logger = Logger.getLogger(Parser.class.getName());

  public Parser(File logsDir) throws SQLException {
    this.logsDir = logsDir;
    this.db = new DatabaseController();
  }

  private void parseFile(BufferedReader in) throws IOException {
    String nextLine = in.readLine();
    String currentStreet = ""; // hole (PF), flop, turn, river, main/side/pot (showdown)
    Integer currentHand = null;

    while (nextLine != null) {
      String[] tokens = nextLine.split("\\s+");

      if ("Hand".equals(tokens[0])) {
        // Starting a new hand

        String hand = tokens[1];
        currentHand = db.getOrInsertHand(hand);
        currentStreet = "";
      } else if ("".equals(tokens[0])) {
        // Blank lines between hands, skip

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
          String playerName = tokens[2]; // Format: "Seat #: (username)"
          Player player = db.getOrInsertPlayer(playerName);

          // Add this pair to the plays table
          db.addToHand(player.getId(), currentHand);
        }
      } else {
        // Player actions (post, show, win, act)
        String playerName = tokens[0];
        Player player;

        if ("posts".equals(tokens[1])) {
          // We should have seen this player before in "Seat: ". However, it's possible for
          // the player to be waiting for the big blind or sitting out and then to post.
          player = db.getOrInsertPlayer(playerName);
        } else {
          player = db.getPlayer(playerName);
        }

        if ("Hole".equals(currentStreet)) {
          if (currentHand == null || player == null) {
            throw new IllegalArgumentException("Bad parse or action from player not in data: "
                + nextLine);
          }

          String action = tokens[1];
          if ("calls".equals(action)) {
            db.updateVPIP(player.getId(), currentHand);
          } else if ("raises".equals(action)) {
            db.updateVPIP(player.getId(), currentHand);
            db.updatePFR(player.getId(), currentHand);
          }
        }
      }

      nextLine = in.readLine();
    }
  }

  public void run() throws IOException {
    logger.info("Searching in " + logsDir.getPath());

    File[] listOfFiles = logsDir.listFiles();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        logger.info("Parsing file " + file.getName());

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
    try {
      Parser parser = new Parser(new File("../logs"));
      parser.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
