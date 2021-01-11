package springbook.user;

public enum Level {

    GOLD(1), SILVER(2), BASIC(3);

    private final int value;

    Level(int value) {
        this.value = value;
    }

    public int intValue() {
        return this.value;
    }

    public static Level valueOf(int value) {
        switch (value) {
            case 1:
                return Level.GOLD;
            case 2:
                return Level.SILVER;
            case 3:
                return Level.BASIC;
            default:
                throw new AssertionError("Unknown value: " + value);
        }
    }
}
