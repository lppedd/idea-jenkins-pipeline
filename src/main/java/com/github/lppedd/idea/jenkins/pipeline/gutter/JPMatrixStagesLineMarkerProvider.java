package com.github.lppedd.idea.jenkins.pipeline.gutter;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Edoardo Luppi
 */
public class JPMatrixStagesLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  public @NotNull String getName() {
    return "Matrix";
  }

  @Override
  public @NotNull Icon getIcon() {
    return JPIcons.MATRIX;
  }

  @Override
  public @Nullable RelatedItemLineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (element.isValid() &&
        element instanceof LeafPsiElement &&
        element.getParent() instanceof final GrReferenceExpression refExpr &&
        refExpr.getParent() instanceof final GrMethodCall methodCall &&
        JPUtils.isJenkinsfile(element.getContainingFile())) {
      if ("matrix".equals(refExpr.getReferenceName()) && element == refExpr.getReferenceNameElement()) {
        if (JPGdslUtils.isGdslGrMethod(refExpr.resolve())) {
          return NavigationGutterIconBuilder.create(JPIcons.MATRIX)
              .setTargets(getMatrixStages(methodCall))
              .setCellRenderer(() -> JPStagePresentationRenderer.INSTANCE)
              .setTooltipText("Matrix")
              .setPopupTitle("Matrix stages")
              .createLineMarkerInfo(element);
        }
      }
    }

    return null;
  }

  private @NotNull Collection<GrMethodCall> getMatrixStages(final @NotNull GrMethodCall matrixMethodCall) {
    final var matrixClosure = PsiTreeUtil.getChildOfType(matrixMethodCall, GrClosableBlock.class);
    final var stagesMethodCall = JPPsiUtils.getFirstChildOfType(matrixClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "stages".equals(invokedMethodName);
    });

    final var stagesClosure = PsiTreeUtil.getChildOfType(stagesMethodCall, GrClosableBlock.class);
    return JPPsiUtils.getChildrenOfType(stagesClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "stage".equals(invokedMethodName);
    });
  }
}
