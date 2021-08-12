import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Genetic algorithm Soduko agent
 *
 * @author Michael Rocca 4/13/2021
 */
public final class Genetic {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private Genetic() {
        // no code needed here
    }

    //get index of element in array
    public static int findIndex(int arr[], int t) {
        // if array is Null
        if (arr == null) {
            return -1;
        }

        // find length of array
        int len = arr.length;
        int i = 0;

        // traverse in the array
        while (i < len) {

            // if the i-th element is t
            // then return the index
            if (arr[i] == t) {
                return i;
            } else {
                i = i + 1;
            }
        }
        return -1;
    }

    //get highest element in array
    static int highest(int[] arr) {
        int i;

        // Initialize maximum element
        int max = arr[0];

        // Traverse array elements from second and
        // compare every element with current max
        for (i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }

        return max;
    }

    //get second highest element in array
    static int secondHighest(int[] nums) {
        int high1 = Integer.MIN_VALUE;
        int high2 = Integer.MIN_VALUE;
        for (int num : nums) {
            if (num > high1) {
                high2 = high1;
                high1 = num;
            } else if (num > high2) {
                high2 = num;
            }
        }
        return high2;
    }

    //check if array contains element
    public static boolean contains(int[] array, int value) {
        for (int num : array) {
            if (value == num) {
                return true;
            }
        }
        return false;
    }

    //performs crossover on two parent states
    public static void crossover(LinkedList<Integer>[] states,
            LinkedList<Integer>[] newStates, int parentOne, int parentTwo,
            int divide) {
        LinkedList<Integer> child1 = new LinkedList<Integer>();
        LinkedList<Integer> child2 = new LinkedList<Integer>();
        for (int i = 0; i < states[0].size(); i++) {
            if (i < divide) {
                child1.add(states[parentOne].get(i));
                child2.add(states[parentTwo].get(i));
            } else {
                child1.add(states[parentTwo].get(i));
                child2.add(states[parentOne].get(i));
            }
        }
        int j = 0;
        while (newStates[j] != null) {
            j++;
        }
        newStates[j] = child1;
        newStates[j + 1] = child2;
    }

    //performs mutation by changing one random board space to a random value
    public static void mutate(LinkedList<Integer> state) {
        Random random = new Random();
        //pick a random space
        int space = random.ints(0, state.size()).findFirst().getAsInt();
        int current = state.get(space);
        //pick a random value and ensure it is not the same as the current value
        int value = random.ints(1, 5).findFirst().getAsInt();
        while (value == current) {
            value = random.ints(1, 5).findFirst().getAsInt();
        }
        //set the space to the new value
        state.set(space, value);
    }

    //get a random index of an array weighted by the values in the array
    public static int getWeightedRandom(int[] fitness) {
        int totalFitness = 0;
        for (int i : fitness) {
            totalFitness += i;
        }
        int weightedRandom = 0;
        for (double r = Math.random()
                * totalFitness; weightedRandom < fitness.length; ++weightedRandom) {
            r -= fitness[weightedRandom];
            if (r <= 0.0) {
                break;
            }
        }
        return weightedRandom;
    }

    //function to get the value of a particular space
    public static int getSpace(int i, String board, List<Integer> state) {
        char space = board.charAt(i);
        int count = 0;
        //getting a state space
        if (space == '*') {
            for (int j = 0; j <= i; j++) {
                if (board.charAt(j) == '*') {
                    count++;
                }
            }
            return state.get(count - 1);
        } else {
            //getting a permanent space
            return Character.getNumericValue(space);
        }
    }

    //reads the input file and creates the board vector
    public static String readInputBoard(BufferedReader file)
            throws IOException {
        String board = "";
        String str;
        file.readLine();
        while ((str = file.readLine()) != null) {
            board += str;
        }
        return board;
    }

