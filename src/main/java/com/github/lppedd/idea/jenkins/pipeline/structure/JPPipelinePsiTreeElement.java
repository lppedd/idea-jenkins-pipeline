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
    treeElements.addAll(getAgent(pipelineClosure));
    treeElements.addAll(getParameters(pipelineClosure));
    treeElements.addAll(getEnvironment(pipelineClosure));
    treeElements.addAll(getStages(pipelineClosure));
    return treeElements;
  }

  private static @NotNull List<StructureViewTreeElement> getAgent(final @NotNull GrClosableBlock pipelineClosure) {
    return getMethodCalls(pipelineClosure, "agent", JPAgentPsiTreeElement::new);
  }

  private static @NotNull List<StructureViewTreeElement> getParameters(final @NotNull GrClosableBlock pipelineClosure) {
    return getMethodCalls(pipelineClosure, "parameters", JPParametersPsiTreeElement::new);
  }

  private static @NotNull List<StructureViewTreeElement> getEnvironment(final @NotNull GrClosableBlock pipelineClosure) {
    return getMethodCalls(pipelineClosure, "environment", JPEnvironmentPsiTreeElement::new);
  }

  private static @NotNull List<StructureViewTreeElement> getStages(final @NotNull GrClosableBlock pipelineClosure) {
    return getMethodCalls(pipelineClosure, "stages", JPStagesPsiTreeElement::new);
  }

  private static @NotNull List<StructureViewTreeElement> getMethodCalls(
      final @NotNull GrClosableBlock pipelineClosure,
      final @NotNull String parameters,
      final @NotNull Function<GrMethodCall, StructureViewTreeElement> ctor) {
    final var methodCalls = JPPsiUtils.getChildrenOfType(
        pipelineClosure,
        GrMethodCall.class,
        mc -> parameters.equals(JPGdslUtils.getInvokedMethodName(mc))
    );

    return methodCalls.stream()
        .map(ctor)
        .collect(Collectors.toList());
  }
}
