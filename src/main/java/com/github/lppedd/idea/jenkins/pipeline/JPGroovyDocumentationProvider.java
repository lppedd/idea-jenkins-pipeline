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

import java.util.Objects;

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
      // This GDSL element is not in our scope.
      // Another documentation provider will take care of it
      return null;
    }

    final var gdslService = mutableElement.getProject().getService(JPGdslService.class);
    final var descriptor = gdslService.getDescriptor(ids.rootId(), ids.descriptorId());

    if (descriptor == null) {
      // This element does not have an XML descriptor associated yet.
      // We will beautify the presentation anyway
      final var standardDoc = getStandardDoc(element, originalElement);

      if (standardDoc != null) {
        final var message = "Missing XML descriptor for: <code>" + gdslDoc + "</code>";
        return standardDoc + DocumentationMarkup.CONTENT_START + message + DocumentationMarkup.CONTENT_END;
      }

      return null;
    }

    // TODO: keep this stuff? Does not seem worth the additional complexity
    if (descriptor.name() != null) {
      return getGdslDoc(mutableElement, descriptor);
    }

    final var standardDoc = getStandardDoc(element, originalElement);
    final var internalDoc = Objects.requireNonNullElse(descriptor.doc(), "");
    final var content = DocumentationMarkup.CONTENT_START + internalDoc + DocumentationMarkup.CONTENT_END;
    return standardDoc != null
        ? standardDoc + content
        : content;
  }

  private @Nullable String getStandardDoc(
      final @NotNull PsiElement element,
      final @Nullable PsiElement originalElement) {
    final var gdslDoc = element.getUserData(NonCodeMembersHolder.DOCUMENTATION);

    element.putUserData(NonCodeMembersHolder.DOCUMENTATION, null);
    final var doc = super.generateDoc(element, originalElement);
    element.putUserData(NonCodeMembersHolder.DOCUMENTATION, gdslDoc);

    return doc;
  }

  // Must be used only if descriptor#name is valid
  private @Nullable String getGdslDoc(
      final @NotNull PsiElement element,
      final @NotNull Descriptor descriptor) {
    final var gdslMethod = JPGdslUtils.getGdslGrMethodOrNull(element);

    if (gdslMethod == null) {
      return null;
    }

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

    sb.append(descriptor.name());
    sb.append(DocumentationMarkup.DEFINITION_END);
    sb.append(DocumentationMarkup.CONTENT_START);
    sb.append(Objects.requireNonNullElse(descriptor.doc(), ""));
    sb.append(DocumentationMarkup.CONTENT_END);
    return sb.toString();
  }
}
