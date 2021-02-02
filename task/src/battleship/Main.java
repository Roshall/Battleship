package battleship;

import java.util.Scanner;

public class Main {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 10;
    private final int [][] battleYard = new int[SIZE][SIZE];
    private int shipRemain = 5 + 4 + 3 + 3 + 2;

    public boolean hasShip() {
        return shipRemain > 0;
    }

    public void shotOneGrid() {
        if (shipRemain > 0) {
            shipRemain--;
        }
    }

    public boolean isOneShipSank(int i, int j) {
        return !isShipNearBy(i, j);
    }

    public void setShip(int i, int j, int value) {
        battleYard[i][j] = value;
    }

    public boolean isShip(int i, int j) {
        return battleYard[i][j] == Status.SHIP_PIECES.ordinal();
    }

    public boolean isShot(int i, int j) {
        return battleYard[i][j] == Status.SHOT.ordinal();
    }

    private boolean isShipNearBy(int i, int j) {
        int upper = (i - 1 + SIZE) % SIZE;
        int lower = (i + 1 + SIZE) % SIZE;
        int left = (j - 1 + SIZE) % SIZE;
        int right = (j + 1 + SIZE) % SIZE;
        return isShip(i, j) || isShip(upper, j) || isShip(lower, j) ||
                isShip(i, left) || isShip(i, right);
    }

    private void printBattleYard() {
        printBattleYard(false);
    }

    private void printBattleYard(boolean masked) {
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        char row = 'A';
        for (int i = 0; i < SIZE; i++) {
            int j = 0;
            System.out.print(row++);
            for (; j < SIZE; j++) {
                int value = battleYard[i][j];
                System.out.print(" " + Status.valueOf((masked ? (value > 1 ? value: 0) : value) ));
            }
            System.out.println();
        }
        System.out.println();
    }

