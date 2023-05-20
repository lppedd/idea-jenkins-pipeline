package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ResolveScopeEnlarger;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * @author Edoardo Luppi
 */
public class JPResolveScopeEnlarger extends ResolveScopeEnlarger {
  @Override
  public @Nullable SearchScope getAdditionalResolveScope(
      final @NotNull VirtualFile file,
      final @NotNull Project project) {
    if (!project.isDefault() && FileTypeRegistry.getInstance().isFileOfType(file, GroovyFileType.GROOVY_FILE_TYPE)) {
      final var module = ModuleUtil.findModuleForFile(file, project);
      return module == null || !isJenkinsPipelineLibraryRegistered(module)
          ? new ProjectAndLibrariesScope(project)
          : null;
    }

    return null;
  }

  private boolean isJenkinsPipelineLibraryRegistered(final @NotNull Module module) {
    final var model = ModuleRootManager.getInstance(module);

    for (final var orderEntry : model.getOrderEntries()) {
      if (orderEntry instanceof final LibraryOrderEntry entry &&
          entry.getLibrary() instanceof final LibraryEx libraryEx &&
          JPLibraryType.KIND.equals(libraryEx.getKind())) {
        return true;
      }
    }

    return false;
  }
}
