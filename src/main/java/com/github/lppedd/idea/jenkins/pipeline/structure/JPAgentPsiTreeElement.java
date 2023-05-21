package com.github.lppedd.idea.jenkins.pipeline.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Edoardo Luppi
 */
public class JPAgentPsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  public JPAgentPsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return "Agent";
  }
}
