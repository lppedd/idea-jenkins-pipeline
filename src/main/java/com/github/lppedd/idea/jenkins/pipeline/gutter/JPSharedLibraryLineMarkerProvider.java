package com.github.lppedd.idea.jenkins.pipeline.gutter;

import com.github.lppedd.idea.jenkins.pipeline.JPConstants;
import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotation;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class JPSharedLibraryLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  public @NotNull String getName() {
    return "Shared library";
  }

  @Override
  public @NotNull Icon getIcon() {
    return JPIcons.SHARED_LIBRARY;
  }

  @Override
  public @Nullable RelatedItemLineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (element.isValid() &&
        element instanceof LeafPsiElement &&
        element.getParent() instanceof final GrCodeReferenceElement codeRefElement &&
        codeRefElement.getParent() instanceof final GrAnnotation annotation &&
        JPConstants.Internal.LIBRARY.equals(annotation.getQualifiedName())) {
      return NavigationGutterIconBuilder.create(JPIcons.SHARED_LIBRARY)
          .setTargets()
          .setTooltipText("Shared library")
          .createLineMarkerInfo(element);
    }

    return null;
  }
}
