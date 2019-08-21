/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    donhui - add GetterAndSetterFilter
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class GetterAndSetterFilter implements IFilter {

    public void filter(MethodNode methodNode, IFilterContext context, IFilterOutput output) {
        Set<FieldNode> fieldNodeSet = context.getClassFields();
        List<String> getGetterAndSetterMethodNameList = getGetterAndSetterMethodNameList(fieldNodeSet);
        if (isMethodFiltered(methodNode, getGetterAndSetterMethodNameList)) {
            output.ignore(methodNode.instructions.getFirst(), methodNode.instructions.getLast());
        }
    }

    private boolean isMethodFiltered(MethodNode methodNode, List<String> getGetterAndSetterMethodNameList) {
        return getGetterAndSetterMethodNameList.contains(methodNode.name);
    }

    private List<String> getGetterAndSetterMethodNameList(Set<FieldNode> fieldNodeSet) {
        List<String> getterAndSetterMethodNameList = new ArrayList<String>();
        for (FieldNode fieldNode : fieldNodeSet) {
            String fieldName = fieldNode.name;
            fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            if ("Z".equals(fieldNode.desc)) {
                getterAndSetterMethodNameList.add("is" + fieldName);
            } else {
                getterAndSetterMethodNameList.add("get" + fieldName);
            }
            getterAndSetterMethodNameList.add("set" + fieldName);
        }
        return getterAndSetterMethodNameList;
    }
}
