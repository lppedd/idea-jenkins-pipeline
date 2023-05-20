package com.github.lppedd.idea.jenkins.pipeline.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * @author Edoardo Luppi
 */
public class JPPsiUtils {
  public static @Nullable <T extends PsiElement> T getFirstChildOfType(
      final @Nullable PsiElement element,
      final @NotNull Class<T> type,
      final @NotNull Predicate<T> predicate) {
    if (element == null) {
      return null;
    }

    final var processor = new PsiElementProcessor.FindElement<>() {
      @Override
      public boolean execute(final @NotNull PsiElement element) {
        if (PsiTreeUtil.instanceOf(element, type) && predicate.test(type.cast(element))) {
          return setFound(element);
        }

        return true;
      }
    };

    PsiTreeUtil.processElements(element, type, processor);
    return type.cast(processor.getFoundElement());
  }

  public static @NotNull <T extends PsiElement> Collection<T> getChildrenOfType(
      final @Nullable PsiElement element,
      final @NotNull Class<T> type,
      final @NotNull Predicate<T> predicate) {
    if (element == null) {
      return Collections.emptyList();
    }

    final var result = new ArrayList<T>(4);

    for (var child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (type.isInstance(child) && predicate.test(type.cast(child))) {
        result.add(type.cast(child));
      }
    }

    return result;
  }
}
