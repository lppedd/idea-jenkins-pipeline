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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Edoardo Luppi
 */
public class JPPipelinePsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  public JPPipelinePsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return "Pipeline";
  }

  @Override
  public @NotNull Icon getIcon(final boolean open) {
    return JPIcons.PIPELINE;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    final var pipelineClosure = PsiTreeUtil.getChildOfType(getElement(), GrClosableBlock.class);

    if (pipelineClosure == null) {
      return Collections.emptyList();
    }

    final var treeElements = new ArrayList<StructureViewTreeElement>(16);
    treeElements.addAll(getMethodCalls(pipelineClosure, "agent", JPAgentPsiTreeElement::new));
    treeElements.addAll(getMethodCalls(pipelineClosure, "parameters", JPParametersPsiTreeElement::new));
    treeElements.addAll(getMethodCalls(pipelineClosure, "environment", JPEnvironmentPsiTreeElement::new));
    treeElements.addAll(getMethodCalls(pipelineClosure, "stages", JPStagesPsiTreeElement::new));
    return treeElements;
  }

  private static @NotNull List<StructureViewTreeElement> getMethodCalls(
      final @NotNull GrClosableBlock pipelineClosure,
      final @NotNull String sectionName,
      final @NotNull Function<GrMethodCall, StructureViewTreeElement> producer) {
    final var methodCalls = JPPsiUtils.getChildrenOfType(
        pipelineClosure,
        GrMethodCall.class,
        mc -> sectionName.equals(JPGdslUtils.getInvokedMethodName(mc))
    );

    return methodCalls.stream()
        .map(producer)
        .collect(Collectors.toList());
  }
}
