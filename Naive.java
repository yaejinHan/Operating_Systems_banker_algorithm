import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Naive {

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


    public static void Naive(ArrayList<Tasks> taskLst, int[] available) {
        int initialNumTasks = taskLst.size();
        int globalT = 0;

        /*
        run algorithm until all tasks terminate
         */
        while (terminated.size() < initialNumTasks) {

            ArrayList<Integer> doneBlocked = new ArrayList<Integer>();
            int[] resourceSemaphore = new int[available.length];

            /*
            checking blocked tasks to see if it can unblock any of the previously blocked tasks
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

                    // if the request can be satisfied by the available units in the system, remove from blocked arrayList
                    if (available[resourceType] >= request) {
                        curr.status = "readyToGo";
                        doneBlocked.add(blockedID);
                        curr.currIndx++;
                        available[resourceType] -= request;
                        curr.currAlloc[resourceType] += request;
                        toRemove.add(blocked.get(i));

                    }
                    else {
                        curr.waitingT++;
                    }
                }

                blocked.removeAll(toRemove);
            }


            /// process tasks that's not terminated, aborted or haven't run yet
            for (int i = 0; i < taskLst.size(); i++) {
                Tasks curr = taskLst.get(i);

                if (curr.status.equals("terminated") || curr.status.equals("aborted") || doneBlocked.contains(curr.id) || blocked.contains(curr.id)) continue;

                int currInstrucIndx = curr.currIndx;
                int[] currInstruc = curr.instrucLst.get(currInstrucIndx);

                // if delay > 0, simply decrement the delay
                if (currInstruc[2] > 0) {
                    curr.instrucLst.get(currInstrucIndx)[2]--;
                }
                else {
                    // if initiating instruction, simply increment the instruction index to do the next instruction at the next cycle
                    if (currInstruc[0] == 0) curr.currIndx++;

                    /*
                     if instruction is "request"
                     grant requests as long as that amount of resource units are available in the system
                     */
                    else if (currInstruc[0] == 1) {
                        int resourceType = currInstruc[3]-1;
                        int request = currInstruc[4];

                        if (available[resourceType] >= request) {
                            curr.currAlloc[resourceType] += request;
                            available[resourceType] -= request;
                            curr.currIndx++;
                        }

                        // block task if request cannot be satisfied
                        else {
                            curr.status = "blocked";
                            curr.waitingT++;
                            blocked.add(curr.id);
                        }
                    }


                    /*
                    release instruction
                    add the releasing units to resourceSemaphore array
                    subtract releasing units from currently allocated array
                     */
                    else if (currInstruc[0] == 2) {

                        int resourceType = currInstruc[3]-1;
                        int release = currInstruc[4];
                        resourceSemaphore[resourceType] += release;
                        curr.currAlloc[resourceType] -= release;
                        curr.currIndx++;
                    }

                    else {
                        // if instruction is "terminate", add the task object to terminated priorityqueue
                        curr.status = "terminated";
                        terminated.add(curr);
                        curr.terminatingT = globalT;
                    }
                }
            }


            if (!doneBlocked.isEmpty()) doneBlocked.clear();


            /// checking for deadlock
            if (!blocked.isEmpty() && blocked.size() == (initialNumTasks- terminated.size()) + aborted.size()) {
                if (aborted.isEmpty()) {
                    /*
                     if aborted arraylist is empty, abort the first task
                     and return resource units it was holding to resourceSemaphore
                     */
                    Tasks toBeAborted = taskLst.get(0);
                    int[] currAlloc = toBeAborted.currAlloc;
                    for (int i = 0; i < available.length; i++) {
                        resourceSemaphore[i] += currAlloc[i];
                    }
                    toBeAborted.status = "aborted";
                    blocked.remove(blocked.indexOf(toBeAborted.id));
                    aborted.add(toBeAborted.id);
                    terminated.add(toBeAborted);
                }

                int indx = 0;
                /*
                check if any of the blocked tasks can be unblocked
                if none can be unblocked, abort tasks starting from low indexed id tasks until there's a task that can be unblocked
                 */
                while (indx < blocked.size()) {
                    Tasks curr = taskLst.get(blocked.get(indx));
                    int[] currInstruc = curr.instrucLst.get(curr.currIndx);
                    int resourceType = currInstruc[3]-1;
                    int request = currInstruc[4];
                    if (available[resourceType] + resourceSemaphore[resourceType] >= request) {
                        curr.status = "readyToGo";
                        break;
                    }
                    else {
                        int idToAbort = aborted.get(aborted.size()-1) + 1;
                        while (true) {
                            if (!terminated.contains(taskLst.get(idToAbort))) {
                                break;
                            }
                            else idToAbort++;
                        }

                        Tasks aborting = taskLst.get(idToAbort);
                        int[] returning = aborting.currAlloc;
                        for (int i = 0; i < returning.length; i++) {
                            resourceSemaphore[i] += returning[i];
                        }
                        aborting.status = "aborted";
                        blocked.remove(blocked.indexOf(aborting.id));
                        aborted.add(idToAbort);
                        terminated.add(aborting);
                    }

                    indx++;
                }

            }



            // add returned units to the available array at the end of each cycle
            for (int i = 0; i < resourceSemaphore.length; i++) {
                available[i] += resourceSemaphore[i];
            }


            globalT++;
        }

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
                System.out.printf("Task %d      deadlocked and aborted", taskNum);
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
