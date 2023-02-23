package org.zgc.nio.producer.command;

import java.util.*;

public class SendMessageCommand extends AbstractCommand {

    private final List<String> messages;

    private Deque<String> args;

    public SendMessageCommand(String command) {
        super(command);
        this.messages = new LinkedList<>();
        this.args = new ArrayDeque<>(Arrays.asList(super.args));
    }

    @Override
    public void execute(CommandExecutor executor) {
        parse();
        messages.forEach(message -> {
            try {
                executor.execute(CommandKeys.SEND, message);
            } catch (Exception e) {
                System.out.println("execute failed exception: " + e);
            }
        });
        executor.addToWaitingList(this);
        waiting();
    }

    private void parse() {
        String arg = args.pollFirst();
        arg = args.pollFirst();
        if (arg.contains("-n")) {
            int count = Integer.parseInt(args.pollFirst());
            String message = String.join(" ", args);
            for (int i = 0; i < count; i++) {
                messages.add(message);
            }
        } else {
            String message = arg + " " + String.join(" ", args);
            messages.add(message);
        }
    }
}
