package org.jacoco.core.runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.IExecutionDataOutput;
import org.jacoco.core.instr.GeneratorConstants;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This {@link IRuntime} implementation places the coverage data in the
 * {@link System#getProperties()} hash table. The advantage is, that the
 * instrumented classes do not get dependencies to other classes than the JRE
 * library itself.
 * 
 * This runtime may cause problems in environments with security restrictions,
 * in applications that replace the system properties or in applications that
 * fail if non-String values are placed in the system properties.
 * 
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SystemPropertiesRuntime implements IRuntime {

	private static final String KEYPREFIX = "jacoco-";

	private final String key;

	/**
	 * Creates a new runtime with the given id. The id helps to separate
	 * different runtime instances. The instrumentation and the target VM must
	 * be based on a runtime with the same id.
	 * 
	 * @param id
	 *            Identifier for the runtime
	 */
	public SystemPropertiesRuntime(int id) {
		this.key = KEYPREFIX + Integer.toHexString(id);
	}

	/**
	 * Creates a new runtime with a random identifier.
	 */
	public SystemPropertiesRuntime() {
		this.key = KEYPREFIX + hashCode();
	}

	// TODO: lokale Variable vermeiden (swap!)
	public void generateRegistration(long classId, GeneratorAdapter gen) {

		// boolean[][] data = pop()
		final int data = gen.newLocal(GeneratorConstants.DATAFIELD_TYPE);
		gen.storeLocal(data);

		// Properties stack := System.getProperties()
		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
				"getProperties", "()Ljava/util/Properties;");

		// gen.swap();

		// Map stack := stack.get(key)
		gen.push(key);
		gen.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties",
				"get", "(Ljava/lang/Object;)Ljava/lang/Object;");
		gen.visitTypeInsn(Opcodes.CHECKCAST, "java/util/Map");

		// stack.put(classId, data)
		gen.push(classId);
		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");
		// gen.swap();
		gen.loadLocal(data);
		gen.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put",
				"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
		gen.pop();
	}

	public void startup() {
		final Map<Long, boolean[][]> dataMap = Collections
				.synchronizedMap(new HashMap<Long, boolean[][]>());
		System.getProperties().put(key, dataMap);
	}

	@SuppressWarnings("unchecked")
	private Map<Long, boolean[][]> getDataMap() {
		final Object object = System.getProperties().get(key);
		if (object == null) {
			throw new IllegalStateException("Runtime not started.");
		}
		return (Map<Long, boolean[][]>) object;
	}

	public void collect(IExecutionDataOutput output, boolean reset) {
		final Map<Long, boolean[][]> dataMap = getDataMap();
		synchronized (dataMap) {
			for (Map.Entry<Long, boolean[][]> entry : dataMap.entrySet()) {
				final long classId = entry.getKey().longValue();
				final boolean[][] blockData = entry.getValue();
				output.classExecution(classId, blockData);
			}
			if (reset) {
				reset();
			}
		}
	}

	public void reset() {
		final Map<Long, boolean[][]> dataMap = getDataMap();
		synchronized (dataMap) {
			for (boolean[][] data : dataMap.values()) {
				for (boolean[] arr : data) {
					Arrays.fill(arr, false);
				}
			}
		}
	}

	public void shutdown() {
		System.getProperties().remove(key);
	}

}
