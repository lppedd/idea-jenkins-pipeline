package org.jenkinsci.plugins.workflow.shared

/**
 * Dummy annotation for code indexing and completion.
 */
@interface Library {
  String[] value() default []
}
