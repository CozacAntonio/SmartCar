package ro.robot.logic;

import Components.Activation;
import Components.Condition;
import Components.GuardMapping;
import Components.PetriNet;
import Components.PetriTransition;
import DataObjects.DataFloat;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

import java.util.ArrayList;
import java.util.Arrays;

public class SpeedControllerBuilder {

    public static void build(PetriNet pn) {

        DataFloat c25 = new DataFloat(); c25.SetName("c25"); c25.SetValue(25.0f); pn.ConstantPlaceList.add(c25);
        DataFloat c15 = new DataFloat(); c15.SetName("c15"); c15.SetValue(15.0f); pn.ConstantPlaceList.add(c15);

        DataFloat c40 = new DataFloat(); c40.SetName("c40"); c40.SetValue(40.0f); pn.ConstantPlaceList.add(c40);
        DataFloat c0 = new DataFloat();  c0.SetName("c0");  c0.SetValue(0.0f);  pn.ConstantPlaceList.add(c0);
        DataFloat cFalse = new DataFloat(); cFalse.SetName("cFalse"); cFalse.SetValue(0.0f); pn.ConstantPlaceList.add(cFalse);
        DataFloat cTrue = new DataFloat();  cTrue.SetName("cTrue");  cTrue.SetValue(1.0f);  pn.ConstantPlaceList.add(cTrue);

        DataFloat pSensorDistance = new DataFloat(); pSensorDistance.SetName("sensorDistSpeed"); pSensorDistance.SetValue(100.0f); pn.PlaceList.add(pSensorDistance);
        DataFloat pStopSign = new DataFloat(); pStopSign.SetName("stopSign"); pStopSign.SetValue(0.0f); pn.PlaceList.add(pStopSign);
        DataFloat pSpeedOut = new DataFloat(); pSpeedOut.SetName("SpeedOut"); pSpeedOut.SetValue(0.0f); pn.PlaceList.add(pSpeedOut);

        ArrayList<String> pControllerMathAdd = new ArrayList<>(Arrays.asList("sensorDistSpeed", "c15"));

        PetriTransition t1 = new PetriTransition(pn);
        t1.TransitionName = "T1_Cruise";
        t1.InputPlaceName.add("sensorDistSpeed");

        Condition t1_c1 = new Condition(t1, "sensorDistSpeed", TransitionCondition.MoreThan, "c25");
        GuardMapping grdT1 = new GuardMapping(); grdT1.condition = t1_c1;

        grdT1.Activations.add(new Activation(t1, "c40", TransitionOperation.Move, "SpeedOut"));
        grdT1.Activations.add(new Activation(t1, "cFalse", TransitionOperation.Move, "stopSign"));
        grdT1.Activations.add(new Activation(t1, "sensorDistSpeed", TransitionOperation.Move, "sensorDistSpeed"));
        t1.GuardMappingList.add(grdT1); pn.Transitions.add(t1);

        PetriTransition t2 = new PetriTransition(pn);
        t2.TransitionName = "T2_ProportionalBrake";
        t2.InputPlaceName.add("sensorDistSpeed");

        Condition t2_c1 = new Condition(t2, "sensorDistSpeed", TransitionCondition.LessThanOrEqual, "c25");
        Condition t2_c2 = new Condition(t2, "sensorDistSpeed", TransitionCondition.MoreThan, "c15");
        t2_c1.SetNextCondition(LogicConnector.AND, t2_c2);
        GuardMapping grdT2 = new GuardMapping(); grdT2.condition = t2_c1;

        grdT2.Activations.add(new Activation(t2, pControllerMathAdd, TransitionOperation.Add, "SpeedOut"));
        grdT2.Activations.add(new Activation(t2, "cFalse", TransitionOperation.Move, "stopSign"));
        grdT2.Activations.add(new Activation(t2, "sensorDistSpeed", TransitionOperation.Move, "sensorDistSpeed"));
        t2.GuardMappingList.add(grdT2); pn.Transitions.add(t2);

        PetriTransition t3 = new PetriTransition(pn);
        t3.TransitionName = "T3_ObstacleStop";
        t3.InputPlaceName.add("sensorDistSpeed");

        Condition t3_c1 = new Condition(t3, "sensorDistSpeed", TransitionCondition.LessThanOrEqual, "c15");
        GuardMapping grdT3 = new GuardMapping(); grdT3.condition = t3_c1;

        grdT3.Activations.add(new Activation(t3, "c0", TransitionOperation.Move, "SpeedOut"));
        grdT3.Activations.add(new Activation(t3, "cTrue", TransitionOperation.Move, "stopSign"));
        grdT3.Activations.add(new Activation(t3, "sensorDistSpeed", TransitionOperation.Move, "sensorDistSpeed"));
        t3.GuardMappingList.add(grdT3); pn.Transitions.add(t3);
    }
}