    private static int[] parseCoordinate(String str) {
        int[] coordinate = new int[2];
        try {
            coordinate[0] = str.charAt(0) - 'A';
            coordinate[1] = Integer.parseInt(str.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }
        // check if it is out of range
        for (int i = 0; i < 2; i++) {
            if (coordinate[i] < 0 || coordinate[i] > SIZE - 1) {
                return null;
            }
        }
        return coordinate;
    }


    private static boolean parseLine(String ord, int[][] coordinates) {
        ord = ord.trim();
        ord = ord.replaceAll("\\s+", " ");
        ord = ord.toUpperCase();
        String[] codnts = ord.split(" ");
        if (codnts.length != 2) {
            return false;
        }

        // get the coordinate
        for (int i = 0; i < 2; i++) {
            if ((coordinates[i] = parseCoordinate(codnts[i])) == null) {
                return false;
            }

        }

        for (int i = 0; i < 2; i++) {
            int maximum = Integer.max(coordinates[0][i], coordinates[1][i]);
            int minimum = Integer.min(coordinates[0][i], coordinates[1][i]);
            coordinates[0][i] = minimum;
            coordinates[1][i] = maximum;
        }
        return (coordinates[0][0] - coordinates[1][0]) * (coordinates[0][1] - coordinates[1][1]) == 0;
    }

    public void startGame() {
        Scanner input = new Scanner(System.in);
        int[][] coordinates = new int[2][2];


        for (Ship ship: Ship.values()) {
            printBattleYard();
            System.out.printf("Enter the coordinates of the %s (%d cells):\n", ship.getName(), ship.getSize());
            ErrorType flag = ErrorType.ON_ERROR;
            retry: do {
                if (flag != ErrorType.ON_ERROR) {
                    System.out.printf("Error! %s! Try again:\n", flag.getMsg());
                }
                if (!parseLine(input.nextLine(), coordinates)) {
                    flag = ErrorType.OUT_OF_RANG;
                    continue;
                }
                // horizontal or vertical
                int direction = coordinates[0][0] == coordinates[1][0] ? HORIZONTAL : VERTICAL;
                if (direction == HORIZONTAL) {
                    if (coordinates[1][1] - coordinates[0][1] + 1 != ship.getSize()) {
                        flag = ErrorType.WRONG_LEN;
                        continue;
                    } else {
                        // check if ships are too close
                        for (int j = coordinates[0][1]; j <= coordinates[1][1]; j++) {
                            if (isShipNearBy(coordinates[0][0], j)) {
                                flag = ErrorType.TOO_CLOSE;
                                continue retry;
                            }
                        }
                        // no overlapping, then occupy the place
                        for (int j = coordinates[0][1]; j <= coordinates[1][1]; j++) {
                            setShip(coordinates[0][0], j, Status.SHIP_PIECES.ordinal());
                        }
                    }
                } else { // vertical
                    if (coordinates[1][0] - coordinates[0][0] + 1 != ship.getSize()) {
                        flag = ErrorType.WRONG_LEN;
                        continue;
                    } else {
                        // check if overlapping
                        for (int i = coordinates[0][0]; i <= coordinates[1][0]; i++) {
                            if (isShipNearBy(i, coordinates[1][1])) {
                                flag = ErrorType.TOO_CLOSE;
                                continue retry;
                            }
                        }
                        // no overlapping, then occupy the place
                        for (int i = coordinates[0][0]; i <= coordinates[1][0]; i++) {
                            setShip(i, coordinates[1][1], Status.SHIP_PIECES.ordinal());
                        }
                    }
                }
                flag = ErrorType.ON_ERROR;
            } while (flag != ErrorType.ON_ERROR);
        }
        printBattleYard();
    }

    public boolean shoot(String str) {
        int[] coordinate = parseCoordinate(str);
        if (coordinate != null) {
            int x = coordinate[0];
            int y = coordinate[1];
            if (isShip(x, y)) {
                setShip(x, y, Status.SHOT.ordinal());
                shotOneGrid();
                if (isOneShipSank(x, y) && hasShip()) {
                    System.out.println("You sank a ship!");
                } else {
                    System.out.println("You hit a ship!");
                }
//                printBattleYard(true);
            } else if (isShot(x, y)) {
//                printBattleYard(true);
                System.out.println("You have already hit this spot.");
            } else {
                setShip(x, y, Status.MISSED.ordinal());
//                printBattleYard(true);
                System.out.println("You missed.");
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        // Write your code here
        Main[] players = new Main[2];
        Scanner input = new Scanner(System.in);

        // set ships for each player
        for (int i = 0 ;i < players.length; i++) {
            players[i] = new Main();
            System.out.printf("Player %d, place your ships on the game field\n", i + 1);
            players[i].startGame();
            if (i < players.length - 1) {
                System.out.println("Press Enter and pass the move to another player");
                input.nextLine();
            }
        }
        /*
         * game is started
         */
        int whoseTurn = 0;
        outermost : while (true) {
            // check if game is over
            for (Main player : players) {
                if (!player.hasShip()) {
                    break outermost;
                }
            }

            System.out.println("Press Enter and pass the move to another player");
            input.nextLine();

            // print all the battle yard
            int enemy = (whoseTurn + 1) % players.length;
            players[enemy].printBattleYard(true);
            System.out.println("---------------------");
            players[whoseTurn].printBattleYard();
            // shoot a spot for each player
            System.out.printf("Player %d, it's your turn:\n", whoseTurn+1);
            Main enemyYard = players[enemy];
            boolean flag = true;
            do {
                if (!flag) {
                    System.out.println("Error! You entered the wrong coordinates! Try again:");
                }
                flag = enemyYard.shoot(input.nextLine().trim());
            } while (!flag);
            whoseTurn = enemy;
        }

        // print all the battle yard
        int enemy = (whoseTurn + 1) % players.length;
        players[enemy].printBattleYard();
        System.out.println("---------------------");
        players[whoseTurn].printBattleYard();
        System.out.println("You sank the last ship. You won. Congratulations!");

    }


}

enum Ship {
    AIRCRAFT_CARRIER("Aircraft Carrier", 5), BATTLESHIP("Battleship", 4),
    SUBMARINE("Submarine", 3), CRUISER("Cruiser ", 3), DESTROYER("Destroyer ", 2)
    ;
    private String name;
    private int size;

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    Ship(String s, int i) {
        this.name = s;
        this.size = i;
    }
}
enum ErrorType {
    ON_ERROR(""), OUT_OF_RANG("Wrong location"), WRONG_LEN("Wrong length"), TOO_CLOSE("Too close to another one");

    private String msg;


    public String getMsg() {
        return msg;
    }

    ErrorType(String msg) {
        this.msg = msg;
    }
}
enum Status {
    FOG("~"), SHIP_PIECES("O"), SHOT("X"), MISSED("M");

    private String symbol;

    Status(String c) {
        symbol = c;
    }

    public String getSymbol() {
        return symbol;
    }

    public static String valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal].getSymbol();
    }
}