package org.zgc.nio.producer.tools;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.zgc.nio.producer.command.Command;
import org.zgc.nio.producer.command.CommandFactory;

import java.io.IOException;
/**
 * @author lucheng
 * @since 2022/8/24
 */
public class CommandReader {

    private LineReader lineReader;

    public CommandReader() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        ArgumentCompleter completer = new ArgumentCompleter(new StringsCompleter("send"),
                new StringsCompleter("hello world"),
                new StringsCompleter("","50","100"),
                NullCompleter.INSTANCE);

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
    }

    public Command readCommand(){
        try {
            String command = lineReader.readLine("client> ");
            return CommandFactory.getCommand(command,this);
        }catch (EndOfFileException e){
            System.out.println("\nBye.");
            System.exit(0);
            return null;
        }
    }
}
