package springbook.user;

public enum Level {

    GOLD(1, null), SILVER(2, Level.GOLD), BASIC(3, Level.SILVER);

    private final int value;
    private final Level next;

    Level(int value, Level next) {
        this.value = value;
        this.next = next;
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

    public Level nextLevel(){
        return next;
    }


}
