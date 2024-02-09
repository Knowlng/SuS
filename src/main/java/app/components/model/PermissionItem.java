package app.components.model;

import app.components.parsing.xmlparsing.RiskAssigner;
import java.util.HashSet;
import java.util.Objects;

/**
 * Holds data about an application permission, including its name, risk level, and description. Used
 * by RiskAssigner for managing permission information.
 *
 * <p>Example: {@code PermissionItem item = new
 * PermissionItem("android.permission.ACCESS_FINE_LOCATION"); item.setPermRiskLevel("High Risk");
 * item.setPermDescription("Allows precise location access."); }
 *
 * @see RiskAssigner
 */
public class PermissionItem {

  private String name;
  private String riskLevel;
  private String description;

  /**
   * Creates a PermissionItem with a name and default 'unassigned' risk level.
   *
   * @param name Permission name, non-null.
   */
  public PermissionItem(String name) {
    this.name = name;
  }

  public PermissionItem(String name, String riskLevel, String description) {
    this.name = name;
    this.riskLevel = riskLevel;
    this.description = description;
  }

  // Standard getters and setters for name, riskLevel, and description

  public void setPermissionName(String name) {
    this.name = name;
  }

  public String getPermissionName() {
    return this.name;
  }

  public void setPermRiskLevel(String riskLevel) {
    this.riskLevel = riskLevel;
  }

  public String getPermRiskLevel() {
    return this.riskLevel;
  }

  public void setPermDescription(String description) {
    this.description = description;
  }

  public String getPermDescription() {
    return this.description;
  }

  /**
   * Compares this {@code PermissionItem} to the specified object for equality. The equality
   * comparison is based on the values of the 'name' field. Two {@code PermissionItem} objects are
   * considered equal if both their 'name' fields match.
   *
   * @param obj The object to compare this {@code PermissionItem} against.
   * @return {@code true} if the given object represents an {@code PermissionItem} equivalent to
   *     this instance, {@code false} otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    PermissionItem that = (PermissionItem) obj;
    return Objects.equals(name, that.name);
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
    return Objects.hash(name);
  }

  /**
   * Returns the PermissionItem's string representation including name, risk level, and description.
   *
   * @return String representation of the PermissionItem.
   */
  @Override
  public String toString() {
    return "\n"
        + "| Name: "
        + name
        + "\n"
        + "| Risk Level: "
        + riskLevel
        + "\n"
        + "| Description: "
        + description
        + "\n";
  }
}
