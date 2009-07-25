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

import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.CommandlineJava;

/**
 * Container task to run Java/Junit tasks with the JaCoCo agent jar. Coverage
 * will only be applied if all of the following are true:
 * <ul>
 * <li>Exactly one sub task may be present</li>
 * <li>Task must be either Java or JUnit</li>
 * <li>Task must be using a forked VM (so vm args can be passed)</li>
 * </ul>
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class CoverageTask extends AbstractCoverageTask implements TaskContainer {

	private Task childTask;

	/**
	 * Creates a new default coverage task
	 */
	public CoverageTask() {

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

		this.childTask = realizeTask(task);
		final boolean isValidChild = isValidChild(childTask);
		if (!isValidChild) {
			throw new BuildException(task.getTaskName()
					+ " is not a valid child of the coverage task");
		}

		addCoverageArguments(childTask);
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

		childTask.perform();
	}

	/**
	 * Appends required JVM args to enable coverage with the JaCoCo agent jar
	 * 
	 * @param task
	 *            Task to add coverage arguments to. Task must be fully realised
	 */
	private void addCoverageArguments(final Task task) {
		final CommandlineJava commandline = getTaskCommandline(task);

		final JvmArgumentHelper jvmArgumentHelper = new JvmArgumentHelper();
		final String agentParam = jvmArgumentHelper
				.createJavaAgentParam(getAgentOptions());

		commandline.createVmArgument().setValue(agentParam);

		log("Enhancing " + task.getTaskName() + " with coverage");
	}

	/**
	 * Configure child task and return real task instead of UnknownElement to
	 * make configuration easier
	 * 
	 * @param task
	 *            Uninitialised sub task
	 * @return Configured subtask
	 */
	private Task realizeTask(final Task task) {
		task.maybeConfigure();

		return ((UnknownElement) task).getTask();
	}

	/**
	 * Determines if the sub task is a valid child of this task. Valid tasks are
	 * those that expose a VM Arguments property in some way
	 * 
	 * @param task
	 *            Task to check
	 * @return <code>true</code> if the task can be executed with coverage
	 *         enabled
	 */
	private boolean isValidChild(final Task task) {
		return getTaskCommandline(task) != null;
	}

	/**
	 * Retrieve the Java command line of the sub task
	 * 
	 * @param task
	 *            Task to get command line for
	 * @return Java command line or <code>null</code> if no property returning
	 *         {@link CommandlineJava} can be found
	 */
	private CommandlineJava getTaskCommandline(final Task task) {
		if (task instanceof Java) {
			return ((Java) task).getCommandLine();
		}

		return getCommandlineWithReflection(task);
	}

	/**
	 * Helper method for getting the {@link CommandlineJava} object from a task
	 * where ir does not expose a public accessor
	 * 
	 * @param task
	 *            Task to get command line for
	 * @return Java command line or <code>null</code> if no property returning
	 *         {@link CommandlineJava} can be found
	 */
	private CommandlineJava getCommandlineWithReflection(final Task task) {
		final Method[] declaredMethods = task.getClass().getDeclaredMethods();

		for (final Method method : declaredMethods) {
			try {
				final boolean returnsCommandLine = (method.getReturnType() == CommandlineJava.class);
				final boolean hasNoParams = (method.getParameterTypes().length == 0);

				if (returnsCommandLine && hasNoParams) {
					method.setAccessible(true);

					return (CommandlineJava) method.invoke(task);
				}
			} catch (final Exception e) {
			}
		}

		return null;
	}

}