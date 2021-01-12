### 简介
+ jacoco二开，主要加入了增量代码匹配的功能

### 改造内容：
+ 新增类：所有新增类处于org.jacoco.core.internal.diff包下面；
+ 修改类：主要修改了核心类org.jacoco.core.analysis.Analyzer,org.jacoco.core.analysis.CoverageBuilder;org.jacoco.core.internal.flowClassProbesAdapter
+ 所有修改内容在commit记录可查，为了方便查看，我尽量减少了commit的次数

### 使用方法
###### 1、 下载源码，然后运行maven: mvn package   -Dmaven.test.skip=true   -Dmaven.javadoc.skip=true
###### 2、 其他包都可以使用jacoco官方包，report时使用我们构建的包，如果是增量覆盖率加入参数--diffCode=
```
[
         {
           "classFile": "collector/src/main/java/com/geely/collector/CollectorApplication.java",
           "methodInfos": [
             {
               "md5": "13E2BFB69F7D987A6DB4272400C94E9B",
               "methodName": "main",
               "parameters": "[String[] args]"
             }
           ],
           "type": "MODIFY"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/bean/CodeQuality.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/dao/basic/CodeQualityMapper.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/dao/extension/CodeQualityExtensionMapper.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/mvc/APIResponse.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/mvc/BusinessException.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "collector/src/main/java/com/geely/collector/task/TestTask.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/gitlab/config/GitlabAPIConfig.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/gitlab/dto/CommitDetail.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/gitlab/dto/GitLabStats.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/gitlab/service/GitlabService.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/gitlab/service/impl/GitlabServiceImpl.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/Test.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/client/BaseHttpClient.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/client/MeasureClient.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/client/ProjectClient.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/client/authentication/PreemptiveAuth.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/config/SonarConnectionConf.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/dto/BaseModel.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/dto/MeasuresBean.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/dto/MeasuresResultDto.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/service/MeasureService.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/service/impl/MeasureServiceImpl.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/util/HttpResponseValidator.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/util/HttpResponseWrapper.java",
           "methodInfos": null,
           "type": "ADD"
         },
         {
           "classFile": "third-sdk/src/main/java/com/geely/sonar/util/SonarContant.java",
           "methodInfos": null,
           "type": "ADD"
         }
       ]
     }
```
由于对象格式的通用性，可以配合 [差异代码获取](https://github.com/rayduan/code-diff.git) 一起使用

