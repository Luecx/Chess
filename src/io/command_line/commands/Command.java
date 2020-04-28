package io.command_line.commands;


import io.command_line.commands.arguments.Argument;
import io.command_line.commands.arguments.BooleanArgument;
import io.command_line.commands.arguments.NumericArgument;
import io.command_line.commands.arguments.TextArgument;
import io.command_line.exceptions.ArgumentException;
import io.command_line.exceptions.CommandException;

import java.util.*;

/**
 * Created by finne on 27.01.2018.
 */
public class Command {

    private final String name;
    private final String description;

    private Executable executable;

    private ArrayList<Argument>         arguments           = new ArrayList<>();
    private ArrayList<BooleanArgument>  booleanArguments    = new ArrayList<>();
    private ArrayList<NumericArgument>  numericArguments    = new ArrayList<>();
    private ArrayList<TextArgument>     textArguments       = new ArrayList<>();

    public Command(String name) {
        this.name = name.toLowerCase();
        this.description = "";
    }

    public Command(String name, String description) {
        this.name = name.toLowerCase();
        this.description = description;
    }

    public Command registerArgument(Argument argument) {
        if(argument instanceof BooleanArgument){
            booleanArguments.add((BooleanArgument) argument);
        }else if(argument instanceof NumericArgument){
            numericArguments.add((NumericArgument) argument);
        }else if(argument instanceof TextArgument){
            textArguments.add((TextArgument) argument);
        }else{
            throw new RuntimeException();
        }
        arguments.add(argument);
        return this;
    }

    public void execute(String[] argumentValues) throws Exception{


        for(Argument arg:arguments){
            arg.setSet(false);
            arg.reset();
        }

        int upperBound = argumentValues.length;
        for(int i = argumentValues.length-1; i>= 0; i--){
            Argument argument = getArgument(argumentValues[i]);
            if(argument != null){
                argument.parse(Arrays.copyOfRange(argumentValues, i+1, upperBound));
                argument.setSet(true);
                upperBound = i;
            }
        }

        for(Argument arg:arguments){
            if(arg.isRequired() && !arg.isSet()){
                throw new ArgumentException("missing required variable: " + arg.getName());
            }
        }

        if (this.executable != null) {
            this.executable.execute(this);
        } else {
            throw new CommandException("No executable registered for this command");
        }

    }




    protected String getCommandLayout() {
        String ret = getInfo();

        for(Argument argument:arguments){
            ret += String.format("%n %3s %-10s %-30s", "", "-"+argument.getName(), " :" + argument.getDescription());
        }

        return ret;
    }

    public String getInfo( ){
        String ret = this.getName();

        for(Argument argument:arguments){
            if(argument.isRequired()){
                ret += " ["+argument.getName() + "]";
            }else{
                ret += " ("+argument.getName() + ")";
            }
        }
        ret += "\n    * " + this.getDescription();

        return ret;
    }

    public Command setExecutable(Executable executable) {
        this.executable = executable;
        return this;
    }

    public Argument getArgument(String s){
        for(Argument arg:arguments){
            if(arg.isAlias(s)){
                return arg;
            }
        }
        return null;
    }

    public BooleanArgument getBooleanArgument(String s){
        for(BooleanArgument arg:booleanArguments){
            if(arg.isAlias(s)){
                return arg;
            }
        }
        return null;
    }

    public TextArgument getTextArgument(String s){
        for(TextArgument arg:textArguments){
            if(arg.isAlias(s)){
                return arg;
            }
        }
        return null;
    }

    public NumericArgument getNumericArgument(String s){
        for(NumericArgument arg:numericArguments){
            if(arg.isAlias(s)){
                return arg;
            }
        }
        return null;
    }


    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Executable getExecutable() {
        return executable;
    }
}
