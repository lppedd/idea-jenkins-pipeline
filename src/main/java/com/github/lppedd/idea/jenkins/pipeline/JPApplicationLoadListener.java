package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.ide.ApplicationLoadListener;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.PluginStateManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Edoardo Luppi
 */
@SuppressWarnings("UnstableApiUsage")
public class JPApplicationLoadListener implements ApplicationLoadListener {
  private static final Logger logger = Logger.getInstance(JPApplicationLoadListener.class);

  @SuppressWarnings("DataFlowIssue")
  @Override
  public void beforeApplicationLoaded(final @NotNull Application application, final @NotNull Path configPath) {
    final var pluginId = PluginId.getId(JPConstants.PLUGIN_ID);
    final var destPath = PluginManagerCore.getPlugin(pluginId).getPluginPath().resolve("lib");
    final var pluginJarPath = Path.of(PathManager.getJarPathForClass(getClass()));

    PluginStateManager.addStateListener(new JPPluginStateListener(destPath));

    try (final var fileSystem = FileSystems.newFileSystem(pluginJarPath)) {
      try (final var sources = Files.list(fileSystem.getPath("standardDsls"))) {
        sources.forEach(source -> {
          final var destination = destPath.resolve(source.toString());

          try {
            Files.deleteIfExists(destination);
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    } catch (final IOException e) {
      logger.error("Error while extracting bundled GDSL files", e);
    }
  }
}
