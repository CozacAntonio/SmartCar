package ro.robot.vision;

import Components.PetriNet;
import DataObjects.DataFloat;
import ro.robot.hardware.ManeuverEngine;
import ro.robot.logic.Navigator;
import ro.robot.logic.RobotState;
import ro.robot.logic.TurnIntent;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import java.util.ArrayList;
import java.util.List;

public class CameraTracker implements Runnable {

    private PetriNet brain;
    private Navigator navigator;
    private ManeuverEngine maneuverEngine;
    private VideoCapture camera;
    private boolean isRunning = true;

    private RobotState currentState = RobotState.LINE_TRACKING;
    private long haltStartTime = 0;
    private float intersectionTriggerDist = 0.0f;
    private long maneuverCooldown = 0;

    public CameraTracker(PetriNet brain, ManeuverEngine maneuverEngine) {
        this.brain = brain;
        this.navigator = new Navigator();
        this.maneuverEngine = maneuverEngine;
    }

    private void freezePetriNet() {
        updatePetriNetPlace("P_Turn", 0.0f);
        updatePetriNetPlace("P_Left", 110.0f);
        updatePetriNetPlace("P_Right", 530.0f);
        updatePetriNetPlace("ManeuverTrigger", 0.0f);
        updatePetriNetPlace("AngleOut", 0.0f);
    }

    @Override
    public void run() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture(0, org.opencv.videoio.Videoio.CAP_V4L2);

        if (!camera.isOpened()) return;

        camera.set(org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH, 640);
        camera.set(org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT, 360);
        camera.set(org.opencv.videoio.Videoio.CAP_PROP_FPS, 30);

        Mat frame = new Mat();
        Mat gray = new Mat();
        Mat binary = new Mat();

        System.out.println("VISION: Camera warming up and clearing buffers...");
        updatePetriNetPlace("NavState", -1.0f);

        for (int i = 0; i < 30; i++) {
            camera.read(frame);
            try { Thread.sleep(33); } catch (InterruptedException e) {}
        }

        System.out.println("VISION: Camera ready. Releasing Brake...");
        updatePetriNetPlace("NavState", 0.0f);

