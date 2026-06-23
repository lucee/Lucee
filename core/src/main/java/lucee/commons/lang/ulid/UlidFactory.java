package lucee.commons.lang.ulid;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;

/**
 * A class that actually generates ULIDs.
 * <p>
 * This class is used by {@link UlidCreator}.
 * <p>
 * You can use this class if you need to use a specific random generator strategy. However, most
 * people just need {@link UlidCreator}.
 * <p>
 * Instances of this class can behave in one of two ways: monotonic or non-monotonic (default).
 * <p>
 * If the factory is monotonic, the random component is incremented by 1 if more than one ULID is
 * generated within the same millisecond.
 * <p>
 * The maximum ULIDs that can be generated per millisecond is 2^80.
 */
public final class UlidFactory {

	private final LongSupplier timeFunction;
	private final LongFunction<Ulid> ulidFunction;
	private final ReentrantLock lock = new ReentrantLock();

	// ******************************
	// Constructors
	// ******************************

	/**
	 * Default constructor.
	 */
	public UlidFactory() {
		this(new UlidFunction());
	}

	private UlidFactory(LongFunction<Ulid> ulidFunction) {
		this(ulidFunction, System::currentTimeMillis);
	}

	private UlidFactory(LongFunction<Ulid> ulidFunction, LongSupplier timeFunction) {

		Objects.requireNonNull(ulidFunction, "ULID function must not be null");
		Objects.requireNonNull(timeFunction, "Time function must not be null");

		this.ulidFunction = ulidFunction;
		this.timeFunction = timeFunction;

		if (this.ulidFunction instanceof MonotonicFunction) {
			// initialize the internal state of the monotonic function
			((MonotonicFunction) this.ulidFunction).initialize(this.timeFunction);
		}
	}

	/**
	 * Returns a new factory.
	 * <p>
	 * It is equivalent to the default constructor {@code new UlidFactory()}.
	 * 
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newInstance() {
		return new UlidFactory(new UlidFunction());
	}

	/**
	 * Returns a new factory.
	 * 
	 * @param random a {@link Random} generator
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newInstance(Random random) {
		return new UlidFactory(new UlidFunction(random));
	}

	/**
	 * Returns a new factory.
	 * <p>
	 * The given random function must return a long value.
	 * 
	 * @param randomFunction a random function that returns a long value
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newInstance(LongSupplier randomFunction) {
		return new UlidFactory(new UlidFunction(randomFunction));
	}

	/**
	 * Returns a new factory.
	 * <p>
	 * The given random function must return a byte array.
	 * 
	 * @param randomFunction a random function that returns a byte array
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newInstance(IntFunction<byte[]> randomFunction) {
		return new UlidFactory(new UlidFunction(randomFunction));
	}

	/**
	 * Returns a new monotonic factory.
	 * 
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance() {
		return new UlidFactory(new MonotonicFunction());
	}

	/**
	 * Returns a new monotonic factory.
	 * 
	 * @param random a {@link Random} generator
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(Random random) {
		return new UlidFactory(new MonotonicFunction(random));
	}

	/**
	 * Returns a new monotonic factory.
	 * <p>
	 * The given random function must return a long value.
	 * 
	 * @param randomFunction a random function that returns a long value
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(LongSupplier randomFunction) {
		return new UlidFactory(new MonotonicFunction(randomFunction));
	}

	/**
	 * Returns a new monotonic factory.
	 * <p>
	 * The given random function must return a byte array.
	 * 
	 * @param randomFunction a random function that returns a byte array
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(IntFunction<byte[]> randomFunction) {
		return new UlidFactory(new MonotonicFunction(randomFunction));
	}

	/**
	 * Returns a new monotonic factory.
	 * 
	 * @param random a {@link Random} generator
	 * @param timeFunction a function that returns the current time in milliseconds, measured from the
	 *            UNIX epoch of 1970-01-01T00:00Z (UTC)
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(Random random, LongSupplier timeFunction) {
		return new UlidFactory(new MonotonicFunction(random), timeFunction);
	}

	/**
	 * Returns a new monotonic factory.
	 * <p>
	 * The given random function must return a long value.
	 * 
	 * @param randomFunction a random function that returns a long value
	 * @param timeFunction a function that returns the current time in milliseconds, measured from the
	 *            UNIX epoch of 1970-01-01T00:00Z (UTC)
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(LongSupplier randomFunction, LongSupplier timeFunction) {
		return new UlidFactory(new MonotonicFunction(randomFunction), timeFunction);
	}

	/**
	 * Returns a new monotonic factory.
	 * <p>
	 * The given random function must return a byte array.
	 * 
	 * @param randomFunction a random function that returns a byte array
	 * @param timeFunction a function that returns the current time in milliseconds, measured from the
	 *            UNIX epoch of 1970-01-01T00:00Z (UTC)
	 * @return {@link UlidFactory}
	 */
	public static UlidFactory newMonotonicInstance(IntFunction<byte[]> randomFunction, LongSupplier timeFunction) {
		return new UlidFactory(new MonotonicFunction(randomFunction), timeFunction);
	}

