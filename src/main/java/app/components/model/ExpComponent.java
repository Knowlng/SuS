package app.components.model;

import java.util.Objects;

/**
 * Represents an exported component within the AndroidManifest.xml file, holds information about
 * component name & 'android:name' value
 */
public class ExpComponent {

  /** The type of the component (e.g., "activity", "service", "receiver", "provider"). */
  private final String componentName;

  /** The 'android:name' attribute value of the component in the AndroidManifest.xml. */
  private final String androidName;

  public ExpComponent(String componentName, String androidName) {
    this.componentName = componentName;
    this.androidName = androidName;
  }

  public String getComponentName() {
    return componentName;
  }

  public String getAndroidName() {
    return androidName;
  }

  /**
   * Compares this {@code ExpComponent} to the specified object for equality. The equality
   * comparison is based on the values of the 'componentName' and 'androidName' fields. Two {@code
   * ExpComponent} objects are considered equal if both their 'componentName' and 'androidName'
   * fields match.
   *
   * @param obj The object to compare this {@code ExpComponent} against.
   * @return {@code true} if the given object represents an {@code ExpComponent} equivalent to this
   *     instance, {@code false} otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ExpComponent that = (ExpComponent) obj;
    return Objects.equals(componentName, that.componentName)
        && Objects.equals(androidName, that.androidName);
  }

  /**
   * This method is overridden to ensure the consistency with the {@code equals} method. This is to
   * ensure that equal objects must have an equal hash code, which guarantees correct behavior when
   * objects of this class are stored in collections that use hash codes, such as a {@link HashSet}.
   *
   * @return A hash code value for this object.
   */
  @Override
  public int hashCode() {
    return Objects.hash(componentName, androidName);
  }

  @Override
  public String toString() {
    return "Component: " + componentName + ", Android Name: " + androidName;
  }
}
