/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Miroslav Pojer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jacoco.core.analysis.CoverageBundleMethodFilterConst.*;

public class ClassCondensate {

	private final List<ElementCondensate> condensate = new ArrayList<>();

	// loading members
	private int curlyCounter;
	private String packageName;
	private int topScopeFlag;
	private int scopeFlag;
	private int lastScopeKeywordDetected;
	private int anonCounter;
	private boolean inText;
	private Map<Integer, Object> inDef;
	private boolean traitIsTopParent;
	private Map<Integer, String> latestClassNameComposite;
	private int leftBracketCounter; // used to detect begin of class constructor

	private void resetLoadingMembers() {
		this.condensate.clear();

		this.curlyCounter = 0;
		this.packageName = "";
		this.topScopeFlag = SCOPE_PUBLIC;
		this.scopeFlag = SCOPE_PUBLIC;
		this.lastScopeKeywordDetected = -1;
		this.anonCounter = 1;
		this.inDef = new HashMap<>();
		this.inText = false;
		this.traitIsTopParent = false;
		this.latestClassNameComposite = new HashMap<>();
		this.leftBracketCounter = 0;
	}

	public int size() {
		return this.condensate.size();
	}

	public int getCurlyIndent(int index) {
		return this.condensate.get(index).curlyIndent;
	}

	public int getGroupType(int index) {
		return this.condensate.get(index).groupType;
	}

	public String getElementType(int index) {
		// TODO - is it used?
		return this.condensate.get(index).elementType;
	}

	public String getElementName(int index) {
		return this.condensate.get(index).elementName;
	}

	public int getScope(int index) {
		return this.condensate.get(index).scope;
	}

