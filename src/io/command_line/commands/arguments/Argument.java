package io.command_line.commands.arguments;


/**
 * Created by finne on 27.01.2018.
 */
public abstract class Argument<T> {

    private final       String      name;
    private final       String      description;
    private final       String[]    aliases;

    private             boolean     set;
    private             boolean     required;
    private             T           value;
    private             T           defaultValue;

    public Argument(String name,boolean required, T defaultValue, String... aliases) {
        this.name = name.toLowerCase();
        this.aliases = aliases;
        this.defaultValue = defaultValue;
        this.description = "N/A";
        this.required = required;
    }

    public Argument(String name, String description, boolean required, T defaultValue, String... aliases) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
        this.aliases = aliases;
        this.required = required;
    }

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAlias(String s) {
        if(s.equals(name)) return true;
        for(String string:aliases){
            if(string.toLowerCase().equals(s.toLowerCase())) return true;
        }
        return false;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public abstract void parse(String[] given);
}
