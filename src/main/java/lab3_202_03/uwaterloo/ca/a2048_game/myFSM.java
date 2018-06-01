package lab3_202_03.uwaterloo.ca.a2048_game;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class myFSM {
    //FSM parameters
    enum FSMStates{WAIT, RISE, FALL, STABLE, DETERMINED};
    private FSMStates myStates;

    //Signature parameters
    enum Signatures{LEFT, RIGHT, UNDETERMINED};
    private Signatures mySig;

    //These are the characteristic thresholds of my choice.
    //1st threshold: minimum slope of the response onset
    //2nd threshold: the maximum response amplitude of the first peak
    //3rd threshold: the maximum response amplitude after settling for 15 samples.
    private final float[] THRESHOLD_RIGHT = {0.8f, 1.2f, -0.5f};
    private final float[] THRESHOLD_LEFT = {-0.5f,-0.6f,0.2f};
    //We expect the reading to appear on the next peak after 15 samples since the
    //occurrence of the maximum of the 1st response peak.
    private int sampleCounter;
    private final int SAMPLE_COUNTER_DEFAULT = 15;
    //most recent historical reading is used so that we can calculate the most recent slope
    private float previousReading;
    Context context;
    private TextView myDisplayTV;
    public GameLoopTask task;

    //The finite state machine is made to start from the WAIT state.
    public myFSM(TextView displayTV,GameLoopTask gameloop,Context c){
        myStates = FSMStates.WAIT;
        mySig= Signatures.UNDETERMINED;
        sampleCounter = SAMPLE_COUNTER_DEFAULT;
        previousReading = 0;
        myDisplayTV = displayTV;
        task=gameloop;
        context=c;
    }

    //Resetting the FSM
    public void resetFSM(){
        myStates = FSMStates.WAIT;
        mySig = Signatures.UNDETERMINED;
        sampleCounter = SAMPLE_COUNTER_DEFAULT;
        previousReading = 0;
    }

    public void activateFSM(float accInput){
        //determining the slope between the most recent input and the most recent historical reading
        float accSlope = accInput - previousReading;
        switch(myStates){
            case WAIT:
                if(accSlope >= THRESHOLD_RIGHT[0]){
                    myStates = FSMStates.RISE; //if the slope is above zero it is expected that the device was moved to the right
                }
                if (accSlope <=THRESHOLD_LEFT[0]){
                    myStates=FSMStates.FALL; //if the slope is below zero it is expected that the device was moved to the left
                }
                else {
                    myStates=FSMStates.WAIT; //check until one of the above conditions is satisfied
                }
                break;
            case RISE:
                if(accSlope <= 0){ //if the phone was moved to the right, the readings would reach a maximum then drop down again

                    if(previousReading >= THRESHOLD_RIGHT[1]){ //previousReading holds the value of the maximum amplitude
                        myStates = FSMStates.STABLE; //if amplitude is above threshold, move on
                    }
                    else{
                        myStates = FSMStates.DETERMINED; //if amplitude is not above threshold, the reading is undetermined
                        mySig = Signatures.UNDETERMINED;
                    }
                }
                break;
            case FALL:
                if(accSlope >= 0) { //if the phone was moved to the left, the readings would reach a minimum then rise up again
                    if (previousReading <= THRESHOLD_LEFT[1]) { //previousReading holds the value of the maximum amplitude
                        myStates = FSMStates.STABLE;//if amplitude is above threshold, move on
                    } else {
                        myStates = FSMStates.DETERMINED; //if amplitude is not above threshold, the reading is undetermined
                        mySig = Signatures.UNDETERMINED;
                    }
                }

            case STABLE:
                //This part is to wait for the readings to drop or rise again.
                //Count down from 15 to 0.
                sampleCounter--;
                //Once reached zero, check the threshold and determine the gesture.
                if(sampleCounter == 0) {
                    myStates = FSMStates.DETERMINED;

                    if (accSlope >= 0) { //if the phone was moved to the right, the readings would reach a minimum as the second peak then rise again
                        if (previousReading <= THRESHOLD_RIGHT[2]) { //the previous value holds the amplitude after almost 15 counts
                            mySig = Signatures.RIGHT; //if the amplitude is above the threshold which I would expect after several counts, the gesture is likely right
                        } else {
                            myStates = FSMStates.DETERMINED; //if the amplitude is not above the threshold which I would expect, the gesture is undetermined
                            mySig = Signatures.UNDETERMINED;
                        }
                    }

                    if (accSlope <= 0) { //if the phone was moved to the left, the readings would reach a maximum as the second peak then fall again
                        if (previousReading >= THRESHOLD_LEFT[2]) { //the previous value holds the amplitude after almost 15 counts
                            mySig = Signatures.LEFT; //if the amplitude is above the threshold which I would expect after several counts, the gesture is likely left
                        } else {
                            myStates = FSMStates.DETERMINED; //if the amplitude is not above the threshold which I would expect, the gesture is undetermined
                            mySig = Signatures.UNDETERMINED;
                        }
                    }
                }
                break;

            case DETERMINED:
                //Once determined, report the gesture and reset the FSM.
                Log.d("My FSM Says:", String.format("I've got signature %s", mySig.toString()));
                //Show the signature on the textview
                myDisplayTV.setText(mySig.toString());
                task.setdirection(mySig.toString(),context);
                resetFSM();
                break;

            default:
                resetFSM();
                break;
        }
        //records the input as the most recent previous reading
        previousReading = accInput;
    }
}

