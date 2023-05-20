package com.github.lppedd.idea.jenkins.pipeline.gutter;

import com.github.lppedd.idea.jenkins.pipeline.JPIcons;
import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Edoardo Luppi
 */
public class JPStageLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  public @NotNull String getName() {
    return "Stage";
  }

  @Override
  public @NotNull Icon getIcon() {
    return JPIcons.STAGE;
  }

  @Override
  public @Nullable RelatedItemLineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (element.isValid() &&
        element instanceof LeafPsiElement &&
        element.getParent() instanceof final GrReferenceExpression refExpr &&
        refExpr.getParent() instanceof GrMethodCall &&
        JPUtils.isJenkinsfile(element.getContainingFile())) {
      if ("stage".equals(refExpr.getReferenceName()) && element == refExpr.getReferenceNameElement()) {
        if (JPGdslUtils.isGdslGrMethod(refExpr.resolve())) {
          return NavigationGutterIconBuilder.create(JPIcons.STAGE)
              .setTargets(getStages(element.getContainingFile()))
              .setCellRenderer(() -> JPStagePresentationRenderer.INSTANCE)
              .setTooltipText("Stage")
              .setPopupTitle("Stages")
              .createLineMarkerInfo(element);
        }
      }
    }

    return null;
  }

  private @NotNull Collection<PsiElement> getStages(final @NotNull PsiFile file) {
    final var stages = new ArrayList<PsiElement>(16);

    if (file instanceof final GroovyFile groovyFile) {
      final var methodCalls = PsiTreeUtil.findChildrenOfType(groovyFile, GrMethodCall.class);

      for (final var call : methodCalls) {
        final var method = call.resolveMethod();

        if (JPGdslUtils.isGdslGrMethod(method) && "stage".equals(method.getName())) {
          stages.add(call);
        }
      }
    }

    return stages;
  }
}
