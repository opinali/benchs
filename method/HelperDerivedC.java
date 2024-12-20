/**
 * Used only to override the polymorphic method.
 */
public class HelperDerivedC extends HelperDerivedB {
  /**
   * Makes Helper.polyHardF() even more polymorphic.
   */
  public void polyHardF(int value) {
    x ^= value;
  }
}
