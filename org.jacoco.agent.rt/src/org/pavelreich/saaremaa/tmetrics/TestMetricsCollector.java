/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *******************************************************************************/
package org.pavelreich.saaremaa.tmetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IterableCodec;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.jacoco.agent.rt.internal.IExceptionLogger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * load list of prod/test classes to analyse from jacoco-classes.txt or
 * classes.txt
 *
 * collect test metrics:
 * <ul>
 * <li>TDATA new objects of relevant classes created</li>
 * <li>TINVOK invocations of prod/test classes.</li>
 * <li>TASSERT calls to org.junit.Assert. TODO: add hamcrest, etc.</li>
 * <li>TMockField Mock fields</li>
 * <li>TMockOperation Mockito operations</li>
 * </ul>
 *
 * dump results to jacoco-result.json along the way log to jacoco.log
 *
 * @author preich
 *
 */
public class TestMetricsCollector {
	private static final Collection<TestingArtifact> occurences = new LinkedHashSet<TestingArtifact>();
	private static final int ASM_VERSION = Opcodes.ASM7;

	private static PrintWriter printWriter = new PrintWriter(System.out);

	private static final Set<String> relevantClasses = new HashSet<String>();
	private static String baseFileName;

	private static void loadClasses(final File f) {

		if (f.exists()) {
			Scanner scnr;
			try {
				scnr = new Scanner(new FileInputStream(f));
				int i = 0;
				while (scnr.hasNextLine()) {
					final String name = scnr.nextLine();
					relevantClasses.add(name.trim());
					i++;
				}
				scnr.close();
				printWriter.println("Loaded " + i + " classes from " + f);
			} catch (final Exception e) {
				e.printStackTrace(printWriter);
			}
		}
	}

	public static void dumpTestingArtifacts() throws FileNotFoundException {
		final PrintWriter jsonWriter = new PrintWriter(
				baseFileName.replaceAll(".exec", "-result.json"));
		final JsonWriterSettings writerSettings = new JsonWriterSettings(
				JsonMode.SHELL, true);
		final List<Document> docs = new ArrayList<Document>();
		for (final TestingArtifact s : occurences) {
			docs.add(s.toDocument());
		}

		final BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap();
		final CodecRegistry registry = CodecRegistries.fromProviders(
				new ValueCodecProvider(), new DocumentCodecProvider(),
				new BsonValueCodecProvider(), new IterableCodecProvider());
		final IterableCodec codec = new IterableCodec(registry,
				bsonTypeClassMap);
		final BsonWriter bsonWriter = new JsonWriter(jsonWriter,
				writerSettings);
		bsonWriter.writeStartDocument();
		bsonWriter.writeStartArray("results");
		codec.encode(bsonWriter, docs, EncoderContext.builder().build());
		bsonWriter.writeEndArray();
		bsonWriter.writeEndDocument();

		bsonWriter.flush();
		jsonWriter.close();
	}

	/**
	 * provide ClassVisitor that collects test metrics and delegates to
	 * nextVisitor.
	 *
	 * @param nextVisitor
	 * @param jacocoDestFilename
	 * @param exceptionLogger
	 * @return
	 */
	public static ClassVisitor provideClassVisitor(
			final ClassVisitor nextVisitor, final String jacocoDestFilename,
			final IExceptionLogger exceptionLogger) {
		try {
			TestMetricsCollector.baseFileName = jacocoDestFilename;
			final String logFname = jacocoDestFilename.replaceAll(".exec",
					".log");
			printWriter = new PrintWriter(new File(logFname));
			loadClasses(new File(
					jacocoDestFilename.replaceAll(".exec", "-classes.txt")));
			loadClasses(new File("classes.txt"));
		} catch (final Exception e) {
			exceptionLogger.logExeption(e);
		}
		return new TestObservingClassVisitor(nextVisitor);
	}

	private static boolean isClassRelevant(final String ownerClassName) {
		final String packageName = TestMetricsCollector.class.getPackage()
				.getName();
		final String replacedName = ownerClassName.replace('/', '.');
		final boolean isClassRelevant = replacedName.startsWith(packageName);
		final String withoutInnerClass = replacedName.replaceAll("\\$.*", "");
		return isClassRelevant || relevantClasses.contains(withoutInnerClass);
	}

	public static class TestingArtifact {
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		public Document toDocument() {
			final Document d = new Document("type", getClass().getName())
					.append("metricType", getClass().getSimpleName());
			return d;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof TestingArtifact) {
				return EqualsBuilder.reflectionEquals(toDocument(),
						((TestingArtifact) obj).toDocument());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(toDocument());
		}

	}

	static class TData extends TestingArtifact {
		private final SourceLocation sourceLocation;
		private final String type;

		public TData(final SourceLocation sourceLocation, final String type) {
			this.sourceLocation = sourceLocation;
			this.type = type;
		}

		@Override
		public Document toDocument() {
			return super.toDocument()
					.append("sourceLocation", sourceLocation.toDocument())
					.append("dataType", type);
		}
	}

