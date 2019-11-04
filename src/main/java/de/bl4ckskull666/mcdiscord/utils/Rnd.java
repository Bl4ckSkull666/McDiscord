package de.bl4ckskull666.mcdiscord.utils;

import java.security.SecureRandom;
import java.util.Random;

public final class Rnd {
    private static final long ADDEND = 11L;
    private static final long MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final RandomContainer rnd;
    private static volatile long SEED_UNIQUIFIER;

    public static long bytesToMegabytes(final long bytes) {
        return bytes / 1048576L;
    }

    public static boolean isNumeric(final String str) {
        try {
            final int i = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(final String str) {
        try {
            final float i = Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(final String str) {
        try {
            final double i = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static final Random directRandom() {
        return Rnd.rnd.directRandom();
    }

    public static final double get() {
        return Rnd.rnd.nextDouble();
    }

    public static final int get(final int n) {
        return Rnd.rnd.get(n);
    }

    public static final int get(final int min, final int max) {
        return Rnd.rnd.get(min, max);
    }

    public static final long get(final long min, final long max) {
        return Rnd.rnd.get(min, max);
    }

    public static final double get(final double min, final double max) {
        return Rnd.rnd.get(min, max);
    }

    public static final RandomContainer newInstance(final RandomType type) {
        switch (type) {
            case UNSECURE_ATOMIC: {
                return new RandomContainer(new Random());
            }
            case UNSECURE_VOLATILE: {
                return new RandomContainer((Random)new NonAtomicRandom());
            }
            case UNSECURE_THREAD_LOCAL: {
                return new RandomContainer((Random)new ThreadLocalRandom());
            }
            case SECURE: {
                return new RandomContainer((Random)new SecureRandom());
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    public static final boolean nextBoolean() {
        return Rnd.rnd.nextBoolean();
    }

    public static final void nextBytes(final byte[] array) {
        Rnd.rnd.nextBytes(array);
    }

    public static final double nextDouble() {
        return Rnd.rnd.nextDouble();
    }

    public static final float nextFloat() {
        return Rnd.rnd.nextFloat();
    }

    public static final double nextGaussian() {
        return Rnd.rnd.nextGaussian();
    }

    public static final int nextInt() {
        return Rnd.rnd.nextInt();
    }

    public static final int nextInt(final int n) {
        return get(n);
    }

    public static final long nextLong() {
        return Rnd.rnd.nextLong();
    }

    static {
        rnd = newInstance(RandomType.UNSECURE_THREAD_LOCAL);
        Rnd.SEED_UNIQUIFIER = 8682522807148012L;
    }

    public static final class NonAtomicRandom extends Random {
        private static final long serialVersionUID = 1L;
        private volatile long _seed;

        public NonAtomicRandom() {
            this(++Rnd.SEED_UNIQUIFIER + System.nanoTime());
        }

        public NonAtomicRandom(final long seed) {
            this.setSeed(seed);
        }

        public final int next(final int bits) {
            final long seed = this._seed * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
            this._seed = seed;
            return (int)(seed >>> 48 - bits);
        }

        @Override
        public final void setSeed(final long seed) {
            this._seed = ((seed ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL);
        }
    }

    public static final class RandomContainer {
        private final Random _random;

        private RandomContainer(final Random random) {
            this._random = random;
        }

        public final Random directRandom() {
            return this._random;
        }

        public final double get() {
            return this._random.nextDouble();
        }

        public final int get(final int n) {
            return (int)(this._random.nextDouble() * n);
        }

        public final int get(final int min, final int max) {
            return min + (int)(this._random.nextDouble() * (max - min + 1));
        }

        public final long get(final long min, final long max) {
            return min + (long)(this._random.nextDouble() * (max - min + 1L));
        }

        public final double get(final double min, final double max) {
            return min + this._random.nextDouble() * (max - min + 1.0);
        }

        public final boolean nextBoolean() {
            return this._random.nextBoolean();
        }

        public final void nextBytes(final byte[] array) {
            this._random.nextBytes(array);
        }

        public final double nextDouble() {
            return this._random.nextDouble();
        }

        public final float nextFloat() {
            return this._random.nextFloat();
        }

        public final double nextGaussian() {
            return this._random.nextGaussian();
        }

        public final int nextInt() {
            return this._random.nextInt();
        }

        public final long nextLong() {
            return this._random.nextLong();
        }
    }

    public enum RandomType {
        SECURE,
        UNSECURE_ATOMIC,
        UNSECURE_THREAD_LOCAL,
        UNSECURE_VOLATILE;
    }
     public static final class ThreadLocalRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final ThreadLocal<Seed> _seedLocal;

        public ThreadLocalRandom() {
            this._seedLocal = new ThreadLocal<Seed>() {
                public final Seed initialValue() {
                    return new Seed(++Rnd.SEED_UNIQUIFIER + System.nanoTime());
                }
            };
        }

        public ThreadLocalRandom(final long seed) {
            this._seedLocal = new ThreadLocal<Seed>() {
                public final Seed initialValue() {
                    return new Seed(seed);
                }
            };
        }

        public final int next(final int bits) {
            return this._seedLocal.get().next(bits);
        }

        @Override
        public final void setSeed(final long seed) {
            if (this._seedLocal != null) {
                this._seedLocal.get().setSeed(seed);
            }
        }

        private static final class Seed {
            long _seed;

            Seed(final long seed) {
                this.setSeed(seed);
            }

            final int next(final int bits) {
                final long seed = this._seed * 25214903917L + 11L & 0xFFFFFFFFFFFFL;
                this._seed = seed;
                return (int)(seed >>> 48 - bits);
            }

            final void setSeed(final long seed) {
                this._seed = ((seed ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL);
            }
        }
    }
}