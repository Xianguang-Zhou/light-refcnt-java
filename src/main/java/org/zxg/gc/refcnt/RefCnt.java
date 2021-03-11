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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
@WeaverIgnore
public abstract class RefCnt {

	private final AtomicLong count = new AtomicLong(1L);

	protected abstract void onClose() throws Throwable;

	@Override
	protected void finalize() throws Throwable {
		_close();
	}

	final void _incRef() {
		count.updateAndGet(value -> {
			if (value > 0L) {
				return ++value;
			} else {
				return value;
			}
		});
	}

	final void _decRef() {
		if (0L == count.updateAndGet(value -> {
			if (value < 0L) {
				return value;
			} else {
				return --value;
			}
		})) {
			callOnClose();
		}
	}

	final void _close() {
		if (0L == count.updateAndGet(value -> {
			if (value > 0L) {
				return 0L;
			} else if (value < 0L) {
				return value;
			} else {
				return --value;
			}
		})) {
			callOnClose();
		}
	}

	final long _count() {
		return count.get();
	}

	private void callOnClose() {
		try {
			onClose();
		} catch (Throwable e) {
			if (RefCntUtil.printExceptions) {
				e.printStackTrace();
			}
		}
	}
}
