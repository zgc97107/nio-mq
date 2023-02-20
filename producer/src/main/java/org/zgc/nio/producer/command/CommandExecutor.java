package org.zgc.nio.producer.command;

import org.zgc.nio.producer.internals.RecordAccumulator;

public class CommandExecutor {
    private RecordAccumulator recordAccumulator;

    public CommandExecutor(RecordAccumulator recordAccumulator) {
        this.recordAccumulator = recordAccumulator;
    }

    public void execute(String key, Object value) throws Exception {
        switch (key){
            case CommandKeys.SEND:
                recordAccumulator.append((String) value);
                break;
            default:
                break;
        }
    }
}
