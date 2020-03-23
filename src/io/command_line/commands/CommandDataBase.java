package io.command_line.commands;

import io.command_line.commands.arguments.Argument;
import io.command_line.commands.arguments.NumericArgument;
import io.command_line.commands.arguments.TextArgument;
import io.command_line.exceptions.CommandException;

import java.util.*;

/**
 * Created by finne on 27.01.2018.
 */
public class CommandDataBase {


    private ArrayList<Command> commands = new ArrayList<>();


    public CommandDataBase(){

        this.registerCommand(new Command("help", "overview of all functions")
                .registerArgument(new TextArgument("func", false, "help","f"))
                .setExecutable(c -> {
                    TextArgument arg = c.getTextArgument("func");
                    if(arg.isSet()){
                        System.out.println(getCommand(arg.getValue()).getCommandLayout());
                    }else{
                        for(Command cmds:commands){
                            System.out.println(cmds.getInfo());
                        }
                    }
                }));
        this.registerCommand(new Command("quit", "quits the java process")
                .setExecutable(c -> System.exit(-1)));
    }

    public void registerCommand(Command c) {
        try{
            commands.add(c);
        }catch (Exception e) {
        }
    }

    public void removeCommand(String key) {
        commands.remove(key.toLowerCase());
    }

    public Command getCommand(String name){
        for(Command c:commands){
            if(c.getName().equals(name)){
                return c;
            }
        }
        return null;
    }

    public void executeCommand(String s) {
        try {

            String readable = s.replace("  ", " ").trim();
            String[] split = readable.split(" ");

            if(split.length == 0) throw new CommandException();

            String commandName = split[0];

            Command c = getCommand(commandName);
            if(c == null) throw new CommandException();

            c.execute(split);


//
//            if (s.startsWith("-")) s = s.substring(1);
//            s = StringManipulation.transformIntoReadableCommand(s);
//            int index = s.indexOf("-");
//
//
//            String command = index >= 0 ? s.substring(0, index).trim() : s;
//            String rest = index >= 0 ? s.substring(index + 1).trim() : "";
//
//            for (String key : commands.keySet()) {
//                if (key.equals(command.trim())) {
//                    commands.get(key).executeTotal(rest.split("-"));
//                    break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CommandDataBase dataBase = new CommandDataBase();
        dataBase.registerCommand(
                new Command("test", "just some casual testing")
                        .registerArgument(new NumericArgument("size", "the size of the input", true,0d))
                        .registerArgument(new NumericArgument("ka", "kolumns",true,0d))

                        .setExecutable(new Executable() {
                            @Override
                            public void execute(Command c) {
                                System.out.println("lkadokawodkawo");
                            }
                        }));


        dataBase.registerCommand(
                new Command("print", "print some text")
                    .registerArgument(new TextArgument("t", "the text to print", true, "text", "txt"))
                    .setExecutable(new Executable() {
                        @Override
                        public void execute(Command c) {
                            System.out.println(c.getTextArgument("t").getValue());
                        }
                    })
        );

        Scanner scanner = new Scanner(System.in);

        while(true){
            String command = scanner.nextLine();
            dataBase.executeCommand(command);
        }

    }

}
