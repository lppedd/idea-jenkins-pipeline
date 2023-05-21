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
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression;
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
  private static final Set<String> ENV_DEFAULTS = Set.of(
      "BRANCH_NAME",
      "CHANGE_ID",
      "CHANGE_URL",
      "CHANGE_TITLE",
      "CHANGE_AUTHOR",
      "CHANGE_AUTHOR_DISPLAY_NAME",
      "CHANGE_AUTHOR_EMAIL",
      "CHANGE_TARGET",
      "BUILD_NUMBER",
      "BUILD_ID",
      "BUILD_DISPLAY_NAME",
      "JOB_NAME",
      "JOB_BASE_NAME",
      "BUILD_TAG",
      "EXECUTOR_NUMBER",
      "NODE_NAME",
      "NODE_LABELS",
      "WORKSPACE",
      "JENKINS_HOME",
      "JENKINS_URL",
      "BUILD_URL",
      "JOB_URL"
  );

  private static final Map<String, String> PARAM_METHODS = Map.ofEntries(
      Map.entry("booleanParam", JPConstants.Classes.BOOLEAN),
      Map.entry("string", JPConstants.Classes.STRING),
      Map.entry("text", JPConstants.Classes.STRING),
      Map.entry("choice", JPConstants.Classes.STRING),
      Map.entry("password", JPConstants.Classes.STRING)
  );

  @Override
  protected @NotNull Collection<String> getKeyVariants(
      final @NotNull GrExpression qualifier,
      final @Nullable PsiElement resolve) {
    if (isEnvObject(resolve)) {
      final var variants = new LinkedHashSet<String>(64);

      if (JPUtils.isJenkinsfile(qualifier.getContainingFile())) {
        // Collect environment variables from "environment" sections
        collectEnvVars(qualifier, variants);
      }

      variants.addAll(ENV_DEFAULTS);
      return variants;
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

    if (isEnvObject(resolve)) {
      return elementFactory.createTypeByFQClassName(JPConstants.Classes.STRING, qualifier.getResolveScope());
    }

    if (JPUtils.isJenkinsfile(qualifier.getContainingFile()) && isParamsObject(resolve)) {
      final var trimmedKey = key.trim();
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
      final var gdslMethod = JPGdslUtils.getGdslGrMethodOrNull(element);
      return gdslMethod != null && "getParams".equals(gdslMethod.getName());
    }

    return false;
  }

  private boolean isInstanceOf(final @Nullable PsiElement element, final @NotNull String canonicalName) {
    final var gdslMethod = JPGdslUtils.getGdslGrMethodOrNull(element);

    if (gdslMethod != null) {
      final var returnType = gdslMethod.getReturnType();
      return returnType != null && returnType.getCanonicalText().equals(canonicalName);
    }

    return false;
  }

  private void collectEnvVars(final @NotNull PsiElement element, final @NotNull Collection<String> variants) {
    final var stageMethodCall = PsiTreeUtil.findFirstParent(element, true, e -> {
      if (e instanceof final GrMethodCall call) {
        final var method = call.resolveMethod();
        return JPGdslUtils.isGdslGrMethod(method) && "stage".equals(method.getName());
      }

      return false;
    });

    if (stageMethodCall != null) {
      collectSectionEnvVars(stageMethodCall, variants);
      collectEnvVars(stageMethodCall, variants);
    }

    final var pipelineMethodCall = PsiTreeUtil.findFirstParent(element, true, e -> {
      if (e instanceof final GrMethodCall call) {
        final var method = call.resolveMethod();
        return JPGdslUtils.isGdslGrMethod(method) && "pipeline".equals(method.getName());
      }

      return false;
    });

    if (pipelineMethodCall != null) {
      collectSectionEnvVars(pipelineMethodCall, variants);
    }
  }

  private void collectSectionEnvVars(final @NotNull PsiElement element, final @NotNull Collection<String> variants) {
    final var sectionClosure = PsiTreeUtil.getChildOfType(element, GrClosableBlock.class);

    // Inside the section, e.g., "stage" or "pipeline", let's look for the "environment" section
    final var environmentMethodCalls = JPPsiUtils.getChildrenOfType(sectionClosure, GrMethodCall.class, mc -> {
      final var invokedMethodName = JPGdslUtils.getInvokedMethodName(mc);
      return "environment".equals(invokedMethodName);
    });

    for (final var environmentMethodCall : environmentMethodCalls) {
      final var environmentClosure = PsiTreeUtil.getChildOfType(environmentMethodCall, GrClosableBlock.class);
      final var environmentAssignments = PsiTreeUtil.getChildrenOfType(environmentClosure, GrAssignmentExpression.class);

      if (environmentAssignments != null) {
        for (final var assignment : environmentAssignments) {
          variants.add(assignment.getLValue().getText());
        }
      }
    }
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
        final var classFQN = PARAM_METHODS.get(method.getName());

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
