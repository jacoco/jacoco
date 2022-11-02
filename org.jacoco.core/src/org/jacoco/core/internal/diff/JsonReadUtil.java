/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @Package: org.jacoco.core.internal.diff
 * @Description: json 读取
 * @Author: rayduan
 * @CreateDate: 2021/11/23 10:48 上午
 * @Version: 1.0
 *           <p>
 */
public class JsonReadUtil {

	/**
	 * 读取很本地json文件
	 *
	 * @param filePath
	 * @return
	 */
	public static String readJsonToString(String filePath) {
		String jsonStr = "";
		try (Reader reader = new InputStreamReader(
				new FileInputStream(filePath), StandardCharsets.UTF_8)) {
			int ch = 0;
			StringBuilder sb = new StringBuilder();
			while ((ch = reader.read()) != -1) {
				sb.append((char) ch);
			}
			jsonStr = sb.toString();
			return jsonStr;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
		readJsonToString("/Users/rayduan/jacoco/a.json");
	}

}
