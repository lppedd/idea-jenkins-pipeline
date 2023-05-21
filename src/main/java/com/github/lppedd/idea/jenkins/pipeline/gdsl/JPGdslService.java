package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.stream.Collectors;

/**
 * @author Edoardo Luppi
 */
@Service(Service.Level.PROJECT)
public final class JPGdslService {
  private static final Logger logger = Logger.getInstance(JPGdslService.class);
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

              if (id.isBlank()) {
                logger.error("Empty internal definition ID");
                continue;
              }

              if (descriptors.containsKey(id)) {
                logger.error("Duplicate internal definition ID '%s'".formatted(id));
              }

              final var name = getFirstChildElementTextContent(definitionElement, "name");
              final var doc = getFirstChildElementTextContent(definitionElement, "doc");
              final var getter = getFirstChildElementTextContent(definitionElement, "hasGetter");
              descriptors.put(id, new Descriptor(id, name, doc, !"false".equals(getter)));
            }
          }
        }
      }
    } catch (final ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    }

    return descriptors;
  }

  private @Nullable String getFirstChildElementTextContent(
      final @NotNull Element element,
      final @NotNull String name) {
    final var child = getFirstChildElement(element, name);
    return child instanceof final Element e
        ? getElementTextContent(e)
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

  private @NotNull String getElementTextContent(final @NotNull Element element) {
    final var textContent = element.getTextContent().trim();
    final var marginPrefix = element.getAttribute("marginPrefix").trim();
    final var marginPrefixChar = marginPrefix.isEmpty()
        ? '|'
        : marginPrefix.charAt(0);

    return textContent.lines()
        .map(line -> trimLineWithMarginPrefix(line, marginPrefixChar))
        .collect(Collectors.joining("\n"));
  }

  private @NotNull String trimLineWithMarginPrefix(
      final @NotNull String line,
      final char marginPrefix) {
    for (var i = 0; i < line.length(); i++) {
      final var ch = line.charAt(i);

      if (!Character.isWhitespace(ch)) {
        if (ch == marginPrefix) {
          return line.substring(i + 1);
        }

        break;
      }
    }

    return line.trim();
  }

  public record Descriptor(
      String id,
      String name,
      String doc,
      boolean getter
  ) { }
}
