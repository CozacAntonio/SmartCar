package ro.robot.logic;

public enum TurnIntent {
    FRONT(0.0f),
    LEFT(1.0f),
    RIGHT(2.0f);

    private final float value;

    TurnIntent(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}