package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.intellij.ide.structureView.StructureViewExtension;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.Collection;
import java.util.List;

/**
 * @author Edoardo Luppi
 */
public class JPStructureViewExtension implements StructureViewExtension {
  @Override
  public @NotNull Class<? extends PsiElement> getType() {
    return GroovyFile.class;
  }

  @Override
  public @NotNull StructureViewTreeElement[] getChildren(final @NotNull PsiElement parent) {
    final var pipelineMethodCalls = JPPsiUtils.getChildrenOfType(
        parent,
        GrMethodCall.class,
        mc -> "pipeline".equals(JPGdslUtils.getInvokedMethodName(mc))
    );

    return pipelineMethodCalls.stream()
        .map(JPPipelinePsiTreeElement::new)
        .toArray(StructureViewTreeElement[]::new);
  }

  @Override
  public @Nullable Object getCurrentEditorElement(final Editor editor, final PsiElement parent) {
    return null;
  }

  @Override
  public void filterChildren(
      final @NotNull Collection<StructureViewTreeElement> baseChildren,
      final @NotNull List<StructureViewTreeElement> extensionChildren) {
    if (!extensionChildren.isEmpty()) {
      baseChildren.removeIf(c -> !extensionChildren.contains(c) && c.getChildren().length == 0);
    }
  }
}
