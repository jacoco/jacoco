package org.jacoco.core.internal.diff;


import java.util.List;

/**
 * @author dr
 */
public class ClassInfoDto {
    /**
     * java文件
     */
    private String classFile;

    /**
     * 类中的方法
     */
    private List<MethodInfoDto> methodInfos;


    /**
     * 修改类型
     */
    private String type;

    public String getClassFile() {
        return classFile;
    }

    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    public List<MethodInfoDto> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<MethodInfoDto> methodInfos) {
        this.methodInfos = methodInfos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
