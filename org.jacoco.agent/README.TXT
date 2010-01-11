This bundle provides the coverage agent "jacocoagent.jar" as a resource. The
agent JAR is created as part of the build process and therefore not available in
the source tree. Which means this bundle is incomplete when directly used in the
Eclise IDE. If you want to use the bundle org.jacoco.agent within the IDE,
please perform the following steps:

  1) Run the local build (see documentation)
  2) In the Eclipse preferences under "Linked Resources" define a path variable
     BUILD_JACOCOAGENT_JAR pointing to the agent JAR created by the build:
     
         <workspacedir>/org.jacoco.build/result/dist/lib/jacocoagent.jar