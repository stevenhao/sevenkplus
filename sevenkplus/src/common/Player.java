package common;

public class Player {
  private final String name;
  private final Integer id;

  public Player(String name, Integer id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }
}
