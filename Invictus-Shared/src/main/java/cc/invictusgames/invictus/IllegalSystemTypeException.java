package cc.invictusgames.invictus;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.12.2020 / 15:46
 * Invictus / cc.invictusgames.invictus.spigot
 */

public class IllegalSystemTypeException extends IllegalStateException {

    public IllegalSystemTypeException() {
        super(Invictus.getSystemType().name());
    }

    public IllegalSystemTypeException(SystemType required) {
        super(Invictus.getSystemType().name() + ", Required: " + required.name());
    }

    public static void checkOrThrow(SystemType systemType) {
        if (Invictus.getSystemType() != systemType)
            throw new IllegalSystemTypeException(systemType);
    }

}
