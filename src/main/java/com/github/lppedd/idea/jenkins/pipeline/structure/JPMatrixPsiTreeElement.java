package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Edoardo Luppi
 */
public class JPMatrixPsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  public JPMatrixPsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return "Matrix";
  }

  @Override
  public @NotNull Icon getIcon(final boolean open) {
    return JPIcons.MATRIX;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    final var stagesClosure = PsiTreeUtil.getChildOfType(getElement(), GrClosableBlock.class);

    if (stagesClosure == null) {
      return Collections.emptyList();
    }

    final var stagesMethodCalls = JPPsiUtils.getChildrenOfType(
        stagesClosure,
        GrMethodCall.class,
        mc -> "stages".equals(JPGdslUtils.getInvokedMethodName(mc))
    );

    return stagesMethodCalls.stream()
        .map(JPStagesPsiTreeElement::new)
        .collect(Collectors.toList());
  }
}
