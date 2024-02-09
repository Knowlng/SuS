package app.components.model;

import java.util.HashSet;
import java.util.Set;

public class XMLFileInfo {
  private Set<PermissionItem> permissionItems = new HashSet<>();
  private boolean isAppDebuggable, isBackupAllowed, usesGrantUriProviders;
  private Set<ExpComponent> expComponents = new HashSet<>();

  public static final String DEBUGGABLE_PNAME = "Enabled android:debuggable property";
  public static final String EXPORTED_PNAME = "Use of exported components";
  public static final String BACKUP_PNAME = "Enabled android:allowBackup property";
  public static final String URIPROVIDER_PNAME = "Insecure Content Providers";

  public Set<PermissionItem> getPermissionItems() {
    return permissionItems;
  }

  public void setPermissionItems(Set<PermissionItem> permissionItems) {
    this.permissionItems = permissionItems;
  }

  public boolean usesGrantUriProviders() {
    return usesGrantUriProviders;
  }

  public void setUsesGrantUriProviders(boolean usesGrantUriProviders) {
    this.usesGrantUriProviders = usesGrantUriProviders;
  }

  public boolean isAppDebuggable() {
    return isAppDebuggable;
  }

  public void setAppDebuggable(boolean appDebuggable) {
    isAppDebuggable = appDebuggable;
  }

  public boolean isBackupAllowed() {
    return isBackupAllowed;
  }

  public void setBackupAllowedValue(boolean appBackupValue) {
    isBackupAllowed = appBackupValue;
  }

  public Set<ExpComponent> getExpComponents() {
    return expComponents;
  }

  public void setExpComponents(Set<ExpComponent> expComponents) {
    this.expComponents = expComponents;
  }
}
