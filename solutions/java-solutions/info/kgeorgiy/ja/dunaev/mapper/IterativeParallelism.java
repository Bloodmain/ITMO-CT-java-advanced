package info.kgeorgiy.ja.dunaev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

/**
 * Copy of the {@link info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism} for {@link info.kgeorgiy.ja.dunaev.mapper} package.
 *
 * @author Dunaev Kirill
 */
public class IterativeParallelism extends info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism {
    /**
     * Constructs class that will create threads itself.
     */
    public IterativeParallelism() {
        super();
    }

    /**
     * Constructs class that will parallelize list using given the mapper.
     * This class itself will not create new threads.
     *
     * @param mapper mapper that will be used
     */
    public IterativeParallelism(ParallelMapper mapper) {
        super(mapper);
    }
}
