package com.github.lppedd.idea.jenkins.pipeline;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslService;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslService.Descriptor;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPGroovyUtils;
import com.intellij.codeInsight.documentation.DocumentationManagerUtil;
import com.intellij.codeInsight.javadoc.JavaDocUtil;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.dsl.holders.NonCodeMembersHolder;
import org.jetbrains.plugins.groovy.lang.documentation.GroovyDocumentationProvider;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

/**
 * @author Edoardo Luppi
 */
public class JPGroovyDocumentationProvider extends GroovyDocumentationProvider {
  @Override
  public @Nullable String generateDoc(
      final @Nullable PsiElement element,
      final @Nullable PsiElement originalElement) {
    if (element == null) {
      return null;
    }

    var mutableElement = element;

    if (mutableElement instanceof final GrReferenceExpression refExpr) {
      final var resolveResult = JPGroovyUtils.extractUniqueResult(refExpr.multiResolve(true));
      final var resolveElement = resolveResult.getElement();

      if (resolveElement != null) {
        mutableElement = resolveElement;
      }
    }

    final var gdslDoc = mutableElement.getUserData(NonCodeMembersHolder.DOCUMENTATION);

    if (gdslDoc == null) {
      return null;
    }

    final var ids = JPGdslUtils.getRootAndDescriptorId(gdslDoc);

    if (ids == null) {
      return null;
    }

    final var gdslService = mutableElement.getProject().getService(JPGdslService.class);
    final var descriptor = gdslService.getDescriptor(ids.first, ids.second);

    if (descriptor == null) {
      return null;
    }

    final var bundleName = descriptor.name;

    if (bundleName != null) {
      return generateGdslDoc(mutableElement, descriptor);
    }

    mutableElement.putUserData(NonCodeMembersHolder.DOCUMENTATION, null);
    final var superDoc = super.generateDoc(mutableElement, originalElement);
    mutableElement.putUserData(NonCodeMembersHolder.DOCUMENTATION, gdslDoc);

    if (superDoc != null) {
      final var internalDoc = descriptor.doc != null
          ? descriptor.doc
          : "No documentation provided for GDSL ID: <code>" + descriptor.id + "</code>";

      return superDoc +
             DocumentationMarkup.CONTENT_START +
             internalDoc +
             DocumentationMarkup.CONTENT_END;
    }

    return null;
  }

  private @Nullable String generateGdslDoc(final @NotNull PsiElement element, final @NotNull Descriptor descriptor) {
    final var gdslMethod = JPGdslUtils.getGdslGrMethodOrNull(element);

    if (gdslMethod != null) {
      final var sb = new StringBuilder(256);
      final var returnType = gdslMethod.getReturnType();
      sb.append(DocumentationMarkup.DEFINITION_START);

      if (returnType != null) {
        final var project = element.getProject();
        final var type = PsiType.getTypeByName(returnType.getCanonicalText(), project, gdslMethod.getResolveScope());
        final var resolvedPsiElement = type.resolve();
        final var refText = JavaDocUtil.getReferenceText(project, resolvedPsiElement);
        final var presentableText = returnType.getPresentableText();
        DocumentationManagerUtil.createHyperlink(sb, resolvedPsiElement, refText, presentableText, false, true);
        sb.append(' ');
      }

      sb.append(descriptor.name);
      sb.append(DocumentationMarkup.DEFINITION_END);

      if (descriptor.doc != null) {
        sb.append(DocumentationMarkup.CONTENT_START);
        sb.append(descriptor.doc);
        sb.append(DocumentationMarkup.CONTENT_END);
      }

      return sb.toString();
    }

    return null;
  }
}
