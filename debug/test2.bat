rem https://www.ibm.com/support/pages/%C2%A0how-change-codepage-dos-batch-files-ansi
chcp 1252

"C:\Program Files\Java\jdk11\bin\java" -XshowSettings:properties -version
"C:\Program Files\Java\jdk11\bin\java" -Dsun.jnu.encoding=cp1252 -Dfile.encoding=cp1252 -javaagent:debug.jar=© -cp debug.jar Main ©
