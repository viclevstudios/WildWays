package com.viclev.wildways;

/** Marks template placement that is already positioned by a PoolElementStructurePiece. */
public final class QuarantinePiecePlacementContext {
	private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

	private QuarantinePiecePlacementContext() {
	}

	public static void enter() {
		DEPTH.set(DEPTH.get() + 1);
	}

	public static void exit() {
		int depth = DEPTH.get() - 1;
		if (depth <= 0) {
			DEPTH.remove();
		} else {
			DEPTH.set(depth);
		}
	}

	public static boolean isActive() {
		return DEPTH.get() > 0;
	}
}
