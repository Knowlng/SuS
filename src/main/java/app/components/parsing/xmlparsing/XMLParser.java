package app.components.parsing.xmlparsing;

import app.components.model.ExpComponent;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@code XMLParser} class is responsible for specific AndroidManifest.xml attribute extraction
 * such as permissions, exported components, and debuggable status.
 */
public class XMLParser {

  private Document doc;
  private static final String CHECK_DEBUGGABLE = "android:debuggable";
  private static final String CHECK_BACKUP = "android:allowBackup";
  private static final String CHECK_EXPORTED = "android:exported";
  private static final String CHECK_GRANT_URI = "android:grantUriPermissions";
  private static final String APPLICATION_XML_TAG = "application";
  private static final String PROVIDER_XML_TAG = "provider";
  private XMLFileInfo xmlFileInfo;

  public XMLParser(XMLFileInfo xmlFileInfo) {
    this.xmlFileInfo = xmlFileInfo;
  }

  /**
   * Initializes a {@link Document} that represents the parsed XML content, then calls {@Code
   * processXMLContent} to parse the content and fill in the {@code XMLFileInfo} object for each
   * passed XML string
   *
   * @param xmlFileStrings A list of {@link String} to be parsed.
   * @throws RuntimeException if an error occurs during parsing the XML content.
   */
  public void initializeXMLParsing(List<String> xmlFileStrings) {
    for (String xmlString : xmlFileStrings) {
      try {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
        doc.getDocumentElement().normalize();
        processXMLContent();
      } catch (ParserConfigurationException | SAXException | IOException e) {
        System.err.println("Could not initializie XML parser, skipping...");
      }
    }
  }

  /**
   * Processes the content of the currently converted XML. This method aggregates the extracted
   * permission items, exported components from the XML and adds them to the {@code XMLFileInfo}
   * object, while also setting the app's debuggable, backup, usesGrantUriProviders status to true
   * or false in the {@code XMLFileInfo} object.
   */
  private void processXMLContent() {
    Set<PermissionItem> permissionSet = xmlFileInfo.getPermissionItems();
    permissionSet.addAll(parseXmlPermissionsToList());

    if (!xmlFileInfo.isAppDebuggable()) {
      xmlFileInfo.setAppDebuggable(isAppDebuggable());
    }
    if (!xmlFileInfo.isBackupAllowed()) {
      xmlFileInfo.setBackupAllowedValue(isBackupAllowed());
    }
    if (!xmlFileInfo.usesGrantUriProviders()) {
      xmlFileInfo.setUsesGrantUriProviders(usesGrantUriProviders());
    }

    Set<ExpComponent> expComponentList = xmlFileInfo.getExpComponents();
    expComponentList.addAll(parseExpComponentsToList());
  }

  /**
   * Parses the XML Document to extract a list of permissions used by the application. It
   * specifically looks for the {@code <uses-permission>} tags in the XML and retrieves the {@code
   * android:name} value
   *
   * @return A {@link Set} of {@code PermissionItem} objects representing each permission found in
   *     the XML.
   */
  private Set<PermissionItem> parseXmlPermissionsToList() {
    Set<PermissionItem> permissionsList = new HashSet<>();
    NodeList permissionNodes = doc.getElementsByTagName("uses-permission");

    for (int i = 0; i < permissionNodes.getLength(); i++) {
      Node permissionNode = permissionNodes.item(i);

      if (permissionNode.getNodeType() == Node.ELEMENT_NODE) {
        Element permissionElement = (Element) permissionNode;
        String permissionName = permissionElement.getAttribute("android:name");

        if (permissionName != null && !permissionName.trim().isEmpty()) {
          permissionName = permissionName.replaceFirst("^android.permission.", "");
          permissionsList.add(new PermissionItem(permissionName));
        }
      }
    }

    return permissionsList;
  }

  /**
   * Checks if a given attribute is set to true within the specified tag of the XML.
   *
   * @param attributeName the name of the attribute to check.
   * @param xmlTag the name of the xml tag where attribute resides.
   * @return {@code true} if the attribute is set to true, {@code false} otherwise.
   */
  private boolean isAttributeTrue(String attributeName, String xmlTag) {
    NodeList applications = doc.getElementsByTagName(xmlTag);

    for (int i = 0; i < applications.getLength(); i++) {
      Element element = (Element) applications.item(i);
      String attributeValue = element.getAttribute(attributeName);

      if ("true".equals(attributeValue)) {
        return true;
      }
    }

    return false;
  }

  private boolean usesGrantUriProviders() {
    return isAttributeTrue(PROVIDER_XML_TAG, CHECK_GRANT_URI);
  }

  /**
   * Checks for the {@code android:debuggable} attribute within the {@code <application>} tag of the
   * XML.
   *
   * @return {@code true} if the application is debuggable, {@code false} otherwise.
   */
  private boolean isAppDebuggable() {
    return isAttributeTrue(CHECK_DEBUGGABLE, APPLICATION_XML_TAG);
  }

  /**
   * Checks for the {@code android:allowBackup} attribute within the {@code <application>} tag of
   * the XML.
   *
   * @return {@code true} if the application allows backup, {@code false} otherwise.
   */
  private boolean isBackupAllowed() {
    return isAttributeTrue(CHECK_BACKUP, APPLICATION_XML_TAG);
  }

  /**
   * Parses the XML Document to identify any components that are exported. The method checks for
   * components (activity, service, receiver, provider) and looks if they have the {@code
   * android:exported} attribute set to "true".
   *
   * <p>For every such component found, it retrieves the component's tag name and the {@code
   * android:name} attribute
   *
   * @return A {@link Set} of {@code ExpComponent} objects, each representing a component that is
   *     exported. Each item has component name & 'android:name' value inside
   */
  private Set<ExpComponent> parseExpComponentsToList() {
    Set<ExpComponent> exportedComponents = new HashSet<>();
    String[] components = {"activity", "service", "receiver", "provider"};

    for (String component : components) {
      NodeList componentNodes = doc.getElementsByTagName(component);

      for (int i = 0; i < componentNodes.getLength(); i++) {
        Node node = componentNodes.item(i);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          String exported = element.getAttribute(CHECK_EXPORTED);

          if ("true".equals(exported)) {
            String androidName = element.getAttribute("android:name");
            exportedComponents.add(new ExpComponent(component, androidName));
          }
        }
      }
    }

    return exportedComponents;
  }
}
