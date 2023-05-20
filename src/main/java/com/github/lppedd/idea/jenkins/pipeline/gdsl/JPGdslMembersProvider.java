package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import com.intellij.psi.PsiElement;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Edoardo Luppi
 */
@SuppressWarnings("unused")
public class JPGdslMembersProvider implements GdslMembersProvider {
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

  public boolean isArgumentFor(final @Nullable List<String> names, final @NotNull GdslMembersHolderConsumer consumer) {
    var place = consumer.getPlace();

    if (names == null || names.isEmpty() || place == null) {
      return false;
    }

    if (place.getParent() instanceof final GrMethodCall methodCall) {
      if (place.getText().equals(JPGdslUtils.getInvokedMethodName(methodCall))) {
        place = methodCall;
      }
    }

    if (place.getParent() instanceof final GrArgumentList argumentList) {
      if (argumentList.getParent() instanceof final GrMethodCall methodCall) {
        for (final var name : names) {
          if (name.equals(JPGdslUtils.getInvokedMethodName(methodCall))) {
            return true;
          }
        }
      }
    }

    return false;
  }

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
      for (final var name : names) {
        if (name.equals(JPGdslUtils.getInvokedMethodName(methodCall))) {
          return true;
        }
      }
    } else if (blockParent instanceof final GrArgumentList argumentList) {
      if (argumentList.getParent() instanceof final GrMethodCall methodCall) {
        for (final var name : names) {
          if (name.equals(JPGdslUtils.getInvokedMethodName(methodCall))) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * TODO
   */
  public boolean isInsideMethod(final @NotNull GdslMembersHolderConsumer consumer) {
    return PsiTreeUtil.getParentOfType(consumer.getPlace(), GrMethod.class, true) != null;
  }

  // PRIVATE UTILITIES

  private static @NotNull List<GrClosableBlock> findAllParents(final @NotNull PsiElement element) {
    final var calls = new ArrayList<GrClosableBlock>(4);
    var call = element;

    while ((call = PsiTreeUtil.getParentOfType(call, GrClosableBlock.class, true)) != null) {
      calls.add((GrClosableBlock) call);
    }

    return calls;
  }
}
