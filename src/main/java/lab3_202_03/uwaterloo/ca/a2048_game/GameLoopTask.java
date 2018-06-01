package lab3_202_03.uwaterloo.ca.a2048_game;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.w3c.dom.Text;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class GameLoopTask extends TimerTask{

    private Activity myActivity;
    private RelativeLayout myrl;
    private Context context;
    Random r=new Random();
    public GameBlock block;
    public LinkedList<GameBlock>a;
    public boolean moved=false;

    //constructor
    public GameLoopTask(Activity myAct, RelativeLayout rl, Context c){
        a=new LinkedList<>();
        myActivity = myAct;
        myrl=rl;
        context=c;
        makeBlock(context);
    }

    public void makeBlock(Context c) {
        int emptyslot=getNumSlots();
        //makeshift endgame
        if(emptyslot==1){

            TextView tv5=new TextView(c);
            tv5.setText("You Lose");
            tv5.setTextSize(60);
            myrl.addView(tv5);//game lose condition

        }
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //making a new block in a random unoccupied space on the board
        else {
            int n = r.nextInt(emptyslot-1);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {

                    if (!isOccupied(-74 + 251 * i, -74 + 251 * j)) {
                        if (n == 0) {
                            block = new GameBlock(c, -74 + 251 * i, -74 + 251 * j, myrl);
                            a.add(block);
                        }
                        n--;
                    }
                }
            }
        }
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    }

    public void setdirection(String b,Context c) { //taking in the direction of the block movement from the finite state machine
            if (b=="LEFT"){
                moveLineLeft(-74);
                moveLineLeft(-74+251);
                moveLineLeft(-74+251*2);
                moveLineLeft(-74+251*3);
            }

            //the above method is applied to all following functions
            if (b == "RIGHT") {
                moveLineRight(-74);
                moveLineRight(-74+251);
                moveLineRight(-74+251*2);
                moveLineRight(-74+251*3);
            }

            if (b == "UP") {
                moveLineUp(-74);
                moveLineUp(-74+251);
                moveLineUp(-74+251*2);
                moveLineUp(-74+251*3);
            }

            if (b == "DOWN") {
                moveLineDown(-74);
                moveLineDown(-74+251);
                moveLineDown(-74+251*2);
                moveLineDown(-74+251*3);
            }

            for (GameBlock item : a) {
                if(item.erased==false){
                    item.setDirection(b);
                }
            }

            if (b!="UNDETERMINED"){
                moved=true;
            }

    }

    public boolean isOccupied(float x,float y){
        for (GameBlock item:a){
            if ((item.getcoordX()==x)&&(item.getcoordY()==y)&&(item.erased==false)) {
                return true;
            }
        }
        return false;
    }

    public int getNumSlots(){
        //determining the number of empty slots on the board
        int emptyslot=16;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {

                if (isOccupied(-74+251*i,-74+251*j)){
                    emptyslot--;
                }

            }
        }
        return emptyslot;
    }

    public boolean finishedMoving(){
        for (GameBlock item:a){
            if (item.isMoving()&&item.erased==false){
                return false;
            }
        }
        return true;
    }