        while (isRunning) {
            if (camera.read(frame)) {

                int roiHeight = frame.rows() / 2;
                int startY = frame.rows() / 2;
                Rect roi = new Rect(0, startY, frame.cols(), roiHeight);
                Mat croppedFrame = new Mat(frame, roi);

                Imgproc.cvtColor(croppedFrame, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(gray, binary, 50, 255, Imgproc.THRESH_BINARY_INV);

                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                int screenCenter = croppedFrame.cols() / 2;
                int screenBottom = croppedFrame.rows();

                float rawLeft = 999.0f;
                float rawRight = 999.0f;
                float calcDist = 100.0f;
                boolean intersectionFound = false;

                Rect bestLeftBox = null;
                Rect bestRightBox = null;
                double maxLeftArea = 0;
                double maxRightArea = 0;

                for (MatOfPoint contour : contours) {
                    Rect box = Imgproc.boundingRect(contour);
                    double area = box.area();
                    if (area < 500) continue;

                    if (box.width > box.height * 1.5 && box.height > 15 && box.x < 520 && (box.x + box.width) > 120) {
                        Imgproc.rectangle(croppedFrame, box.tl(), box.br(), new Scalar(0, 0, 255), 2);
                        calcDist = (float) (screenBottom - (box.y + box.height));
                        intersectionFound = true;
                    } else {
                        int boxCenter = box.x + (box.width / 2);

                        if (boxCenter < screenCenter) {
                            int targetEdge = box.x + box.width;
                            if (area > maxLeftArea) {
                                maxLeftArea = area;
                                rawLeft = (float) targetEdge;
                                bestLeftBox = box;
                            }
                        } else {
                            int targetEdge = box.x;
                            if (area > maxRightArea) {
                                maxRightArea = area;
                                rawRight = (float) targetEdge;
                                bestRightBox = box;
                            }
                        }
                    }
                }

                if (bestLeftBox != null) { Imgproc.rectangle(croppedFrame, bestLeftBox.tl(), bestLeftBox.br(), new Scalar(0, 255, 0), 2); }
                if (bestRightBox != null) { Imgproc.rectangle(croppedFrame, bestRightBox.tl(), bestRightBox.br(), new Scalar(255, 0, 0), 2); }
                Imgproc.line(croppedFrame, new Point(screenCenter, 0), new Point(screenCenter, screenBottom), new Scalar(0, 255, 255), 1);

                HighGui.imshow("Raw WebCam Feed (Cropped)", croppedFrame);
                HighGui.imshow("Robot Brain (Binary Filter)", binary);
                if (HighGui.waitKey(1) == 27) break;

                float currentIntentFloat = navigator.getCurrentTurnIntent();
                TurnIntent intent = parseTurnIntent(currentIntentFloat);

                switch (currentState) {
                    case LINE_TRACKING:
                        if (intersectionFound && currentIntentFloat != 9.0f && (System.currentTimeMillis() - maneuverCooldown > 2500)) {
                            intersectionTriggerDist = calcDist;
                            maneuverEngine.sendPwmDirectly(0.0, 0.0);
                            currentState = RobotState.APPROACH_HALT;
                            haltStartTime = System.currentTimeMillis();
                            break;
                        }

                        updatePetriNetPlace("P_Turn", currentIntentFloat);
                        updatePetriNetPlace("P_Left", rawLeft);
                        updatePetriNetPlace("P_Right", rawRight);
                        updatePetriNetPlace("DistToTurn", calcDist);

                        try { Thread.sleep(40); } catch (InterruptedException e) {}

                        float rawError = getPetriNetValue("AngleOut");
                        float currentAngle = rawError * 1.1f;

                        int spinTimeMs = (int) Math.abs(currentAngle);
                        if (spinTimeMs > 0 && spinTimeMs < 30) { spinTimeMs = 30; }

                        float currentSpeed = getPetriNetValue("SpeedOut");

                        float triggerState = getPetriNetValue("ManeuverTrigger");
                        boolean executedMacro = false;

                        if (triggerState == 1.0f) {
                            if (rawLeft == 999.0f) {
                                maneuverEngine.laneShiftLeft(300);
                                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                                flushCameraBuffer();
                                maneuverCooldown = System.currentTimeMillis();
                                executedMacro = true;
                            }
                            updatePetriNetPlace("ManeuverTrigger", 0.0f);
                            updatePetriNetPlace("AngleOut", 0.0f);
                            if(executedMacro) break;
                        } else if (triggerState == 2.0f) {
                            if (rawRight == 999.0f) {
                                maneuverEngine.laneShiftRight(300);
                                try { Thread.sleep(1500); } catch (InterruptedException e) {}
                                flushCameraBuffer();
                                maneuverCooldown = System.currentTimeMillis();
                                executedMacro = true;
                            }
                            updatePetriNetPlace("ManeuverTrigger", 0.0f);
                            updatePetriNetPlace("AngleOut", 0.0f);
                            if(executedMacro) break;
                        }

                        if (triggerState > 0.0f) { updatePetriNetPlace("ManeuverTrigger", 0.0f); }

                        boolean intentLineVisible = false;
                        if (currentIntentFloat == 1.0f && rawLeft != 999.0f) intentLineVisible = true;
                        else if (currentIntentFloat == 2.0f && rawRight != 999.0f) intentLineVisible = true;
                        else if (currentIntentFloat == 0.0f && (rawLeft != 999.0f || rawRight != 999.0f)) intentLineVisible = true;

                        if (currentAngle < -0.1f) {
                            maneuverEngine.sendPwmDirectly(-50.0, 50.0);
                            try { Thread.sleep(spinTimeMs); } catch (InterruptedException e) {}
                            updatePetriNetPlace("AngleOut", 0.0f);

                        } else if (currentAngle > 0.1f) {
                            maneuverEngine.sendPwmDirectly(50.0, -50.0);
                            try { Thread.sleep(spinTimeMs); } catch (InterruptedException e) {}
                            updatePetriNetPlace("AngleOut", 0.0f);

                        } else if (intentLineVisible) {
                            if (currentSpeed > 0.1f) {
                                float driveSpeed = Math.max(30.0f, currentSpeed);
                                maneuverEngine.sendPwmDirectly(driveSpeed, driveSpeed);
                                try { Thread.sleep(150); } catch (InterruptedException e) {}
                            }
                        }

                        maneuverEngine.sendPwmDirectly(0.0, 0.0);
                        try { Thread.sleep(1500); } catch (InterruptedException e) {}
                        flushCameraBuffer();
                        break;

                    case APPROACH_HALT:
                        updatePetriNetPlace("stopSign", 1.0f);
                        freezePetriNet();

                        if (System.currentTimeMillis() - haltStartTime > 1000) {
                            updatePetriNetPlace("stopSign", 0.0f);
                            currentState = RobotState.STATIC_INTERSECTION_MANEUVER;
                        }
                        break;

                    case STATIC_INTERSECTION_MANEUVER:
                        freezePetriNet();

                        int initialPush = 600 + (int) (intersectionTriggerDist * 2.0f);
                        maneuverEngine.driveStraight(initialPush);

                        int turnDuration = 420;
                        maneuverEngine.executeIntersectionTurn(intent, turnDuration);

                        maneuverEngine.driveStraight(300);

                        maneuverEngine.sendPwmDirectly(0.0, 0.0);
                        try { Thread.sleep(1500); } catch (InterruptedException e) {}

                        updatePetriNetPlace("ManeuverTrigger", 0.0f);
                        updatePetriNetPlace("AngleOut", 0.0f);
                        maneuverEngine.setNavState(0.0f);
                        navigator.advanceToNextSegment();
                        flushCameraBuffer();
                        maneuverCooldown = System.currentTimeMillis();
                        currentState = RobotState.LINE_TRACKING;
                        break;
                }
            }
        }
        camera.release();
        HighGui.destroyAllWindows();
    }

    private TurnIntent parseTurnIntent(float intent) {
        if (intent == 1.0f) return TurnIntent.LEFT;
        if (intent == 2.0f) return TurnIntent.RIGHT;
        return TurnIntent.FRONT;
    }

    private void flushCameraBuffer() {
        Mat throwaway = new Mat();
        for(int i = 0; i < 15; i++) { camera.read(throwaway); }
    }

    private void updatePetriNetPlace(String placeName, float value) {
        for (Object p : brain.PlaceList) {
            if (p instanceof DataFloat) {
                DataFloat place = (DataFloat) p;
                if (place.GetName().equals(placeName)) {
                    place.SetValue(value);
                    break;
                }
            }
        }
    }

    private float getPetriNetValue(String placeName) {
        for (Object p : brain.PlaceList) {
            if (p instanceof DataFloat) {
                DataFloat place = (DataFloat) p;
                if (place.GetName().equals(placeName)) {
                    Object val = place.GetValue();
                    if (val == null) { return 0.0f; }
                    return (float) val;
                }
            }
        }
        return 0.0f;
    }

    public void stop() { isRunning = false; }
}