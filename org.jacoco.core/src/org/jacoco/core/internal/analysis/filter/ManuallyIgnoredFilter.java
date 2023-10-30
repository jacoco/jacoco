package org.jacoco.core.internal.analysis.filter;

import java.util.List;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters out any annotation that is named has the value "JacocoIgnored".
 * Example:
 * The following method that for some reason is not reachable by tests will be ignored by Jacoco:
 * <pre>{@code
 *    @JacocoIgnored
 *    public void foo(Object bar){
 *      //DO SOMETHING HERE
 *    }
 *
 * }</pre>
 */
public class ManuallyIgnoredFilter implements IFilter {

  public static final String ANNOTATION_VALUE = "JacocoIgnored";

  @Override
  public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {
    for (String annotation : context.getClassAnnotations()) {
      if (matches(annotation)) {
        output.ignore(methodNode.instructions.getFirst(),
          methodNode.instructions.getLast());
        return;
      }
    }

    if (presentIn(methodNode.visibleAnnotations)) {
      output.ignore(methodNode.instructions.getFirst(),
        methodNode.instructions.getLast());
    }

  }

  private static boolean matches(final String annotation) {
    return annotation.contains(ANNOTATION_VALUE);
  }

  private static boolean presentIn(final List<AnnotationNode> annotations) {
    if (annotations != null) {
      for (AnnotationNode annotation : annotations) {
        if (matches(annotation.desc)) {
          return true;
        }
      }
    }
    return false;
  }
}
