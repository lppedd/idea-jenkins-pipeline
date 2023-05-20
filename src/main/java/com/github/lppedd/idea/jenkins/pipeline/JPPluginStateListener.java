package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Edoardo Luppi
 */
public class JPPluginStateListener implements PluginStateListener {
  private static final Logger logger = Logger.getInstance(JPPluginStateListener.class);
  private final Path gdslDestPath;

  JPPluginStateListener(final @NotNull Path gdslDestPath) {
    this.gdslDestPath = gdslDestPath;
  }

  @Override
  public void install(final @NotNull IdeaPluginDescriptor descriptor) {
    if (JPConstants.PLUGIN_ID.equals(descriptor.getPluginId().getIdString())) {
      logger.info("Plugin installed");
    }
  }

  @Override
  public void uninstall(final @NotNull IdeaPluginDescriptor descriptor) {
    if (JPConstants.PLUGIN_ID.equals(descriptor.getPluginId().getIdString())) {
      try (final var fileSystem = FileSystems.newFileSystem(gdslDestPath)) {
        final var matcher = fileSystem.getPathMatcher("glob:*.gdsl");

        try (final var files = Files.list(gdslDestPath)) {
          files.filter(matcher::matches).forEach(file -> {
            try {
              Files.delete(file);
            } catch (final IOException e) {
              logger.error("Error deleting GDSL file while uninstalling: " + file, e);
            }
          });
        }
      } catch (final IOException e) {
        logger.error("Error deleting GDSL files while uninstalling", e);
      }
    }
  }
}
