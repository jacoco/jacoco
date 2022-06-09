/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.diff;

import org.jacoco.core.analysis.CoverageBuilder;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Stream;

/**
 * @ProjectName: root
 * @Package: org.jacoco.core.internal.diff
 * @Description: 差异代码处理类
 * @Author: duanrui
 * @CreateDate: 2021/1/12 15:17
 * @Version: 1.0
 *           <p>
 *           Copyright: Copyright (c) 2021
 */
public class CodeDiffUtil {

	private final static String OPERATE_ADD = "ADD";

	/**
	 * 检测类是否在差异代码中
	 *
	 * @param className
	 * @return Boolean
	 */
	public static Boolean checkClassIn(String className) {
		if (null == CoverageBuilder.classInfos
				|| CoverageBuilder.classInfos.isEmpty() || null == className) {
			return Boolean.FALSE;
		}
		// 这里要考虑匿名内部类的问题
		return CoverageBuilder.classInfos.stream()
				.anyMatch(c -> className.equals(c.getClassFile())
						|| className.split("\\$")[0].equals(c.getClassFile()));
	}

	/**
	 * 检测方法是否在差异代码中
	 *
	 * @param className
	 * @param methodName
	 * @return Boolean
	 */
	public static Boolean checkMethodIn(String className, String methodName,
			String desc) {
		// 参数校验
		if (null == CoverageBuilder.classInfos
				|| CoverageBuilder.classInfos.isEmpty() || null == methodName
				|| null == className) {
			return Boolean.FALSE;
		}
		ClassInfoDto classInfoDto = CoverageBuilder.classInfos.stream()
				.filter(c -> className.equals(c.getClassFile())
						|| className.split("\\$")[0].equals(c.getClassFile()))
				.findFirst().orElse(null);
		if (null == classInfoDto) {
			return Boolean.FALSE;
		}
		// 如果是新增类，不用匹配方法，直接运行
		if (OPERATE_ADD.equals(classInfoDto.getType())) {
			return Boolean.TRUE;
		}
		if (null == classInfoDto.getMethodInfos()
				|| classInfoDto.getMethodInfos().isEmpty()) {
			return Boolean.FALSE;
		}
		// 匹配了方法，参数也需要校验
		return classInfoDto.getMethodInfos().stream().anyMatch(m -> {
			if (methodName.equals(m.getMethodName())) {
				return checkParamsIn(m.getParameters(), desc);
			} else {
				return Boolean.FALSE;
			}
		});

	}

	/**
	 * 匹配餐数
	 *
	 * @param params
	 *            格式：String a
	 * @param desc
	 *            转换后格式： java.lang.String
	 * @return
	 */
	public static Boolean checkParamsIn(List<String> params, String desc) {
		// 解析ASM获取的参数
		Type[] argumentTypes = Type.getArgumentTypes(desc);
		// 说明是无参数的方法，匹配成功
		if (params.size() == 0 && argumentTypes.length == 0) {
			return Boolean.TRUE;
		}
		String[] diffParams = params.toArray(new String[params.size()]);
		// 只有参数数量完全相等才做下一次比较，Type格式：I C Ljava/lang/String;
		if (diffParams.length > 0
				&& argumentTypes.length == diffParams.length) {
			for (int i = 0; i < argumentTypes.length; i++) {
				// 去掉包名只保留最后一位匹配,getClassName格式： int java/lang/String
				String[] args = argumentTypes[i].getClassName().split("\\.");
				String arg = args[args.length - 1];
				// 如果参数是内部类类型，再截取下
				if (arg.contains("$")) {
					arg = arg.split("\\$")[arg.split("\\$").length - 1];
				}
				if (!diffParams[i].contains(arg)) {
					return Boolean.FALSE;
				}
			}
			// 只有个数和类型全匹配到才算匹配
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
