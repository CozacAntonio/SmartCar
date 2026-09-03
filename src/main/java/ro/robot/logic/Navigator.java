package ro.robot.logic;

import java.util.LinkedList;
import java.util.Queue;

public class Navigator {

    private Queue<TrackSegment> route;
    private TrackSegment currentSegment;

    public Navigator() {
        route = new LinkedList<>();
        loadTrackAlpha();
        advanceToNextSegment();
    }

    private void loadTrackAlpha() {
        System.out.println("Navigator: Loading Hand-Drawn Track into memory...");
        route.clear();

        // Intent Key: 0.0=Forward, 1.0=Left, 2.0=Right, 9.0=Terminal Stop

        route.add(new TrackSegment(1, 30.0f, 50.0f, 1.0f));

        // Segment 2
        route.add(new TrackSegment(2, 20.0f, 40.0f, 2.0f));

        // Segment 3
        route.add(new TrackSegment(3, 50.0f, 50.0f, 2.0f));

        // Segment 4
        route.add(new TrackSegment(4, 40.0f, 30.0f, 2.0f));

        // Segment 5
        route.add(new TrackSegment(5, 30.0f, 50.0f, 9.0f));
    }

    public void advanceToNextSegment() {
        if (!route.isEmpty()) {
            currentSegment = route.poll();
            System.out.println("Navigator: Advanced to Segment " + currentSegment.segmentId + " | Loaded Intent: " + currentSegment.turnIntentAtEnd);
        } else {
            System.out.println("Navigator: TRACK COMPLETE. Stopping Vehicle.");
            currentSegment = new TrackSegment(99, 0.0f, 0.0f, 0.0f);
        }
    }

    public float getCurrentSpeedLimit() { return currentSegment.speedLimit; }
    public float getCurrentTurnIntent() { return currentSegment.turnIntentAtEnd; }
}