package com.github.lppedd.idea.jenkins.pipeline;

import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.ElementBase;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class JPFileIconProvider implements FileIconProvider {
  @Override
  public @Nullable Icon getIcon(final @NotNull VirtualFile file, final int flags, final @Nullable Project project) {
    if (project == null || !JPUtils.isJenkinsfile(file)) {
      return null;
    }

    final var psiFile = PsiManager.getInstance(project).findFile(file);

    if (psiFile == null) {
      return null;
    }

    final var iconManager = IconManager.getInstance();
    final var iconFlags = ElementBase.transformFlags(psiFile, flags);
    return iconManager.createLayeredIcon(psiFile, JPIcons.JENKINS, iconFlags);
  }
}
