import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Banker {

    /*
    blocked arrayLists to store the id of the task that's blocked
    aborted arrayLists to store the id of the task that's aborted
    terminated priorityqueue to store the Task object that's terminated
      this priorityqueue will arrange Task objects in a way that the smaller task id will come before the bigger task id
     */
    static ArrayList<Integer> blocked = new ArrayList<Integer>();
    static ArrayList<Integer> aborted = new ArrayList<Integer>();
    static PriorityQueue<Tasks> terminated = new PriorityQueue<>(new Comparator<Tasks>() {
        @Override
        public int compare(Tasks t1, Tasks t2) {
            if (t1.id > t2.id) return 1;
            else if (t1.id < t2.id) return -1;
            return 0;
        }
    });

    static StringBuilder errorMsg = new StringBuilder();


    public static void Banker(ArrayList<Tasks> taskLst, int[] available) {
        int initialNumTasks = taskLst.size();
        int[] existing = Arrays.copyOfRange(available, 0, available.length);
        int globalT = 0;


        /*
        run algorithm until all tasks terminate
         */
        while (terminated.size() < initialNumTasks) {

            /*
            have an arrayList called doneBlocked so that it doesn't run the already run task twice
            resourceSemaphore array to add the returned units at the end of each cycle, not add them right away to the system
             */
            ArrayList<Integer> doneBlocked = new ArrayList<Integer>();
            int[] resourceSemaphore = new int[available.length];

            /*
             checking blocked tasks in the beginning of each cycle
             to see if it can unblock any of the previously blocked tasks
             */
            if (!blocked.isEmpty()) {
                ArrayList<Integer> toRemove = new ArrayList<Integer>();

                for (int i = 0; i < blocked.size(); i++) {
                    int blockedID = blocked.get(i);
                    Tasks curr = taskLst.get(blockedID);
                    int currInstrucIndx = curr.currIndx;
                    int[] currInstruc = curr.instrucLst.get(currInstrucIndx);
                    int resourceType = currInstruc[3]-1;
                    int request = currInstruc[4];

                    int[] maxRequest = curr.maxAdditional;

                    boolean stillBlocked = false;
                    /*
                    if there exists a resource type where its maximum additional request cannot be satisfied by the
                    resources present in the system, this task remains blocked
                    This is checking for safety
                     */
                    for (int j = 0; j < maxRequest.length; j++) {
                        if (maxRequest[j] > available[j]) {
                            curr.waitingT++;
                            stillBlocked = true;
                            break;
                        }
                    }

                    /*
                    if all the maximum additional requests can be satisfied by the resource units available in the system,
                    remove from the blocked arraylist, increment the index of the current task's instruction list to go do the next instruction at the next cycle
                     */
                    if (!stillBlocked) {
                        curr.maxAdditional[resourceType] -= request;
                        available[resourceType] -= request;
                        curr.status = "readyToGo";
                        doneBlocked.add(blockedID);
                        curr.currIndx++;
                        toRemove.add(blocked.get(i));
                    }

                }


                blocked.removeAll(toRemove);
            }


            /*
            process tasks that are not blocked, terminated, or aborted
             */
            for (int i = 0; i < taskLst.size(); i++) {
                Tasks curr = taskLst.get(i);

                if (curr.status.equals("terminated") || curr.status.equals("aborted") || doneBlocked.contains(curr.id) || blocked.contains(curr.id)) continue;

                int currInstrucIndx = curr.currIndx;
                int[] currInstruc = curr.instrucLst.get(currInstrucIndx);

                // if there's a delay, simply decrement its delay
                if (currInstruc[2] > 0) {
                    curr.instrucLst.get(currInstrucIndx)[2]--;
                }

                else {
                    // when currInstruc[0] == 0, which means when the instruction is "initiate"
                    if (currInstruc[0] == 0) {
                        int resourceType = currInstruc[3]-1;
                        int claim = currInstruc[4];

                        // checking for illegal claim
                        // illegal tasks are aborted right away
                        if (claim > existing[resourceType]) {
                            String illegalClaim = "Banker aborts task " + (curr.id+1) + " before run begins: \n     claim for resource " + (resourceType+1) + " (" + claim + ")" +
                                    " exceeds number of units present (" + existing[resourceType] + ")";
                            errorMsg.append(illegalClaim + "\n");
                            curr.status = "aborted";
                            aborted.add(curr.id);
                            terminated.add(curr);
                        }

                        // legal claim
                        else {
                            curr.initialClaim[resourceType] = claim;
                            curr.maxAdditional[resourceType] = claim;
                            curr.currIndx++;
                        }
                    }

                    // when currInstruc[0] == 1, which means when the instruction is "request"
                    else if (currInstruc[0] == 1) {
                        int resourceType = currInstruc[3]-1;
                        int request = currInstruc[4];
                        int[] maxRequest = curr.maxAdditional;

                        // checking for illegal request
                        // illegal tasks aborted right away
                        if (maxRequest[resourceType] - request < 0) {
                            int[] claimArr = curr.initialClaim;
                            for (int m = 0; m < claimArr.length; m++) {
                                resourceSemaphore[m] += (claimArr[m] - maxRequest[m]);
                            }
                            String illegalClaim = "During cycle " + globalT + "-" + (globalT+1) + " of the Banker's algorithms \n      Task " + (curr.id+1) +
                                    "'s request exceeds its claim; aborted; ";

                            for (int c = 0; c < maxRequest.length; c++) {
                                if (claimArr[c] - maxRequest[c] > 0) {
                                    illegalClaim += (claimArr[c] - maxRequest[c]) + " units of resource " + (c+1) + ", ";
                                }
                            }
                            String add = illegalClaim.substring(0, illegalClaim.length()-2);
                            illegalClaim = add + " available next cycle";

                            errorMsg.append(illegalClaim + "\n");
                            curr.status = "aborted";
                            aborted.add(curr.id);
                            terminated.add(curr);
                        }

                        else {
                            /*
                             if not an illegal request, but the request could not be granted, block task
                             This is checking for safety
                             */
                            boolean isBlocked = false;
                            for (int n = 0; n < maxRequest.length; n++) {
                                if (maxRequest[n] > available[n]) {
                                    curr.status = "blocked";
                                    curr.waitingT++;
                                    blocked.add(curr.id);
                                    isBlocked = true;
                                    break;
                                }
                            }

                            // if the request could be granted
                            if (!isBlocked) {
                                curr.maxAdditional[resourceType] -= request;
                                available[resourceType] -= request;
                                curr.currIndx++;
                            }
                        }

                    }

                    /*
                     when currInstruc[0] == 2, which means when the instruction is "release"
                     add the returned units to resourceSemaphore
                     subtract the releasing units from current task's maximum additional request array
                     */

                    else if (currInstruc[0] == 2) {

                        int resourceType = currInstruc[3]-1;
                        int release = currInstruc[4];
                        resourceSemaphore[resourceType] += release;
                        curr.maxAdditional[resourceType] += release;
                        curr.currIndx++;
                    }

                    else {
                        /*
                        when currInstruc[0] == 3, which means when the instruction is "terminate"
                         */
                        curr.status = "terminated";
                        terminated.add(curr);
                        curr.terminatingT = globalT;
                    }
                }
            }


            globalT++;


            // add returned units to available array at the end of each cycle
            for (int i = 0; i < resourceSemaphore.length; i++) {
                available[i] += resourceSemaphore[i];
            }


        }

    }


    public static void errorMsgPrinting(StringBuilder errorMsg) {
        if (errorMsg.length() != 0) System.out.println(errorMsg);
    }


    // result printing
    public static void resultPrinting(PriorityQueue<Tasks> terminated) {

        int totalTimeTaken = 0;
        int totalWaitTime = 0;

        while (!terminated.isEmpty()) {
            Tasks curr = terminated.poll();

            int taskNum = curr.id + 1;
            int terminatingT = curr.terminatingT;
            int waitingT = curr.waitingT;
            double waitTPercentage = ((double)waitingT/terminatingT)*100;
            if (curr.status.equals("aborted")) {
                System.out.printf("Task %d      aborted", taskNum);
                System.out.println();
            }
            else{
                System.out.printf("Task %d %5d %5d", taskNum, terminatingT, waitingT);
                System.out.println("     " + (int)Math.round(waitTPercentage) + "%");
                totalTimeTaken += terminatingT;
                totalWaitTime += waitingT;
            }

        }

        double totalPercentage = ((double)totalWaitTime/totalTimeTaken)*100;

        System.out.printf("Total  %5d %5d", totalTimeTaken, totalWaitTime);
        System.out.println("     " + (int)Math.round(totalPercentage) + "%");


    }


}
