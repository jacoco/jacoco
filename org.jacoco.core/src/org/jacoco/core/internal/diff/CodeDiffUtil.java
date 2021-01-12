package org.jacoco.core.internal.diff;

import org.jacoco.core.analysis.CoverageBuilder;

/**
 * @ProjectName: root
 * @Package: org.jacoco.core.internal.diff
 * @Description: 差异代码处理类
 * @Author: duanrui
 * @CreateDate: 2021/1/12 15:17
 * @Version: 1.0
 * <p>
 * Copyright: Copyright (c) 2021
 */
public class CodeDiffUtil {

    /**
     * 检测类是否在差异代码中
     *
     * @return
     */
    public static Boolean checkClassIn(String className) {
        if (null == CoverageBuilder.classInfos || CoverageBuilder.classInfos.isEmpty() || null == className) {
            return Boolean.FALSE;
        }
        return CoverageBuilder.classInfos.stream().anyMatch(c -> className.equals(c.getClassFile()));
    }


    /**
     * 检测方法是否在差异代码中
     *
     * @param className
     * @param methodName
     * @return
     */
    public static Boolean checkMethodIn(String className, String methodName) {
        //参数校验
        if (null == CoverageBuilder.classInfos || CoverageBuilder.classInfos.isEmpty() || null == methodName || null == className) {
            return Boolean.FALSE;
        }
        ClassInfoDto classInfoDto = CoverageBuilder.classInfos.stream().filter(c -> className.equals(c.getClassFile())).findFirst().get();
        if(null == classInfoDto.getMethodInfos() || classInfoDto.getMethodInfos().isEmpty()){
            return Boolean.FALSE;
        }
        //匹配了方法，参数也需要校验
        return classInfoDto.getMethodInfos().stream().anyMatch(m -> methodName.equals(m.getMethodName()));
    }
}
