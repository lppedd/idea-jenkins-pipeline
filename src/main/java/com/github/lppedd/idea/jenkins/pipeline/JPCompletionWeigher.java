package com.github.lppedd.idea.jenkins.pipeline;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.dsl.GdslNamedParameter;
import org.jetbrains.plugins.groovy.extensions.NamedArgumentDescriptor;

/**
 * @author Edoardo Luppi
 */
public class JPCompletionWeigher extends CompletionWeigher {
  // Note: the higher the weight the higher is the element in the list,
  //  so the user can see it immediately
  private static final int WEIGHT_NAMED_ARGUMENT = 200;
  private static final int WEIGHT_PROPERTY = 100;
  private static final int WEIGHT_METHOD = 50;

  @Override
  public Integer weigh(
      final @NotNull LookupElement element,
      final @NotNull CompletionLocation location) {
    if (isNamedArgumentElement(element)) {
      return WEIGHT_NAMED_ARGUMENT;
    }

    final var psiElement = element.getPsiElement();

    if (JPGdslUtils.isGdslGrProperty(psiElement)) {
      return WEIGHT_PROPERTY;
    }

    if (JPGdslUtils.isGdslGrMethod(psiElement)) {
      return WEIGHT_METHOD;
    }

    return 0;
  }

  private boolean isNamedArgumentElement(final @NotNull LookupElement element) {
    final var object = element.getObject();

    if (object instanceof final NamedArgumentDescriptor namedArgumentDescriptor) {
      return namedArgumentDescriptor.getNavigationElement() instanceof GdslNamedParameter;
    }

    return false;
  }
}
