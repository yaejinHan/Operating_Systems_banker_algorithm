import java.util.ArrayList;

public class Tasks {


    /*
    id is the order in which the task entered the system, staring from 0
    status can be "aborted", "terminated", "readyToGo", "blocked"
    instrucLst contains all the instructions that belong to that particular task
    terminatingT and waitingT to print out the result for each task
    maxAdditional array for Banker's algorithm
    initialClaim array for Banker's algorithm
    currAllocated array for Naive's algorithm
     */
    int id;
    String status = "";
    int currIndx;
    ArrayList<int[]> instrucLst;
    int terminatingT = 0;
    int waitingT = 0;

    int[] maxAdditional;
    int[] initialClaim;
    int[] currAlloc;


    // instantiate each Tasks object with the following parameters
    public Tasks(int id, ArrayList<int[]> instrucLst, int numResourceTypes) {
        this.id = id;
        this.instrucLst = instrucLst;
        this.currAlloc = new int[numResourceTypes];
        for (int i = 0; i < this.currAlloc.length; i++) {
            this.currAlloc[i] = 0;
        }
        this.currIndx = 0;
        this.maxAdditional = new int[numResourceTypes];
        this.initialClaim = new int[numResourceTypes];
    }
}
