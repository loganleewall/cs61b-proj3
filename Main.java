package core;
import edu.princeton.cs.algs4.StdDraw;
//Should be good now

public class Main {
    public static void main(String[] args) {
        World newWorld = new World();
        World lineOfSightWorld = new World();
        World shownWorld = newWorld;
        int world = 0;
        while (true) {
            //In menu
            while (!newWorld.getIsWorldMade()) {
                if (!newWorld.getInMenu()) {
                    newWorld.openMenu();
                    newWorld.toggleMenu();
                }
                if (StdDraw.hasNextKeyTyped()) {
                    char nextKey = StdDraw.nextKeyTyped();
                    //System.out.println("Key pressed: " + nextKey);
                    if (String.valueOf(nextKey).equalsIgnoreCase("N")) {
                        newWorld.clearSave();
                        newWorld.updateFile("N");
                        StringBuilder inputSeed = new StringBuilder();
                        while (true) {
                            if (StdDraw.hasNextKeyTyped()) {
                                char key = StdDraw.nextKeyTyped(); // Read the next key typed
                                if (key == '\n') {  // If 'Enter' key is pressed, break the loop
                                    break;
                                }
                                if (Character.isDigit(key)) {
                                    inputSeed.append(key);
                                    newWorld.updateFile(String.valueOf(key));
                                } else {
                                    System.out.println("Invalid character ignored: " + key);
                                }
                            }
                        }
                        newWorld.updateFile("S");
                        newWorld.createWorld(Double.parseDouble(String.valueOf(inputSeed)));
                        newWorld.initializeRenderer();
                    } else if (String.valueOf(nextKey).equalsIgnoreCase("L")) {
                        newWorld = World.loadGame(World.getSaveFile());
                        newWorld.initializeRenderer();
                    } else if (String.valueOf(nextKey).equalsIgnoreCase("Q")) {
                        System.exit(0);
                    }
                }
            }
            //In game
            if (StdDraw.hasNextKeyTyped()) {
                newWorld.updateThroughInput(StdDraw.nextKeyTyped(), false);
            }

            newWorld.renderGame();

        }
    }
}
