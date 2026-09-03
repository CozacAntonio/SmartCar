package ro.robot;

import Components.PetriNet;
import ro.robot.hardware.ManeuverEngine;
import ro.robot.hardware.MotorController;
import ro.robot.hardware.UltrasonicTracker;
import ro.robot.logic.AngleControllerBuilder;
import ro.robot.logic.SpeedControllerBuilder;
import ro.robot.vision.CameraTracker;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;

public class MainVehicleBrain {

    public static void main(String[] args) {
        System.out.println("Booting Autonomous System...");

        Context sharedPi4j = Pi4J.newAutoContext();

        PetriNet vehicleBrain = new PetriNet();
        vehicleBrain.PetriNetName = "Main Autonomous Controller";

        AngleControllerBuilder.build(vehicleBrain);
        SpeedControllerBuilder.build(vehicleBrain);

        System.out.println("Initializing Motor Hardware...");
        MotorController motorController = new MotorController(sharedPi4j);
        ManeuverEngine maneuverEngine = new ManeuverEngine(motorController, vehicleBrain);

        System.out.println("Waking up Ultrasonic Sensor...");
        UltrasonicTracker ultraTracker = new UltrasonicTracker(vehicleBrain, sharedPi4j);
        Thread ultraThread = new Thread(ultraTracker);
        ultraThread.start();

        System.out.println("Waking up Camera & FSM...");
        CameraTracker eyes = new CameraTracker(vehicleBrain, maneuverEngine);
        Thread visionThread = new Thread(eyes);
        visionThread.start();

        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        System.out.println("Starting Petri Net Engine...");
        Thread brainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                vehicleBrain.Start();
            }
        });
        brainThread.start();
    }
}