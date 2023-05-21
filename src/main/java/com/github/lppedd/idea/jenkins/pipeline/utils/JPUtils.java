package com.github.lppedd.idea.jenkins.pipeline.utils;

import com.github.lppedd.idea.jenkins.pipeline.JPConstants;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

/**
 * @author Edoardo Luppi
 */
public class JPUtils {
  public static boolean isJenkinsfile(final @NotNull VirtualFile virtualFile) {
    return JPConstants.Files.JENKINSFILE.equals(virtualFile.getName()) &&
           FileTypeRegistry.getInstance().isFileOfType(virtualFile, GroovyFileType.GROOVY_FILE_TYPE);
  }

  public static boolean isJenkinsfile(final @NotNull PsiFile psiFile) {
    final var virtualFile = psiFile.getVirtualFile();
    return isJenkinsfile(
        virtualFile != null
            ? virtualFile
            : psiFile.getViewProvider().getVirtualFile()
    );
  }

  public static @Nullable String getStageName(final @NotNull GrMethodCall methodCall) {
    final var argumentList = methodCall.getArgumentList();
    final var arguments = argumentList.getAllArguments();

    if (arguments.length > 0 && arguments[0] instanceof final GrLiteral literal && literal.isString()) {
      final var value = (String) literal.getValue();

      if (value != null && !value.isBlank()) {
        return value;
      }
    }

    return null;
  }
}