	static class TAssert extends TestingArtifact {

		private final SourceLocation sourceLocation;
		private final TargetLocation targetLocation;

		public TAssert(final SourceLocation sourceLocation,
				final TargetLocation targetLocation) {
			this.sourceLocation = sourceLocation;
			this.targetLocation = targetLocation;

		}

		@Override
		public Document toDocument() {
			return super.toDocument()
					.append("sourceLocation", sourceLocation.toDocument())
					.append("targetLocation", targetLocation.toDocument());
		}
	}

	static class TInvok extends TestingArtifact {

		private final SourceLocation sourceLocation;
		private final TargetLocation targetLocation;
		private final int opcode;

		public TInvok(final SourceLocation sourceLocation,
				final TargetLocation targetLocation, final int opcode) {
			this.sourceLocation = sourceLocation;
			this.targetLocation = targetLocation;
			this.opcode = opcode;
		}

		@Override
		public Document toDocument() {
			return super.toDocument()
					.append("sourceLocation", sourceLocation.toDocument())
					.append("targetLocation", targetLocation.toDocument())
					.append("opcode", opcode);
		}
	}

	static class TMockField extends TestingArtifact {

		private final VisitClassRecord visitClassRecord;
		private final VisitFieldRecord visitFieldRecord;

		public TMockField(final VisitClassRecord visitClassRecord,
				final VisitFieldRecord visitFieldRecord) {
			this.visitClassRecord = visitClassRecord;
			this.visitFieldRecord = visitFieldRecord;
		}

		@Override
		public Document toDocument() {
			return super.toDocument().append("className", visitClassRecord.name)
					.append("fieldName", visitFieldRecord.name);
		}
	}

	static class TMockOperation extends TestingArtifact {

		private final SourceLocation sourceLocation;
		private final TargetLocation targetLocation;

		public TMockOperation(final SourceLocation sourceLocation,
				final TargetLocation targetLocation) {
			this.sourceLocation = sourceLocation;
			this.targetLocation = targetLocation;
		}

		@Override
		public Document toDocument() {
			return super.toDocument()
					.append("sourceLocation", sourceLocation.toDocument())
					.append("targetLocation", targetLocation.toDocument());
		}

	}

	static class SourceLocation {

		private final VisitClassRecord visitClassRecord;
		private final VisitMethodRecord visitMethodRecord;
		private final int currentLine;

		public SourceLocation(final VisitClassRecord visitClassRecord,
				final VisitMethodRecord visitMethodRecord,
				final int currentLine) {
			this.visitClassRecord = visitClassRecord;
			this.visitMethodRecord = visitMethodRecord;
			this.currentLine = currentLine;
		}

		@Override
		public String toString() {
			return visitClassRecord.name + ":" + visitMethodRecord.methodName
					+ ":" + currentLine;
		}

		public Document toDocument() {
			return new Document("type", getClass().getName())
					.append("className", visitClassRecord.name)
					.append("methodName", visitMethodRecord.methodName)
					.append("line", currentLine);
		}
	}

	static class TargetLocation {

		private final String className;
		private final String methodName;

		public TargetLocation(final String ownerClassName,
				final String methodName) {
			this.className = ownerClassName;
			this.methodName = methodName;
		}

		@Override
		public String toString() {
			return className + ":" + methodName;
		}

		public Document toDocument() {
			return new Document("type", getClass().getName())
					.append("className", className)
					.append("methodName", methodName);
		}
	}

	static class TestObservingMethodVisitor extends MethodVisitor {

		private static final Set<String> ASSERT_CLASSES = new HashSet<String>(
				Arrays.asList("org.junit.Assert", "junit.framework.Assert"));
		private final VisitClassRecord visitClassRecord;
		private final VisitMethodRecord visitMethodRecord;
		private int currentLine;

		public TestObservingMethodVisitor(
				final VisitClassRecord visitClassRecord,
				final VisitMethodRecord visitMethodRecord,
				final MethodVisitor nextVisitor) {
			super(ASM_VERSION, nextVisitor);
			this.visitClassRecord = visitClassRecord;
			this.visitMethodRecord = visitMethodRecord;
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner,
				final String methodName, final String descriptor,
				final boolean isInterface) {
			printWriter.println(toString() + ". opcode: " + opcode + ", owner: "
					+ owner + ", name: " + methodName);
			// TODO: add hamcrest, etc.
			final String ownerClassName = owner.replace('/', '.');
			if (ASSERT_CLASSES.contains(ownerClassName)) {
				occurences.add(new TAssert(getSourceLocation(),
						new TargetLocation(ownerClassName, methodName)));
			}
			if ("org.mockito.Mockito".equals(ownerClassName)) {
				occurences.add(new TMockOperation(getSourceLocation(),
						new TargetLocation(ownerClassName, methodName)));
			}
			final boolean isClassRelevant = isClassRelevant(ownerClassName);
			if (isClassRelevant) {
				if (opcode == Opcodes.INVOKEVIRTUAL
						|| opcode == Opcodes.INVOKESTATIC) {
					final SourceLocation sourceLocation = getSourceLocation();
					final TargetLocation targetLocation = new TargetLocation(
							ownerClassName, methodName);
					occurences.add(
							new TInvok(sourceLocation, targetLocation, opcode));
				}
			}
			super.visitMethodInsn(opcode, ownerClassName, methodName,
					descriptor, isInterface);
		}

