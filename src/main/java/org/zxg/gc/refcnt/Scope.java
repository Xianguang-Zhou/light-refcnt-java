/*
 * Copyright (c) 2021, Xianguang Zhou <xianguang.zhou@outlook.com>. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zxg.gc.refcnt;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
@WeaverIgnore
public final class Scope implements Closeable {

	private static final ThreadLocal<LinkedList<Scope>> stack = ThreadLocal.withInitial(LinkedList::new);

	private final LinkedList<RefCnt> resources = new LinkedList<>();

	public Scope() {
		stack.get().addFirst(this);
	}

	@SuppressWarnings("resource")
	public static void afterNew(Object obj) {
		if (obj instanceof RefCnt) {
			stack.get().getFirst().resources.addFirst((RefCnt) obj);
		}
	}

	@SuppressWarnings("resource")
	public static void beforeReturn(Object obj) {
		if (obj instanceof RefCnt) {
			RefCnt rc = (RefCnt) obj;
			stack.get().get(1).resources.addFirst(rc);
			rc._incRef();
		}
	}

	@SuppressWarnings("resource")
	public static void afterFieldRead(Object value) {
		if (value instanceof RefCnt) {
			RefCnt rc = (RefCnt) value;
			stack.get().getFirst().resources.addFirst(rc);
			rc._incRef();
		}
	}

	public static void afterFieldGet(Field field, Object value) {
		if (field.getDeclaredAnnotation(WeakRef.class) == null) {
			afterFieldRead(value);
		}
	}

	@Override
	public void close() {
		Scope scope = stack.get().removeFirst();
		assert this == scope;
		for (RefCnt rc : resources) {
			rc._decRef();
		}
	}

	@SuppressWarnings("resource")
	public static void begin() {
		new Scope();
	}

	public static void end() {
		stack.get().getFirst().close();
	}

	public static boolean isAvailable() {
		return !stack.get().isEmpty();
	}
}
