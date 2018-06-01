package lab3_202_03.uwaterloo.ca.a2048_game;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class myFSM2 {
    //FSM parameters-the same as the first FSM but for a different axis
    enum FSMStates{WAIT2, RISE2, FALL2, STABLE2, DETERMINED2};
    private myFSM2.FSMStates myStates2;

    //Signature parameters
    enum Signatures{UP, DOWN, UNDETERMINED};
    private myFSM2.Signatures mySig2;

    //These are the characteristic thresholds of my choice.
    //1st threshold: minimum slope of the response onset
    //2nd threshold: the maximum response amplitude of the first peak
    //3rd threshold: the maximum response amplitude after settling for 15 samples.
    private final float[] THRESHOLD_UP = {0.5f, 0.8f, -0.5f};
    private final float[] THRESHOLD_DOWN = {-0.8f,-0.8f,0.5f};

    //We expect the reading to appear on the next peak after 15 samples since the
    //occurrence of the maximum of the 1st response peak.
    private int sampleCounter;
    private final int SAMPLE_COUNTER_DEFAULT = 15;
    public static Timer timer1;
    public GameLoopTask task;

    //Keep the most recent historical reading so we can calculate the most recent slope
    private float previousReading;
    Context context;
    private TextView myDisplayTV;

    //The finite state machine is made to start from the WAIT state.
    public myFSM2(TextView displayTV, GameLoopTask gameloop,Context c){
        myStates2 = myFSM2.FSMStates.WAIT2;
        mySig2 = myFSM2.Signatures.UNDETERMINED;
        sampleCounter = SAMPLE_COUNTER_DEFAULT;
        previousReading = 0;
        myDisplayTV = displayTV;
        task=gameloop;
        context=c;
    }

    //Resetting the FSM
    public void resetFSM(){
        myStates2 = myFSM2.FSMStates.WAIT2;
        mySig2 = myFSM2.Signatures.UNDETERMINED;
        sampleCounter = SAMPLE_COUNTER_DEFAULT;
        previousReading = 0;
    }

    public void activateFSM(float accInput){
        //determining the slope between the most recent input and the most recent historical reading
        float accSlope = accInput - previousReading;
        switch(myStates2){
            case WAIT2:
                if(accSlope >= THRESHOLD_UP[0]){
                    myStates2 = myFSM2.FSMStates.RISE2;//if the slope is above zero it is expected that the device was moved up
                }
                if (accSlope <=THRESHOLD_DOWN[0]){
                    myStates2= myFSM2.FSMStates.FALL2;//if the slope is below zero it is expected that the device was moved down
                }
                else {
                    myStates2= FSMStates.WAIT2; //check until one of the above conditions is satisfied
                }
                break;
            case RISE2:
                if(accSlope <= 0){ //if the phone was moved up, the readings would reach a maximum then drop down again
                    if(previousReading >= THRESHOLD_UP[1]){ //previousReading holds the value of the maximum amplitude
                        myStates2 = myFSM2.FSMStates.STABLE2; //if amplitude is above threshold, move on
                    }
                    else{
                        myStates2 = myFSM2.FSMStates.DETERMINED2; //if amplitude is not above threshold, the reading is undetermined
                        mySig2 = myFSM2.Signatures.UNDETERMINED;
                    }
                }
                break;
            case FALL2:
                if(accSlope >= 0) { //if the phone was moved down, the readings would reach a minimum then rise up again
                    if (previousReading <= THRESHOLD_DOWN[1]) { //previousReading holds the value of the maximum amplitude
                        myStates2 = myFSM2.FSMStates.STABLE2; //if amplitude is above threshold, move on
                    } else {
                        myStates2 = myFSM2.FSMStates.DETERMINED2; //if amplitude is not above threshold, the reading is undetermined
                        mySig2 = myFSM2.Signatures.UNDETERMINED;
                    }
                }
            case STABLE2:
                //This part is to wait for the readings to drop or rise again.
                //Count down from 15 to 0.
                sampleCounter--;
                //Once reached zero, check the threshold and determine the gesture.
                if(sampleCounter == 0) {
                    myStates2 = myFSM2.FSMStates.DETERMINED2;

                    if (accSlope >= 0) { //if the phone was moved up, the readings would reach a minimum as the second peak then rise again
                        if (previousReading <= THRESHOLD_UP[2]) { //the previous value holds the amplitude after almost 15 counts
                            mySig2 = myFSM2.Signatures.UP; //if the amplitude is above the threshold which I would expect after several counts, the gesture is likely up
                        } else {
                            myStates2 = myFSM2.FSMStates.DETERMINED2; //if the amplitude is not above the threshold which I would expect, the gesture is undetermined
                            mySig2 = myFSM2.Signatures.UNDETERMINED;
                        }
                    }

                    if (accSlope <= 0) { //if the phone was moved down, the readings would reach a maximum as the second peak then fall again
                        if (previousReading >= THRESHOLD_DOWN[2]) { //the previous value holds the amplitude after almost 15 counts
                            mySig2 = myFSM2.Signatures.DOWN; //if the amplitude is above the threshold which I would expect after several counts, the gesture is likely down
                        } else {
                            myStates2 = myFSM2.FSMStates.DETERMINED2; //if the amplitude is not above the threshold which I would expect, the gesture is undetermined
                            mySig2 = myFSM2.Signatures.UNDETERMINED;
                        }
                    }
                }
                break;

            case DETERMINED2:
                //Once determined, report the gesture and reset the FSM.
                Log.d("My FSM Says:", String.format("I've got signature %s", mySig2.toString()));
                //Show the signature on the textview
                myDisplayTV.setText(mySig2.toString());
                task.setdirection(mySig2.toString(),context);
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
