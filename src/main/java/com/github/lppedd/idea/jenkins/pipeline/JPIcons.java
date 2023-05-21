package com.github.lppedd.idea.jenkins.pipeline;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class JPIcons {
  public static final Icon JENKINS = getIcon("/icons/jenkins.svg");
  public static final Icon JENKINS_NOMARGIN = getIcon("/icons/jenkins-nomargin.svg");
  public static final Icon SHARED_LIBRARY = getIcon("/icons/shared-library.svg");
  public static final Icon PIPELINE = getIcon("/icons/pipeline.svg");
  public static final Icon MATRIX = getIcon("/icons/matrix-stages.svg");
  public static final Icon PARALLEL_STAGES = getIcon("/icons/parallel-stages.svg");
  public static final Icon STAGE = getIcon("/icons/stage.svg");

  private static @NotNull Icon getIcon(final @NotNull String iconPath) {
    return IconLoader.getIcon(iconPath, JPIcons.class);
  }
}
