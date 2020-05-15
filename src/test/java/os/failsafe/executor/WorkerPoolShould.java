/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2020 Oliver Selinger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package os.failsafe.executor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkerPoolShould {

    private int threadCount = 2;
    private WorkerPool workerPool;

    @BeforeEach
    public void init() {
        workerPool = new WorkerPool(threadCount);
    }

    @AfterEach
    public void stop() {
        workerPool.stop();
    }

    @Test
    public void
    accept_more_tasks_if_workers_are_idle() {
        assertFalse(workerPool.allWorkersBusy());
    }

    @Test
    public void
    not_accept_more_tasks_if_all_workers_are_busy() throws InterruptedException, ExecutionException {
        BlockingExecution firstBlockingExecution = new BlockingExecution();
        Future<String> execution = workerPool.execute(firstBlockingExecution);

        IntStream.range(1, threadCount + WorkerPool.FILL_UP_QUEUE_THRESHOLD)
                .mapToObj(i -> new BlockingExecution())
                .forEach(workerPool::execute);

        assertTrue(workerPool.allWorkersBusy());

        firstBlockingExecution.release();
        execution.get();

        assertFalse(workerPool.allWorkersBusy());

        workerPool.execute(new BlockingExecution());

        assertTrue(workerPool.allWorkersBusy());
    }

    static class BlockingExecution extends Execution {
        Phaser phaser;

        BlockingExecution() {
            super(null, null);
            phaser = new Phaser(2);
        }

        @Override
        public String perform() {
            phaser.arriveAndAwaitAdvance();
            return "id";
        }

        public void release() {
            phaser.arriveAndAwaitAdvance();
        }
    }
}