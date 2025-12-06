package org.jacoco.core.internal.diff;

/**
 * @author NoahArno
 * @version 1.0.0
 * @since 2025/12/6 23:46
 */
public class ChangeLine {

    /**
     * 变更类型
     */
    private String type;

    private Integer startLineNum;

    private Integer endLineNum;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStartLineNum() {
        return startLineNum;
    }

    public void setStartLineNum(Integer startLineNum) {
        this.startLineNum = startLineNum;
    }

    public Integer getEndLineNum() {
        return endLineNum;
    }

    public void setEndLineNum(Integer endLineNum) {
        this.endLineNum = endLineNum;
    }

    public static enum Type {
        INSERT,
        DELETE,
        REPLACE,
        EMPTY;
        private Type() {
        }
    }
}

