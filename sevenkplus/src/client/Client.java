package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Client {
  private final String host;
  private final int port;
  private PrintWriter out;
  private BufferedReader in;
  private JLabel statusBar;
  private PlayerView playerView;
  private JList<String> list;
  private Socket socket;
  private String playerName;
  private final static Logger logger = Logger.getLogger(Client.class.getName());

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  private void connectToServer() {
    try {
      logger.info("connecting to port " + port);
      socket = new Socket(host, port);
      logger.info("connected.");

      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  private String makeServerCall(String command) {
    logger.info("Making server call [" + command + "]");
    try {
      out.println(command);
      String result = in.readLine();
      logger.info("Received result [" + result + "]");
      return result;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Not connected to server.");
      return null;
    }
  }

  private String[] getPlayerList() {
    try {
      String result = makeServerCall("getPlayerList");
      String[] ar = result.split(",");
      Arrays.sort(ar);
      return ar;
    } catch (Exception e) {
      String[] defaultPlayerList = new String[200];
      for (int i = 0; i < 200; ++i) {
        defaultPlayerList[i] = String.format("default_user_%d", i);
      }
      return defaultPlayerList;
    }
  }

  private String getStatValue(String stat) {
    String result = makeServerCall("getStat " + stat + " " + this.playerName);
    return result;
  }

  private void selectPlayer(String name) {
    if (!name.equals(this.playerName)) {
      this.playerName = name;
      logger.info("Selected Player " + playerName);
      playerView.setPlayerName(list.getSelectedValue());
      for (String statName : playerView.getStats()) {
        logger.info("Found stat " + statName);
        playerView.setStat(statName, getStatValue(statName));
      }
      playerView.refresh();
    }
  }

  private void makeGui() {
    JFrame frame = new JFrame("7k+");

    // Create the menu bar. Make it have a green background.
    JMenuBar menuBar = new JMenuBar();
    menuBar.setOpaque(true);
    menuBar.setBackground(new Color(154, 165, 127));
    menuBar.setPreferredSize(new Dimension(0, 20));

    String[] playerList = getPlayerList();
    list = new JList<String>(playerList);
    list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    list.setVisibleRowCount(-1);
    // list.setPreferredSize(new Dimension(150, 200));
    list.addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        selectPlayer(list.getSelectedValue());
      }
    });

    JScrollPane listScroller = new JScrollPane(list);
    listScroller.setPreferredSize(new Dimension(150, 200));

    statusBar = new JLabel();
    statusBar.setBackground(Color.WHITE);
    statusBar.setOpaque(true);
    // statusBar.setText("Status bar.");
    statusBar.setHorizontalAlignment(SwingConstants.CENTER);
    statusBar.setVerticalAlignment(SwingConstants.CENTER);
    statusBar.setPreferredSize(new Dimension(0, 20));

    playerView = new PlayerView();
    playerView.setPreferredSize(new Dimension(300, 200));
    frame.add(playerView);

    // Set the menu bar and add the content to the content pane.
    frame.setJMenuBar(menuBar);
    frame.getContentPane().add(listScroller, BorderLayout.WEST);
    frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    int portNumber = args.length >= 1 ? Integer.parseInt(args[0]) : 5000;
    String hostName = "localhost";

    Client client = new Client(hostName, portNumber);
    client.connectToServer();
    client.makeGui();
  }
}