	// ******************************
	// Public methods
	// ******************************

	/**
	 * Returns a new ULID.
	 * 
	 * @return a ULID
	 */
	public Ulid create() {
		return create(timeFunction.getAsLong());
	}

	/**
	 * Returns a new ULID.
	 * 
	 * @param time the current time in milliseconds, measured from the UNIX epoch of 1970-01-01T00:00Z
	 *            (UTC)
	 * @return a ULID
	 */
	public Ulid create(final long time) {
		lock.lock();
		try {
			return this.ulidFunction.apply(time);
		}
		finally {
			lock.unlock();
		}
	}

	// ******************************
	// Package-private inner classes
	// ******************************

	/**
	 * Function that creates ULIDs.
	 */
	static final class UlidFunction implements LongFunction<Ulid> {

		private final IRandom random;

		private UlidFunction(IRandom random) {
			this.random = random;
		}

		public UlidFunction() {
			this(IRandom.newInstance());
		}

		public UlidFunction(Random random) {
			this(IRandom.newInstance(random));
		}

		public UlidFunction(LongSupplier randomFunction) {
			this(IRandom.newInstance(randomFunction));
		}

		public UlidFunction(IntFunction<byte[]> randomFunction) {
			this(IRandom.newInstance(randomFunction));
		}

		@Override
		public Ulid apply(final long time) {
			if (this.random instanceof ByteRandom) {
				return new Ulid(time, this.random.nextBytes(Ulid.RANDOM_BYTES));
			}
			else {
				final long msb = (time << 16) | (this.random.nextLong() & 0xffffL);
				final long lsb = this.random.nextLong();
				return new Ulid(msb, lsb);
			}
		}
	}

	/**
	 * Function that creates Monotonic ULIDs.
	 */
	static final class MonotonicFunction implements LongFunction<Ulid> {

		private Ulid lastUlid;

		private final IRandom random;

		// Used to preserve monotonicity when the system clock is
		// adjusted by NTP after a small clock drift or when the
		// system clock jumps back by 1 second due to leap second.
		static final int CLOCK_DRIFT_TOLERANCE = 10_000;

		private MonotonicFunction(IRandom random) {
			this.random = random;
		}

		public MonotonicFunction() {
			this(IRandom.newInstance());
		}

		public MonotonicFunction(Random random) {
			this(IRandom.newInstance(random));
		}

		public MonotonicFunction(LongSupplier randomFunction) {
			this(IRandom.newInstance(randomFunction));
		}

		public MonotonicFunction(IntFunction<byte[]> randomFunction) {
			this(IRandom.newInstance(randomFunction));
		}

