package types;

public interface Generating {
  public record Variable(
    String name,
    int stackLocation
  ) {} 
}