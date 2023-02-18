package org.zgc.nio.producer.command;

public abstract class AbstractCommand implements Command{

    protected String command;
    protected String[] args;


    public AbstractCommand(String command){
        this.command = command;
        this.args = command.split(" ");
    }

}
