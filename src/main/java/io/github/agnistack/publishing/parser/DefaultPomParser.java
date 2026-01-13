/*-
 * #%L
 * custom-central-publishing-maven-plugin
 * %%
 * Copyright (C) 2026 Ashish Thakur
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.agnistack.publishing.parser;

import io.github.agnistack.publishing.model.MavenCoordinates;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Default implementation of PomParser using DOM parsing with XXE protection.
 *
 * @since 0.1.0
 */
public class DefaultPomParser implements PomParser {

  @Override
  public MavenCoordinates parse(File pomFile) throws IOException {
    try {
      DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(pomFile);
      doc.getDocumentElement().normalize();

      String groupId = getElementText(doc, "groupId");
      String artifactId = getElementText(doc, "artifactId");
      String version = getElementText(doc, "version");

      // Check parent for missing coordinates
      String[] parentCoordinates = extractParentCoordinates(doc, groupId, version);
      groupId = parentCoordinates[0];
      version = parentCoordinates[1];

      validateCoordinates(groupId, artifactId, version, pomFile);

      return new MavenCoordinates(groupId, artifactId, version);
    } catch (ParserConfigurationException e) {
      throw new IOException("XML parser configuration error for POM file: " + pomFile, e);
    } catch (SAXException e) {
      throw new IOException("Invalid XML in POM file: " + pomFile, e);
    }
  }

  private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // Security: Disable external entities to prevent XXE attacks
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);

    return factory;
  }

  private String[] extractParentCoordinates(Document doc, String groupId, String version) {
    NodeList parentNodes = doc.getElementsByTagName("parent");
    if (parentNodes.getLength() > 0) {
      Element parent = (Element) parentNodes.item(0);

      if (groupId == null || groupId.isEmpty()) {
        NodeList groupIdNodes = parent.getElementsByTagName("groupId");
        if (groupIdNodes.getLength() > 0) {
          groupId = groupIdNodes.item(0).getTextContent().trim();
        }
      }

      if (version == null || version.isEmpty()) {
        NodeList versionNodes = parent.getElementsByTagName("version");
        if (versionNodes.getLength() > 0) {
          version = versionNodes.item(0).getTextContent().trim();
        }
      }
    }
    return new String[]{groupId, version};
  }

  private void validateCoordinates(String groupId, String artifactId, String version, File pomFile)
      throws IOException {
    if (groupId == null || groupId.isEmpty()) {
      throw new IOException("Missing or empty groupId in POM: " + pomFile);
    }
    if (artifactId == null || artifactId.isEmpty()) {
      throw new IOException("Missing or empty artifactId in POM: " + pomFile);
    }
    if (version == null || version.isEmpty()) {
      throw new IOException("Missing or empty version in POM: " + pomFile);
    }
  }

  private String getElementText(Document doc, String tagName) {
    NodeList nodeList = doc.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent().trim();
    }
    return null;
  }
}
