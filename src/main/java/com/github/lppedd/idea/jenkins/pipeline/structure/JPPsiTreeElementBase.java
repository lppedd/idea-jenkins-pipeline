package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.structureView.impl.java.AccessLevelProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Edoardo Luppi
 */
public abstract class JPPsiTreeElementBase<T extends PsiElement> extends PsiTreeElementBase<T> implements AccessLevelProvider {
  public JPPsiTreeElementBase(final @NotNull T psiElement) {
    super(psiElement);
  }

  @Override
  public @Nullable Icon getIcon(final boolean open) {
    return null;
  }

  @Override
  public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
    return Collections.emptyList();
  }

  @Override
  public int getAccessLevel() {
    return 0;
  }

  @Override
  public int getSubLevel() {
    return 0;
  }
}
