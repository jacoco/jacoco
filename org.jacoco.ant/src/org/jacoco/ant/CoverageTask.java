/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - TestNG support
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;

/**
 * Container task to run Java/JUnit tasks with the JaCoCo agent jar. Coverage
 * will only be applied if all of the following are true:
 * <ul>
 * <li>Exactly one sub task may be present</li>
 * <li>Task must be either Java or JUnit</li>
 * <li>Task must be using a forked VM (so vm args can be passed)</li>
 * </ul>
 */
public class CoverageTask extends AbstractCoverageTask
		implements TaskContainer {

	private final Collection<TaskEnhancer> taskEnhancers = new ArrayList<TaskEnhancer>();
	private Task childTask;

	/**
	 * Creates a new default coverage task
	 */
	public CoverageTask() {
		super();
		taskEnhancers.add(new JavaLikeTaskEnhancer("java"));
		taskEnhancers.add(new JavaLikeTaskEnhancer("junit"));
		taskEnhancers.add(new TestNGTaskEnhancer("testng"));
	}

	/**
	 * Add child task to this container and reconfigure it to run with coverage
	 * enabled
	 */
	public void addTask(final Task task) {
		if (childTask != null) {
			throw new BuildException(
					"Only one child task can be supplied to the coverge task",
					getLocation());
		}

		this.childTask = task;

		final String subTaskTypeName = task.getTaskType();

		final TaskEnhancer enhancer = findEnhancerForTask(subTaskTypeName);
		if (enhancer == null) {
			throw new BuildException(
					format("%s is not a valid child of the coverage task",
							subTaskTypeName),
					getLocation());
		}

		if (isEnabled()) {
			log(format("Enhancing %s with coverage", childTask.getTaskName()));
			enhancer.enhanceTask(task);
		}

		task.maybeConfigure();
	}

	private TaskEnhancer findEnhancerForTask(final String taskName) {
		for (final TaskEnhancer enhancer : taskEnhancers) {
			if (enhancer.supportsTask(taskName)) {
				return enhancer;
			}
		}

		return null;
	}

	/**
	 * Executes subtask and performs any required cleanup
	 */
	@Override
	public void execute() throws BuildException {
		if (childTask == null) {
			throw new BuildException(
					"A child task must be supplied for the coverage task",
					getLocation());
		}

		childTask.execute();
	}

	/**
	 * Task enhancer for TestNG. TestNG task always run in a forked VM and has
	 * nested jvmargs elements
	 */
	private class TestNGTaskEnhancer extends JavaLikeTaskEnhancer {

		public TestNGTaskEnhancer(final String supportedTaskName) {
			super(supportedTaskName);
		}

		@Override
		public void enhanceTask(final Task task) {
			addJvmArgs(task);
		}

	}

	/**
	 * Basic task enhancer that can handle all 'java like' tasks. That is, tasks
	 * that have a top level fork attribute and nested jvmargs elements
	 */
	private class JavaLikeTaskEnhancer implements TaskEnhancer {

		private final String supportedTaskName;

		public JavaLikeTaskEnhancer(final String supportedTaskName) {
			this.supportedTaskName = supportedTaskName;
		}

		public boolean supportsTask(final String taskname) {
			return taskname.equals(supportedTaskName);
		}

		public void enhanceTask(final Task task) {
			final RuntimeConfigurable configurableWrapper = task
					.getRuntimeConfigurableWrapper();

			final String forkValue = getProject().replaceProperties(
					(String) configurableWrapper.getAttributeMap().get("fork"));

			if (!Project.toBoolean(forkValue)) {
				throw new BuildException(
						"Coverage can only be applied on a forked VM",
						getLocation());
			}

			addJvmArgs(task);
		}

		public void addJvmArgs(final Task task) {
			final UnknownElement el = new UnknownElement("jvmarg");
			el.setTaskName("jvmarg");
			el.setQName("jvmarg");

			final RuntimeConfigurable runtimeConfigurableWrapper = el
					.getRuntimeConfigurableWrapper();
			runtimeConfigurableWrapper.setAttribute("value",
					getLaunchingArgument());

			task.getRuntimeConfigurableWrapper()
					.addChild(runtimeConfigurableWrapper);

			((UnknownElement) task).addChild(el);
		}
	}

	/**
	 * The task enhancer is responsible for potentially reconfiguring a task to
	 * support running with code coverage enabled
	 */
	private interface TaskEnhancer {
		/**
		 * @param taskname
		 *            Task type to enhance
		 * @return <code>true</code> if this enhancer is capable of enhancing
		 *         the requested task type
		 */
		boolean supportsTask(String taskname);

		/**
		 * Attempt to enhance the supplied task with coverage information. This
		 * operation may fail if the task is being executed in the current VM
		 *
		 * @param task
		 *            Task instance to enhance (usually an
		 *            {@link UnknownElement})
		 * @throws BuildException
		 *             Thrown if this enhancer can handle this type of task, but
		 *             this instance can not be enhanced for some reason.
		 */
		void enhanceTask(Task task) throws BuildException;
	}
}