    public static int evaluate(String board, List<Integer> state) {
        int conflicts = 0;
        int checkSpaceValue = 0;
        int currentSpaceValue;
        int checkCount = 0;
        int[][] checks = { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 }, { 0, 4, 8, 12 }, { 1, 5, 9, 13 },
                { 2, 6, 10, 14 }, { 3, 7, 11, 15 }, { 0, 1, 4, 5 },
                { 2, 3, 6, 7 }, { 8, 9, 12, 13 }, { 10, 11, 14, 15 } };
        //check conflicts at each space
        for (int i = 0; i < board.length(); i++) {
            //get the value of current space being checked
            currentSpaceValue = getSpace(i, board, state);
            //check for conflicts in each row, column box, containing space i
            for (int j = 0; j < 12; j++) {
                //inspect a col/row/box that includes the current space
                if (contains(checks[j], i)) {
                    checkCount = 0;
                    //look at each space in that col/row/box and count how many of the spaces are the same as the current space
                    for (int k = 0; k < 4; k++) {
                        checkSpaceValue = getSpace(checks[j][k], board, state);
                        if (currentSpaceValue == checkSpaceValue) {
                            checkCount++;
                        }
                    }
                    //if there are 2+ instances of the current value being checked in a row/col/box, log a conflict
                    if (checkCount > 1) {
                        conflicts++;
                    }
                }
            }
        }
        return board.length() * 3 - conflicts;
    }

    //fill the state vector with random values 1-4 for all open spaces
    public static void getInitialState(String board,
            LinkedList<Integer>[] states) {
        Random random = new Random();
        for (int i = 0; i < states.length; i++) {
            states[i] = new LinkedList<Integer>();
            for (int j = 0; j < board.length(); j++) {
                if (board.charAt(j) == '*') {
                    states[i].add(random.ints(1, 5).findFirst().getAsInt());
                }
            }
        }
    }

    //Genetic function that
    public static LinkedList<Integer> genetic(String board,
            LinkedList<Integer>[] states, SimpleWriter out) {
        int iterations = 0;
        int evaluation = 0;
        int highestEval = 0;
        int parentOne = 0;
        int parentTwo = 0;
        LinkedList<Integer>[] newStates;
        int[] fitness = new int[8];
        int divide = states[0].size() / 2;
        //keep searching until solution found
        while (evaluate(board, states[highestEval]) < board.length() * 3) {
            highestEval = 0;
            newStates = new LinkedList[8];
            iterations++;

            //get fitness values for each state in current population
            for (int i = 0; i < states.length; i++) {
                evaluation = evaluate(board, states[i]);
                fitness[i] = evaluation;
            }

            //select pairs of parents based on weighted fitness and cross breed them
            for (int j = 0; j < states.length / 2; j++) {
                //get parent indexes
                parentOne = getWeightedRandom(fitness);
                parentTwo = getWeightedRandom(fitness);
                while (parentTwo == parentOne) {
                    parentTwo = getWeightedRandom(fitness);
                }

                //perform the crossover on both parents
                crossover(states, newStates, parentOne, parentTwo, divide);
            }

            //mutate all next generation states
            for (int k = 0; k < newStates.length; k++) {
                mutate(newStates[k]);
            }

            //after crossover and mutation, set the population to be the new generation
            states = newStates;

            //get fitness values for each state in current population
            for (int i = 0; i < states.length; i++) {
                evaluation = evaluate(board, states[i]);
                fitness[i] = evaluation;
                if (evaluation > evaluate(board, states[highestEval])) {
                    highestEval = i;
                }
            }

            //get the two most fit states
            int first = highest(fitness);
            int maxIndex = findIndex(fitness, first);

            int second = secondHighest(fitness);
            int maxIndex2 = findIndex(fitness, second);

            //print the two most fit states and their evaluation values
            out.println("Iteration " + iterations + ":");
            out.print("Most fit state: ");
            for (int z = 0; z < states[maxIndex].size() - 1; z++) {
                out.print(states[maxIndex].get(z) + ", ");
            }
            out.print(states[maxIndex].get(states[maxIndex].size() - 1));
            out.println();
            out.println("State evaluation value: "
                    + evaluate(board, states[maxIndex]));

            //second state
            out.print("Second most fit state: ");
            for (int z2 = 0; z2 < states[maxIndex2].size() - 1; z2++) {
                out.print(states[maxIndex2].get(z2) + ", ");
            }
            out.print(states[maxIndex2].get(states[maxIndex2].size() - 1));
            out.println();
            out.println("State evaluation value: "
                    + evaluate(board, states[maxIndex2]));
            out.println();

        }
        return states[highestEval];
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleWriter out = new SimpleWriter1L();
        BufferedReader inputFile = null;
        String board = "";

        //check command usage

        if (args.length != 1) {
            System.err.println("Usage: Genetic <filename>");
            System.exit(1);
        }
        //open input file
        try {
            inputFile = new BufferedReader(new FileReader(args[0]));
        } catch (Exception e) {
            System.err.println(
                    "Ooops!  I can't seem to load the file \"" + args[0]
                            + "\", do you have the file in the correct place?");
            System.exit(1);
        }

        //LinkedLists for current state and whole board
        LinkedList<Integer>[] states = new LinkedList[8];

        //read input file and save initial Board as a string
        try {
            board = readInputBoard(inputFile);
        } catch (Exception e) {
            System.err.println("Error reading file");
            System.exit(1);
        }

        //initialize the blank spaces on the board in the state vector
        getInitialState(board, states);

        out.println("Each generation contains " + states.length + " states");

        /*
         * //run the hill climber algorithm. The final state is in spaceVector
         * when the hill Climber is complete
         */
        LinkedList<Integer> solution = genetic(board, states, out);

        out.println("Solution found!");
        out.println();
        for (int i = 0; i < solution.size() - 1; i++) {
            out.print(solution.get(i) + ", ");
        }
        if (solution.size() > 0) {
            out.print(solution.get(solution.size() - 1));
        }

    }

}
