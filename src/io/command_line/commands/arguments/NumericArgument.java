package io.command_line.commands.arguments;

public class NumericArgument extends Argument<Double> {

    public NumericArgument(String name, boolean required, Double defaultValue, String... aliases) {
        super(name, required, defaultValue, aliases);
    }

    public NumericArgument(String name, String description, boolean required, Double defaultValue, String... aliases) {
        super(name, description, required, defaultValue, aliases);
    }


    @Override
    public void parse(String[] given) {
        if(given == null) return;
        if(given.length > 0){
            this.setValue(Double.parseDouble(given[0]));
        }
    }
}
