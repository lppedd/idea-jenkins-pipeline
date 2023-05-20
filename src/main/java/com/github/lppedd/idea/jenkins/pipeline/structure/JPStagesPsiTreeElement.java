package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Edoardo Luppi
 */
public class JPStagesPsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  private final String methodName;

  public JPStagesPsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
    methodName = JPGdslUtils.getInvokedMethodName(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return "parallel".equals(methodName)
        ? "Parallel stages"
        : "Stages";
  }

  @Override
  public @Nullable Icon getIcon(final boolean open) {
    return "parallel".equals(methodName)
        ? JPIcons.PARALLEL_STAGES
        : null;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    final var stagesClosure = PsiTreeUtil.getChildOfType(getElement(), GrClosableBlock.class);

    if (stagesClosure == null) {
      return Collections.emptyList();
    }

    final var stageMethodCalls = JPPsiUtils.getChildrenOfType(
        stagesClosure,
        GrMethodCall.class,
        mc -> "stage".equals(JPGdslUtils.getInvokedMethodName(mc))
    );

    return stageMethodCalls.stream()
        .map(JPStagePsiTreeElement::new)
        .collect(Collectors.toList());
  }
}
