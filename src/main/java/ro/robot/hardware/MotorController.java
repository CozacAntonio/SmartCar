package ro.robot.hardware;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;

public class MotorController {

    private final Context pi4j;

    private final DigitalOutput leftDirM1;
    private final DigitalOutput rightDirM2;
    private final Pwm leftSpeedE1;
    private final Pwm rightSpeedE2;

    public MotorController(Context pi4j) {
        this.pi4j = pi4j;
        System.out.println("Initializing Motors with PiGPIO Native Provider...");

        this.leftDirM1 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .address(23).provider("pigpio-digital-output").build());

        this.rightDirM2 = pi4j.create(DigitalOutput.newConfigBuilder(pi4j)
                .address(16).provider("pigpio-digital-output").build());

        this.leftSpeedE1 = pi4j.create(Pwm.newConfigBuilder(pi4j)
                .address(12).pwmType(PwmType.SOFTWARE).provider("pigpio-pwm").build());

        this.rightSpeedE2 = pi4j.create(Pwm.newConfigBuilder(pi4j)
                .address(13).pwmType(PwmType.SOFTWARE).provider("pigpio-pwm").build());
    }

    public void setMotorsDirectly(double leftPwm, double rightPwm) {
        System.out.println("Motors -> Left PWM: " + leftPwm + " | Right PWM: " + rightPwm);

        if (leftPwm >= 0) { leftDirM1.low(); } else { leftDirM1.high(); }
        leftSpeedE1.on(Math.min(100, Math.max(0, Math.abs(leftPwm))), 100);

        if (rightPwm >= 0) { rightDirM2.low(); } else { rightDirM2.high(); }
        rightSpeedE2.on(Math.min(100, Math.max(0, Math.abs(rightPwm))), 100);
    }

    public void stop() {
        leftSpeedE1.off();
        rightSpeedE2.off();
        leftDirM1.low();
        rightDirM2.low();
    }

    public void shutdown() {
        stop();
        pi4j.shutdown();
    }
}