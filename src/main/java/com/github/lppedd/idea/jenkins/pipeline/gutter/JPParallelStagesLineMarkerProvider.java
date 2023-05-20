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
public class JPParallelStagesLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  public @NotNull String getName() {
    return "Parallel stages";
  }

  @Override
  public @NotNull Icon getIcon() {
    return JPIcons.PARALLEL_STAGES;
  }

  @Override
  public @Nullable RelatedItemLineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (element.isValid() &&
        element instanceof LeafPsiElement &&
        element.getParent() instanceof final GrReferenceExpression refExpr &&
        refExpr.getParent() instanceof final GrMethodCall methodCall &&
        JPUtils.isJenkinsfile(element.getContainingFile())) {
      if ("parallel".equals(refExpr.getReferenceName()) && element == refExpr.getReferenceNameElement()) {
        if (JPGdslUtils.isGdslGrMethod(refExpr.resolve())) {
          return NavigationGutterIconBuilder.create(JPIcons.PARALLEL_STAGES)
              .setTargets(getParallelStages(methodCall))
              .setCellRenderer(() -> JPStagePresentationRenderer.INSTANCE)
              .setTooltipText("Parallel stages")
              .setPopupTitle("Parallel stages")
              .createLineMarkerInfo(element);
        }
      }
    }

    return null;
  }

  private @NotNull Collection<GrMethodCall> getParallelStages(final @NotNull GrMethodCall methodCall) {
    final var closableBlock = PsiTreeUtil.getChildOfType(methodCall, GrClosableBlock.class);
    return JPPsiUtils.getChildrenOfType(closableBlock, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "stage".equals(invokedMethodName);
    });
  }
}
