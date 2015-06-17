package client;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PlayerView extends JPanel {
  String name;
  Integer id;
  JLabel nameLabel;
  
  public PlayerView() {
    this.name = null;
    this.id = null;
    nameLabel = new JLabel();
    this.add(nameLabel);
    refresh();
  }
  
  public void setPlayerName(String name) {
    this.name = name;
    this.refresh();
  }
  
  private void refresh() {
    if (name != null) {
      nameLabel.setText(name);
    } else {
      nameLabel.setText("No player selected");
    }
  }
}
