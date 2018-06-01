package lab3_202_03.uwaterloo.ca.a2048_game;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;


public class Game_2048 extends AppCompatActivity {
    String text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_2048);
        //label references to the xml file and Linear Layout ensures that the text boxes are all presented in a linear fashion
        RelativeLayout r1 = (RelativeLayout) findViewById(R.id.label);

        TextView tv4 = new TextView(getApplicationContext());
        TextView tv5 = new TextView(getApplicationContext());
        //r1.addView(tv4); //adding the text view label used to display data
        tv4.setTextSize(50);
        tv4.setTextColor(Color.BLACK);
        r1.setBackgroundColor(0);
        tv4.setY(900);

        r1.addView(tv5);
        tv5.setText(text);
        tv5.setTextSize(40);
        tv5.setTextColor(Color.BLACK);
        tv5.setY(700);

        r1.getLayoutParams().height=1000;
        r1.getLayoutParams().width=1000;
        r1.setBackgroundResource(R.drawable.gameboard);

        Timer myGameLoop=new Timer();
        GameLoopTask myGameLoopTask=new GameLoopTask(this,r1,getApplicationContext());
        myGameLoop.schedule(myGameLoopTask,16,16);
        //request for sensor manager
        SensorManager sensorManager2 = (SensorManager) getSystemService(SENSOR_SERVICE);
        //request for the accelerometer sensor
        Sensor AccelerometerSensor = sensorManager2.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //creating event handler and setting the input values
        final AccelerometerEventHandler l2 = new AccelerometerEventHandler(tv4,myGameLoopTask);
        //Registering the event listener with the sensor manager
        sensorManager2.registerListener(l2, AccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    class AccelerometerEventHandler implements SensorEventListener {
        TextView output;
        private myFSM FSM_X;
        private myFSM2 FSM_Y;
        float [] in=new float[2];

        public AccelerometerEventHandler(TextView outputView,GameLoopTask gameloop) {
            output = outputView;
            FSM_X = new myFSM(output,gameloop,getApplicationContext());
            FSM_Y = new myFSM2(output,gameloop,getApplicationContext());
        }

        public void onAccuracyChanged(Sensor s, int i) {
        }

        public void onSensorChanged(SensorEvent se) {
            if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //creating a low pass filter to smooth out the data collected
                in[0]+=(se.values[0]-in[0])/10;
                in[1]+=(se.values[1]-in[1])/10;
                output.setTextSize(40);
                //calling the finite state machines to determine gestures
                FSM_X.activateFSM(in[0]);
                FSM_Y.activateFSM(in[1]);
            }
        }
    }
}