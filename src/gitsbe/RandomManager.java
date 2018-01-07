package gitsbe;

import java.util.Objects;
import java.util.Random;

public class RandomManager {
	
	private static ThreadLocal<Random> RANDOMS = new ThreadLocal<>();
    
	public static void withRandom(Random rnd, Runnable run) {
        Objects.requireNonNull(rnd);
        Objects.requireNonNull(run);
        if (RANDOMS.get() != null) throw new IllegalStateException("nested invocation");
        
        RANDOMS.set(rnd);
        
        try {
            run.run();
        }
        
        finally {
            RANDOMS.remove();
        }
    }
	
	/**
	 * get random integer between min and max (inclusive)
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
    public static int randInt(int min, int max) {
        Random r = RANDOMS.get();
        if(r == null) throw new IllegalStateException("not within withRandom(...)");
        return r.nextInt((max - min) + 1) + min;
    }
}
