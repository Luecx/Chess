package io.command_line.commands.arguments;

public class BooleanArgument extends Argument<Boolean> {

    public BooleanArgument(String name, boolean required, Boolean defaultValue, String... aliases) {
        super(name, required, defaultValue, aliases);
    }

    public BooleanArgument(String name, String description, boolean required, Boolean defaultValue, String... aliases) {
        super(name, description, required, defaultValue, aliases);
    }

    @Override
    public void parse(String[] given) {
        this.setValue(true);
    }
}
