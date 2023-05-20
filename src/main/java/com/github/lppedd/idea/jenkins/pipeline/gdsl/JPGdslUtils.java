package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import com.github.lppedd.idea.jenkins.pipeline.utils.JPGroovyUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.dsl.holders.NonCodeMembersHolder;
import org.jetbrains.plugins.groovy.lang.completion.GrPropertyForCompletion;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

import java.util.regex.Pattern;

/**
 * @author Edoardo Luppi
 */
public class JPGdslUtils {
  private static final Pattern ROOT_PATTERN = Pattern.compile("jpgdsl@([a-z0-9.]+?):(.+)?", Pattern.CASE_INSENSITIVE);

  @Contract("null -> false")
  public static boolean isGdslGrProperty(final @Nullable PsiElement element) {
    if (element instanceof final GrPropertyForCompletion propertyForCompletion) {
      final var originalAccessor = propertyForCompletion.getOriginalAccessor();
      final var documentation = originalAccessor.getUserData(NonCodeMembersHolder.DOCUMENTATION);
      return getRootAndDescriptorId(documentation) != null;
    }

    return false;
  }

  @Contract("null -> false")
  public static boolean isGdslGrMethod(final @Nullable PsiElement element) {
    if (element instanceof LightElement && element instanceof GrMethod) {
      final var documentation = element.getUserData(NonCodeMembersHolder.DOCUMENTATION);
      return getRootAndDescriptorId(documentation) != null;
    }

    return false;
  }

  @Contract("null -> null")
  public static @Nullable GrMethod isGdslGrMethodOrNull(final @Nullable PsiElement element) {
    return element instanceof LightElement && element instanceof final GrMethod method
        ? method
        : null;
  }

  @SuppressWarnings("unused")
  @Contract("null -> null")
  public static @Nullable Pair<String, String> getRootAndDescriptorId(final @Nullable PsiElement psiElement) {
    return psiElement != null
        ? getRootAndDescriptorId(psiElement.getUserData(NonCodeMembersHolder.DOCUMENTATION))
        : null;
  }

  @Contract("null -> null")
  public static @Nullable Pair<String, String> getRootAndDescriptorId(final @Nullable String gdslDoc) {
    if (gdslDoc != null) {
      final var matcher = ROOT_PATTERN.matcher(gdslDoc);

      if (matcher.matches()) {
        final var rootId = matcher.group(1);
        final var definitionId = matcher.group(2);
        return Pair.create(rootId, definitionId);
      }
    }

    return null;
  }

  public static @Nullable String getInvokedMethodName(final @NotNull GrMethodCall methodCall) {
    final var expr = methodCall.getInvokedExpression();

    if (expr instanceof final GrReferenceExpression refExpr) {
      // The user might have specified a method with the same name as the Jenkins one.
      // In that case we don't want to provide support, so we want "light" elements only
      final var resolveResult = JPGroovyUtils.extractUniqueResult(refExpr.resolve(true));

      if (JPGdslUtils.isGdslGrMethod(resolveResult.getElement())) {
        return refExpr.getReferenceName();
      }
    }

    return null;
  }

  public static @Nullable String getInvokedMethodReturnType(final @NotNull GrMethodCall methodCall) {
    final var expr = methodCall.getInvokedExpression();

    if (expr instanceof final GrReferenceExpression refExpr) {
      // The user might have specified a method with the same name as the Jenkins one.
      // In that case we don't want to provide support, so we want "light" elements only
      final var resolveResult = refExpr.resolve();
      final var method = JPGdslUtils.isGdslGrMethodOrNull(resolveResult);

      if (method != null) {
        final var returnType = method.getReturnType();

        if (returnType != null) {
          return returnType.getCanonicalText();
        }
      }
    }

    return null;
  }
}