	/**
	 * Read class source data and convert it in "JaCoCo" condensate form.
	 *
	 * @param filePath
	 *            Path to class file.
	 * @return List of class elements in "JaCoCo" condensate form.
	 */
	public ClassCondensate load(String filePath) {
		this.resetLoadingMembers();

		String[] dataBySpaces = this.formatClassFileForCondensate(filePath)
				.split(" ");
		for (int i = 0; i < dataBySpaces.length; i++) {
			switch (dataBySpaces[i]) {
			// curly brackets counter
			case "{":
				if (!inText)
					curlyCounter++;
				break;
			case "}":
				if (!inText)
					detectedLeftCurlyBracket();
				break;

			// brackets counter
			case "(":
				leftBracketCounter++; // possible start of class constructor
				break;
			case ")":
				leftBracketCounter--; // close condition in case of constructor
										// met
				break;

			// check scope flags
			case "protected":
				scopeFlag = SCOPE_PROTECTED;
				lastScopeKeywordDetected = i;
				break;
			case "private":
				scopeFlag = SCOPE_PRIVATE;
				lastScopeKeywordDetected = i;
				break;

			case "\"":
				inText = !inText;
				break;

			// detect report main objects (class, object, trait, def, val, new)
			case "class":
				if (dataBySpaces.length > (i + 1)) {
					String className = dataBySpaces[i + 1];
					String latestClassNameCompositeStr = getClassNameComposite(
							className, TYPE_CLASS);

					this.add(curlyCounter, GROUP_MEMBER, STYPE_CLASS, className,
							scopeFlag);
					this.add(curlyCounter, GROUP_OBJECT, STYPE_CLASS,
							latestClassNameCompositeStr, scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			case "object":
				if (dataBySpaces.length > (i + 1)) {
					String objectName = dataBySpaces[i + 1];
					String latestClassNameCompositeStr = getClassNameComposite(
							objectName, TYPE_OBJECT, "%s/%s$", "$%s$",
							"%s/package$%s$");
					if (scopeFlag == SCOPE_PACKAGE)
						latestClassNameCompositeStr = getClassNameComposite(
								objectName, TYPE_OBJECT, "%s/%s$", "$%s$",
								"%s/package$");

					this.add(curlyCounter, GROUP_MEMBER, STYPE_OBJECT,
							objectName, scopeFlag);
					this.add(curlyCounter, GROUP_OBJECT, STYPE_OBJECT,
							latestClassNameCompositeStr, scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			case "trait":
				if (dataBySpaces.length > (i + 1)) {
					String traitName = dataBySpaces[i + 1];
					String latestClassNameCompositeStr = getClassNameComposite(
							traitName, TYPE_TRAIT);

					this.add(curlyCounter, GROUP_MEMBER, STYPE_TRAIT, traitName,
							scopeFlag);
					this.add(curlyCounter, GROUP_OBJECT, STYPE_TRAIT, String
							.format("%s$class", latestClassNameCompositeStr),
							scopeFlag);
					this.add(curlyCounter, GROUP_OBJECT, STYPE_TRAIT,
							latestClassNameCompositeStr, scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			case "def":
				if (dataBySpaces.length > (i + 1)) {
					// Add member first to keep object->members hierarchy
					String defName = solveSignMethodNames(dataBySpaces[i + 1]);

					if (scopeFlag == SCOPE_PRIVATE) {
						defName = composeMemberNameComposite(defName);
						this.add(curlyCounter, GROUP_MEMBER, STYPE_DEV, defName,
								scopeFlag);
					}

					if (inDef.size() > 0) {
						if ((scopeFlag == SCOPE_PRIVATE) || traitIsTopParent) {
							this.add(curlyCounter, GROUP_MEMBER, STYPE_DEV,
									String.format("%s$1", defName), scopeFlag);
						} else {
							defName = composeMemberNameComposite(defName);
							// why adding $1 - not expected to detect def in def
							// in def for now
							this.add(curlyCounter, GROUP_MEMBER, STYPE_DEV,
									String.format("%s$1", defName), scopeFlag);
						}
					}

					this.add(curlyCounter, GROUP_MEMBER, STYPE_DEV,
							solveSignMethodNames(dataBySpaces[i + 1]),
							scopeFlag);
					this.add(curlyCounter, GROUP_OBJECT, STYPE_DEV, defName,
							scopeFlag);

					trySetPublicScope(i,
							solveSignMethodNames(dataBySpaces[i - 1]));

					inDef.put(curlyCounter, null);
				}
				break;
			case "val":
				// possible weakness when val in class constructor - it is then
				// not used part of class condensate
				if (dataBySpaces.length > (i + 1)) {
					String defName = dataBySpaces[i + 1];

					// Add member first to keep object->members hierarchy
					int localCurlyCounter = curlyCounter;
					if (leftBracketCounter == 1) // meet val usage in class
													// constructor
						localCurlyCounter++;

					if ((scopeFlag == SCOPE_PRIVATE) && (!traitIsTopParent))
						defName = composeMemberNameComposite(defName);

					this.add(localCurlyCounter, GROUP_MEMBER, STYPE_VAL,
							defName, scopeFlag);
					this.add(localCurlyCounter, GROUP_MEMBER, STYPE_VAL,
							dataBySpaces[i + 1], scopeFlag);
					this.add(localCurlyCounter, GROUP_OBJECT, STYPE_VAL,
							defName, scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			case "var":
				// possible weakness when val in class constructor - it is then
				// not used part of class condensate
				if (dataBySpaces.length > (i + 1)) {
					String defName = dataBySpaces[i + 1];

					// Add member first to keep object->members hierarchy
					int localCurlyCounter = curlyCounter;
					if (leftBracketCounter == 1) // meet val usage in class
													// constructor
						localCurlyCounter++;

					if ((scopeFlag == SCOPE_PRIVATE) && (!traitIsTopParent))
						defName = composeMemberNameComposite(defName);

					this.add(localCurlyCounter, GROUP_MEMBER, STYPE_VAR,
							defName, scopeFlag);
					this.add(localCurlyCounter, GROUP_MEMBER, STYPE_VAR,
							dataBySpaces[i + 1], scopeFlag);
					this.add(localCurlyCounter, GROUP_OBJECT, STYPE_VAR,
							defName, scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			case "new":
				// possible weakness - anon generated on object in apply - not
				// required by Jacoco - Is it a problem?
				String latestClassNameCompositeStr = String
						.join("", latestClassNameComposite.values())
						.replace("$$", "$");
				String anonName = String.format("%s$$anon$%d",
						latestClassNameCompositeStr, anonCounter)
						.replace("$$$", "$$");

				this.add(curlyCounter, GROUP_MEMBER, STYPE_ANON, anonName,
						scopeFlag);
				this.add(curlyCounter, GROUP_OBJECT, STYPE_ANON, anonName,
						scopeFlag);

				trySetPublicScope(i, dataBySpaces[i - 1]);
				anonCounter += 1;
				break;

			// check package (real package path or scope)
			case "package":
				if (dataBySpaces.length > (i + 1)) {
					if ("object".equals(dataBySpaces[i + 1])) {
						scopeFlag = SCOPE_PACKAGE;
						lastScopeKeywordDetected = i;
					} else {
						packageName = dataBySpaces[i + 1].replace(".", "/");
					}
				}
				break;
			case "enum":
				if (dataBySpaces.length > (i + 1)) {
					String enumName = dataBySpaces[i + 1];
					this.add(curlyCounter, GROUP_OBJECT, STYPE_ENUM, enumName,
							scopeFlag);

					trySetPublicScope(i, dataBySpaces[i - 1]);
				}
				break;
			}
		}

		return this;
	}

	private String formatClassFileForCondensate(String filePath) {
		try {
			Stream<String> lines = Files.lines(Paths.get(filePath));
			String data = lines.filter((l) -> !l.trim().startsWith("*"))
					.filter((l) -> !l.trim().startsWith("/*"))
					.filter((l) -> !l.trim().startsWith("//"))
					.map((l) -> l.replace("class ", " class "))
					.map((l) -> l.replace("object ", " object "))
					.map((l) -> l.replace("trait ", " trait "))
					.map((l) -> l.replace("def ", " def "))
					.map((l) -> l.replace("val ", " val "))
					.map((l) -> l.replace("* {{{", "*")) // expected that list
															// of { in
															// strings/comments
															// is much wider
					.map((l) -> l.replace("* }}}", "*"))
					.map((l) -> l.replace("\"\"", " Y ")) // '""' -> ' Y '
					.map((l) -> l.replace("\\\\", " Z ")) // '\\' -> ' Z '
					.map((l) -> l.replace("\\\"", "Y")) // '\"' -> 'Y'
					.map((l) -> l.replace("}", " } "))
					.map((l) -> l.replace("{", " { "))
					.map((l) -> l.replace("(", " ( "))
					.map((l) -> l.replace(")", " ) "))
					.map((l) -> l.replace("[", " [ "))
					.map((l) -> l.replace(":", " : "))
					.map((l) -> l.replace("\"", " \" "))
					.map((l) -> l.replace("=>", " => "))
					.map((l) -> l.replace("package", " package "))
					.map((l) -> l.split("\n")[0] + " \n")
					.map((l) -> l.replace("   ", " "))
					.map((l) -> l.replace("  ", " "))
					.collect(Collectors.joining("\n"));

			lines.close();
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void detectedLeftCurlyBracket() {
		// remove known in def indent
		inDef.remove(curlyCounter);
		curlyCounter--;

		for (Iterator<Map.Entry<Integer, String>> it = latestClassNameComposite
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Integer, String> entry = it.next();
			if (curlyCounter <= entry.getKey()) {
				it.remove();
			}
		}
	}

	/**
	 * Fix situation when some object does not have curly brackets. When zero
	 * indent reached that clear of name composite is needed.
	 */
	private void resetToZeroIndent() {
		if (curlyCounter == 0) {
			if (latestClassNameComposite.size() == 1) {
				latestClassNameComposite.clear();
			}
			topScopeFlag = scopeFlag;
		}
	}

	private String composeClassNameComposite(String objectName,
			String scopePackagePattern) {
		// generic
		String latestClassNameCompositeStr = String
				.join("", latestClassNameComposite.values()).replace("$$", "$");

		if (topScopeFlag == SCOPE_PACKAGE) {
			// override common logic when package scope on top parent
			latestClassNameCompositeStr = String.format(scopePackagePattern,
					latestClassNameComposite.get(0).replaceAll(".$", ""),
					objectName);
		}

		return latestClassNameCompositeStr;
	}

	private String composeMemberNameComposite(String objName) {
		String latestMethodNameCompositeStr = String
				.join("", latestClassNameComposite.values()).replace("$$", "$");

		String newObjName = latestMethodNameCompositeStr.replace("/", "$")
				+ "$$" + objName;
		if (newObjName.contains("$$$"))
			newObjName = newObjName.replace("$$$", "$$");

		return newObjName;
	}

	private String solveSignMethodNames(String defName) {
		if (defName.equals("+")) {
			return "$plus";
		}
		if (defName.equals("*")) {
			return "$times";
		}
		if (defName.equals("unary_!")) {
			return "unary_$bang";
		} else
			return defName;
	}

	private String getClassNameComposite(String objName, int objType) {
		return getClassNameComposite(objName, objType, "%s/%s", "$%s",
				"%s/package$%s");
	}

	private String getClassNameComposite(String objName, int objType,
			String updateFirstPattern, String updateNextPattern,
			String scopePackagePattern) {
		resetToZeroIndent(); // if curlyCounter == 0 >> evaluated inside
		updateLatestClassName(objName, updateFirstPattern, updateNextPattern,
				objType);
		return composeClassNameComposite(objName, scopePackagePattern);
	}

	private void updateLatestClassName(String objectName, String firstPattern,
			String nextPattern, int typeObject) {
		if (latestClassNameComposite.size() == 0) {
			latestClassNameComposite.put(curlyCounter,
					String.format(firstPattern, packageName, objectName));
			traitIsTopParent = typeObject == TYPE_TRAIT;
		} else {
			latestClassNameComposite.put(curlyCounter,
					String.format(nextPattern, objectName));
		}
	}

	private void trySetPublicScope(int index, String dataMinusOne) {
		int shift = 0;
		if (dataMinusOne.equals("lazy")) // only valis for val - in time of
											// writing
			shift = 1;

		if (lastScopeKeywordDetected != (index - 1 - shift))
			scopeFlag = SCOPE_PUBLIC;
	}

	private void add(int curlyIndent, int groupType, String elementType,
			String elementName, int scope) {
		this.condensate.add(new ElementCondensate(curlyIndent, groupType,
				elementType, elementName, scope));
	}

	public String asString() {
		String result = this.condensate.stream().map(String::valueOf)
				.collect(Collectors.joining("\n"));

		return result;
	}

	private class ElementCondensate {

		ElementCondensate(int curlyIndent, int groupType, String elementType,
				String elementName, int scope) {
			this.curlyIndent = curlyIndent;
			this.groupType = groupType;
			this.elementType = elementType;
			this.elementName = elementName;
			this.scope = scope;
		}

		int curlyIndent;
		int groupType;
		String elementType;
		String elementName;
		int scope;

		public String toString() {
			return String.format("%d:%d:%s:%s:%d", curlyIndent, groupType,
					elementType, elementName, scope);
		}

	}
}
