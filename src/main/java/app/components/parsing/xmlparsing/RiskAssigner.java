/**
 * Package handling the risk assignment for application permissions.
 *
 * <p>Utilizes the RiskAssigner class to process permissions based on a database data mapping.
 *
 * <p>Example: {@code RiskAssigner riskAssigner = new RiskAssigner(); Set<PermissionItem>
 * permissions = //... initialization riskAssigner.assignRiskLevel(permissions); }
 *
 * @see RiskAssigner
 */
package app.components.parsing.xmlparsing;

import app.components.model.PermissionItem;
import app.database.databaseFetch.DatabaseFetchAnalysis;
import java.util.HashMap;
import java.util.Set;

/** Handles risk level assignment for permissions, using data from a database. */
public class RiskAssigner {

  private HashMap<String, PermissionItem> permissionsMap;

  /** Initializes RiskAssigner, loading data from the database. */
  public RiskAssigner() {
    permissionsMap = DatabaseFetchAnalysis.fetchPermissions();
  }

  /**
   * Assigns risk levels to provided permissions based on database data. Throws
   * IllegalArgumentException for an empty input set.
   *
   * @param permissionItems A {@link Set} of PermissionItem objects.
   * @throws IllegalArgumentException if given set is empty.
   */
  public void assignRiskLevel(Set<PermissionItem> permissionItems) {
    if (permissionItems.isEmpty()) {
      throw new IllegalArgumentException("Empty permissions list.");
    }

    for (PermissionItem item : permissionItems) {
      String permName = item.getPermissionName();
      if (permissionsMap.containsKey(permName)) {
        PermissionItem info = permissionsMap.get(permName);
        item.setPermRiskLevel(info.getPermRiskLevel());
        item.setPermDescription(info.getPermDescription());
      } else {
        item.setPermRiskLevel("Undefined");
        item.setPermDescription("Undefined");
      }
    }
  }
}
