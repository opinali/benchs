/**
 * Used only to override the polymorphic method.
 */
public class HelperDerivedA extends Helper {
  /**
   * Makes Helper.polyHardF() even more polymorphic.
   */
  public void polyHardF(int value) {
    x &= value;
  }
}
