package com.github.lppedd.idea.jenkins.pipeline;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPGroovyUtils;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyLanguage;
import org.jetbrains.plugins.groovy.dsl.GdslNamedParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrString;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;

/**
 * Filter out error highlighting when passing a {@code GString} argument
 * to a {@code String} named parameter.
 *
 * @author Edoardo Luppi
 */
public class JPGStringHighlightInfoFilter implements HighlightInfoFilter {
  @Override
  public boolean accept(final @NotNull HighlightInfo highlightInfo, final @Nullable PsiFile file) {
    if (!isGroovyAssignabilityCheck(highlightInfo, file)) {
      return true;
    }

    final var element = findRootHighlightElement(file, highlightInfo.getStartOffset());

    if (element == null) {
      return true;
    }

    if (element.getParent() instanceof final GrNamedArgument namedArgument &&
        namedArgument.getParent() instanceof final GrArgumentList namedArgumentParent &&
        namedArgumentParent.getParent() instanceof final GrMethodCallExpression methodCallExpr) {
      final var gdslMethod = JPGdslUtils.getGdslGrMethodOrNull(methodCallExpr.resolveMethod());

      if (gdslMethod == null) {
        return true;
      }

      final var namedParameters = gdslMethod.getNamedParameters();
      final var descriptor = namedParameters.get(namedArgument.getLabelName());

      // We want to consider only named parameters that accept the String type
      if (descriptor == null ||
          !(descriptor.getNavigationElement() instanceof final GdslNamedParameter namedParameter) ||
          !JPConstants.Classes.STRING.equals(namedParameter.myParameterTypeText)) {
        return true;
      }

      if (element instanceof GrString) {
        // The highlighted element is a GString literal, so it is a valid argument
        return false;
      }

      if (element instanceof GrMethodCallExpression) {
        // The highlighted element is a method call.
        // We find out its return type, and if it is GString then it is a valid argument
        final var type = ((GrMethodCallExpression) element).getNominalType();
        return type == null || !JPConstants.Classes.GSTRING.equals(type.getCanonicalText());
      }

      if (element instanceof GrReferenceExpression) {
        // The highlighted element is a reference to some other element, like a variable.
        // We find out the reference type, e.g., a variable's type, and if it is GString
        // then it is a valid argument
        final var resolveResults = ((GrReferenceExpression) element).resolve(true);
        final var resolveResult = JPGroovyUtils.extractUniqueResult(resolveResults);
        final var resolvedElement = resolveResult.getElement();

        if (resolvedElement instanceof GrVariable) {
          final var type = ((GrVariable) resolvedElement).getTypeGroovy();
          return type == null || !JPConstants.Classes.GSTRING.equals(type.getCanonicalText());
        }

        return true;
      }
    }

    return true;
  }

  private boolean isGroovyAssignabilityCheck(
      final @NotNull HighlightInfo highlightInfo,
      final @Nullable PsiFile file) {
    return file != null &&
           file.isValid() &&
           file.getLanguage() instanceof GroovyLanguage &&
           "GroovyAssignabilityCheck".equals(highlightInfo.getInspectionToolId()) &&
           HighlightSeverity.WARNING.equals(highlightInfo.getSeverity());
  }

  private @Nullable PsiElement findRootHighlightElement(
      final @NotNull PsiFile file,
      final int offsetInFile) {
    final var element = file.findElementAt(offsetInFile);

    if (element == null) {
      return null;
    }

    final var parent = element.getParent();
    return parent.getParent() instanceof final GrMethodCallExpression methodCallExpr
        ? methodCallExpr
        : parent;
  }
}