		protected SourceLocation getSourceLocation() {
			final SourceLocation sourceLocation = new SourceLocation(
					visitClassRecord, visitMethodRecord, currentLine);
			return sourceLocation;
		}

		@Override
		public String toString() {
			return visitClassRecord.name + "::" + visitMethodRecord.methodName;
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
			this.currentLine = line;
			super.visitLineNumber(line, start);
		}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {
			if (isClassRelevant(type) && opcode == Opcodes.NEW) {
				occurences.add(new TData(getSourceLocation(), type));
			}
			printWriter.println(
					"visitTypeInsn:opcode=" + opcode + ", type= " + type);
			super.visitTypeInsn(opcode, type);
		}

	}

	static class TestObservingFieldVisitor extends FieldVisitor {

		private final VisitClassRecord visitClassRecord;
		private final VisitFieldRecord visitFieldRecord;

		public TestObservingFieldVisitor(
				final VisitClassRecord visitClassRecord,
				final VisitFieldRecord visitFieldRecord,
				final FieldVisitor fieldVisitor) {
			super(ASM_VERSION, fieldVisitor);
			this.visitClassRecord = visitClassRecord;
			this.visitFieldRecord = visitFieldRecord;
		}

		@Override
		public AnnotationVisitor visitAnnotation(final String descriptor,
				final boolean visible) {
			if (descriptor.contains("org/mockito/Mock")) {
				occurences.add(
						new TMockField(visitClassRecord, visitFieldRecord));
			}
			return super.visitAnnotation(descriptor, visible);
		}
	}

	public static class TestObservingClassVisitor extends ClassVisitor {
		private VisitClassRecord visitClassRecord;

		public TestObservingClassVisitor(final ClassVisitor nextVisitor) {
			super(ASM_VERSION, nextVisitor);
		}

		@Override
		public FieldVisitor visitField(final int access, final String name,
				final String descriptor, final String signature,
				final Object value) {
			return new TestObservingFieldVisitor(visitClassRecord,
					new VisitFieldRecord(access, name, descriptor, signature,
							value),
					super.visitField(access, name, descriptor, signature,
							value));
		}

		@Override
		public AnnotationVisitor visitAnnotation(final String descriptor,
				final boolean visible) {
			return super.visitAnnotation(descriptor, visible);
		}

		@Override
		public void visit(final int version, final int access,
				final String name, final String signature,
				final String superName, final String[] interfaces) {
			this.visitClassRecord = new VisitClassRecord(version, access, name,
					signature, superName, interfaces);
			super.visit(version, access, name, signature, superName,
					interfaces);
		}

		@Override
		public void visitEnd() {
			this.visitClassRecord = null;
			super.visitEnd();
		}

		@Override
		public MethodVisitor visitMethod(final int access,
				final String methodName, final String descriptor,
				final String signature, final String[] exceptions) {
			final MethodVisitor nextVisitor = super.visitMethod(access,
					methodName, descriptor, signature, exceptions);
			final VisitMethodRecord visitMethodRecord = new VisitMethodRecord(
					access, methodName, descriptor, signature, exceptions);
			return new TestObservingMethodVisitor(visitClassRecord,
					visitMethodRecord, nextVisitor);
		}

	}

	static class VisitRecord {
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	static class VisitClassRecord extends VisitRecord {
		int version;
		int access;
		String name;
		String signature;
		String superName;
		String[] interfaces;

		public VisitClassRecord(final int version, final int access,
				final String name, final String signature,
				final String superName, final String[] interfaces) {
			this.version = version;
			this.access = access;
			this.name = name;
			this.signature = signature;
			this.superName = superName;
			this.interfaces = interfaces;
		}

	}

	static class VisitFieldRecord extends VisitRecord {
		int access;
		String name;
		String descriptor;
		String signature;
		Object value;

		public VisitFieldRecord(final int access, final String name,
				final String descriptor, final String signature,
				final Object value) {
			this.access = access;
			this.name = name;
			this.descriptor = descriptor;
			this.signature = signature;
			this.value = value;
		}

	}

	static class VisitMethodRecord extends VisitRecord {
		int access;
		String methodName;
		String descriptor;
		String signature;
		String[] exceptions;

		public VisitMethodRecord(final int access, final String methodName,
				final String descriptor, final String signature,
				final String[] exceptions) {
			this.access = access;
			this.methodName = methodName;
			this.descriptor = descriptor;
			this.signature = signature;
			this.exceptions = exceptions;
		}

	}
}
