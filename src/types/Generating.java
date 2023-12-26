package types;

public interface Generating {
  public enum VariableType {
    integer,
    bool,
    string
  }

  public record Variable(
    VariableType type,
    int stackLocation
  ) {
    public Variable with(VariableType type, int stackLocation) { return new Variable(type, stackLocation); }

    public Variable withType(VariableType type) { return new Variable(type, stackLocation()); }
    public Variable withStackLocation(int stackLocation) { return new Variable(type(), stackLocation); }
  }
}