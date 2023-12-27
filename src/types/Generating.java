package types;

public interface Generating {
  public enum VariableType {
    integer,
    bool,
    string
  }

  public record Variable(
    VariableType type,
    int stackLocation,
    int length
  ) {
    public Variable with(VariableType type, int stackLocation) { return new Variable(type, stackLocation, length); }

    public Variable withType(VariableType type) { return new Variable(type, stackLocation(), length()); }
    public Variable withStackLocation(int stackLocation) { return new Variable(type(), stackLocation, length()); }
    public Variable withLength(int length) { return new Variable(type(), stackLocation(), length); }
  }
}