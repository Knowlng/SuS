package app.XMLAnalysis;

import static org.junit.jupiter.api.Assertions.*;

import app.components.model.ExpComponent;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import app.components.parsing.xmlparsing.RiskAssigner;
import app.components.parsing.xmlparsing.XMLParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XMLFileAnalysisIT {
  private XMLFileInfo xmlFileInfo;
  private XMLParser xmlParser;
  private List<String> xmlStringList;

  @BeforeEach
  void setUp() throws IOException {
    xmlFileInfo = new XMLFileInfo();
    xmlParser = new XMLParser(xmlFileInfo);
    xmlStringList = new ArrayList<>();

    InputStream XMLFileInputStream = getClass().getResourceAsStream("/xml/AndroidManifest.xml");
    String XMLFileContent = new String(XMLFileInputStream.readAllBytes());
    xmlStringList.add(XMLFileContent);

    xmlParser.initializeXMLParsing(xmlStringList);
  }

  @Test
  public void testXMLFileAnalysis() {

    RiskAssigner riskAssigner = new RiskAssigner();

    Set<PermissionItem> permissionList = xmlFileInfo.getPermissionItems();
    assertFalse(permissionList.isEmpty(), "No permmisions were found");

    riskAssigner.assignRiskLevel(permissionList);
    for (PermissionItem item : permissionList) {
      assertNotNull(item.getPermRiskLevel(), "Item should have a risk level set");
      assertNotNull(item.getPermDescription(), "Item should have a description set");
    }

    boolean debuggable = xmlFileInfo.isAppDebuggable();
    assertFalse(debuggable, "Debuggable should be set to false");

    Set<ExpComponent> componentList = xmlFileInfo.getExpComponents();
    assertFalse(componentList.isEmpty(), "No exported components were found");
    for (ExpComponent component : componentList) {
      assertNotNull(component.getComponentName(), "Component should have a name set");
      assertNotNull(
          component.getAndroidName(), "Component should have a 'android:name' attribute value");
    }
  }
}
