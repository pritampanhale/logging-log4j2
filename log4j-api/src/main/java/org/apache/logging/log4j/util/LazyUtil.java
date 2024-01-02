/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

final class LazyUtil {
    private static final Object NULL = new Object() {
        @Override
        public String toString() {
            return "null";
        }
    };

    static Object wrapNull(final Object value) {
        return value == null ? NULL : value;
    }

    static <T> T unwrapNull(final Object value) {
        return value == NULL ? null : Cast.cast(value);
    }

    record Constant<T>(T value) implements Lazy<T> {
        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public void set(final T newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    static class WeakConstant<T> implements Lazy<T> {
        private final WeakReference<T> reference;

        WeakConstant(final T value) {
            reference = new WeakReference<>(value);
        }

        @Override
        public T get() {
            return reference.get();
        }

        @Override
        public boolean isInitialized() {
            return true;
        }

        @Override
        public void set(final T newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return String.valueOf(get());
        }
    }

    static class SafeLazy<T> implements Lazy<T> {
        private final Lock lock = new ReentrantLock();
        private final Supplier<T> supplier;
        private volatile Object value;

        SafeLazy(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            Object value = this.value;
            if (value == null) {
                lock.lock();
                try {
                    value = this.value;
                    if (value == null) {
                        value = supplier.get();
                        this.value = wrapNull(value);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return unwrapNull(value);
        }

        @Override
        public void set(final T newValue) {
            value = newValue;
        }

        public void reset() {
            value = null;
        }

        @Override
        public boolean isInitialized() {
            return value != null;
        }

        @Override
        public String toString() {
            return isInitialized() ? String.valueOf(value) : "Lazy value not initialized";
        }
    }

    static class ReleaseAcquireLazy<T> implements Lazy<T> {
        private static final VarHandle VALUE;

        static {
            try {
                VALUE = MethodHandles.lookup().findVarHandle(ReleaseAcquireLazy.class, "value", Object.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        private final Supplier<T> supplier;
        private volatile Object value;

        ReleaseAcquireLazy(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            final var currentValue = VALUE.getAcquire(this);
            if (currentValue != null) {
                return unwrapNull(currentValue);
            }
            final T newValue = supplier.get();
            final Object witness = VALUE.compareAndExchangeRelease(this, null, wrapNull(newValue));
            return witness == null ? newValue : unwrapNull(witness);
        }

        @Override
        public void set(final T newValue) {
            // equivalent to VALUE.setVolatile(this, newValue)
            value = newValue;
        }

        @Override
        public boolean isInitialized() {
            final var current = VALUE.getAcquire(this);
            return current != null;
        }

        @Override
        public String toString() {
            return isInitialized() ? String.valueOf(VALUE.getOpaque(value)) : "Lazy value not initialized";
        }
    }

    static class PureLazy<T> implements Lazy<T> {
        private final Supplier<T> supplier;
        private Object value;

        public PureLazy(final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            Object value = this.value;
            if (value == null) {
                value = supplier.get();
                this.value = wrapNull(value);
            }
            return unwrapNull(value);
        }

        @Override
        public boolean isInitialized() {
            return value != null;
        }

        @Override
        public void set(final T newValue) {
            value = newValue;
        }
    }
}
