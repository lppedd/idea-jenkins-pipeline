package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class JPLibraryType extends LibraryType<DummyLibraryProperties> {
  public static final PersistentLibraryKind<DummyLibraryProperties> KIND = new JPPersistentLibraryKind();

  JPLibraryType() {
    super(KIND);
  }

  @Override
  public @NotNull String getCreateActionName() {
    return "Jenkins Pipeline";
  }

  @Override
  public @NotNull Icon getIcon(final @Nullable DummyLibraryProperties properties) {
    return JPIcons.JENKINS_NOMARGINS;
  }

  @Override
  public @NotNull NewLibraryConfiguration createNewLibrary(
      final @NotNull JComponent parentComponent,
      final @Nullable VirtualFile contextDirectory,
      final @NotNull Project project) {
    return new JenkinsPipelineLibraryConfiguration(getCreateActionName(), this, DummyLibraryProperties.INSTANCE);
  }

  @Override
  public @Nullable LibraryPropertiesEditor createPropertiesEditor(
      final @NotNull LibraryEditorComponent<DummyLibraryProperties> editorComponent) {
    return null;
  }

  private static class JenkinsPipelineLibraryConfiguration extends NewLibraryConfiguration {
    JenkinsPipelineLibraryConfiguration(
        final @NotNull String defaultLibraryName,
        final @Nullable LibraryType<DummyLibraryProperties> libraryType,
        final @Nullable DummyLibraryProperties properties) {
      super(defaultLibraryName, libraryType, properties);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void addRoots(final @NotNull LibraryEditor editor) {
      final var jarPath = PathManager.getJarPathForClass(getClass());
      final var jarVirtualFile = JarFileSystem.getInstance().findLocalVirtualFileByPath(jarPath);
      final var classesRoot = jarVirtualFile.findFileByRelativePath("groovy/classes");
      editor.addRoot(classesRoot, OrderRootType.CLASSES);
    }
  }

  private static class JPPersistentLibraryKind extends PersistentLibraryKind<DummyLibraryProperties> {
    JPPersistentLibraryKind() {
      super("com.github.lppedd.library.JenkinsPipeline");
    }

    @Override
    public @NotNull DummyLibraryProperties createDefaultProperties() {
      return DummyLibraryProperties.INSTANCE;
    }
  }
}
