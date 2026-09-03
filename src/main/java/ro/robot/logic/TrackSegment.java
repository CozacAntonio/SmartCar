package ro.robot.logic;

public class TrackSegment {
    public int segmentId;
    public float expectedDistanceCm;
    public float speedLimit;
    public float turnIntentAtEnd;

    public TrackSegment(int id, float distance, float speed, float turnIntent) {
        this.segmentId = id;
        this.expectedDistanceCm = distance;
        this.speedLimit = speed;
        this.turnIntentAtEnd = turnIntent;
    }
}