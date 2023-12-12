/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.perf.targets;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * "Game of Life" implementation as a more reference scenario. Obviously the
 * implementation could be more elegant using several classes, but the test
 * runner targets one class only. Also one could think about more efficient
 * implementations which again is not the focus here.
 */
public class Target03 implements Callable<Void> {

	private final int width;

	private final int height;

	private boolean[][] field;

	public Target03(int width, int height) {
		this.width = width;
		this.height = height;
		this.field = createField();
	}

	public Target03() {
		this(64, 64);
	}

	private boolean[][] createField() {
		boolean[][] f = new boolean[height][];
		for (int i = 0; i < height; i++) {
			f[i] = new boolean[width];
		}
		return f;
	}

	public void set(int x, int y, boolean flag) {
		field[wrap(x, width)][wrap(y, height)] = flag;
	}

	public boolean get(int x, int y) {
		return field[wrap(x, width)][wrap(y, height)];
	}

	public void clear() {
		field = createField();
	}

	public void randomFill(long seed, int count) {
		Random r = new Random(seed);
		for (int i = 0; i < count; i++) {
			set(r.nextInt(), r.nextInt(), true);
		}

	}

	public void tick() {
		boolean[][] next = createField();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int n = getNeighbors(x, y);
				if (get(x, y)) {
					next[x][y] = 2 <= n && n <= 3;
				} else {
					next[x][y] = n == 3;
				}
			}
		}
		field = next;
	}

	// Neighbor
	private int getNeighbors(int x, int y) {
		int count = 0;
		if (get(x - 1, y - 1)) {
			count++;
		}
		if (get(x + 0, y - 1)) {
			count++;
		}
		if (get(x + 1, y - 1)) {
			count++;
		}
		if (get(x + 1, y + 0)) {
			count++;
		}
		if (get(x + 1, y + 1)) {
			count++;
		}
		if (get(x + 0, y + 1)) {
			count++;
		}
		if (get(x - 1, y + 1)) {
			count++;
		}
		if (get(x - 1, y + 0)) {
			count++;
		}
		return count;
	}

	private int wrap(int value, int size) {
		int res = value % size;
		if (res < 0) {
			res += size;
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				sb.append(get(x, y) ? 'O' : '.');
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public Void call() throws Exception {
		clear();
		randomFill(123, width * height / 2);
		for (int i = 0; i < 20; i++) {
			tick();
		}
		return null;
	}

	// Demo
	public static void main(String[] args) {
		Target03 t = new Target03(10, 10);
		t.randomFill(123, 20);

		for (int i = 0; i < 10; i++) {
			System.out.println("Generation " + i + ":");
			System.out.println(t);
			t.tick();
		}
	}

}
