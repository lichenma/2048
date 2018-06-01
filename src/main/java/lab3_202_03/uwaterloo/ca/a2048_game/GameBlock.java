package lab3_202_03.uwaterloo.ca.a2048_game;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class GameBlock extends android.support.v7.widget.AppCompatImageView {

    private float positionX;
    private float velocityX;
    private float positionY;
    private float velocityY;
    public boolean merged;
    public boolean erased;
    public boolean moving=false;
    Random r=new Random();
    protected int value=2*(r.nextInt(2)+1);
    private float LEFT_BOUND=-74f;
    private float RIGHT_BOUND=679f;
    private float UPPER_BOUND=-74f;
    private float LOWER_BOUND=679f;
    // upper and lower bound values determined through trial and error
    RelativeLayout myr1;
    private float acceleration=1;
    TextView tv1;

    GameBlock(Context myContext, int CoordX, int CoordY,RelativeLayout r1){
        super(myContext);
        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(0.65f);
        this.setScaleY(0.65f); //setting up the scale for the block for aesthetics
        this.setX(CoordX);
        this.setY(CoordY);
        positionX = CoordX;
        positionY=CoordY;
        velocityX =0;
        velocityY=0;
        myr1=r1;
        myr1.addView(this);
        tv1=new TextView(myContext);
        tv1.setTextSize(50);
        tv1.setText(String.format("%d",value));
        tv1.setX(CoordX+150);
        tv1.setY(CoordY+100);
        bringToFront();
        myr1.addView(tv1);
        erased=false;
    }

    public void setDirection(String input){

        if ((input=="LEFT")&& (positionX==LEFT_BOUND+251*2 ||positionX==LEFT_BOUND+251 ||positionX==LEFT_BOUND || positionX==RIGHT_BOUND) && (positionY==UPPER_BOUND+251*2 ||positionY==UPPER_BOUND+251 ||positionY==UPPER_BOUND || positionY==LOWER_BOUND)) {
            velocityX=-20;
        }

        else if ((input=="RIGHT")&& (positionX==LEFT_BOUND+251*2 ||positionX==LEFT_BOUND+251 ||positionX==LEFT_BOUND || positionX==RIGHT_BOUND) && (positionY==UPPER_BOUND+251*2 ||positionY==UPPER_BOUND+251 ||positionY==UPPER_BOUND || positionY==LOWER_BOUND)) {
            velocityX=5;
        }

        else if ((input=="UNDETERMINED")&& (positionX==LEFT_BOUND+251*2 ||positionX==LEFT_BOUND+251 ||positionX==LEFT_BOUND || positionX==RIGHT_BOUND)&& (positionY==UPPER_BOUND+251*2 ||positionY==UPPER_BOUND+251 ||positionY==UPPER_BOUND || positionY==LOWER_BOUND)){
            velocityX=0;
            velocityY=0;
        }

        else if ((input=="UP")&&(positionX==LEFT_BOUND+251*2 ||positionX==LEFT_BOUND+251 ||positionX==LEFT_BOUND || positionX==RIGHT_BOUND)&&(positionY==UPPER_BOUND+251*2 ||positionY==UPPER_BOUND+251 ||positionY==UPPER_BOUND || positionY==LOWER_BOUND)){
            velocityY=-20;
        }

        else if ((input=="DOWN")&& (positionX==LEFT_BOUND+251*2 ||positionX==LEFT_BOUND+251 ||positionX==LEFT_BOUND || positionX==RIGHT_BOUND) && (positionY==UPPER_BOUND+251*2 ||positionY==UPPER_BOUND+251 ||positionY==UPPER_BOUND || positionY==LOWER_BOUND)){ //comparing the direction string from the finite state machine with multiple test cases
            //ensures that blocks only move when they reach an endpoint
            velocityY=5;
        }
    }


    public void setdestinationright(float x,int block, int slot){

        RIGHT_BOUND=x; //sets the right bound in order for the blocks to not pass through one another when the reach the right hand side
    }

    public void setdestinationleft(float x,int block, int slot){
        LEFT_BOUND=x;
    }

    public void setdestinationup(float x,int block, int slot){

        UPPER_BOUND=x;
    }

    public void setdestinationdown(float x,int block, int slot){

        LOWER_BOUND=x;
    }

    public boolean isMoving(){
        return moving;
    }

    public void move(){
        //Constant Velocity Displacement
        moving=true;
        positionX += velocityX;

        //Boundary Checking
        if(positionX < LEFT_BOUND){
            positionX = LEFT_BOUND;
            moving=false;
        }

        if ((velocityX>0)){
            velocityX=velocityX+acceleration;
        }

        if (velocityX<0){
            if (velocityX<-2){
                velocityX=velocityX-acceleration;
            }
            //newtonian acceleration
        }

        if (positionX > RIGHT_BOUND){
            positionX = RIGHT_BOUND;
            moving=false;
        }

        //Update the image position
        this.setX(positionX);
        tv1.setX(positionX+150);

        positionY += velocityY;

        if(positionY < UPPER_BOUND){
            positionY = UPPER_BOUND;
            moving=false;
        }
        else if (positionY > LOWER_BOUND){
            positionY = LOWER_BOUND;
            moving=false;
        }

        if ((velocityY>0)){
            velocityY=velocityY+acceleration;
        }
        if (velocityY<0){
            if (velocityY<-2){
                velocityY=velocityY-acceleration;
            }
        }
        //newtonian acceleration

        //Update the image position
        this.setY(positionY);
        tv1.setY(positionY+100);

    }

    public float getcoordX(){
        return positionX;
    }

    public float getcoordY(){
        return positionY;
    }

    public void deleteBlock(LinkedList<GameBlock>a){
       myr1.removeView(tv1);
       myr1.removeView(this);
       this.erased=true;
       a.remove(this);
    }

    public void doubleValue(){
        value=value*2;
        tv1.setText(String.format("%d",value));
        this.erased=false;
    }

    public int getnumber(){
        return value;
    } //returns the value of the textview for checking purposes
}
