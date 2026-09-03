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

public class AngleControllerBuilder {

    public static void build(PetriNet pn) {

        DataFloat cZero = new DataFloat(); cZero.SetName("cZero"); cZero.SetValue(0.0f); pn.ConstantPlaceList.add(cZero);
        DataFloat cLost = new DataFloat(); cLost.SetName("cLost"); cLost.SetValue(999.0f); pn.ConstantPlaceList.add(cLost);

        DataFloat cTurnForward = new DataFloat(); cTurnForward.SetName("cTurnForward"); cTurnForward.SetValue(0.0f); pn.ConstantPlaceList.add(cTurnForward);
        DataFloat cTurnLeft = new DataFloat(); cTurnLeft.SetName("cTurnLeft"); cTurnLeft.SetValue(1.0f); pn.ConstantPlaceList.add(cTurnLeft);
        DataFloat cTurnRight = new DataFloat(); cTurnRight.SetName("cTurnRight"); cTurnRight.SetValue(2.0f); pn.ConstantPlaceList.add(cTurnRight);

        DataFloat cLeftOuter = new DataFloat(); cLeftOuter.SetName("cLeftOuter"); cLeftOuter.SetValue(80.0f); pn.ConstantPlaceList.add(cLeftOuter);
        DataFloat cLeftInner = new DataFloat(); cLeftInner.SetName("cLeftInner"); cLeftInner.SetValue(140.0f); pn.ConstantPlaceList.add(cLeftInner);
        DataFloat cRightInner = new DataFloat(); cRightInner.SetName("cRightInner"); cRightInner.SetValue(500.0f); pn.ConstantPlaceList.add(cRightInner);
        DataFloat cRightOuter = new DataFloat(); cRightOuter.SetName("cRightOuter"); cRightOuter.SetValue(560.0f); pn.ConstantPlaceList.add(cRightOuter);

        DataFloat cMult = new DataFloat(); cMult.SetName("cMult"); cMult.SetValue(1.1f); pn.ConstantPlaceList.add(cMult);

        DataFloat pTurn = new DataFloat(); pTurn.SetName("P_Turn"); pTurn.SetValue(0.0f); pn.PlaceList.add(pTurn);

        DataFloat pLeft = new DataFloat(); pLeft.SetName("P_Left"); pLeft.SetValue(999.0f); pn.PlaceList.add(pLeft);
        DataFloat pRight = new DataFloat(); pRight.SetName("P_Right"); pRight.SetValue(999.0f); pn.PlaceList.add(pRight);

        DataFloat pAngleOut = new DataFloat(); pAngleOut.SetName("AngleOut"); pAngleOut.SetValue(0.0f); pn.PlaceList.add(pAngleOut);
        DataFloat pManeuverTrigger = new DataFloat(); pManeuverTrigger.SetName("ManeuverTrigger"); pManeuverTrigger.SetValue(0.0f); pn.PlaceList.add(pManeuverTrigger);

        ArrayList<String> leftMathL = new ArrayList<>(Arrays.asList("P_Left", "cLeftOuter"));
        ArrayList<String> leftMathR = new ArrayList<>(Arrays.asList("P_Left", "cLeftInner"));
        ArrayList<String> rightMathL = new ArrayList<>(Arrays.asList("P_Right", "cRightInner"));
        ArrayList<String> rightMathR = new ArrayList<>(Arrays.asList("P_Right", "cRightOuter"));


        PetriTransition tL1 = new PetriTransition(pn); tL1.TransitionName = "T_Left_Deadzone";
        tL1.InputPlaceName.add("P_Turn"); tL1.InputPlaceName.add("P_Left");
        Condition tL1_1 = new Condition(tL1, "P_Turn", TransitionCondition.Equal, "cTurnLeft");
        Condition tL1_2 = new Condition(tL1, "P_Left", TransitionCondition.MoreThanOrEqual, "cLeftOuter");
        Condition tL1_3 = new Condition(tL1, "P_Left", TransitionCondition.LessThanOrEqual, "cLeftInner");
        tL1_1.SetNextCondition(LogicConnector.AND, tL1_2); tL1_2.SetNextCondition(LogicConnector.AND, tL1_3);
        GuardMapping grdL1 = new GuardMapping(); grdL1.condition = tL1_1;
        grdL1.Activations.add(new Activation(tL1, "cZero", TransitionOperation.Move, "AngleOut"));
        grdL1.Activations.add(new Activation(tL1, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdL1.Activations.add(new Activation(tL1, "P_Left", TransitionOperation.Move, "P_Left"));
        tL1.GuardMappingList.add(grdL1); pn.Transitions.add(tL1);

        PetriTransition tL2 = new PetriTransition(pn); tL2.TransitionName = "T_Left_Correct_L";
        tL2.InputPlaceName.add("P_Turn"); tL2.InputPlaceName.add("P_Left");
        Condition tL2_1 = new Condition(tL2, "P_Turn", TransitionCondition.Equal, "cTurnLeft");
        Condition tL2_2 = new Condition(tL2, "P_Left", TransitionCondition.LessThan, "cLeftOuter");
        tL2_1.SetNextCondition(LogicConnector.AND, tL2_2);
        GuardMapping grdL2 = new GuardMapping(); grdL2.condition = tL2_1;
        grdL2.Activations.add(new Activation(tL2, leftMathL, TransitionOperation.Sub, "AngleOut"));
        grdL2.Activations.add(new Activation(tL2, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdL2.Activations.add(new Activation(tL2, "P_Left", TransitionOperation.Move, "P_Left"));
        tL2.GuardMappingList.add(grdL2); pn.Transitions.add(tL2);

        PetriTransition tL3 = new PetriTransition(pn); tL3.TransitionName = "T_Left_Correct_R";
        tL3.InputPlaceName.add("P_Turn"); tL3.InputPlaceName.add("P_Left");
        Condition tL3_1 = new Condition(tL3, "P_Turn", TransitionCondition.Equal, "cTurnLeft");
        Condition tL3_2 = new Condition(tL3, "P_Left", TransitionCondition.MoreThan, "cLeftInner");
        Condition tL3_3 = new Condition(tL3, "P_Left", TransitionCondition.NotEqual, "cLost");
        tL3_1.SetNextCondition(LogicConnector.AND, tL3_2); tL3_2.SetNextCondition(LogicConnector.AND, tL3_3);
        GuardMapping grdL3 = new GuardMapping(); grdL3.condition = tL3_1;
        grdL3.Activations.add(new Activation(tL3, leftMathR, TransitionOperation.Sub, "AngleOut"));
        grdL3.Activations.add(new Activation(tL3, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdL3.Activations.add(new Activation(tL3, "P_Left", TransitionOperation.Move, "P_Left"));
        tL3.GuardMappingList.add(grdL3); pn.Transitions.add(tL3);

        PetriTransition tL4 = new PetriTransition(pn); tL4.TransitionName = "T_Left_Macro";
        tL4.InputPlaceName.add("P_Turn"); tL4.InputPlaceName.add("P_Left");
        Condition tL4_1 = new Condition(tL4, "P_Turn", TransitionCondition.Equal, "cTurnLeft");
        Condition tL4_2 = new Condition(tL4, "P_Left", TransitionCondition.Equal, "cLost");
        tL4_1.SetNextCondition(LogicConnector.AND, tL4_2);
        GuardMapping grdL4 = new GuardMapping(); grdL4.condition = tL4_1;
        grdL4.Activations.add(new Activation(tL4, "cTurnLeft", TransitionOperation.Move, "ManeuverTrigger"));
        grdL4.Activations.add(new Activation(tL4, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdL4.Activations.add(new Activation(tL4, "P_Left", TransitionOperation.Move, "P_Left"));
        tL4.GuardMappingList.add(grdL4); pn.Transitions.add(tL4);


        PetriTransition tR1 = new PetriTransition(pn); tR1.TransitionName = "T_Right_Deadzone";
        tR1.InputPlaceName.add("P_Turn"); tR1.InputPlaceName.add("P_Right");
        Condition tR1_1 = new Condition(tR1, "P_Turn", TransitionCondition.Equal, "cTurnRight");
        Condition tR1_2 = new Condition(tR1, "P_Right", TransitionCondition.MoreThanOrEqual, "cRightInner");
        Condition tR1_3 = new Condition(tR1, "P_Right", TransitionCondition.LessThanOrEqual, "cRightOuter");
        tR1_1.SetNextCondition(LogicConnector.AND, tR1_2); tR1_2.SetNextCondition(LogicConnector.AND, tR1_3);
        GuardMapping grdR1 = new GuardMapping(); grdR1.condition = tR1_1;
        grdR1.Activations.add(new Activation(tR1, "cZero", TransitionOperation.Move, "AngleOut"));
        grdR1.Activations.add(new Activation(tR1, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdR1.Activations.add(new Activation(tR1, "P_Right", TransitionOperation.Move, "P_Right"));
        tR1.GuardMappingList.add(grdR1); pn.Transitions.add(tR1);

        PetriTransition tR2 = new PetriTransition(pn); tR2.TransitionName = "T_Right_Correct_L";
        tR2.InputPlaceName.add("P_Turn"); tR2.InputPlaceName.add("P_Right");
        Condition tR2_1 = new Condition(tR2, "P_Turn", TransitionCondition.Equal, "cTurnRight");
        Condition tR2_2 = new Condition(tR2, "P_Right", TransitionCondition.LessThan, "cRightInner");
        tR2_1.SetNextCondition(LogicConnector.AND, tR2_2);
        GuardMapping grdR2 = new GuardMapping(); grdR2.condition = tR2_1;
        grdR2.Activations.add(new Activation(tR2, rightMathL, TransitionOperation.Sub, "AngleOut"));
        grdR2.Activations.add(new Activation(tR2, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdR2.Activations.add(new Activation(tR2, "P_Right", TransitionOperation.Move, "P_Right"));
        tR2.GuardMappingList.add(grdR2); pn.Transitions.add(tR2);

        PetriTransition tR3 = new PetriTransition(pn); tR3.TransitionName = "T_Right_Correct_R";
        tR3.InputPlaceName.add("P_Turn"); tR3.InputPlaceName.add("P_Right");
        Condition tR3_1 = new Condition(tR3, "P_Turn", TransitionCondition.Equal, "cTurnRight");
        Condition tR3_2 = new Condition(tR3, "P_Right", TransitionCondition.MoreThan, "cRightOuter");
        Condition tR3_3 = new Condition(tR3, "P_Right", TransitionCondition.NotEqual, "cLost");
        tR3_1.SetNextCondition(LogicConnector.AND, tR3_2); tR3_2.SetNextCondition(LogicConnector.AND, tR3_3);
        GuardMapping grdR3 = new GuardMapping(); grdR3.condition = tR3_1;
        grdR3.Activations.add(new Activation(tR3, rightMathR, TransitionOperation.Sub, "AngleOut"));
        grdR3.Activations.add(new Activation(tR3, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdR3.Activations.add(new Activation(tR3, "P_Right", TransitionOperation.Move, "P_Right"));
        tR3.GuardMappingList.add(grdR3); pn.Transitions.add(tR3);

        PetriTransition tR4 = new PetriTransition(pn); tR4.TransitionName = "T_Right_Macro";
        tR4.InputPlaceName.add("P_Turn"); tR4.InputPlaceName.add("P_Right");
        Condition tR4_1 = new Condition(tR4, "P_Turn", TransitionCondition.Equal, "cTurnRight");
        Condition tR4_2 = new Condition(tR4, "P_Right", TransitionCondition.Equal, "cLost");
        tR4_1.SetNextCondition(LogicConnector.AND, tR4_2);
        GuardMapping grdR4 = new GuardMapping(); grdR4.condition = tR4_1;
        grdR4.Activations.add(new Activation(tR4, "cTurnRight", TransitionOperation.Move, "ManeuverTrigger"));
        grdR4.Activations.add(new Activation(tR4, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdR4.Activations.add(new Activation(tR4, "P_Right", TransitionOperation.Move, "P_Right"));
        tR4.GuardMappingList.add(grdR4); pn.Transitions.add(tR4);


        PetriTransition tF_Deadzone = new PetriTransition(pn); tF_Deadzone.TransitionName = "T_Forward_Deadzone";
        tF_Deadzone.InputPlaceName.add("P_Turn"); tF_Deadzone.InputPlaceName.add("P_Left");
        Condition tF1_1 = new Condition(tF_Deadzone, "P_Turn", TransitionCondition.Equal, "cTurnForward");
        Condition tF1_2 = new Condition(tF_Deadzone, "P_Left", TransitionCondition.MoreThanOrEqual, "cLeftOuter");
        Condition tF1_3 = new Condition(tF_Deadzone, "P_Left", TransitionCondition.LessThanOrEqual, "cLeftInner");
        tF1_1.SetNextCondition(LogicConnector.AND, tF1_2); tF1_2.SetNextCondition(LogicConnector.AND, tF1_3);
        GuardMapping grdF1 = new GuardMapping(); grdF1.condition = tF1_1;
        grdF1.Activations.add(new Activation(tF_Deadzone, "cZero", TransitionOperation.Move, "AngleOut"));
        grdF1.Activations.add(new Activation(tF_Deadzone, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdF1.Activations.add(new Activation(tF_Deadzone, "P_Left", TransitionOperation.Move, "P_Left"));
        tF_Deadzone.GuardMappingList.add(grdF1); pn.Transitions.add(tF_Deadzone);

        PetriTransition tF_Correct_L = new PetriTransition(pn); tF_Correct_L.TransitionName = "T_Forward_Correct_L";
        tF_Correct_L.InputPlaceName.add("P_Turn"); tF_Correct_L.InputPlaceName.add("P_Left");
        Condition tF2_1 = new Condition(tF_Correct_L, "P_Turn", TransitionCondition.Equal, "cTurnForward");
        Condition tF2_2 = new Condition(tF_Correct_L, "P_Left", TransitionCondition.LessThan, "cLeftOuter");
        tF2_1.SetNextCondition(LogicConnector.AND, tF2_2);
        GuardMapping grdF2 = new GuardMapping(); grdF2.condition = tF2_1;
        grdF2.Activations.add(new Activation(tF_Correct_L, leftMathL, TransitionOperation.Sub, "AngleOut"));
        grdF2.Activations.add(new Activation(tF_Correct_L, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdF2.Activations.add(new Activation(tF_Correct_L, "P_Left", TransitionOperation.Move, "P_Left"));
        tF_Correct_L.GuardMappingList.add(grdF2); pn.Transitions.add(tF_Correct_L);

        PetriTransition tF_Correct_R = new PetriTransition(pn); tF_Correct_R.TransitionName = "T_Forward_Correct_R";
        tF_Correct_R.InputPlaceName.add("P_Turn"); tF_Correct_R.InputPlaceName.add("P_Left");
        Condition tF3_1 = new Condition(tF_Correct_R, "P_Turn", TransitionCondition.Equal, "cTurnForward");
        Condition tF3_2 = new Condition(tF_Correct_R, "P_Left", TransitionCondition.MoreThan, "cLeftInner");
        Condition tF3_3 = new Condition(tF_Correct_R, "P_Left", TransitionCondition.NotEqual, "cLost");
        tF3_1.SetNextCondition(LogicConnector.AND, tF3_2); tF3_2.SetNextCondition(LogicConnector.AND, tF3_3);
        GuardMapping grdF3 = new GuardMapping(); grdF3.condition = tF3_1;
        grdF3.Activations.add(new Activation(tF_Correct_R, leftMathR, TransitionOperation.Sub, "AngleOut"));
        grdF3.Activations.add(new Activation(tF_Correct_R, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdF3.Activations.add(new Activation(tF_Correct_R, "P_Left", TransitionOperation.Move, "P_Left"));
        tF_Correct_R.GuardMappingList.add(grdF3); pn.Transitions.add(tF_Correct_R);

        PetriTransition tF_Lost = new PetriTransition(pn); tF_Lost.TransitionName = "T_Forward_Lost";
        tF_Lost.InputPlaceName.add("P_Turn"); tF_Lost.InputPlaceName.add("P_Left");
        Condition tF4_1 = new Condition(tF_Lost, "P_Turn", TransitionCondition.Equal, "cTurnForward");
        Condition tF4_2 = new Condition(tF_Lost, "P_Left", TransitionCondition.Equal, "cLost");
        tF4_1.SetNextCondition(LogicConnector.AND, tF4_2);
        GuardMapping grdF4 = new GuardMapping(); grdF4.condition = tF4_1;
        grdF4.Activations.add(new Activation(tF_Lost, "cZero", TransitionOperation.Move, "AngleOut"));
        grdF4.Activations.add(new Activation(tF_Lost, "P_Turn", TransitionOperation.Move, "P_Turn"));
        grdF4.Activations.add(new Activation(tF_Lost, "P_Left", TransitionOperation.Move, "P_Left"));
        tF_Lost.GuardMappingList.add(grdF4); pn.Transitions.add(tF_Lost);
    }
}