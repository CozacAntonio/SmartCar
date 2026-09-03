package ro.robot.hardware;

import Components.PetriNet;
import DataObjects.DataFloat;
import ro.robot.logic.TurnIntent;

public class ManeuverEngine {

    private final MotorController motorController;
    private final PetriNet brain;

    public ManeuverEngine(MotorController motorController, PetriNet brain) {
        this.motorController = motorController;
        this.brain = brain;
    }

    public void driveStraight(long durationMillis) {
        setNavState(4.0f);
        sendPwmDirectly(40.0, 40.0);
        try { Thread.sleep(durationMillis); } catch (InterruptedException e) {}
        sendPwmDirectly(0.0, 0.0);
    }

    public void executeIntersectionTurn(TurnIntent intent, long turnDurationMillis) {
        setNavState(4.0f);
        if (intent == TurnIntent.LEFT) {
            setPetriPlace("P_Turn", 1.0f);
            sendPwmDirectly(-50.0, 50.0);
        } else if (intent == TurnIntent.RIGHT) {
            setPetriPlace("P_Turn", 2.0f);
            sendPwmDirectly(50.0, -50.0);
        } else {
            return;
        }

        try { Thread.sleep(turnDurationMillis); } catch (InterruptedException e) {}
        sendPwmDirectly(0.0, 0.0);
    }

    public void sendPwmDirectly(double left, double right) {
        this.motorController.setMotorsDirectly(left, right);
    }

    public void setNavState(float stateValue) {
        setPetriPlace("NavState", stateValue);
    }

    private void setPetriPlace(String name, float value) {
        for (Object p : brain.PlaceList) {
            if (p instanceof DataFloat) {
                DataFloat place = (DataFloat) p;
                if (place.GetName().equals(name)) {
                    place.SetValue(value);
                    break;
                }
            }
        }
    }

    public void laneShiftLeft(int spinTimeMs) {
        setNavState(4.0f);
        sendPwmDirectly(-50.0, 50.0);
        try { Thread.sleep(spinTimeMs); } catch (InterruptedException e) {}

        sendPwmDirectly(50.0, 50.0);
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        sendPwmDirectly(50.0, -50.0);
        try { Thread.sleep(spinTimeMs+60); } catch (InterruptedException e) {}

        sendPwmDirectly(0.0, 0.0);
        setNavState(0.0f);
    }

    public void laneShiftRight(int spinTimeMs) {
        setNavState(4.0f);
        sendPwmDirectly(50.0, -50.0);
        try { Thread.sleep(spinTimeMs); } catch (InterruptedException e) {}

        sendPwmDirectly(50.0, 50.0);
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        sendPwmDirectly(-50.0, 50.0);
        try { Thread.sleep(spinTimeMs+50); } catch (InterruptedException e) {}

        sendPwmDirectly(0.0, 0.0);
        setNavState(0.0f);
    }
}