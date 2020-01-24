/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Daniel Kraft - initial API and implementation
 *
 *******************************************************************************/

package org.jacoco.core.test.validation.java5.targets;

import java.util.ArrayList;

public class GetterAndSetterTarget {

	private long id;
	private String name;
	private String desc;
	private double value;
	private boolean valid;
	private ArrayList<String> hobbies;

	public GetterAndSetterTarget(long id, String name, String desc,
			double value) {
		this.id = id; // assertFullyCovered()
		this.name = name;
		this.desc = desc;
		this.value = value;
		this.valid = true;
	}

	public String desc() {
		return desc; // assertFullyCovered()
	}

	public long getId() {
		return id; // assertEmpty()
	}

	public String getName() {
		return name; // assertEmpty()
	}

	public String getDesc() {
		return desc; // assertEmpty()
	}

	public double getValue() {
		return value; // assertEmpty()
	}

	public boolean isValid() {
		return valid; // assertFullyCovered()
	}

	public ArrayList<String> getHobbies() {
		return hobbies; // assertEmpty()
	}

	public void setId(long id) {
		this.id = id; // assertEmpty()
	}

	public void setName(String name) {
		this.name = name; // assertEmpty()
	}

	public void setDesc(String desc) {
		this.desc = desc; // assertEmpty()
	}

	public void setInfo(String desc) {
		this.desc = desc; // assertFullyCovered()
	}

	public void setValue(double value) {
		if (value > this.value) {
			this.value = value; // assertFullyCovered()
		}
	}

	public void setValid(boolean valid) {
		this.valid = valid; // assertEmpty()
	}

	public void setHobbies(ArrayList<String> hobbies) {
		this.hobbies = hobbies; // assertEmpty()
	}

	@Override
	public String toString() {
		return String.format("Name: %s, Desc: %s", name, desc); // assertFullyCovered()
	}

	public String getJSON() {
		return String.format("{\"name\":\"%s\",\"desc\":\"%s\"}", name, desc); // assertFullyCovered()
	}

	public static void main(String[] args) {
		GetterAndSetterTarget target = new GetterAndSetterTarget(1,
				"GetterTest", "Test", 1.0);
		target.desc();
		target.getId();
		target.getName();
		target.getDesc();
		target.getValue();
		target.isValid();
		target.setId(2);
		target.setName("new");
		target.setDesc("update");
		target.setInfo("info");
		target.setValue(2.0);
		target.setValid(false);
		target.toString();
		target.getJSON();
	}
}
