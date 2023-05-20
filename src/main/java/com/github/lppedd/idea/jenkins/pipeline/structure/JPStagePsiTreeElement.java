package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public class JPStagePsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  public JPStagePsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return Objects.requireNonNullElse(JPUtils.getStageName(getValue()), "(unnamed)");
  }

  @Override
  public @NotNull Icon getIcon(final boolean open) {
    return JPIcons.STAGE;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    final var stageClosure = PsiTreeUtil.getChildOfType(getElement(), GrClosableBlock.class);

    if (stageClosure == null) {
      return Collections.emptyList();
    }

    final var stagesMethodCalls = JPPsiUtils.getChildrenOfType(stageClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "stages".equals(invokedMethodName) || "parallel".equals(invokedMethodName);
    });


    final var stagesElements = stagesMethodCalls.stream()
        .map(JPStagesPsiTreeElement::new)
        .toList();

    final var matrixMethodCalls = JPPsiUtils.getChildrenOfType(stageClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "matrix".equals(invokedMethodName);
    });

    final var matrixElements = matrixMethodCalls.stream()
        .map(JPMatrixPsiTreeElement::new)
        .toList();

    final var elements = new ArrayList<StructureViewTreeElement>(16);
    elements.addAll(stagesElements);
    elements.addAll(matrixElements);
    return elements;
  }
}
