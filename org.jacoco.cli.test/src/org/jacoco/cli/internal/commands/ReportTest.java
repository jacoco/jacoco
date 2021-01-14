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
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link Report}.
 */
public class ReportTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("report");

		assertFailure();
		assertContains("\"--classfiles\"", err);
		assertContains(
				"Usage: java -jar jacococli.jar report [<execfiles> ...]", err);
	}

	@Test
	public void should_print_warning_when_no_exec_files_are_provided()
			throws Exception {
		execute("report", "--classfiles", getClassPath());

		assertOk();
		assertContains("[WARN] No execution data files provided.", out);
	}

	@Test
	public void should_print_number_of_analyzed_classes() throws Exception {
		execute("report", "--classfiles", getClassPath());

		assertOk();
		assertContains("[INFO] Analyzing 14 classes.", out);
	}

	@Test
	public void should_print_warning_when_exec_data_does_not_match()
			throws Exception {
		File exec = new File(tmp.getRoot(), "jacoco.exec");
		final FileOutputStream execout = new FileOutputStream(exec);
		ExecutionDataWriter writer = new ExecutionDataWriter(execout);
		// Add probably invalid id for this test class:
		writer.visitClassExecution(
				new ExecutionData(0x123, getClass().getName().replace('.', '/'),
						new boolean[] { true }));
		execout.close();

		execute("report", exec.getAbsolutePath(), "--classfiles",
				getClassPath());

		assertOk();
		assertContains("[WARN] Some classes do not match with execution data.",
				out);
		assertContains(
				"[WARN] For report generation the same class files must be used as at runtime.",
				out);
		assertContains(
				"[WARN] Execution data for class org/jacoco/cli/internal/commands/ReportTest does not match.",
				out);
	}

	@Test
	public void should_create_xml_report_when_xml_option_is_provided()
			throws Exception {
		File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", "--classfiles", getClassPath(), "--xml",
				xml.getAbsolutePath());

		assertOk();
		assertTrue(xml.isFile());
	}

	@Test
	public void should_create_csv_report_when_csv_option_is_provided()
			throws Exception {
		File csv = new File(tmp.getRoot(), "coverage.csv");

		execute("report", "--classfiles", getClassPath(), "--csv",
				csv.getAbsolutePath());

		assertOk();
		assertTrue(csv.isFile());
	}

	@Test
	public void should_create_html_report_when_html_option_is_provided()
			throws Exception {
		File html = new File(tmp.getRoot(), "coverage");

		execute("report", "--classfiles", getClassPath(), "--sourcefiles",
				"./src", "--html", html.getAbsolutePath());

		assertOk();
		assertTrue(html.isDirectory());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.html").isFile());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.java.html")
						.isFile());
	}

	@Test
	public void mytest() throws Exception {

		execute("report", "D:\\jacoco\\jacoco-demo.exec", "--classfiles",
				"D:\\IdeaProjects\\base\\base-service\\application\\target\\classes\\com",
				"--sourcefiles",
				"D:\\IdeaProjects\\base\\base-service\\application\\src\\main\\java ",
				"--html", "D:\\jacoco\\report", "--xml",
				"D:\\jacoco\\report.xml", "--diffCode",
				"[{\"classFile\":\"com/dr/application/InstallCert\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/controller/Calculable\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/controller/JenkinsPluginController\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/controller/LoginController\",\"methodInfos\":[{\"md5\":\"6E69BAD8148ACE09617AA27E86B33334\",\"methodName\":\"captcha\",\"parameters\":\"HttpServletRequest request,HttpServletResponse response\"},{\"md5\":\"D41D8CD98F00B204E9800998ECF8427E\",\"methodName\":\"login\",\"parameters\":\"@RequestBody LoginUserParam loginUserParam,HttpServletRequest request\"},{\"md5\":\"90842DFA5372DCB74335F22098B36A53\",\"methodName\":\"logout\",\"parameters\":\"\"},{\"md5\":\"FBA90F9C2FE9D21020A4FBEF0A13DF36\",\"methodName\":\"testInt\",\"parameters\":\"int a,char b\"},{\"md5\":\"5D7953EDD5E08483B01B73B79FA4B375\",\"methodName\":\"testInt\",\"parameters\":\"String a,int b\"},{\"md5\":\"F9BF585A4F6E158CD4475700847336A6\",\"methodName\":\"testInt\",\"parameters\":\"short a,int b\"},{\"md5\":\"D80B222F40338D56F7224B871804DA7C\",\"methodName\":\"testInt\",\"parameters\":\"int[] a\"},{\"md5\":\"381C8CBF1F381A58E1E93774AE1AF4EC\",\"methodName\":\"testInt\",\"parameters\":\"AddUserParam param\"},{\"md5\":\"D41D8CD98F00B204E9800998ECF8427E\",\"methodName\":\"testInt\",\"parameters\":\"T[] a\"},{\"md5\":\"D091AB0AD9160407AED4182259200B9B\",\"methodName\":\"testInt\",\"parameters\":\"Calculable calc,int n1,int n2\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/application/app/controller/RoleController\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/controller/TestController\",\"methodInfos\":[{\"md5\":\"C0698DD4010643D164801E3A5ADFA7A2\",\"methodName\":\"test\",\"parameters\":\"\"},{\"md5\":\"B1840C873BF0BA74CB6749E1CEE93ED7\",\"methodName\":\"getPom\",\"parameters\":\"HttpServletResponse response\"},{\"md5\":\"987697EAE1BCBECDD186CD0A048689B5\",\"methodName\":\"getDeList\",\"parameters\":\"\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/application/app/controller/UserController\",\"methodInfos\":[{\"md5\":\"99902D81228EEEA951456E4513ADE334\",\"methodName\":\"get\",\"parameters\":\"\"},{\"md5\":\"2B35EA4FB5054C6EF13D557C2ACBB581\",\"methodName\":\"list\",\"parameters\":\"@ApiParam(required = true, name = \\\"page\\\", defaultValue = \\\"1\\\", value = \\\"当前页码\\\") @RequestParam(name = \\\"page\\\") Integer page,@ApiParam(required = true, name = \\\"pageSize\\\", defaultValue = \\\"10\\\", value = \\\"每页数量\\\") @RequestParam(name = \\\"pageSize\\\") Integer pageSize,@ApiParam(name = \\\"userId\\\", value = \\\"用户id\\\") @RequestParam(name = \\\"userId\\\", required = false) Long userId,@ApiParam(name = \\\"username\\\", value = \\\"用户名\\\") @RequestParam(name = \\\"username\\\", required = false) String username,@ApiParam(name = \\\"userSex\\\", value = \\\"性别\\\") @RequestParam(name = \\\"userSex\\\", required = false) Integer userSex,@ApiParam(name = \\\"mobile\\\", value = \\\"手机号\\\") @RequestParam(name = \\\"mobile\\\", required = false) String mobile\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/application/app/controller/view/RoleViewController\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/controller/view/UserViewController\",\"methodInfos\":[{\"md5\":\"0154CF1652F1F021EF4EDE3C6D636EA6\",\"methodName\":\"index\",\"parameters\":\"\"},{\"md5\":\"9A1DDA3F41B36026FC2F3ACDAE85C1DB\",\"methodName\":\"user\",\"parameters\":\"\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/application/app/param/AddRoleParam\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/vo/DependencyVO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/vo/JenkinsPluginsVO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/app/vo/RoleVO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/config/ExceptionAdvice\",\"methodInfos\":[{\"md5\":\"34F4711395C00A4B006ACDA21B8FBCA6\",\"methodName\":\"handleBizException\",\"parameters\":\"HttpServletResponse resp,BizException e\"},{\"md5\":\"A4E167EA0A7B85C41D9442536DC24978\",\"methodName\":\"handleBindException\",\"parameters\":\"MethodArgumentNotValidException e\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/application/config/GitConfig\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/config/JenkinsConfig\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/ddd/StaticTest\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/ddd/Test\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/filter/JudgeSessionFilter\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/application/util/GitAdapter\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/CodeDiffApplication\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/config/ExecutorConfig\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/config/GitConfig\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/controller/CodeDiffController\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/controller/TestApi\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/dto/DiffMethodParams\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/dto/MethodInfoResult\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/service/CodeDiffService\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/service/impl/CodeDiffServiceImpl\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/util/MethodParser\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/vo/GoodsVO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/codediff/vo/JobAddVo\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/common/response/ApiResponse\",\"methodInfos\":[{\"md5\":\"D41D8CD98F00B204E9800998ECF8427E\",\"methodName\":\"success\",\"parameters\":\"\"},{\"md5\":\"D41D8CD98F00B204E9800998ECF8427E\",\"methodName\":\"success\",\"parameters\":\"T data\"},{\"md5\":\"EC1A2DD12D51791D2544F4E3C9724CB3\",\"methodName\":\"error\",\"parameters\":\"Code code,T data\"},{\"md5\":\"D41D8CD98F00B204E9800998ECF8427E\",\"methodName\":\"error\",\"parameters\":\"Code code\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/jenkins/JenkinsApplication\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/config/JenkinsConfigure\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/controller/JenkinsController\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/controller/TestApi\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/dto/JobAddDto\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/service/JenkinsService\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/service/impl/JenkinsServiceImpl\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/util/GenerateUniqueIdUtil\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/vo/DeviceVo\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/vo/GoodsVO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/jenkins/vo/JobAddVo\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/repository/user/dto/query/RoleQueryDto\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/repository/user/dto/query/UserQueryDto\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/repository/user/dto/result/MenuDTO\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/repository/user/dto/result/RoleResultDto\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/repository/user/dto/result/UserResultDto\",\"methodInfos\":null,\"type\":\"ADD\"},{\"classFile\":\"com/dr/user/service/impl/RoleServiceImpl\",\"methodInfos\":[{\"md5\":\"47405162B3397D02156DE636059049F2\",\"methodName\":\"getListByPage\",\"parameters\":\"RoleQueryDto roleQueryDto\"}],\"type\":\"MODIFY\"},{\"classFile\":\"com/dr/user/service/impl/UserServiceImpl\",\"methodInfos\":[{\"md5\":\"46FE5B7D6D114020E101F998F12E2C2A\",\"methodName\":\"selectListByPage\",\"parameters\":\"UserQueryDto userQueryDto\"}],\"type\":\"MODIFY\"}]");
		// execute("report","D:\\jacoco\\jacoco-demo.exec", "--classfiles",
		// "D:\\IdeaProjects\\base\\base-service\\application\\target\\classes\\com",
		// "--sourcefiles",
		// "D:\\IdeaProjects\\base\\base-service\\application\\src\\main\\java
		// ", "--html", "D:\\jacoco\\all\\report","--xml",
		// "D:\\jacoco\\report.xml");

		assertOk();
		// assertTrue(html.isDirectory());
		// assertTrue(new File(html,
		// "org.jacoco.cli.internal.commands/ReportTest.html").isFile());
		// assertTrue(new File(html,
		// "org.jacoco.cli.internal.commands/ReportTest.java.html")
		// .isFile());
	}

	@Test
	public void should_use_all_values_when_multiple_classfiles_options_are_provided()
			throws Exception {
		File html = new File(tmp.getRoot(), "coverage");

		final String c1 = getClassPath()
				+ "/org/jacoco/cli/internal/commands/ReportTest.class";
		final String c2 = getClassPath()
				+ "/org/jacoco/cli/internal/commands/DumpTest.class";

		execute("report", "--classfiles", c1, "--classfiles", c2, "--html",
				html.getAbsolutePath());

		assertOk();
		assertTrue(html.isDirectory());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.html").isFile());
		assertTrue(
				new File(html, "org.jacoco.cli.internal.commands/DumpTest.html")
						.isFile());
	}

}
