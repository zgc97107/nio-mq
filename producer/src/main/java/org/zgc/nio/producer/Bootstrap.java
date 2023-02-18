package org.zgc.nio.producer;

import org.jline.reader.UserInterruptException;
import org.zgc.nio.producer.command.Command;
import org.zgc.nio.producer.thread.Processor;
import org.zgc.nio.producer.tools.CommandReader;

import java.io.IOException;

/**
 * @author lucheng
 * @since 2022/8/24
 */
public class Bootstrap {
    public static boolean isStart = true;
    public static void main(String[] args) throws InterruptedException, IOException {
        Processor processor = new Processor("localhost", 8848);
        processor.start();
        CommandReader reader = new CommandReader();
        try {
            while (isStart) {
                Command command = reader.readCommand();
                command.execute(processor);
            }
        } catch (UserInterruptException e){
            isStart = false;
            processor.exit();
            System.exit(0);
        }

    }
}
