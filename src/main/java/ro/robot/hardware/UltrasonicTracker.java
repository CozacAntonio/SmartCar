package ro.robot.hardware;

import Components.PetriNet;
import DataObjects.DataFloat;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;

public class UltrasonicTracker implements Runnable {

    private PetriNet brain;
    private boolean isRunning = true;

    private final Context pi4j;
    private final DigitalOutput triggerPin;
    private final DigitalInput echoPin;

    public UltrasonicTracker(PetriNet brain, Context pi4j) {
        this.brain = brain;
        this.pi4j = pi4j;

        System.out.println("Initializing Ultrasonic Hardware (Pi4J v2 / pigpio) on GPIO 17 & 27...");

        this.triggerPin = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .address(17)
                .provider("pigpio-digital-output")
                .build());

        this.echoPin = pi4j.create(DigitalInput.newConfigBuilder(pi4j)
                .address(27)
                .provider("pigpio-digital-input")
                .build());
    }

    @Override
    public void run() {
        System.out.println("Ultrasonic Tracker Started. Pinging environment...");

        while (isRunning) {
            try {
                float distance = pingSensor();

                if (distance > 0.0f && distance < 400.0f) {
                    updatePetriNetPlace("sensorDistSpeed", distance);
                }

                Thread.sleep(60);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private float pingSensor() {
        try {
            triggerPin.low();
            Thread.sleep(2);

            triggerPin.high();
            long end = System.nanoTime() + 10000;
            while(System.nanoTime() < end) { }
            triggerPin.low();

            long startTime = System.nanoTime();
            long timeout = startTime + 1000000000L;

            while (echoPin.isLow()) {
                startTime = System.nanoTime();
                if (startTime > timeout) return -1.0f;
            }

            long endTime = System.nanoTime();
            timeout = startTime + 1000000000L;
            while (echoPin.isHigh()) {
                endTime = System.nanoTime();
                if (endTime > timeout) return -1.0f;
            }

            long duration = endTime - startTime;
            float distanceCm = (duration / 1000.0f) * 0.0343f / 2.0f;

            return distanceCm;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1.0f;
        }
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

    public void stop() {
        isRunning = false;
        pi4j.shutdown();
    }
}