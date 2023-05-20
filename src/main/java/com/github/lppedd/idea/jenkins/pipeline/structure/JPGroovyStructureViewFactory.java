package com.github.lppedd.idea.jenkins.pipeline.structure;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModel.ExpandInfoProvider;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.java.JavaFileTreeModel;
import com.intellij.ide.structureView.impl.java.JavaInheritedMembersNodeProvider;
import com.intellij.ide.util.treeView.smartTree.NodeProvider;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.Collection;
import java.util.List;

/**
 * @author Edoardo Luppi
 */
public class JPGroovyStructureViewFactory implements PsiStructureViewFactory {
  @Override
  public @Nullable StructureViewBuilder getStructureViewBuilder(final @NotNull PsiFile psiFile) {
    if (!(psiFile instanceof final GroovyFileBase groovyFile)) {
      return null;
    }

    return new TreeBasedStructureViewBuilder() {
      @Override
      public @NotNull StructureViewModel createStructureViewModel(final @Nullable Editor editor) {
        return new JPGroovyFileTreeModel(groovyFile, editor);
      }
    };
  }

  private static class JPGroovyFileTreeModel extends JavaFileTreeModel implements ExpandInfoProvider {
    JPGroovyFileTreeModel(final @NotNull PsiClassOwner file, final @Nullable Editor editor) {
      super(file, editor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Collection<NodeProvider<?>> getNodeProviders() {
      return List.of(new JavaInheritedMembersNodeProvider());
    }

    @Override
    protected boolean isSuitable(final @Nullable PsiElement element) {
      if (element instanceof final GrMethodCall methodCall &&
          JPGdslUtils.isGdslGrMethod(methodCall.resolveMethod())) {
        return true;
      }

      return super.isSuitable(element);
    }

    @Override
    public boolean isAutoExpand(final @NotNull StructureViewTreeElement element) {
      return element instanceof JPPipelinePsiTreeElement ||
             element instanceof JPStagesPsiTreeElement ||
             element instanceof JPStagePsiTreeElement;
    }

    @Override
    public boolean isSmartExpand() {
      return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public int getMinimumAutoExpandDepth() {
      return 4;
    }
  }
}
