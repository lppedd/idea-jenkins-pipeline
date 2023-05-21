package com.github.lppedd.idea.jenkins.pipeline;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.resolve.imports.GrImportContributor;
import org.jetbrains.plugins.groovy.lang.resolve.imports.GroovyImport;
import org.jetbrains.plugins.groovy.lang.resolve.imports.RegularImport;

import java.util.List;

/**
 * Contributes default imports, a.k.a. definitions for which
 * an import statement is not required.
 *
 * @author Edoardo Luppi
 */
public class JPImportContributor implements GrImportContributor {
  private static final List<GroovyImport> JENKINS_IMPORTS = List.of(
      new RegularImport("org.jenkinsci.plugins.workflow.shared.Library", "Library")
  );

  @Override
  public @NotNull List<GroovyImport> getFileImports(final @NotNull GroovyFile file) {
    return JENKINS_IMPORTS;
  }
}
