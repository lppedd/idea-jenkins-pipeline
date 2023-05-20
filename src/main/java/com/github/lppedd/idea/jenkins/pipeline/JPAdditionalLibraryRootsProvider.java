package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public class JPAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {
  @SuppressWarnings("DataFlowIssue")
  @Override
  public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(final @NotNull Project project) {
    final var jarPath = PathManager.getJarPathForClass(getClass());
    final var jarVirtualFile = JarFileSystem.getInstance().findLocalVirtualFileByPath(jarPath);
    final var classesRoots = List.of(jarVirtualFile.findFileByRelativePath("groovy/classes"));
    return List.of(new JenkinsPipelineSyntheticLibrary(classesRoots));
  }

  private static class JenkinsPipelineSyntheticLibrary extends SyntheticLibrary {
    final Collection<VirtualFile> classesRoot;

    JenkinsPipelineSyntheticLibrary(final @NotNull Collection<VirtualFile> classesRoot) {
      this.classesRoot = classesRoot;
    }

    @Override
    public @NotNull Collection<VirtualFile> getSourceRoots() {
      return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<VirtualFile> getBinaryRoots() {
      return classesRoot;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      if (!(o instanceof final JenkinsPipelineSyntheticLibrary other)) {
        return false;
      }

      return Objects.equals(classesRoot, other.classesRoot);
    }

    @Override
    public int hashCode() {
      return Objects.hash(classesRoot);
    }
  }
}
