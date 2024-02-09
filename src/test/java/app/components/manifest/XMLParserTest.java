package app.components.manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.components.model.ExpComponent;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import app.components.parsing.xmlparsing.XMLParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XMLParserTest {

  private XMLFileInfo xmlFileInfo;
  private XMLParser xmlParser;
  private List<String> xmlStringList;

  @BeforeEach
  void setUp() throws IOException {
    xmlFileInfo = new XMLFileInfo();
    xmlParser = new XMLParser(xmlFileInfo);
    xmlStringList = new ArrayList<>();

    InputStream firstXMLFileInputStream =
        getClass().getResourceAsStream("/xml/AndroidManifest.xml");
    String firstXMLFileContent = new String(firstXMLFileInputStream.readAllBytes());
    xmlStringList.add(firstXMLFileContent);

    InputStream secondXMLFileInputStream =
        getClass().getResourceAsStream("/xml/AndroidManifest2.xml");
    String secondXMLFileContent = new String(secondXMLFileInputStream.readAllBytes());
    xmlStringList.add(secondXMLFileContent);

    xmlParser.initializeXMLParsing(xmlStringList);
  }

  @Test
  void testParseXmlPermissionsToList() throws IOException {

    Set<PermissionItem> expectedPermissions = new HashSet<>();
    /* Prefill expected ouput from first xml file */
    expectedPermissions.add(new PermissionItem("INTERNET"));
    expectedPermissions.add(new PermissionItem("CAMERA"));
    expectedPermissions.add(new PermissionItem("VIBRATE"));
    expectedPermissions.add(new PermissionItem("ACCESS_NETWORK_STATE"));
    expectedPermissions.add(new PermissionItem("WAKE_LOCK"));
    expectedPermissions.add(new PermissionItem("com.google.android.c2dm.permission.RECEIVE"));
    expectedPermissions.add(
        new PermissionItem(
            "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"));
    expectedPermissions.add(new PermissionItem("com.google.android.gms.permission.AD_ID"));
    expectedPermissions.add(new PermissionItem("RECORD_AUDIO"));
    /* Prefill expected ouput from second xml file */
    expectedPermissions.add(new PermissionItem("GET_PACKAGE_SIZE"));
    expectedPermissions.add(
        new PermissionItem("com.google.android.providers.gsf.permission.READ_GSERVICES"));
    expectedPermissions.add(
        new PermissionItem(
            "com.google.android.calculator.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"));

    Set<PermissionItem> permissions = xmlFileInfo.getPermissionItems();

    assertEquals(expectedPermissions, permissions, "Check if permissions retrieved as expected");
  }

  @Test
  void testParseExpComponentsToList() throws IOException {

    Set<ExpComponent> expectedComponent = new HashSet<>();

    /* Prefill expected ouput from first xml file */
    expectedComponent.add(
        new ExpComponent("activity", "tv.zender.zenderexample.ZenderExampleMainActivity"));
    expectedComponent.add(
        new ExpComponent("activity", "com.squareup.leakcanary.internal.DisplayLeakActivity"));
    expectedComponent.add(new ExpComponent("activity", "com.facebook.CustomTabActivity"));
    expectedComponent.add(
        new ExpComponent("receiver", "com.google.firebase.iid.FirebaseInstanceIdReceiver"));
    /* Prefill expected ouput from second xml file */
    expectedComponent.add(new ExpComponent("activity", "com.android.calculator2.Calculator"));
    expectedComponent.add(
        new ExpComponent("service", "com.android.calculator2.CalculatorTileService"));
    expectedComponent.add(new ExpComponent("activity", "com.android.calculator2.Licenses"));
    expectedComponent.add(
        new ExpComponent(
            "receiver",
            "com.google.android.libraries.phenotype.client.stable.AccountRemovedBroadcastReceiver"));
    expectedComponent.add(
        new ExpComponent(
            "receiver",
            "com.google.android.libraries.phenotype.client.stable.PhenotypeUpdateBackgroundBroadcastReceiver"));

    Set<ExpComponent> components = xmlFileInfo.getExpComponents();

    assertEquals(expectedComponent, components, "Check if components retrieved as expected");
  }
}
