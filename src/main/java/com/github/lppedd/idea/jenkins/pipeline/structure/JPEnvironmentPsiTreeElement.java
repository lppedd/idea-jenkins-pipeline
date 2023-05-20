package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Edoardo Luppi
 */
public class JPEnvironmentPsiTreeElement extends JPPsiTreeElementBase<GrMethodCall> {
  public JPEnvironmentPsiTreeElement(final @NotNull GrMethodCall methodCall) {
    super(methodCall);
  }

  @Override
  public @NotNull String getPresentableText() {
    return "Environment";
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    return Collections.emptyList();
  }
}
