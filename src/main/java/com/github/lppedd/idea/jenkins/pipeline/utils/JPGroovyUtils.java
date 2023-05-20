package com.github.lppedd.idea.jenkins.pipeline.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.completion.GrPropertyForCompletion;
import org.jetbrains.plugins.groovy.lang.psi.api.EmptyGroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

import java.util.Collection;

/**
 * @author Edoardo Luppi
 */
@SuppressWarnings("unused")
public class JPGroovyUtils {
  public static @NotNull GroovyResolveResult extractUniqueResult(
      final @NotNull GroovyResolveResult[] results) {
    for (final var result : results) {
      if (result.getElement() != null) {
        return result;
      }
    }

    return EmptyGroovyResolveResult.INSTANCE;
  }

  public static @NotNull GroovyResolveResult extractUniqueResult(
      final @NotNull Collection<? extends GroovyResolveResult> results) {
    for (final var result : results) {
      if (result.getElement() != null) {
        return result;
      }
    }

    return EmptyGroovyResolveResult.INSTANCE;
  }

  @Contract("null -> false")
  public static boolean isGdslGrProperty(final @Nullable PsiElement element) {
    if (element instanceof final GrPropertyForCompletion propertyForCompletion) {
      return isGdslGrMethod(propertyForCompletion.getOriginalAccessor());
    }

    return false;
  }

  @Contract("null -> false")
  public static boolean isGdslGrMethod(final @Nullable PsiElement element) {
    return element instanceof LightElement && element instanceof GrMethod;
  }

  public static @Nullable String getInvokedMethodNameFast(final @NotNull GrMethodCall methodCall) {
    final var expr = methodCall.getInvokedExpression();
    return expr instanceof final GrReferenceExpression refExpr
        ? refExpr.getReferenceName()
        : null;
  }
}
