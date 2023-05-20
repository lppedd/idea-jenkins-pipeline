package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.dsl.GdslMembersHolderConsumer;
import org.jetbrains.plugins.groovy.dsl.dsltop.GdslMembersProvider;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

import java.util.List;

/**
 * Important: keep the code as simple as possible, as this part is difficult to debug.
 *
 * @author Edoardo Luppi
 */
public class JPGdslMembersProvider implements GdslMembersProvider {
  @GdslMethod
  public boolean isPropertyOf(final @Nullable String name, final @NotNull GdslMembersHolderConsumer consumer) {
    if (name == null) {
      return false;
    }

    final var place = consumer.getPlace();

    if (place instanceof final GrReferenceElement<?> ref) {
      final var refName = ref.getQualifiedReferenceName();
      return refName != null && (
          refName.equals(name) ||
          refName.startsWith(name + ".")
      );
    }

    return true;
  }

  @GdslMethod
  public boolean isType(final @Nullable String name, final @NotNull GdslMembersHolderConsumer consumer) {
    if (name == null) {
      return false;
    }

    final var place = consumer.getPlace();

    if (place instanceof final GrReferenceExpression refExpr) {
      final var parent = refExpr.getParent();

      if (parent instanceof final GrMethodCall methodCall) {
        final var type = JPGdslUtils.getInvokedMethodReturnType(methodCall);

        if (name.equals(type)) {
          return true;
        }
      }

      final var results = refExpr.resolve(true);
      final var iterator = results.iterator();

      if (iterator.hasNext()) {
        final var first = iterator.next();
        final var element = first.getElement();

        if (element instanceof final GrMethod method) {
          final var returnType = method.getReturnType();

          if (returnType != null) {
            if (name.equals(returnType.getCanonicalText())) {
              return true;
            }
          }
        }
      }

      final var qualifierExpression = refExpr.getQualifierExpression();

      if (qualifierExpression != null) {
        final var type = qualifierExpression.getType();

        if (type != null && name.equals(type.getCanonicalText())) {
          return true;
        }
      }
    }

    return true;
  }

  /**
   * Returns {@code true} if the place the element is located in is just under the PSI file.
   * <p>
   * Useful for the {@code pipeline} closure, which cannot be nested inside other closures.
   */
  @GdslMethod
  public boolean isTopLevel(final @NotNull GdslMembersHolderConsumer consumer) {
    final var place = consumer.getPlace();

    if (place == null) {
      return false;
    }

    final var parent = place.getParent();

    if (parent instanceof PsiFile) {
      return true;
    }

    if (parent instanceof final GrMethodCall methodCall) {
      return methodCall.getParent() instanceof PsiFile;
    }

    return false;
  }

  @GdslMethod
  public boolean isArgumentFor(final @Nullable List<String> names, final @NotNull GdslMembersHolderConsumer consumer) {
    final var place = consumer.getPlace();

    if (names == null || names.isEmpty() || !(place instanceof final GrReferenceExpression refExpr)) {
      return false;
    }

    final var refExprParent = refExpr.getParent();

    if (refExprParent instanceof final GrArgumentList argumentList) {
      if (argumentList.getParent() instanceof final GrMethodCall methodCall) {
        return names.contains(JPGdslUtils.getInvokedMethodName(methodCall));
      }
    } else if (refExprParent instanceof final GrMethodCall methodCall) {
      if (methodCall.getParent() instanceof final GrArgumentList argumentList) {
        if (argumentList.getParent() instanceof final GrMethodCall outerMethodCall) {
          return names.contains(JPGdslUtils.getInvokedMethodName(outerMethodCall));
        }
      }

      return names.contains(JPGdslUtils.getInvokedMethodName(methodCall));
    }

    return false;
  }

  @GdslMethod
  public boolean isEnclosedBy(final @Nullable List<String> names, final @NotNull GdslMembersHolderConsumer consumer) {
    final var place = consumer.getPlace();

    if (names == null || names.isEmpty() || place == null) {
      return false;
    }

    final var block = PsiTreeUtil.getParentOfType(place, GrClosableBlock.class, true);

    if (block == null) {
      return false;
    }

    final var blockParent = block.getParent();

    if (blockParent instanceof final GrMethodCall methodCall) {
      return names.contains(JPGdslUtils.getInvokedMethodName(methodCall));
    }

    if (blockParent instanceof final GrArgumentList argumentList) {
      if (argumentList.getParent() instanceof final GrMethodCall methodCall) {
        return names.contains(JPGdslUtils.getInvokedMethodName(methodCall));
      }
    }

    return false;
  }

  /**
   * TODO
   */
  @GdslMethod
  public boolean isInsideMethod(final @NotNull GdslMembersHolderConsumer consumer) {
    return PsiTreeUtil.getParentOfType(consumer.getPlace(), GrMethod.class, true) != null;
  }
}
