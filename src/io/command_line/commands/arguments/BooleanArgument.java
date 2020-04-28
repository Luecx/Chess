package io.command_line.commands.arguments;

public class BooleanArgument extends Argument<Boolean> {

    public BooleanArgument(String name, boolean required, String... aliases) {
        super(name, required, false, aliases);
    }

    public BooleanArgument(String name, String description, boolean required,String... aliases) {
        super(name, description, required, false, aliases);
    }

    @Override
    public void parse(String[] given) {
        this.setValue(true);
    }
}
