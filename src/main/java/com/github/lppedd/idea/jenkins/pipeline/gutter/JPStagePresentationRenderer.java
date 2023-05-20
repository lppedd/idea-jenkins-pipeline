package com.github.lppedd.idea.jenkins.pipeline.gutter;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.TextWithIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import javax.swing.*;

/**
 * @author Edoardo Luppi
 */
public class JPStagePresentationRenderer extends PsiElementListCellRenderer<PsiElement> {
  static final PsiElementListCellRenderer<PsiElement> INSTANCE = new JPStagePresentationRenderer();

  @Override
  protected @NotNull Icon getIcon(final @NotNull PsiElement element) {
    return JPIcons.STAGE;
  }

  @Override
  public @NotNull String getElementText(final @NotNull PsiElement element) {
    if (element instanceof final GrMethodCall methodCall) {
      final var stageName = JPUtils.getStageName(methodCall);

      if (stageName != null) {
        return stageName;
      }
    }

    return "(unnamed)";
  }

  @Override
  protected @Nullable TextAttributes getNavigationItemAttributes(final Object value) {
    final var elementText = getElementText((PsiElement) value);
    return "(unnamed)".equals(elementText)
        ? SimpleTextAttributes.GRAYED_ATTRIBUTES.toTextAttributes()
        : super.getNavigationItemAttributes(value);
  }

  @Override
  protected @Nullable String getContainerText(final PsiElement element, final String name) {
    return null;
  }

  @Override
  protected @Nullable TextWithIcon getItemLocation(final Object value) {
    return null;
  }
}
