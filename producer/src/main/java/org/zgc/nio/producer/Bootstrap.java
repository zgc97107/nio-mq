package org.zgc.nio.producer;

import org.jline.reader.UserInterruptException;
import org.zgc.nio.producer.command.Command;
import org.zgc.nio.producer.command.CommandExecutor;
import org.zgc.nio.producer.internals.BufferPool;
import org.zgc.nio.producer.internals.RecordAccumulator;
import org.zgc.nio.producer.thread.Sender;
import org.zgc.nio.producer.tools.CommandReader;

import java.io.IOException;

/**
 * @author lucheng
 * @since 2022/8/24
 */
public class Bootstrap {
    public static boolean isStart = true;

    public static void main(String[] args) throws InterruptedException, IOException {
        BufferPool bufferPool = new BufferPool(ProducerConfig.BUFFER_POOL_MAX_MEMORY, ProducerConfig.BUFFER_POOL_MAX_SIZE);
        RecordAccumulator recordAccumulator = new RecordAccumulator(bufferPool);
        CommandExecutor commandExecutor = new CommandExecutor(recordAccumulator);
        Sender sender = new Sender(ProducerConfig.HOST, ProducerConfig.PORT, recordAccumulator, commandExecutor);
        sender.start();
        synchronized (sender) {
            sender.wait();
        }
        CommandReader commandReader = new CommandReader();
        try {
            while (isStart) {
                Command command = commandReader.readCommand();
                command.execute(commandExecutor);
            }
        } catch (UserInterruptException e) {
            isStart = false;
            sender.exit();
            System.exit(0);
        }

    }
}
