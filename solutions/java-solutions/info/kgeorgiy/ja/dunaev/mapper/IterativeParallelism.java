package info.kgeorgiy.ja.dunaev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Extension of {@link info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism} for using {@link ParallelMapper}.
 *
 * @author Dunaev Kirill
 */
public class IterativeParallelism extends info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism {
    private final ParallelMapper mapper;

    /**
     * Constructs class that will create threads itself.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Constructs class that will parallelize list using given the mapper.
     * This class itself will not create new threads.
     *
     * @param mapper mapper that will be used
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected <T, R> List<R> runInParallel(
            Function<? super Stream<T>, R> processor,
            List<Stream<T>> segments
    ) throws InterruptedException {
        if (mapper != null) {
            return mapper.map(processor, segments);
        }
        return super.runInParallel(processor, segments);
    }
}
