package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static java.awt.Font.BOLD;

public class World {
    private Double seed;
    private Random generator;
    private Random seedGenerator;
    private final int width;
    private final int height;
    private boolean theme;
    private final int[] roomCenter;
    private TETile[][] map = null;
    private TETile[][] losMap = null;
    private TETile[][] mapCopy = null;
    private boolean isWorldMade = false;
    private boolean inMenu = false;
    private TERenderer renderer;
    private ArrayList<Room> mapRooms;
    private final int numberOfSplits;
    private final Room originalRoom;
    private Player player;
    private boolean showUI = true;
    private boolean showLOS = false;
    private static final String SAVE_FILE = "save.txt";
    private boolean quitFirstPhase = false;
    public World() {
        this.width = 90;
        this.height = 50;
        this.originalRoom = new Room(width - 2, height - 2, 1, 1);
        this.roomCenter = new int[]{45, 25};
        mapRooms = new ArrayList<>();
        this.numberOfSplits = 8;
        theme = true;
        this.theme = true;
    }
    //Accessors and Modifiers
    public TETile[][] getWorld() {
        return map; }
    public void toggleWorldMade() {
        this.isWorldMade = !this.isWorldMade; }
    public boolean getIsWorldMade() {
        return this.isWorldMade; }
    public boolean getInMenu() {
        return this.inMenu; }
    public void toggleMenu() {
        this.inMenu = !this.inMenu; }
    private void toggleUI() {
        this.showUI = !this.showUI;
    }
    private void toggleLOS() {
        this.showLOS = !this.showLOS;
    }
    public void initializeRenderer() {
        renderer = new TERenderer();
        renderer.initialize(width, height);
    }
    public static String getSaveFile() {
        return SAVE_FILE;
    }
    /*
    public void autoGraderSaveFileChanger() {
        SAVE_FILE = "save.txt";
    }
     */
    public void openMenu() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledSquare(0.5, 0.5, 0.5);
        Font titleFont = new Font("Monospaced", BOLD, 25);
        Font wordFont = new Font("Monospaced", BOLD, 20);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(titleFont);
        StdDraw.text(0.5, 0.75, "CS61B: THE GAME");
        StdDraw.setFont(wordFont);
        StdDraw.text(0.5, 0.5, "New Game (N)");
        StdDraw.text(0.5, 0.45, "Load Game (L)");
        StdDraw.text(0.5, 0.4, "Quit (Q)");
    }
    private void updateUI() {
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledRectangle(6.5, height - 1.5, 6.5, 1.5);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("Monospaced", BOLD, 15));
        StdDraw.textLeft(0.3, height - 0.5, "Points : " + player.getPoints());
        StdDraw.textLeft(0.3, height - 1.5, "Pointing to : " + this.getWorld()[(int) StdDraw.mouseX()][(int)
                StdDraw.mouseY()].description());
        if (theme) {
            StdDraw.textLeft(0.3, height - 2.5, "Current Theme : Tundra");
        } else {
            StdDraw.textLeft(0.3, height - 2.5, "Current Theme : Forest");
        }
        StdDraw.show();
    }
    public void renderGame() {
        //StdDraw.setFont();
        StdDraw.setPenColor();
        StdDraw.setPenRadius();
        renderer.drawTiles(map);
        if (showUI) {
            updateUI();
        }
        StdDraw.show();
    }
    public void createWorld(double newSeed) {
        this.seed = newSeed;
        this.seedGenerator = new Random((int) (this.seed % 2147483647));
        int intSeed = seed.intValue();
        this.generator = new Random(intSeed);
        theme = true;
        toggleMenu();
        toggleWorldMade();

        //Creates and initializes world object
        map = new TETile[width][height];
        losMap = new TETile[width][height];
        mapCopy = new TETile[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                losMap[x][y] = Tileset.NOTHING;
                mapCopy[x][y] = Tileset.NOTHING;
            }
        }

        fillWithNothing(map);
        generateWorld();
        filterRooms();
        createHallways(mapRooms);
        drawRooms();
        drawCenters();
        makeWalls();
        createPlayer();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mapCopy[x][y] = map[x][y];
            }
        }
        changeTheme();
    }
    public void clearSave() {
        FileUtils.writeFile(SAVE_FILE, "");
    }
    //Loads world from file
    public static World loadGame(String fileName) {
        World newWorld = null;
        StringBuilder seed = new StringBuilder();
        String input = FileUtils.readFile(fileName);
        boolean seedInputed = false;
        char[] split = input.toCharArray();
        for (char c: split) {
            String current = String.valueOf(c);
            if (current.equalsIgnoreCase("N")) {
                newWorld = new World();
            } else if (Character.isDigit(c)) {
                seed.append(c);
            } else if (current.equalsIgnoreCase("S") && !seedInputed) {
                newWorld.createWorld(Double.parseDouble(String.valueOf(seed)));
                seedInputed = true;
            } else {
                newWorld.updateThroughInput(c, true);
            }
        }
        return newWorld;
    }

    //Select Random Room and Place Player there
    private void createPlayer() {
        player = new Player(mapRooms.get(seedGenerator.nextInt(0, mapRooms.size())).getRoomCenter());
    }

    private void generateWorld() {
        originalRoom.splitRoom(numberOfSplits);
    }
    private void drawCenters() {
        for (Room r: mapRooms) {
            map[r.roomCenter[0]][r.roomCenter[1]] = Tileset.FLOWER;
        }
    }
    private void filterRooms() {
        ArrayList<Room> finalRoomList = new ArrayList<>();
        for (Room r: mapRooms) {
            if (r.origin[0] >= 0 && r.origin[1] >= 0 && r.origin[0] + r.getRoomDimensions()[0] < width
                    && r.origin[1] + r.getRoomDimensions()[1] < height && r.getRoomDimensions()[0] >= 3
                    && r.getRoomDimensions()[1] >= 3) {
                finalRoomList.add(r);
            }
        }
        mapRooms = finalRoomList;
    }
    private void drawRooms() {
        for (Room r: mapRooms) {
            for (int y = r.getOrigin()[1]; y < r.getOrigin()[1] + r.getRoomDimensions()[1]; y++) {
                for (int x = r.getOrigin()[0]; x < r.getOrigin()[0] + r.getRoomDimensions()[0]; x++) {
                    map[x][y] = Tileset.GRASS;
                }
            }
        }
    }
    private void makeWalls() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] currentCheck = new int[]{x, y};
                if ((map[x][y] != Tileset.GRASS && map[x][y] != Tileset.WALL && map[x][y] != Tileset.FLOWER)) {
                    if (x - 1 > 0 && y + 1 < height) {
                        if (map[x - 1][y + 1] == Tileset.GRASS || map[x - 1][y + 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (y + 1 < height) {
                        if (map[x][y + 1] == Tileset.GRASS || map[x][y + 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (x + 1 < width && y + 1 < height) {
                        if (map[x + 1][y + 1] == Tileset.GRASS || map[x + 1][y + 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (x - 1 > 0) {
                        if (map[x - 1][y] == Tileset.GRASS || map[x - 1][y] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (x + 1 < width) {
                        if (map[x + 1][y] == Tileset.GRASS || map[x + 1][y] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (x - 1 > 0 && y - 1 > 0) {
                        if (map[x - 1][y - 1] == Tileset.GRASS || map[x - 1][y - 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (y - 1 > 0) {
                        if (map[x][y - 1] == Tileset.GRASS || map[x][y - 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                    if (x + 1 < width && y - 1 > 0) {
                        if (map[x + 1][y - 1] == Tileset.GRASS || map[x + 1][y - 1] == Tileset.WALL) {
                            map[x][y] = Tileset.SAND;
                        }
                    }
                }
            }
        }
    }
    private void fillWithNothing(TETile[][] input) {
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                input[x][y] = Tileset.NOTHING;
            }
        }
    }
    private class Room {
        private final int[] roomDimensions;
        private final int[] roomCenter;
        private final int[] origin;
        private static final int UPPERBOUND = 7;
        private static final int LOWERBOUND = 4;
        public Room(int width, int height, int originX, int originY) {
            this.roomDimensions = new int[] {width, height};
            this.origin = new int[] {originX, originY};
            this.roomCenter = new int[] {originX + width / 2, originY + height / 2};
        }
        public int[] getRoomDimensions() {
            return this.roomDimensions;
        }
        public int[] getRoomCenter() {
            return roomCenter;
        }
        public int[] getOrigin() {
            return origin;
        }

        private Room[] splitVertical(Room r, int amount, int xBorderChange, int yBorderChange) {
            //give a room, splits "amount" from the center
            Room temp1;
            Room temp2;
            temp1 = new Room(r.roomDimensions[0] - amount - 2 * xBorderChange,
                    r.roomDimensions[1] - 2 * yBorderChange,
                    r.origin[0] + xBorderChange,
                    r.origin[1] + yBorderChange);
            temp2 = new Room(amount - 2 * xBorderChange,
                    r.roomDimensions[1] - 2 * yBorderChange,
                    r.origin[0] + r.roomDimensions[0] - amount + xBorderChange,
                    r.origin[1] + yBorderChange);
            return new Room[]{temp1, temp2};
        }
        private Room[] splitHorizontal(Room r, int amount, int xBorderChange, int yBorderChange) {
            Room temp1;
            Room temp2;
            temp1 = new Room(r.roomDimensions[0] - 2 * xBorderChange,
                    r.roomDimensions[1] - amount - 2 * yBorderChange,
                    r.origin[0] + xBorderChange,
                    r.origin[1] + yBorderChange);
            temp2 = new Room(r.roomDimensions[0] - 2 * xBorderChange,
                    amount - 2 * yBorderChange,
                    r.origin[0] + xBorderChange,
                    r.origin[1] + r.roomDimensions[1] - amount + yBorderChange);
            return new Room[]{temp1, temp2};
        }
        public void splitRoom(int numberOfSplits1) {
            Room[] temp;
            if (this.roomDimensions[0] <= 3 || this.roomDimensions[1] <= 3) {
                mapRooms.add(this);
            } else if (numberOfSplits1 > 0) {
                //Random either 0 or 1 inclusive
                int randomIndicatorOne = Math.abs(seedGenerator.nextInt(2));
                //Random between 1 and 7 inclusive (biggest room dimension should be upperBound)
                int randomIndicatorTwo = seedGenerator.nextInt(-UPPERBOUND, UPPERBOUND);

                //Must split vertical
                if (this.roomDimensions[0] >= this.roomDimensions[1] * 1.25) {
                    //Splits Down Center
                    if (randomIndicatorOne == 0 || this.roomDimensions[1] <= LOWERBOUND) {
                        temp = splitVertical(this, this.roomDimensions[0] / 2,
                                Math.abs(seedGenerator.nextInt(-1, 1)), 0);
                        //Splits randomly
                    } else {
                        temp = splitVertical(this, this.roomDimensions[0] / 2 + randomIndicatorTwo,
                                0, 0);
                    }

                    //Must split horizontal
                } else if (this.roomDimensions[1] >= this.roomDimensions[0] * 1.25) {
                    //Splits Down Center
                    if (randomIndicatorOne == 0  || this.roomDimensions[0] <= LOWERBOUND) {
                        temp = splitHorizontal(this, this.roomDimensions[1] / 2,
                                0, Math.abs(seedGenerator.nextInt(-1, 1)));
                        //Splits randomly
                    } else {
                        temp = splitHorizontal(this, this.roomDimensions[1] / 2 + randomIndicatorTwo,
                                0, 0);
                    }

                    //Splits randomly
                } else {
                    if (randomIndicatorOne == 0) {
                        temp = splitVertical(this, this.roomDimensions[0] / 2 + randomIndicatorTwo,
                                Math.abs(seedGenerator.nextInt(-1, 1)),
                                Math.abs(seedGenerator.nextInt(-1, 1)));
                    } else {
                        temp = splitHorizontal(this, this.roomDimensions[1] / 2 + randomIndicatorTwo,
                                Math.abs(seedGenerator.nextInt(-1, 1)),
                                Math.abs(seedGenerator.nextInt(-1, 1)));
                    }
                }

                //Decides whether they should be split again
                for (Room r: temp) {
                    if (r.roomDimensions[0] < UPPERBOUND || r.roomDimensions[1] < UPPERBOUND) {
                        if (seedGenerator.nextBoolean()) {
                            mapRooms.add(r);
                        }
                    } else {
                        r.splitRoom(numberOfSplits1 - 1);
                    }
                }
            } else {
                mapRooms.add(this);
            }
        }
    }
    public Room findClosestRoom(Room startRoom, ArrayList<Room> roomList) {
        int distanceSq = 1000000;
        Room closestRoom = null;
        for (Room room : roomList) {
            int xDist = Math.abs(startRoom.roomCenter[0] - room.roomCenter[0]);
            int yDist = Math.abs(startRoom.roomCenter[1] - room.roomCenter[1]);
            if (Math.pow(xDist, 2) + Math.pow(yDist, 2) < distanceSq) {
                distanceSq = (int) (Math.pow(xDist, 2) + Math.pow(yDist, 2));
                closestRoom = room;
            }
        }
        return closestRoom;
    }
    public Room findFurthestRoom(Room startRoom, ArrayList<Room> roomList) {
        int distanceSq = 0;
        Room closestRoom = null;
        for (Room room : roomList) {
            int xDist = Math.abs(startRoom.roomCenter[0] - room.roomCenter[0]);
            int yDist = Math.abs(startRoom.roomCenter[1] - room.roomCenter[1]);
            if (Math.pow(xDist, 2) + Math.pow(yDist, 2) > distanceSq) {
                distanceSq = (int) (Math.pow(xDist, 2) + Math.pow(yDist, 2));
                closestRoom = room;
            }
        }
        return closestRoom;
    }
    public void drawHallwayX(int startPointX, int endPointX, int y) {
        if (endPointX < startPointX) {
            int tempPoint = startPointX;
            startPointX = endPointX;
            endPointX = tempPoint;
        }
        for (int x = startPointX; x <= endPointX; x++) {
            map[x][y] = Tileset.WALL;
        }
    }
    public void drawHallwayY(int startPointY, int endPointY, int x) {
        if (endPointY < startPointY) {
            int tempPoint = startPointY;
            startPointY = endPointY;
            endPointY = tempPoint;
        }
        for (int y = startPointY; y <= endPointY; y++) {
            map[x][y] = Tileset.WALL;
        }
    }
    public void drawHallway(Room start, Room end) {
        if (seedGenerator.nextBoolean()) { //draw x-dim of hallway first
            drawHallwayX(start.roomCenter[0], end.roomCenter[0], start.roomCenter[1]);
            drawHallwayY(start.roomCenter[1], end.roomCenter[1], end.roomCenter[0]);
        } else {
            drawHallwayY(start.roomCenter[1], end.roomCenter[1], start.roomCenter[0]);
            drawHallwayX(start.roomCenter[0], end.roomCenter[0], end.roomCenter[1]);
        }
    }

    public void createHallways(ArrayList<Room> roomList) {
        ArrayList<Room> roomListCopy = (ArrayList<Room>) roomList.clone();
        //System.out.println(roomListCopy);
        Room startRoom = roomListCopy.get(0);
        roomListCopy.remove(0);
        while (!roomListCopy.isEmpty()) {
            Room nextClosest = findClosestRoom(startRoom, roomListCopy);
            drawHallway(startRoom, nextClosest);
            roomListCopy.remove(nextClosest);
            startRoom = nextClosest;
        }

        int numRooms = roomList.size();
        int intSeed = seed.intValue();
        //Random generator = new Random(intSeed);
        for (int i = 0; i < 3; i++) {
            Room room1 = roomList.get(generator.nextInt(numRooms));
            Room room2 = findFurthestRoom(room1, roomList);
            drawHallway(room1, room2);
        }
    }

    public boolean touchingTwoHallways(int x, int y) {
        int counter = 0;
        if (x + 1 < width && map[x + 1][y] == Tileset.WALL) {
            counter++;
        }
        if (x - 1 < width && map[x - 1][y] == Tileset.WALL) {
            counter++;
        }
        if (y + 1 < width && map[x][y + 1] == Tileset.WALL) {
            counter++;
        }
        if (y - 1 < width && map[x][y - 1] == Tileset.WALL) {
            counter++;
        }
        if (counter > 2) {
            return true;
        }
        return false;
    }
    public class Player {

        private int[] location;
        private TETile standingOn;
        private final TETile design = Tileset.AVATAR;
        private int points;
        //private int health;
        private Player(int[] startingLocation) {
            this.location = startingLocation;
            this.standingOn = Tileset.GRASS;
            map[startingLocation[0]][startingLocation[1]] = design;
            points = 0;
        }

        public int getPoints() {
            return points;
        }

        enum Directions {
            UP,
            DOWN,
            LEFT,
            RIGHT
        }

        private void tryChangeLocation(int x, int y) {
            if (!theme) {
                if (map[this.location[0] + x][this.location[1] + y] != Tileset.SAND
                        && mapCopy[this.location[0] + x][this.location[1] + y] != Tileset.SAND) {
                    map[this.location[0]][this.location[1]] = standingOn;
                    mapCopy[this.location[0]][this.location[1]] = standingOn;
                    this.standingOn = map[this.location[0] + x][this.location[1] + y];
                    this.location[0] += x;
                    this.location[1] += y;
                    map[this.location[0]][this.location[1]] = design;
                    mapCopy[this.location[0]][this.location[1]] = design;
                }
            } else {
                if (map[this.location[0] + x][this.location[1] + y] != Tileset.LOCKED_DOOR
                        && mapCopy[this.location[0] + x][this.location[1] + y] != Tileset.LOCKED_DOOR) {
                    map[this.location[0]][this.location[1]] = standingOn;
                    mapCopy[this.location[0]][this.location[1]] = standingOn;
                    this.standingOn = map[this.location[0] + x][this.location[1] + y];
                    this.location[0] += x;
                    this.location[1] += y;
                    map[this.location[0]][this.location[1]] = design;
                    mapCopy[this.location[0]][this.location[1]] = design;
                }
            }
        }
        public void updatePoints(int newPoints) {
            this.points += newPoints;
        }
        public void updateStandingOn(TETile newStandingOn) {
            this.standingOn = newStandingOn;
        }
        public void updateLocation(int[] newLocation) {
            this.location = newLocation;
        }
    }
    public void movePlayer(Player.Directions direction) {
        switch (direction) {
            case UP:
                player.tryChangeLocation(0, 1);
                break;
            case DOWN:
                player.tryChangeLocation(0, -1);
                break;
            case LEFT:
                player.tryChangeLocation(-1, 0);
                break;
            case RIGHT:
                player.tryChangeLocation(1, 0);
                break;
            default:
                break;
        }
    }
    public void updateFile(String input) {
        FileUtils.writeFile(SAVE_FILE, FileUtils.readFile(SAVE_FILE) + input);
    }
    public void updateThroughInput(char input, boolean loading) {
        String temp = String.valueOf(input).toUpperCase();
        switch (temp) {
            // Moves Player Up
            case "W":
                this.movePlayer(Player.Directions.UP);
                if (!loading) {
                    updateFile(temp);
                }
                updateLOSMap();
                quitFirstPhase = false;
                break;
            // Moves Player Down
            case "S":
                this.movePlayer(Player.Directions.DOWN);
                if (!loading) {
                    updateFile(temp);
                }
                updateLOSMap();
                quitFirstPhase = false;
                break;
            // Moves Player Right
            case "D":
                this.movePlayer(Player.Directions.RIGHT);
                if (!loading) {
                    updateFile(temp);
                }
                updateLOSMap();
                quitFirstPhase = false;
                break;
            // Moves Player Left
            case "A":
                this.movePlayer(Player.Directions.LEFT);
                if (!loading) {
                    updateFile(temp);
                }
                updateLOSMap();
                quitFirstPhase = false;
                break;
            // Quit
            case "Q":
                if (quitFirstPhase) {
                    System.exit(0);
                }

                break;
            case "I":
                toggleUI();
                quitFirstPhase = false;
                break;
            case "O":
                updateFile(temp);
                quitFirstPhase = false;
                break;
            case "P":
                updateFile(temp);
                quitFirstPhase = false;
                break;
            case ":":
                quitFirstPhase = true;
                break;
            default:
                //otherwise
                System.err.println("Unhandled input: " + temp);
                quitFirstPhase = false;
                break;
        }
        if (player.standingOn == Tileset.FLOWER) {
            player.updatePoints(100);
            if (!theme) {
                player.updateStandingOn(Tileset.GRASS);
            } else {
                player.updateStandingOn(Tileset.CELL);
            }
        }
        if (temp.equalsIgnoreCase("p")) {
            if (showLOS) {
                toggleLOS();
                map = mapCopy;
            } else {
                toggleLOS();
                updateLOSMap();  // Update the LOS map
                copyMap(map, mapCopy); // Update mapCopy with the latest state of map before switching
                map = losMap; // Switch to LOS map
            }
            quitFirstPhase = false;
        }
        if (temp.equalsIgnoreCase("o")) {
            changeTheme();
            quitFirstPhase = false;
        }
    }

    private boolean inSight(int[] start, int[] end) {
        //within radius of 5 is visible
        int tX = end[0] - start[0];
        int tY = end[1] - start[1];
        return tX * tX + tY * tY <= 25;
    }

    private void updateLOSMap() {
        int x = player.location[0];
        int y = player.location[1];
        fillWithNothing(losMap);
        //losMap[x][y] = mapCopy[x][y];
        losMap[x][y] = player.design;
        idk(x, y, 10);
    }
    private void idk(int x, int y, int distance) {
        if (!this.theme) {
            if (distance >= 0) {
                if (mapCopy[x][y] == Tileset.GRASS || mapCopy[x][y] == Tileset.FLOWER || mapCopy[x][y] == Tileset.AVATAR
                        || mapCopy[x][y] == Tileset.WALL) {
                    idk(x - 1, y, distance - 1);
                    idk(x, y + 1, distance - 1);
                    idk(x, y - 1, distance - 1);
                    idk(x + 1, y, distance - 1);
                    losMap[x][y] = mapCopy[x][y];
                } else if (mapCopy[x][y] == Tileset.SAND) {
                    losMap[x][y] = mapCopy[x][y];
                }
            }
        } else {
            if (distance >= 0) {
                if (mapCopy[x][y] == Tileset.CELL || mapCopy[x][y] == Tileset.FLOWER || mapCopy[x][y] == Tileset.AVATAR
                        || mapCopy[x][y] == Tileset.WATER) {
                    idk(x - 1, y, distance - 1);
                    idk(x, y + 1, distance - 1);
                    idk(x, y - 1, distance - 1);
                    idk(x + 1, y, distance - 1);
                    losMap[x][y] = mapCopy[x][y];
                } else if (mapCopy[x][y] == Tileset.LOCKED_DOOR) {
                    losMap[x][y] = mapCopy[x][y];
                }
            }
        }
    }

    private void copyMap(TETile[][] original, TETile[][] copy) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy[x][y] = original[x][y];
            }
        }
    }

    private void changeTheme() {
        updatePlayerStandingOn();
        System.out.println(this.theme);
        System.out.println(theme);
        if (!this.theme) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (map[x][y] == Tileset.WALL) {
                        map[x][y] = Tileset.WATER;
                        mapCopy[x][y] = Tileset.WATER;
                        losMap[x][y] = Tileset.WATER;
                    }
                    if (map[x][y] == Tileset.GRASS) {
                        map[x][y] = Tileset.CELL;
                        mapCopy[x][y] = Tileset.CELL;
                        losMap[x][y] = Tileset.CELL;
                    }
                    if (map[x][y] == Tileset.SAND) {
                        map[x][y] = Tileset.LOCKED_DOOR;
                        mapCopy[x][y] = Tileset.LOCKED_DOOR;
                        losMap[x][y] = Tileset.LOCKED_DOOR;
                    }
                }
            }
        } else if (this.theme) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (map[x][y] == Tileset.WATER) {
                        map[x][y] = Tileset.WALL;
                        mapCopy[x][y] = Tileset.WALL;
                        losMap[x][y] = Tileset.WALL;
                    }
                    if (map[x][y] == Tileset.CELL) {
                        map[x][y] = Tileset.GRASS;
                        mapCopy[x][y] = Tileset.GRASS;
                        losMap[x][y] = Tileset.GRASS;
                    }
                    if (map[x][y] == Tileset.LOCKED_DOOR) {
                        map[x][y] = Tileset.SAND;
                        mapCopy[x][y] = Tileset.SAND;
                        losMap[x][y] = Tileset.SAND;
                    }
                }
            }
        } /*
        if (generator.nextInt(3) == 2) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (map[x][y] == Tileset.WALL) {
                        map[x][y] = Tileset.WATER;
                        mapCopy[x][y] = Tileset.WATER;
                        losMap[x][y] = Tileset.WATER;
                    }
                    if (map[x][y] == Tileset.GRASS) {
                        map[x][y] = Tileset.CELL;
                        mapCopy[x][y] = Tileset.CELL;
                        losMap[x][y] = Tileset.CELL;
                    }
                    if (map[x][y] == Tileset.SAND) {
                        map[x][y] = Tileset.LOCKED_DOOR;
                        mapCopy[x][y] = Tileset.LOCKED_DOOR;
                        losMap[x][y] = Tileset.LOCKED_DOOR;
                    }
                }
            }
        }
        /*
        */
        this.theme = !this.theme;
    }
    private void updatePlayerStandingOn() {
        // This method should check what the player is standing on and update it to match the new theme
        TETile playerTile = player.standingOn;
        if (this.theme) {
            if (player.standingOn == Tileset.CELL) {
                player.standingOn = Tileset.GRASS;
            }
            if (player.standingOn == Tileset.WATER) {
                player.standingOn = Tileset.WALL;
            }
        } else {
            if (player.standingOn == Tileset.GRASS) {
                player.standingOn = Tileset.CELL;
            }
            if (player.standingOn == Tileset.WALL) {
                player.standingOn = Tileset.WATER;
            }
        }
    }
}
