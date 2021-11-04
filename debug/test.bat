cd debug

javac -encoding utf-8 *.java
jar cfm debug.jar META-INF/MANIFEST.MF *.class

call test3.bat

type premain
type main