		void initialize(LongSupplier timeFunction) {
			// initialize the factory with the instant 1970-01-01 00:00:00.000 UTC
			this.lastUlid = new Ulid(0L, this.random.nextBytes(Ulid.RANDOM_BYTES));
		}

		@Override
		public Ulid apply(final long time) {

			final long lastTime = lastUlid.getTime();

			// Check if the current time is the same as the previous time or has moved
			// backwards after a small system clock adjustment or after a leap second.
			// Drift tolerance = (previous_time - 10s) < current_time <= previous_time
			if ((time > lastTime - CLOCK_DRIFT_TOLERANCE) && (time <= lastTime)) {
				this.lastUlid = this.lastUlid.increment();
			}
			else {
				if (this.random instanceof ByteRandom) {
					this.lastUlid = new Ulid(time, this.random.nextBytes(Ulid.RANDOM_BYTES));
				}
				else {
					final long msb = (time << 16) | (this.random.nextLong() & 0xffffL);
					final long lsb = this.random.nextLong();
					this.lastUlid = new Ulid(msb, lsb);
				}
			}

			return new Ulid(this.lastUlid);
		}
	}

	static interface IRandom {

		public long nextLong();

		public byte[] nextBytes(int length);

		static IRandom newInstance() {
			return new ByteRandom();
		}

		static IRandom newInstance(Random random) {
			if (random == null) {
				return new ByteRandom();
			}
			else {
				if (random instanceof SecureRandom) {
					return new ByteRandom(random);
				}
				else {
					return new LongRandom(random);
				}
			}
		}

		static IRandom newInstance(LongSupplier randomFunction) {
			return new LongRandom(randomFunction);
		}

		static IRandom newInstance(IntFunction<byte[]> randomFunction) {
			return new ByteRandom(randomFunction);
		}
	}

	static class LongRandom implements IRandom {

		private final LongSupplier randomFunction;

		public LongRandom() {
			this(newRandomFunction(null));
		}

		public LongRandom(Random random) {
			this(newRandomFunction(random));
		}

		public LongRandom(LongSupplier randomFunction) {
			this.randomFunction = randomFunction != null ? randomFunction : newRandomFunction(null);
		}

		@Override
		public long nextLong() {
			return randomFunction.getAsLong();
		}

		@Override
		public byte[] nextBytes(int length) {

			int shift = 0;
			long random = 0;
			final byte[] bytes = new byte[length];

			for (int i = 0; i < length; i++) {
				if (shift < Byte.SIZE) {
					shift = Long.SIZE;
					random = randomFunction.getAsLong();
				}
				shift -= Byte.SIZE; // 56, 48, 40...
				bytes[i] = (byte) (random >>> shift);
			}

			return bytes;
		}

		static LongSupplier newRandomFunction(Random random) {
			final Random entropy = random != null ? random : new SecureRandom();
			return entropy::nextLong;
		}
	}

	static class ByteRandom implements IRandom {

		private final IntFunction<byte[]> randomFunction;

		public ByteRandom() {
			this(newRandomFunction(null));
		}

		public ByteRandom(Random random) {
			this(newRandomFunction(random));
		}

		public ByteRandom(IntFunction<byte[]> randomFunction) {
			this.randomFunction = randomFunction != null ? randomFunction : newRandomFunction(null);
		}

		@Override
		public long nextLong() {
			long number = 0;
			byte[] bytes = this.randomFunction.apply(Long.BYTES);
			for (int i = 0; i < Long.BYTES; i++) {
				number = (number << 8) | (bytes[i] & 0xff);
			}
			return number;
		}

		@Override
		public byte[] nextBytes(int length) {
			return this.randomFunction.apply(length);
		}

		static IntFunction<byte[]> newRandomFunction(Random random) {
			final Random entropy = random != null ? random : new SecureRandom();
			return (final int length) -> {
				final byte[] bytes = new byte[length];
				entropy.nextBytes(bytes);
				return bytes;
			};
		}
	}
}
