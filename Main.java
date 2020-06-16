import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {

   /*
   In this main method, input is read, stored appropriately and both optimistic resource manager algorithm
   and the banker algorithm are run
    */

    public static void main(String[] args) throws IOException {



        String f = "";


        /*
        reads the name of the input file from the command line argument
         */
        if (args.length == 1) {
            Scanner ip = new Scanner(args[0]);
            f = ip.next();
        }

        // get the working directory
        String dir = System.getProperty("user.dir");


        // an arraylist that will have all the content of the file copied into
        ArrayList<String> arr = new ArrayList<String>();


        try {
            // read all the content from the file that the user entered using command line argument
            List<String> fs = (Files.readAllLines(Paths.get(dir+"/"+f+".txt")));


            // add all lines of List<String> fs into arraylist 'arr'
            arr.addAll(fs);

        }

        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        /*
         two taskLst arrayLists, one for Optimistic Resource Manager Algorithm and one for Banker's Algorithm
         taskLst arrayLists contain Task objects that are in the system
            Each Task Object contains different fields such as instrucLst (e.g., initiate 1 0 1 4), initial claim array,
            maximum additonal request array, waiting time, terminating time, task id, and so on
         */
        ArrayList<Tasks> taskLstOne = new ArrayList<Tasks>();
        ArrayList<Tasks> taskLstTwo = new ArrayList<Tasks>();
        String[] firstLine = arr.get(0).split("\\s+");  // first line contains info such as the num of tasks, num of resource types and the number of their units

        int numTasks = Integer.parseInt(firstLine[0]);
        int numResourceTypes = Integer.parseInt(firstLine[1]);

        // two available arrays, one for Banker and one for optimistic resource manager
        // available array is to have the algorithm know how many units of each resource type are available
        int[] availableOne = new int[numResourceTypes];
        int[] availableTwo = new int[numResourceTypes];


        for (int i = 0; i < numResourceTypes; i++) {
            availableOne[i] = Integer.parseInt(firstLine[i+2]);
            availableTwo[i] = Integer.parseInt(firstLine[i+2]);
        }



        /*
        instruction storing process
        Assign each instruction to a task it belongs to (e.g., if instruction line = initiate 1 0 1 4, it belongs to task 1, thus assign this instruction to task 1
         */
        for (int i = 0; i < numTasks; i++) {
            ArrayList<int[]> instrucLstOne = new ArrayList<int[]>();
            ArrayList<int[]> instrucLstTwo = new ArrayList<int[]>();

            for (int j = 1; j < arr.size(); j++) {
                if (arr.get(j).equals("")) continue;
                String[] curr = arr.get(j).split("\\s+");
                int[] instrucOne = new int[5];
                int[] instrucTwo = new int[5];
                boolean occupied = false;

                if (Integer.parseInt(curr[1])-1 == i) {
                    occupied = true;

                    /*
                    make each instruction line readable in int array
                    if curr[0] == initiate, give the first indexed element of instruc array the value of 0
                    if curr[0] == request, give the first indexed element of instruc array the value of 1
                    if curr[0] == release, give the first indexed element of instruc array the value of 2
                    if curr[0] == terminate, give the first indexed element of instruc array the value of 3
                     */
                    for (int k = 0; k < curr.length; k++) {
                        if (k == 0) {
                            if (curr[0].equals("initiate")) {
                                instrucOne[0] = 0;
                                instrucTwo[0] = 0;
                            }
                            else if (curr[0].equals("request")) {
                                instrucOne[0] = 1;
                                instrucTwo[0] = 1;
                            }
                            else if (curr[0].equals("release")) {
                                instrucOne[0] = 2;
                                instrucTwo[0] = 2;
                            }
                            else {
                                instrucOne[0] = 3;
                                instrucTwo[0] = 3;
                            }
                        }

                        else {
                            instrucOne[k] = Integer.parseInt(curr[k]);
                            instrucTwo[k] = Integer.parseInt(curr[k]);
                        }
                    }
                }


                if (occupied) {
                    instrucLstOne.add(instrucOne);
                    instrucLstTwo.add(instrucTwo);
                }


            }

            // instantiate each task with id, instruction array, number of resource types and add to taskLst arrayList
            taskLstOne.add(new Tasks(i, instrucLstOne, numResourceTypes));
            taskLstTwo.add(new Tasks(i, instrucLstTwo, numResourceTypes));


        }


        // create Naive class and Banker algorithm class
        Naive optimisticResourceManager = new Naive();
        Banker BankerAlgo = new Banker();

        // run banker algorithm first to get the error message printed in the very beginning
        // then run the naive resource manager after
        BankerAlgo.Banker(taskLstOne, availableOne);
        optimisticResourceManager.Naive(taskLstTwo, availableTwo);

        Banker.errorMsgPrinting(Banker.errorMsg);


        // result printing
        System.out.printf("%13s \n", "FIFO");
        optimisticResourceManager.resultPrinting(optimisticResourceManager.terminated);
        System.out.println();

        System.out.printf("%16s \n", "BANKER'S");
        Banker.resultPrinting(Banker.terminated);
        System.out.println();

    }



}
