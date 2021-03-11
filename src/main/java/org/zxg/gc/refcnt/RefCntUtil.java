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

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
@WeaverIgnore
public final class RefCntUtil {

	public static volatile boolean printExceptions = true;

	public static void beforeFieldWrite(Object oldValue, Object newValue) {
		if (newValue instanceof RefCnt) {
			((RefCnt) newValue)._incRef();
		}
		if (oldValue instanceof RefCnt) {
			((RefCnt) oldValue)._decRef();
		}
	}

	public static void beforeFieldSet(Object obj, Field field, Object value) {
		if (field.getDeclaredAnnotation(WeakRef.class) == null) {
			try {
				beforeFieldWrite(field.get(obj), value);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void decRef(Object obj) {
		if (obj instanceof RefCnt) {
			((RefCnt) obj)._decRef();
		}
	}

	public static boolean isNotDecRef(Object obj) {
		if (obj instanceof RefCnt) {
			((RefCnt) obj)._decRef();
			return false;
		}
		return true;
	}

	public static void incRef(Object obj) {
		if (obj instanceof RefCnt) {
			((RefCnt) obj)._incRef();
		}
	}

	public static long count(RefCnt rc) {
		return rc._count();
	}

	public static boolean isClosed(RefCnt rc) {
		return count(rc) <= 0L;
	}

	public static void close(RefCnt rc) {
		rc._close();
	}
}
