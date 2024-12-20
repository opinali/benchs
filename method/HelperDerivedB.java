/**
 * Used only to override the polymorphic method.
 */
public class HelperDerivedB extends HelperDerived {
  /**
   * Makes Helper.polyHardF() even more polymorphic.
   */
  public void polyHardF(int value) {
    x |= value;
  }
}
