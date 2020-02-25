package io.command_line.commands.arguments;

public class TextArgument extends Argument<String> {

    public TextArgument(String name, boolean required, String defaultValue, String... aliases) {
        super(name, required, defaultValue, aliases);
    }

    public TextArgument(String name, String description, boolean required, String defaultValue, String... aliases) {
        super(name, description, required, defaultValue, aliases);
    }

    @Override
    public void reset() {
        this.setValue(""+getDefaultValue());
    }

    @Override
    public void parse(String[] given) {
        if(given == null) return;
        if(given.length > 0){
            this.setValue(String.join(" ", given));
        }
    }
}
