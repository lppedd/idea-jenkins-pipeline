package com.github.lppedd.idea.jenkins.pipeline;

import com.github.lppedd.idea.jenkins.pipeline.gdsl.JPGdslUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPPsiUtils;
import com.github.lppedd.idea.jenkins.pipeline.utils.JPUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.extensions.GroovyMapContentProvider;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: support stage input parameters
 *
 * @author Edoardo Luppi
 */
public class JPMapContentProvider extends GroovyMapContentProvider {
  private final Map<String, String> envDefaults = Map.ofEntries(
      Map.entry("BRANCH_NAME", "java.lang.String"),
      Map.entry("CHANGE_ID", "java.lang.String"),
      Map.entry("CHANGE_URL", "java.lang.String"),
      Map.entry("CHANGE_TITLE", "java.lang.String"),
      Map.entry("CHANGE_AUTHOR", "java.lang.String"),
      Map.entry("CHANGE_AUTHOR_DISPLAY_NAME", "java.lang.String"),
      Map.entry("CHANGE_AUTHOR_EMAIL", "java.lang.String"),
      Map.entry("CHANGE_TARGET", "java.lang.String"),
      Map.entry("BUILD_NUMBER", "java.lang.String"),
      Map.entry("BUILD_ID", "java.lang.String"),
      Map.entry("BUILD_DISPLAY_NAME", "java.lang.String"),
      Map.entry("JOB_NAME", "java.lang.String"),
      Map.entry("JOB_BASE_NAME", "java.lang.String"),
      Map.entry("BUILD_TAG", "java.lang.String"),
      Map.entry("EXECUTOR_NUMBER", "java.lang.String"),
      Map.entry("NODE_NAME", "java.lang.String"),
      Map.entry("NODE_LABELS", "java.lang.String"),
      Map.entry("WORKSPACE", "java.lang.String"),
      Map.entry("JENKINS_HOME", "java.lang.String"),
      Map.entry("JENKINS_URL", "java.lang.String"),
      Map.entry("BUILD_URL", "java.lang.String"),
      Map.entry("JOB_URL", "java.lang.String")
  );

  private final Map<String, String> paramMethods = Map.ofEntries(
      Map.entry("booleanParam", "java.lang.Boolean"),
      Map.entry("string", "java.lang.String"),
      Map.entry("text", "java.lang.String"),
      Map.entry("choice", "java.lang.String"),
      Map.entry("password", "java.lang.String")
  );

  @Override
  protected @NotNull Collection<String> getKeyVariants(
      final @NotNull GrExpression qualifier,
      final @Nullable PsiElement resolve) {
    if (isEnvObject(resolve)) {
      return envDefaults.keySet();
    }

    if (JPUtils.isJenkinsfile(qualifier.getContainingFile()) && isParamsObject(resolve)) {
      return getParameters(qualifier).stream()
          .map(pair -> pair.getFirst())
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

  @Override
  public @Nullable PsiType getValueType(
      final @NotNull GrExpression qualifier,
      final @Nullable PsiElement resolve,
      final @NotNull String key) {
    final var elementFactory = JavaPsiFacade.getElementFactory(qualifier.getProject());
    final var trimmedKey = key.trim();

    if (isEnvObject(resolve)) {
      final var classFQN = envDefaults.get(trimmedKey);

      if (classFQN != null) {
        return elementFactory.createTypeByFQClassName(classFQN, qualifier.getResolveScope());
      }
    }

    if (JPUtils.isJenkinsfile(qualifier.getContainingFile()) && isParamsObject(resolve)) {
      return getParameters(qualifier).stream()
          .filter(pair -> pair.getFirst().equals(trimmedKey))
          .findFirst()
          .map(pair -> pair.getSecond())
          .map(classFQN -> elementFactory.createTypeByFQClassName(classFQN, qualifier.getResolveScope()))
          .orElse(null);
    }

    return null;
  }

  private boolean isEnvObject(final @Nullable PsiElement element) {
    return isInstanceOf(element, "org.jenkinsci.plugins.workflow.cps.EnvActionImpl");
  }

  @Contract("null -> false")
  private boolean isParamsObject(final @Nullable PsiElement element) {
    if (isInstanceOf(element, "java.util.Map")) {
      final var method = JPGdslUtils.isGdslGrMethodOrNull(element);
      return method != null && "getParams".equals(method.getName());
    }

    return false;
  }

  private boolean isInstanceOf(final @Nullable PsiElement element, final @NotNull String canonicalName) {
    final var method = JPGdslUtils.isGdslGrMethodOrNull(element);

    if (method != null) {
      final var returnType = method.getReturnType();
      return returnType != null && returnType.getCanonicalText().equals(canonicalName);
    }

    return false;
  }

  private @NotNull List<Pair<String, String>> getParameters(final @NotNull PsiElement element) {
    // Get the first "pipeline" method call going up the tree.
    // We will then navigate down again to find the "parameters" method call
    final var pipelineMethodCall = PsiTreeUtil.findFirstParent(element, true, e -> {
      if (e instanceof final GrMethodCall call) {
        final var method = call.resolveMethod();
        return JPGdslUtils.isGdslGrMethod(method) && "pipeline".equals(method.getName());
      }

      return false;
    });

    final var pipelineClosure = PsiTreeUtil.getChildOfType(pipelineMethodCall, GrClosableBlock.class);

    // Inside the "pipeline" closure, let's look for the "parameters" method call
    final var parametersMethodCall = JPPsiUtils.getFirstChildOfType(pipelineClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "parameters".equals(invokedMethodName);
    });

    final var parametersClosure = PsiTreeUtil.getChildOfType(parametersMethodCall, GrClosableBlock.class);
    final var parameterMethodCalls = PsiTreeUtil.getChildrenOfType(parametersClosure, GrMethodCall.class);

    if (parameterMethodCalls == null) {
      return Collections.emptyList();
    }

    final var variants = new ArrayList<Pair<String, String>>(parameterMethodCalls.length);

    for (final var parameterMethodCall : parameterMethodCalls) {
      final var method = parameterMethodCall.resolveMethod();

      if (JPGdslUtils.isGdslGrMethod(method)) {
        final var classFQN = paramMethods.get(method.getName());

        if (classFQN != null) {
          final var argumentList = parameterMethodCall.getArgumentList();
          final var namedArgument = argumentList.findNamedArgument("name");

          if (namedArgument != null &&
              namedArgument.getExpression() instanceof final GrLiteral literal && literal.isString()) {
            variants.add(Pair.create((String) literal.getValue(), classFQN));
          }
        }
      }
    }

    return variants;
  }
}
