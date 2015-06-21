package client;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StatView extends JPanel {
  private static final long serialVersionUID = 1L;
  private JLabel nameLabel;
  private JLabel valueLabel;

  public StatView(String name, String initialValue) {
    // this.setPreferredSize(new Dimension(0, 0));
    this.setLayout(new GridLayout(0, 1));
    this.setBackground(Color.WHITE);
    this.setOpaque(true);
    nameLabel = new JLabel(name, SwingConstants.CENTER);
    valueLabel = new JLabel(initialValue);
    nameLabel.setBackground(Color.YELLOW);
    valueLabel.setBackground(Color.PINK);
    // nameLabel.setOpaque(true);
    // valueLabel.setOpaque(true);
    this.add(nameLabel);
    this.add(valueLabel);
  }

  public StatView(String name, Object initialValue) {
    this(name, initialValue.toString());
  }

  public void setValue(String value) {
    valueLabel.setText(value);
  }
}
