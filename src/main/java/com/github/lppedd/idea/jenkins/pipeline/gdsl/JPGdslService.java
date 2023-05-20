package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edoardo Luppi
 */
@Service(Service.Level.PROJECT)
public final class JPGdslService {
  private final Map<String, Map<String, Descriptor>> descriptorsRoots = new ConcurrentHashMap<>(16);

  public @Nullable Descriptor getDescriptor(
      final @NotNull String gdslId,
      final @NotNull String definitionId) {
    if ("jpgdsl".equals(gdslId)) {
      return getInternalDescriptor(definitionId);
    }

    throw new UnsupportedOperationException("Not implemented yet");
  }

  private @Nullable Descriptor getInternalDescriptor(final @NotNull String definitionId) {
    final var descriptors = descriptorsRoots.computeIfAbsent("jpgdsl", k -> createInternalDescriptors());
    return descriptors.get(definitionId);
  }

  private Map<String, Descriptor> createInternalDescriptors() {
    final var descriptors = new HashMap<String, Descriptor>(128);

    try {
      final var inputStream = getClass().getResourceAsStream("/descriptors/jenkinsPipeline.xml");
      final var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final var document = documentBuilder.parse(inputStream);
      final var definitionsList = document.getElementsByTagName("definitions");

      for (var i = 0; i < definitionsList.getLength(); i++) {
        final var definitions = definitionsList.item(i);

        if (definitions instanceof final Element definitionsElement) {
          final var definitionList = definitionsElement.getElementsByTagName("definition");

          for (var x = 0; x < definitionList.getLength(); x++) {
            final var definition = definitionList.item(x);

            if (definition instanceof final Element definitionElement) {
              final var id = definitionElement.getAttribute("id");

              if (!id.isBlank()) {
                final var name = getFirstChildElementValue(definitionElement, "name");
                final var doc = getFirstChildElementValue(definitionElement, "doc");
                final var getter = getFirstChildElementValue(definitionElement, "getter");
                descriptors.put(id, new Descriptor(id, name, doc, !"false".equals(getter)));
              }
            }
          }
        }
      }
    } catch (final ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    }

    return descriptors;
  }

  private @Nullable String getFirstChildElementValue(
      final @NotNull Element element,
      final @NotNull String name) {
    final var child = getFirstChildElement(element, name);
    return child != null
        ? sanitizeTextContent(child.getTextContent())
        : null;
  }

  private @Nullable Node getFirstChildElement(
      final @NotNull Element element,
      final @NotNull String name) {
    final var elementsByTagName = element.getElementsByTagName(name);

    if (elementsByTagName.getLength() > 0) {
      return elementsByTagName.item(0);
    }

    return null;
  }

  private @NotNull String sanitizeTextContent(final @NotNull String text) {
    return text.replaceAll("\n *", " ").trim();
  }

  public static class Descriptor {
    public final String id;
    public final String name;
    public final String doc;
    public final boolean getter;

    private Descriptor(
        final @NotNull String id,
        final @Nullable String name,
        final @Nullable String doc,
        final boolean getter) {
      this.id = id;
      this.name = name;
      this.doc = doc;
      this.getter = getter;
    }
  }
}