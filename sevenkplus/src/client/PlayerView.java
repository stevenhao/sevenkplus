package client;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PlayerView extends JPanel {
  private static final long serialVersionUID = 1L;
  String name;
  Integer id;
  JLabel nameLabel;
  JPanel statPanel;
  // private Player player; -- where the value will be stored?
  Map<String, StatView> statViews = new HashMap<>();
  Map<String, String> statValues = new HashMap<>();

  public PlayerView() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.name = null;
    this.id = null;
    this.nameLabel = new JLabel();
    this.add(nameLabel);

    statPanel = new JPanel();
    this.add(statPanel);
    this.addStat("VPIP");
    this.refresh();
  }

  public void setPlayerName(String name) {
    this.name = name;
    this.refresh();
  }

  public void setStat(String stat, String statValue) {
    statValues.put(stat, statValue);
  }

  private void refresh() {
    if (name != null) {
      nameLabel.setText(name);
      statPanel.setVisible(true);
    } else {
      statPanel.setVisible(true);
      nameLabel.setText("No player selected");
    }

    for (String stat : statViews.keySet()) {
      StatView statView = statViews.get(stat);
      statView.setValue(this.getValue(stat));
    }
  }

  private String getValue(String stat) {
    if (statValues.containsKey(stat)) {
      return statValues.get(stat);
    } else {
      return "not computed";
    }
  }

  private void addStat(String stat, String value) {
    StatView statView = new StatView(stat, value);
    statViews.put(stat, statView);
    statPanel.add(statView);
  }

  private void addStat(String stat) {
    this.addStat(stat, getValue(stat));
  }
}
