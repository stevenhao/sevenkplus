package common;

public class PPlayer {
  private final String name;
  private final Integer id;

  public PPlayer(String name, Integer id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }
}
