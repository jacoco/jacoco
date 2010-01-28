/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
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
 * Container task to run Java/Junit tasks with the JaCoCo agent jar. Coverage
 * will only be applied if all of the following are true:
 * <ul>
 * <li>Exactly one sub task may be present</li>
 * <li>Task must be either Java or JUnit</li>
 * <li>Task must be using a forked VM (so vm args can be passed)</li>
 * </ul>
 * 
 * @ant.task category="java"
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class CoverageTask extends AbstractCoverageTask implements TaskContainer {

	private final Collection<TaskEnhancer> taskEnhancers = new ArrayList<TaskEnhancer>();
	private Task childTask;

	/**
	 * Creates a new default coverage task
	 */
	public CoverageTask() {
		taskEnhancers.add(new JavaLikeTaskEnhancer("java"));
		taskEnhancers.add(new JavaLikeTaskEnhancer("junit"));
	}

	/**
	 * Add child task to this container and reconfigure it to run with coverage
	 * enabled
	 */
	public void addTask(final Task task) {
		if (childTask != null) {
			throw new BuildException(
					"Only one child task can be supplied to the coverge task");
		}

		this.childTask = task;

		final UnknownElement unknownElement = (UnknownElement) task;

		final String subTaskTypeName = unknownElement.getTaskType();

		for (final TaskEnhancer enhancer : taskEnhancers) {
			if (enhancer.supportsTask(subTaskTypeName)) {
				enhancer.enhanceTask(task);
				return;
			}
		}

		throw new BuildException(format(
				"%s is not a valid child of the coverage task.",
				subTaskTypeName));

	}

	/**
	 * Executes subtask and performs any required cleanup
	 */
	@Override
	public void execute() throws BuildException {
		if (childTask == null) {
			throw new BuildException(
					"A child task must be supplied for the coverage task");
		}
		if (isEnabled()) {
			log(format("Enhancing %s with coverage.", childTask.getTaskName()));
			childTask.execute();
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

			final String forkValue = (String) configurableWrapper
					.getAttributeMap().get("fork");

			if (forkValue == null || !Project.toBoolean(forkValue)) {
				throw new BuildException(
						"Coverage can only be applied on a forked VM");
			}

			addJvmArgs((UnknownElement) task);
			task.maybeConfigure();
		}

		public void addJvmArgs(final UnknownElement task) {
			final UnknownElement el = new UnknownElement("jvmarg");
			el.setTaskName("jvmarg");
			el.setQName("jvmarg");

			final RuntimeConfigurable runtimeConfigurableWrapper = el
					.getRuntimeConfigurableWrapper();
			runtimeConfigurableWrapper.setAttribute("value",
					getLaunchingArgument());

			task.getRuntimeConfigurableWrapper().addChild(
					runtimeConfigurableWrapper);

			task.addChild(el);
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
		 * @return <code>true</code> iff this enhancer is capable of enhacing
		 *         the requested task type
		 */
		public boolean supportsTask(String taskname);

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
		public void enhanceTask(Task task) throws BuildException;
	}
}