//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public void moveLineLeft(float x){
        float adjx;
        float adjy;
        float adjright;
        float adjleft;
        int itemnum;
        merged:for (GameBlock item : a) {
            if (x==item.getcoordY()) { //cycles through the gameblocks
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //determining the number of slots and blocks in front of each GameBlock
                int blockCount = 0;
                int slotCount = 0;
                while (-74 + 251 * slotCount < item.getcoordX()) {
                    if (isOccupied(-74 + 251 * slotCount, item.getcoordY())) {
                        blockCount++; //if a slot is occupied, then the number of blocks is incremented
                    }
                    slotCount++; //calculates the number of slots
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //checking how many blocks on on the x axis plane
                int blocksonaxis = 0;
                if (isOccupied(-74, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251 * 2, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251 * 3, item.getcoordY())) {
                    blocksonaxis++;
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving two blocks on one axis - case involving one block is not necessary
                if (blockCount == 1 && slotCount == 1 && blocksonaxis == 2) { //this indicates that this block is right next to a block right by the edge
                    adjx = -74; //the adjacent block is located at -74 right next to LEFT_BOUND
                    adjy = item.getcoordY(); //the adjacent block is located at the same y value as the item
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) { //blocks essentially don't move
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                item.setdestinationleft(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 2 && blocksonaxis == 2) { //there are two possible places where the adjacent block could be located
                    if (isOccupied(-74, item.getcoordY())) {
                        adjx = -74;
                    } else { //since there are only two locations the adjacent block must be in the other spot
                        adjx = -74 + 251;
                    }
                    adjy = item.getcoordY();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                item.setdestinationleft(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 3 && blocksonaxis == 2) { //there are three possible places where the adjacent block could be located
                    if (isOccupied(-74, item.getcoordY())) {
                        adjx = -74;
                    } else if (isOccupied(-74 + 251, item.getcoordY())) {
                        adjx = -74 + 251;
                    } else { //since there are only three locations the adjacent block must be in the other spot
                        adjx = -74 + 251 * 2;
                    }
                    adjy = item.getcoordY();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationleft(-74, blockCount, slotCount);
                                item.setdestinationleft(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }
                //--------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving three blocks
                if (blockCount == 2 && slotCount == 2 && blocksonaxis == 3) { //there are three blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordX() == item.getcoordX() - 251 * 2 && left.getcoordY() == item.getcoordY()) && (right.getcoordX() == item.getcoordX() - 251 && right.getcoordY() == item.getcoordY())) {
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    right.doubleValue();
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    right.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) {
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251 * 2, blockCount, slotCount);
                                    break merged;
                                }
                                if (left.getnumber() != right.getnumber() && left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) {
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251 * 2, blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }

                if (blockCount == 2 && slotCount == 3 && blocksonaxis == 3) {//there are three blocks spread out across four spaces
                    if (isOccupied(item.getcoordX() - 251, item.getcoordY())) {
                        adjright = item.getcoordX() - 251;
                        if (isOccupied(item.getcoordX() - 251 * 2, item.getcoordY())) {
                            adjleft = item.getcoordX() - 251 * 2;
                        } else {
                            adjleft = item.getcoordX() - 251 * 3;
                        }
                    } else {//the two occupied spots must be the leftmost ones
                        adjleft = item.getcoordX() - 251 * 3;
                        adjright = item.getcoordX() - 251 * 2;
                    }
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordX() == adjleft && left.getcoordY() == item.getcoordY()) && (right.getcoordX() == adjright && right.getcoordY() == item.getcoordY())) {
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    right.doubleValue();
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() == right.getnumber()) {//when the first two blocks have the same number
                                    right.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) {
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251 * 2, blockCount, slotCount);
                                }
                                if (left.getnumber() != right.getnumber() && left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) {
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                    item.setdestinationleft(-74 + 251 * 2, blockCount, slotCount);
                                }
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving all four blocks
                if (blockCount == 3 && slotCount == 3 && blocksonaxis == 4) { //there are four blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock middle : a) {
                            for (GameBlock right : a) {
                                if ((left.getcoordX() == item.getcoordX() - 251 * 3 && left.getcoordY() == item.getcoordY()) && (middle.getcoordX() == item.getcoordX() - 251 * 2 && middle.getcoordY() == item.getcoordY()) && (right.getcoordX() == item.getcoordX() - 251 && right.getcoordY() == item.getcoordY())) {
                                    //two middle blocks are matching n y y n
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()!=item.getnumber()) {
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        middle.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        item.setdestinationleft(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost blocks are matching y y n n
                                    if (left.getnumber() == middle.getnumber()&&middle.getnumber()!=right.getnumber()&&right.getnumber()!=item.getnumber()) { //case where the first two blocks are the same
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        item.setdestinationleft(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two rightmost blocks are matching n n y y
                                    if (left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&right.getnumber()==item.getnumber()) { //case where the first two blocks are the same
                                        item.deleteBlock(a);
                                        right.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        middle.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        right.setdestinationleft(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost and right block are matching y y | y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        item.deleteBlock(a);
                                        left.doubleValue();
                                        right.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all are matching y y y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        item.deleteBlock(a);
                                        left.doubleValue();
                                        right.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all matching except right one y y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()!=item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        item.setdestinationleft(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //scattered y n y n
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()&&left.getnumber()==right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        middle.setdestinationleft(-74+251,blockCount,slotCount);
                                        right.setdestinationleft(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationleft(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered n y n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()&&middle.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        middle.setdestinationleft(-74+251,blockCount,slotCount);
                                        right.setdestinationleft(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationleft(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all matching except right one n y y y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        right.doubleValue();
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        right.setdestinationleft(-74 + 251, blockCount, slotCount);
                                        item.setdestinationleft(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two on the ends are matching y n n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber()!=right.getnumber()&& left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationleft(-74, blockCount, slotCount);
                                        middle.setdestinationleft(-74+251,blockCount,slotCount);
                                        right.setdestinationleft(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationleft(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            }
    }

//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public void moveLineUp(float x){
        float adjx;
        float adjy;
        float adjright;
        float adjleft;
        int itemnum;
        merged:for (GameBlock item : a) {
            if (x==item.getcoordX()) { //cycles through the gameblocks
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //determining the number of slots and blocks in front of each GameBlock
                int blockCount = 0;
                int slotCount = 0;
                while (-74 + 251 * slotCount < item.getcoordY()) {
                    if (isOccupied(item.getcoordX(),-74 + 251 * slotCount)) {
                        blockCount++; //if a slot is occupied, then the number of blocks is incremented
                    }
                    slotCount++; //calculates the number of slots
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //checking how many blocks on on the x axis plane
                int blocksonaxis = 0;
                if (isOccupied(item.getcoordX(),-74)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251 * 2)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251 * 3)) {
                    blocksonaxis++;
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving two blocks on one axis - case involving one block is not necessary
                if (blockCount == 1 && slotCount == 1 && blocksonaxis == 2) { //this indicates that this block is right next to a block right by the edge
                    adjy = -74; //the adjacent block is located at -74 right next to LEFT_BOUND
                    adjx = item.getcoordX(); //the adjacent block is located at the same y value as the item
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) { //blocks essentially don't move
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                item.setdestinationup(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 2 && blocksonaxis == 2) { //there are two possible places where the adjacent block could be located
                    if (isOccupied(item.getcoordX(),-74)) {
                        adjy = -74;
                    } else { //since there are only two locations the adjacent block must be in the other spot
                        adjy = -74 + 251;
                    }
                    adjx = item.getcoordX();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                item.setdestinationup(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 3 && blocksonaxis == 2) { //there are three possible places where the adjacent block could be located
                    if (isOccupied(item.getcoordX(),-74)) {
                        adjy = -74;
                    } else if (isOccupied(item.getcoordX(),-74 + 251)) {
                        adjy = -74 + 251;
                    } else { //since there are only three locations the adjacent block must be in the other spot
                        adjy = -74 + 251 * 2;
                    }
                    adjx = item.getcoordX();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationup(-74, blockCount, slotCount);
                                item.setdestinationup(-74 + 251, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }
                //--------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving three blocks
                if (blockCount == 2 && slotCount == 2 && blocksonaxis == 3) { //there are three blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordY() == item.getcoordY() - 251 * 2 && left.getcoordX() == item.getcoordX()) && (right.getcoordY() == item.getcoordY() - 251 && right.getcoordX() == item.getcoordX())) {
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    right.doubleValue();
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    right.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) { //blocks essentially don't move
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251 * 2, blockCount, slotCount);
                                    break merged;
                                }
                                if (left.getnumber() != right.getnumber() && left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) { //blocks essentially don't move
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251 * 2, blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }

                if (blockCount == 2 && slotCount == 3 && blocksonaxis == 3) {//there are three blocks spread out across four spaces
                    if (isOccupied(item.getcoordX(), item.getcoordY()-251)) {
                        adjright = item.getcoordY() - 251;
                        if (isOccupied(item.getcoordX(), item.getcoordY()-251*2)) {
                            adjleft = item.getcoordY() - 251 * 2;
                        } else {
                            adjleft = item.getcoordY() - 251 * 3;
                        }
                    } else {//the two occupied spots must be the leftmost ones
                        adjleft = item.getcoordY() - 251 * 3;
                        adjright = item.getcoordY() - 251 * 2;
                    }
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordY() == adjleft && left.getcoordX() == item.getcoordX()) && (right.getcoordY() == adjright && right.getcoordX() == item.getcoordX())) {
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    right.doubleValue();
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() == right.getnumber()) {//when the first two blocks have the same number
                                    right.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) {
                                    left.setdestinationleft(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251 * 2, blockCount, slotCount);
                                }
                                if (left.getnumber() != right.getnumber() && left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) {
                                    left.setdestinationup(-74, blockCount, slotCount);
                                    right.setdestinationup(-74 + 251, blockCount, slotCount);
                                    item.setdestinationup(-74 + 251 * 2, blockCount, slotCount);
                                }
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving all four blocks
                if (blockCount == 3 && slotCount == 3 && blocksonaxis == 4) { //there are four blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock middle : a) {
                            for (GameBlock right : a) {
                                if ((left.getcoordY() == item.getcoordY() - 251 * 3 && left.getcoordX() == item.getcoordX()) && (middle.getcoordY() == item.getcoordY() - 251 * 2 && middle.getcoordX() == item.getcoordX()) && (right.getcoordY() == item.getcoordY() - 251 && right.getcoordX() == item.getcoordX())) {
                                    //two middle blocks are matching n y y n
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()!=item.getnumber()) {
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        middle.setdestinationup(-74 + 251, blockCount, slotCount);
                                        item.setdestinationup(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost blocks are matching y y n n
                                    if (left.getnumber() == middle.getnumber()&&middle.getnumber()!=right.getnumber()&&right.getnumber()!=item.getnumber()) { //case where the first two blocks are the same
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        right.setdestinationup(-74 + 251, blockCount, slotCount);
                                        item.setdestinationup(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two rightmost blocks are matching n n y y
                                    if (left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&right.getnumber()==item.getnumber()) { //case where the first two blocks are the same
                                        item.deleteBlock(a);
                                        right.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        middle.setdestinationup(-74 + 251, blockCount, slotCount);
                                        right.setdestinationup(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost and right block are matching y y | y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        item.deleteBlock(a);
                                        left.doubleValue();
                                        right.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        right.setdestinationup(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all are matching y y y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        item.deleteBlock(a);
                                        left.doubleValue();
                                        right.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        right.setdestinationup(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered y n y n
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()&&left.getnumber()==right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        middle.setdestinationup(-74+251,blockCount,slotCount);
                                        right.setdestinationup(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationup(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered n y n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()&&middle.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        middle.setdestinationup(-74+251,blockCount,slotCount);
                                        right.setdestinationup(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationup(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all matching except right one y y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()!=item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        right.setdestinationup(-74 + 251, blockCount, slotCount);
                                        item.setdestinationup(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //all matching except right one n y y y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) { //blocks essentially don't move
                                        middle.deleteBlock(a);
                                        right.doubleValue();
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        right.setdestinationup(-74 + 251, blockCount, slotCount);
                                        item.setdestinationup(-74+251*2,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two on the ends are matching y n n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber()!=right.getnumber()&& left.getnumber() == item.getnumber() && item.getnumber() != right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationup(-74, blockCount, slotCount);
                                        middle.setdestinationup(-74+251,blockCount,slotCount);
                                        right.setdestinationup(-74 + 251*2, blockCount, slotCount);
                                        item.setdestinationup(-74 + 251 * 3, blockCount, slotCount);
                                        break merged;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public void moveLineRight(float x){
        float adjx;
        float adjy;
        float adjright;
        float adjleft;
        int itemnum;
        merged:for (GameBlock item : a) {
            if (x==item.getcoordY()) { //cycles through the gameblocks
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //determining the number of slots and blocks in front of each GameBlock
                int blockCount = 0;
                int slotCount = 0;
                while (-74 + 251 *(3- slotCount) > item.getcoordX()) {
                    if (isOccupied(-74 + 251 * (3-slotCount), item.getcoordY())) {
                        blockCount++; //if a slot is occupied, then the number of blocks is incremented
                    }
                    slotCount++; //calculates the number of slots
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //checking how many blocks on on the x axis plane
                int blocksonaxis = 0;
                if (isOccupied(-74, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251 * 2, item.getcoordY())) {
                    blocksonaxis++;
                }
                if (isOccupied(-74 + 251 * 3, item.getcoordY())) {
                    blocksonaxis++;
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving two blocks on one axis - case involving one block is not necessary
                if (blockCount == 1 && slotCount == 1 && blocksonaxis == 2) { //this indicates that this block is right next to a block right by the edge
                    adjx = -74+251*3; //the adjacent block is located at -74+251*3 right next to RIGHT_BOUND
                    adjy = item.getcoordY(); //the adjacent block is located at the same y value as the item
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) { //blocks essentially don't move
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 2 && blocksonaxis == 2) { //there are two possible places where the adjacent block could be located
                    if (isOccupied(-74+251*3, item.getcoordY())) {
                        adjx = -74+251*3;
                    } else { //since there are only two locations the adjacent block must be in the other spot
                        adjx = -74 + 251*2;
                    }
                    adjy = item.getcoordY();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 3 && blocksonaxis == 2) { //there are three possible places where the adjacent block could be located
                    if (isOccupied(-74+251*3, item.getcoordY())) {
                        adjx = -74+251*3;
                    } else if (isOccupied(-74 + 251*2, item.getcoordY())) {
                        adjx = -74 + 251*2;
                    } else { //since there are only three locations the adjacent block must be in the other spot
                        adjx = -74 + 251;
                    }
                    adjy = item.getcoordY();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationright(-74+251*3, blockCount, slotCount);
                                item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }
                //--------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving three blocks
                if (blockCount == 2 && slotCount == 2 && blocksonaxis == 3) { //there are three blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((right.getcoordX() == item.getcoordX() + 251 * 2 && right.getcoordY() == item.getcoordY()) && (left.getcoordX() == item.getcoordX() + 251 && left.getcoordY() == item.getcoordY())) {
                                // case where - y y n
                                if (right.getnumber() != left.getnumber() && left.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    left.doubleValue();
                                    right.setdestinationright(-74+251*3, blockCount, slotCount);
                                    left.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n y y and - y y y
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    left.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationright(-74+251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n n n
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) { //blocks essentially don't move
                                    left.setdestinationright(-74+251*2, blockCount, slotCount);
                                    right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                                //case where -y n y
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                    left.setdestinationright(-74+251*2, blockCount, slotCount);
                                    right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }

                if (blockCount == 2 && slotCount == 3 && blocksonaxis == 3) {//there are three blocks spread out across four spaces
                    if (isOccupied(item.getcoordX() + 251, item.getcoordY())) {
                        adjleft = item.getcoordX() + 251;
                        if (isOccupied(item.getcoordX() + 251 * 2, item.getcoordY())) {
                            adjright = item.getcoordX() + 251 * 2;
                        } else {
                            adjright = item.getcoordX() + 251 * 3;
                        }
                    } else {//the two occupied spots must be the leftmost ones
                        adjright = item.getcoordX() + 251 * 3;
                        adjleft = item.getcoordX() + 251 * 2;
                    }
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordX() == adjleft && left.getcoordY() == item.getcoordY()) && (right.getcoordX() == adjright && right.getcoordY() == item.getcoordY())) {
                                // case where - y y n
                                if (right.getnumber() != left.getnumber() && left.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    left.doubleValue();
                                    right.setdestinationright(-74+251*3, blockCount, slotCount);
                                    left.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n y y and - y y y
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    left.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationright(-74+251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n n n
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) { //blocks essentially don't move
                                    left.setdestinationright(-74+251*2, blockCount, slotCount);
                                    right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                                //case where -y n y
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                    left.setdestinationright(-74+251*2, blockCount, slotCount);
                                    right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationright(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving all four blocks
                if (blockCount == 3 && slotCount == 3 && blocksonaxis == 4) { //there are four blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock middle : a) {
                            for (GameBlock right : a) {
                                if ((right.getcoordX() == item.getcoordX() + 251 * 3 && right.getcoordY() == item.getcoordY()) && (middle.getcoordX() == item.getcoordX() + 251 * 2 && middle.getcoordY() == item.getcoordY()) && (left.getcoordX() == item.getcoordX() + 251 && left.getcoordY() == item.getcoordY())) {
                                    //two middle blocks are matching n y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()) {
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationright(-74+251*2, blockCount, slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost blocks are matching y y n n
                                    if (item.getnumber() == left.getnumber()&&middle.getnumber()!=right.getnumber()&&left.getnumber()!=middle.getnumber()) { //case where the first two blocks are the same
                                        left.deleteBlock(a);
                                        item.doubleValue();
                                        middle.setdestinationright(-74+251*2, blockCount, slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two rightmost blocks are matching n n y y
                                    if (left.getnumber() != middle.getnumber()&&middle.getnumber()==right.getnumber()&&left.getnumber()!=item.getnumber()) { //case where the first two blocks are the same
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationright(-74+251*2, blockCount, slotCount);
                                        middle.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost and right blocks are matching y y | y y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&left.getnumber()==item.getnumber()) {
                                        left.deleteBlock(a);
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        item.doubleValue();
                                        middle.setdestinationright(-74+251*3, blockCount, slotCount);
                                        item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all are matching y y y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) {
                                        left.deleteBlock(a);
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        item.doubleValue();
                                        middle.setdestinationright(-74+251*3, blockCount, slotCount);
                                        item.setdestinationright(-74 + 251*2, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered y n y n
                                    if (item.getnumber() != left.getnumber() && left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&item.getnumber()==middle.getnumber()) { //blocks essentially don't move
                                        left.setdestinationright(-74+251, blockCount, slotCount);
                                        middle.setdestinationright(-74+251*2,blockCount,slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered n y n y
                                    if (item.getnumber() != left.getnumber() && left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&left.getnumber()==right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationright(-74+251, blockCount, slotCount);
                                        middle.setdestinationright(-74+251*2,blockCount,slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all matching except right one y y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&left.getnumber()==item.getnumber()) {
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationright(-74+251*2, blockCount, slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //all matching except right one n y y y
                                    if (item.getnumber() != left.getnumber() && left.getnumber() == middle.getnumber()&&middle.getnumber()==right.getnumber()) {
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationright(-74+251*2, blockCount, slotCount);
                                        middle.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two on the ends are matching y n n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber()!=right.getnumber()&& right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                        left.setdestinationright(-74+251, blockCount, slotCount);
                                        middle.setdestinationright(-74+251*2,blockCount,slotCount);
                                        right.setdestinationright(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationright(-74, blockCount, slotCount);
                                        break merged;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public void moveLineDown(float x){
        float adjx;
        float adjy;
        float adjright;
        float adjleft;
        int itemnum;
        merged:for (GameBlock item : a) {
            if (x==item.getcoordX()) { //cycles through the gameblocks
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //determining the number of slots and blocks in front of each GameBlock
                int blockCount = 0;
                int slotCount = 0;
                while (-74 + 251 *(3- slotCount) > item.getcoordY()) {
                    if (isOccupied(item.getcoordX(),-74 + 251 * (3-slotCount))) {
                        blockCount++; //if a slot is occupied, then the number of blocks is incremented
                    }
                    slotCount++; //calculates the number of slots
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //checking how many blocks on on the x axis plane
                int blocksonaxis = 0;
                if (isOccupied(item.getcoordX(),-74)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251 * 2)) {
                    blocksonaxis++;
                }
                if (isOccupied(item.getcoordX(),-74 + 251 * 3)) {
                    blocksonaxis++;
                }
                //-----------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving two blocks on one axis - case involving one block is not necessary
                if (blockCount == 1 && slotCount == 1 && blocksonaxis == 2) { //this indicates that this block is right next to a block right by the edge
                    adjy = -74+251*3; //the adjacent block is located at -74+251*3 right next to RIGHT_BOUND
                    adjx = item.getcoordX(); //the adjacent block is located at the same y value as the item
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) { //blocks essentially don't move
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 2 && blocksonaxis == 2) { //there are two possible places where the adjacent block could be located
                    if (isOccupied(item.getcoordX(),-74+251*3)) {
                        adjy = -74+251*3;
                    } else { //since there are only two locations the adjacent block must be in the other spot
                        adjy = -74 + 251*2;
                    }
                    adjx = item.getcoordX();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }

                if (blockCount == 1 && slotCount == 3 && blocksonaxis == 2) { //there are three possible places where the adjacent block could be located
                    if (isOccupied(item.getcoordX(),-74+251*3)) {
                        adjy = -74+251*3;
                    } else if (isOccupied(item.getcoordX(),-74 + 251*2)) {
                        adjy = -74 + 251*2;
                    } else { //since there are only three locations the adjacent block must be in the other spot
                        adjy = -74 + 251;
                    }
                    adjx = item.getcoordX();
                    itemnum = item.getnumber();
                    for (GameBlock adjacent : a) {
                        if (adjacent.getcoordX() == adjx && adjacent.getcoordY() == adjy) {
                            if (itemnum == adjacent.getnumber()) {
                                item.deleteBlock(a);
                                adjacent.doubleValue();
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                break merged;
                            }
                            if (itemnum != adjacent.getnumber()) {
                                adjacent.setdestinationdown(-74+251*3, blockCount, slotCount);
                                item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                break merged;
                            }
                        }
                    }
                }
                //--------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving three blocks
                if (blockCount == 2 && slotCount == 2 && blocksonaxis == 3) { //there are three blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((right.getcoordY() == item.getcoordY() + 251 * 2 && right.getcoordX() == item.getcoordX()) && (left.getcoordY() == item.getcoordY() + 251 && left.getcoordX() == item.getcoordX())) {
                                // case where - y y n
                                if (right.getnumber() != left.getnumber() && left.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    left.doubleValue();
                                    right.setdestinationdown(-74+251*3, blockCount, slotCount);
                                    left.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n y y and - y y y
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    left.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationdown(-74+251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n n n
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) { //blocks essentially don't move
                                    left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                    right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                                //case where -y n y
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                    left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                    right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }

                if (blockCount == 2 && slotCount == 3 && blocksonaxis == 3) {//there are three blocks spread out across four spaces
                    if (isOccupied(item.getcoordX(),item.getcoordY() + 251)) {
                        adjleft = item.getcoordY() + 251;
                        if (isOccupied(item.getcoordX(),item.getcoordY() + 251 * 2)) {
                            adjright = item.getcoordY() + 251 * 2;
                        } else {
                            adjright = item.getcoordY() + 251 * 3;
                        }
                    } else {//the two occupied spots must be the leftmost ones
                        adjright = item.getcoordY() + 251 * 3;
                        adjleft = item.getcoordY() + 251 * 2;
                    }
                    for (GameBlock left : a) {
                        for (GameBlock right : a) {
                            if ((left.getcoordY() == adjleft && left.getcoordX() == item.getcoordX()) && (right.getcoordY() == adjright && right.getcoordX() == item.getcoordX())) {
                                // case where - y y n
                                if (right.getnumber() != left.getnumber() && left.getnumber() == item.getnumber()) {
                                    item.deleteBlock(a);
                                    left.doubleValue();
                                    right.setdestinationdown(-74+251*3, blockCount, slotCount);
                                    left.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n y y and - y y y
                                if (left.getnumber() == right.getnumber()) { //case where the first two blocks are the same
                                    left.deleteBlock(a);
                                    left.doubleValue();
                                    left.setdestinationdown(-74+251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                    break merged;//we want it to leave the if statement if it merged once already
                                }
                                //case where - n n n
                                if (left.getnumber() != right.getnumber() && right.getnumber() != item.getnumber()) { //blocks essentially don't move
                                    left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                    right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                                //case where -y n y
                                if (left.getnumber() != right.getnumber() && right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                    left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                    right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                    item.setdestinationdown(-74 + 251 , blockCount, slotCount);
                                    break merged;
                                }
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------------------------------------------------------------------------------------
                //cases involving all four blocks
                if (blockCount == 3 && slotCount == 3 && blocksonaxis == 4) { //there are four blocks in a row
                    for (GameBlock left : a) {
                        for (GameBlock middle : a) {
                            for (GameBlock right : a) {
                                if ((right.getcoordY() == item.getcoordY() + 251 * 3 && right.getcoordX() == item.getcoordX()) && (middle.getcoordY() == item.getcoordY() + 251 * 2 && middle.getcoordX() == item.getcoordX()) && (left.getcoordY() == item.getcoordY() + 251 && left.getcoordX() == item.getcoordX())) {
                                    //two middle blocks are matching n y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&right.getnumber()!=item.getnumber()) {
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost blocks are matching y y n n
                                    if (item.getnumber() == left.getnumber()&&middle.getnumber()!=right.getnumber()&&left.getnumber()!=middle.getnumber()) { //case where the first two blocks are the same
                                        left.deleteBlock(a);
                                        item.doubleValue();
                                        middle.setdestinationdown(-74+251*2, blockCount, slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two rightmost blocks are matching n n y y
                                    if (left.getnumber() != middle.getnumber()&&middle.getnumber()==right.getnumber()&&left.getnumber()!=item.getnumber()) { //case where the first two blocks are the same
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                        middle.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two leftmost and right blocks are matching y y | y y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber() == right.getnumber()&&left.getnumber()==item.getnumber()) {
                                        left.deleteBlock(a);
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        item.doubleValue();
                                        middle.setdestinationdown(-74+251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all are matching y y y y
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() == right.getnumber()&&right.getnumber()==item.getnumber()) {
                                        left.deleteBlock(a);
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        item.doubleValue();
                                        middle.setdestinationdown(-74+251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74 + 251*2, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered y n y n
                                    if (item.getnumber() != left.getnumber() && left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&item.getnumber()==middle.getnumber()) { //blocks essentially don't move
                                        left.setdestinationdown(-74+251, blockCount, slotCount);
                                        middle.setdestinationdown(-74+251*2,blockCount,slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //scattered n y n y
                                    if (item.getnumber() != left.getnumber() && left.getnumber() != middle.getnumber()&&middle.getnumber()!=right.getnumber()&&left.getnumber()==right.getnumber()) { //blocks essentially don't move
                                        left.setdestinationdown(-74+251, blockCount, slotCount);
                                        middle.setdestinationdown(-74+251*2,blockCount,slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74 + 251, blockCount, slotCount);
                                        break merged;
                                    }
                                    //all matching except right one y y y n
                                    if (left.getnumber() == middle.getnumber() && middle.getnumber() != right.getnumber()&&left.getnumber()==item.getnumber()) {
                                        middle.deleteBlock(a);
                                        left.doubleValue();
                                        left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //all matching except right one n y y y
                                    if (item.getnumber() != left.getnumber() && left.getnumber() == middle.getnumber()&&middle.getnumber()==right.getnumber()) {
                                        right.deleteBlock(a);
                                        middle.doubleValue();
                                        left.setdestinationdown(-74+251*2, blockCount, slotCount);
                                        middle.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74+251,blockCount,slotCount);
                                        break merged;
                                    }
                                    //two on the ends are matching y n n y
                                    if (left.getnumber() != middle.getnumber() && middle.getnumber()!=right.getnumber()&& right.getnumber() == item.getnumber() && item.getnumber() != left.getnumber()) { //blocks essentially don't move
                                        left.setdestinationdown(-74+251, blockCount, slotCount);
                                        middle.setdestinationdown(-74+251*2,blockCount,slotCount);
                                        right.setdestinationdown(-74 + 251*3, blockCount, slotCount);
                                        item.setdestinationdown(-74, blockCount, slotCount);
                                        break merged;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    //running this on UI Thread.
    public void run(){
        myActivity.runOnUiThread(
                new Runnable(){
                    @Override
                    public void run(){
                        for (GameBlock item:a) {
                            item.move();
                        }

                        if (moved&&finishedMoving()){
                            makeBlock(context);
                            moved=false;
                        }

                    }
                }
        );
    }

}
