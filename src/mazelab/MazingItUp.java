/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mazelab;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;
/**
 *
 * @author hunterl & darnesey
 */
public class MazingItUp extends JFrame {

    private static final int MAX_WIDTH = 255;
    private static final int MAX_HEIGHT = 255;
    
    private char [][] maze = new char[MAX_HEIGHT][MAX_WIDTH];

    private Random random = new Random();
    private JPanel mazePanel = new JPanel();
    private int width = 0;
    private int height = 0;
    private boolean step = false;
    
    private boolean timerFired = false;
    private Timer timer;
    private final int TIMER_DELAY = 200;
    
    private final int SPRITE_WIDTH = 25;
    private final int SPRITE_HEIGHT = 25;
    
    private BufferedImage mazeImage;
    private ImageIcon ground = new ImageIcon("sprites/ground.png");
    private ImageIcon wall1 = new ImageIcon("sprites/cactus.png");
    private ImageIcon wall2 = new ImageIcon("sprites/rock.png");
    private ImageIcon finish = new ImageIcon("sprites/well.png");
    private ImageIcon south1 = new ImageIcon("sprites/cowboy-forward-1.png");
    private ImageIcon south2 = new ImageIcon("sprites/cowboy-forward-2.png");
    private ImageIcon north1 = new ImageIcon("sprites/cowboy-back-1.png");
    private ImageIcon north2 = new ImageIcon("sprites/cowboy-back-2.png");
    private ImageIcon west1 = new ImageIcon("sprites/cowboy-left-1.png");
    private ImageIcon west2 = new ImageIcon("sprites/cowboy-left-2.png");
    private ImageIcon east1 = new ImageIcon("sprites/cowboy-right-1.png");
    private ImageIcon east2 = new ImageIcon("sprites/cowboy-right-2.png");
    
    private long startTime;
    private long currentTime;
    
    Storage sto = new Storage();
    ArrayList<Integer> path = new ArrayList<>();
    
    
    /**
     * Constructor for class Maze.  Opens a text file containing the maze, then attempts to 
     * solve it.
     * 
     * @param   fname   String value containing the filename of the maze to open.
     */
    public MazingItUp(String fname) {        
        //load my file if exists
        if(sto.load() == null)
            openMaze(fname);
        else {
            maze = sto.load();
            width = sto.width;
            height = sto.height;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (maze[i][j] == '-')
                        maze[i][j] = '.';
                }
            }
        }
        mazeImage = printMaze();
        printTextMaze();

        timer = new Timer(TIMER_DELAY, new TimerHandler());     // setup a Timer to slow the animation down.
        timer.start();
        
        
        addWindowListener(new WindowHandler());     // listen for window event windowClosing
        
        setTitle("Cowboy Maze");
        setSize(width*SPRITE_WIDTH+10, height*SPRITE_HEIGHT+30);
        setVisible(true);

        add(mazePanel);
        setContentPane(mazePanel);
        
        solveMaze();
        printTextMaze();
    }
    
    /**
     * Called from the operating system.  If no command line arguments are supplied,
     * the method displays an error message and exits.  Otherwise, a new instance of
     * Maze() is created with the supplied filename from the command line.
     * 
     * @param   args[]  Command line arguments, the first of which should be the filename to open.
     */
    public static void main(String [] args) {
        int runny = 1;
        if (args.length > 0) {
            new MazingItUp(args[0]);
        }
        else {
            System.out.println();
            System.out.println("Usage: java Maze <filename>.");
            System.out.println("Maximum Maze size:" + MAX_WIDTH + " x " + MAX_HEIGHT + ".");
            System.out.println();
            System.exit(1);
        }
    }
    
    /**
     * Finds the starting location and passes it to the recursive algorithm solve(x, y, facing).
     * The starting location should be the only '.' on the outer wall of the maze.
     */
    public void solveMaze() {
        boolean startFound = false;
        if (!startFound) {
            for (int i=0; i<width; i++) {       // look for the starting location on the top and bottom walls of the Maze.
                if (maze[0][i] == '.') {
                    if (path.size() != 0)
                        preSolve(i, 0, "path");
                    preSolve(i, 0, "south");
                    startFound = true;
                }
                else if (maze[height-1][i] == '.') {
                    if (path.size() != 0)
                        preSolve(i, height - 1, "path");
                    preSolve(i, height-1, "north");
                    startFound = true;
                }
            }
        }
        if (!startFound) {
            for (int i=0; i<height; i++) {      // look for the starting location on the left and right walls of the Maze.
                if (maze[i][0] == '.') {
                    if (path.size() != 0)
                        preSolve(0, i, "path");
                    preSolve(0, i, "east");
                    startFound = true;
                }
                else if (maze[i][width-1] == '.') {
                    if (path.size() != 0)
                        preSolve(width - 1, i, "path");
                    preSolve(width-1, i, "west");
                    startFound = true;
                }
            }
        }
        if (!startFound) {
            System.out.println("Start not found!");
        }
    }
    
    
    public void preSolve(int x, int y, String facing)
    {
        //Graphics2D g2 = (Graphics2D)mazePanel.getGraphics();
        //g2.drawImage(mazeImage, null, 0, 0);
        //g2.drawImage(printGuy(facing), x*SPRITE_WIDTH, y*SPRITE_HEIGHT, null, null);
        Scanner input = new Scanner(System.in);
        System.out.println("Press 1 to start");
        input.nextLine();
        startTime = System.currentTimeMillis();
        try {
            solve(x, y, facing);
        } catch(ArrayIndexOutOfBoundsException ouch) {
            solve(1, 1, "FOUND");
            sto.save(maze, height, width);
        }
    }
    
    /**
     * Recursive algorithm to solve a Maze.  Places a X at locations already visited.
     * This algorithm is very inefficient, it follows the right hand wall and will
     * never find the end if the path leads it in a circle.
     * 
     * @param   column  int value of the current X location in the Maze.
     * @param   row     int value of the current Y location in the Maze.
     * @param   facing  String value holding one of four cardinal directions 
     *                  determined by the current direction facing.
     */
    private void solve(int column, int row, String facing) throws ArrayIndexOutOfBoundsException {
        Graphics2D g2 = (Graphics2D)mazePanel.getGraphics(); //don't mess with the next 
        while (!timerFired) {   // wait for the timer.
          try{Thread.sleep(10);} catch(Exception e){}
        }
        timerFired = false;
        currentTime = System.currentTimeMillis();
        if((currentTime - startTime) > 50000)
        {
            sto.save(maze, height, width);
            closingMethod();
        }
        
         
        
        if (maze[row][column] != 'F') {  //this is if it doesn't find the finish on a turn.........
            g2.drawImage(mazeImage, null, 0, 0); 
            g2.drawImage(printGuy(facing), column*SPRITE_WIDTH, row*SPRITE_HEIGHT, null, null);
            mazePanel.setSize(width*SPRITE_WIDTH+10, height*SPRITE_HEIGHT+30);
            maze[row][column] = '-';   // mark this spot as visited. This is how you can keep track of where you've been. 

            
            
            if (facing.compareTo("FOUND") == 0){
                //Just having this state logic expression prevents the recursion from
                //continuing after the error code is thrown. 
            } 
            
            else if(facing.compareTo("east") == 0){
                //facing right or column + 1
                //right = row + 1
                if(maze[row + 1][column] != '.' && maze[row - 1][column] != '.' && maze[row][column + 1] != '.'){
                    //DEAD END
                    //or F! FIND IT!
                    if (maze[row + 1][column] == 'F') {//south
                        solve(column, row + 1, "FOUND");
                    }
                    if (maze[row][column + 1] == 'F') {//east
                        solve(column + 1, row, "FOUND"); 
                    }
                    if (maze[row - 1][column] == 'F') {//north
                        solve(column, row - 1, "FOUND");
                    }
                    return;
                } if (maze[row + 1][column] == '.' || maze[row + 1][column] == 'F'){ //south
                    if (maze[row + 1][column] == 'F') 
                        solve(column, row + 1, "FOUND");
                    solve(column, row + 1, "south");// stepping backwards call
                    maze[row + 1][column] = '+'; //mark DEAD path
                    solve(column, row, "east");
                } if (maze[row][column + 1] == '.' || maze[row][column + 1] == 'F') { //east
                    if (maze[row][column + 1] == 'F')
                        solve(column + 1, row, "FOUND");
                    solve(column + 1, row, "east");
                    maze[row][column + 1] = '+'; //mark DEAD path
                    solve(column, row, "east"); //step backwards and check
                } if (maze[row - 1][column] == '.' || maze[row - 1][column] == 'F') { //north
                    if (maze[row - 1][column] == 'F')
                        solve(column, row - 1, "FOUND");
                    solve(column, row - 1, "north");
                    maze[row - 1][column] = '+'; //mark DEAD path
                    solve(column, row, "east");
                }
            } else if(facing.compareTo("west") == 0){
                //facing left or column - 1
                //right = row - 1
                if(maze[row + 1][column] != '.' && maze[row - 1][column] != '.' && maze[row][column - 1] != '.'){
                    //DEAD END
                    if (maze[row - 1][column] == 'F') {//north
                        solve(column, row - 1, "FOUND");
                    }
                    if (maze[row][column - 1] == 'F') {//west
                        solve(column - 1, row, "FOUND");
                    }
                    if (maze[row + 1][column] == 'F') {//south
                        solve(column, row + 1, "FOUND");
                    }
                    return;
                } if (maze[row - 1][column] == '.' || maze[row - 1][column] == 'F'){ //north
                    if (maze[row - 1][column] == 'F')
                        solve(column, row - 1, "FOUND");
                    solve(column, row - 1, "north");
                    maze[row - 1][column] = '+';
                    solve(column, row, "west");
                } if (maze[row][column - 1] == '.' || maze[row][column - 1] == 'F') { //west
                    if (maze[row][column - 1] == 'F')
                        solve(column - 1, row, "FOUND");
                    solve(column - 1, row, "west");
                    maze[row][column - 1] = '+';
                    solve(column, row, "west");
                } if (maze[row + 1][column] == '.' || maze[row + 1][column] == 'F') { //south
                    if (maze[row + 1][column] == 'F')
                        solve(column, row + 1, "FOUND");
                    solve(column, row + 1, "south");
                    maze[row + 1][column] = '+';
                    solve(column, row, "west");
                }
                
            } else if(facing.compareTo("north") == 0){
                //facing up or row - 1
                //right = column + 1
                if(maze[row - 1][column] != '.' && maze[row][column - 1] != '.' && maze[row][column + 1] != '.'){ //includes #'s, %'s, and +'s
                    //DEAD END or path retrace
                    if (maze[row][column + 1] == 'F') {//east
                        solve(column + 1, row, "FOUND");
                    }
                    if (maze[row - 1][column] == 'F') {//north
                        solve(column, row - 1, "FOUND");
                    }
                    if (maze[row][column - 1] == 'F') {//west
                        solve(column - 1, row, "FOUND");
                    }
                    return;
                } if (maze[row][column + 1] == '.' || maze[row][column + 1] == 'F'){ //east
                    if (maze[row][column + 1] == 'F')
                        solve(column + 1, row, "FOUND");
                    solve(column + 1, row, "east");
                    maze[row][column + 1] = '+';
                    solve(column, row, "north");
                } if (maze[row - 1][column] == '.' || maze[row - 1][column] == 'F') { //north
                    if (maze[row - 1][column] == 'F')
                        solve(column, row - 1, "FOUND");
                    solve(column, row - 1, "north");
                    maze[row - 1][column] = '+';
                    solve(column, row, "north");
                } if (maze[row][column - 1] == '.' || maze[row][column - 1] == 'F') { //west
                    if (maze[row][column - 1] == 'F')
                        solve(column - 1, row, "FOUND");
                    solve(column - 1, row, "west");
                    maze[row][column - 1] = '+';
                    solve(column, row, "north");
                }
                
            } else { //facing south
                //facing down or row + 1
                //right = column - 1
                if(maze[row + 1][column] != '.' && maze[row][column - 1] != '.' && maze[row][column + 1] != '.'){
                    //DEAD END
                    if (maze[row][column - 1] == 'F') {//west
                        solve(column - 1, row, "FOUND");
                    }
                    if (maze[row + 1][column] == 'F') {//south
                        solve(column, row + 1, "FOUND"); 
                    }
                    if (maze[row][column + 1] == 'F') {//east
                        solve(column + 1, row, "FOUND");
                    }
                    return;
                } if (maze[row][column - 1] == '.' || maze[row][column - 1] == 'F'){ //west
                    if (maze[row][column - 1] == 'F')
                        solve(column - 1, row, "FOUND");
                    solve(column - 1, row, "west");
                    maze[row][column - 1] = '+';
                    solve(column, row, "south");
                } if (maze[row + 1][column] == '.' || maze[row + 1][column] == 'F') { //south
                    if (maze[row + 1][column] == 'F')
                        solve(column, row + 1, "FOUND");
                    solve(column, row + 1, "south");
                    maze[row + 1][column] = '+';
                    solve(column, row, "south");
                } if (maze[row][column + 1] == '.' || maze[row][column + 1] == 'F') { //east
                    if (maze[row][column + 1] == 'F')
                        solve(column + 1, row, "FOUND");
                    solve(column + 1, row, "east");
                    maze[row][column + 1] = '+';
                    solve(column, row, "south");
                }
            }
            sto.save(maze, height, width);
                
        }
        else {
            //Time for some dirty code... besides the above code... Break the recursion...
            try {
                    maze[300][300] = 0;
                } catch(Exception ouch) {
            
                System.out.println("Found the finish!");

                //don't mess with the following 4 lines, but you can add stuff below that if you need. 
                currentTime = System.currentTimeMillis();
                long endTime = currentTime - startTime;
                long finalTime = endTime / 1000;
                System.out.println("Final Time = " + finalTime);
                
                sto.save(maze, height, width);
                
                throw new ArrayIndexOutOfBoundsException("BREAK THE RECURSION!");
            }
            }
        }        
    

    

    
    /**
     * Opens a text file containing a maze and stores the data in the 2D char array maze[][].
     * 
     * @param   fname   String value containing the file name of the maze to open.
     */
    public void openMaze(String fname) {
        String in = "";
        int i = 0;
        try {
            Scanner sc = new Scanner(new File(fname));
            while (sc.hasNext()) {
                in = sc.nextLine();
                in = trimWhitespace(in);
                if (in.length() <= MAX_WIDTH && i < MAX_HEIGHT) {
                    for (int j=0; j<in.length(); j++) {
                        if (in.charAt(j) == '#') {      // if this spot is a wall, randomize the wall peice to display
                            if (random.nextInt(2) == 0) {
                                maze[i][j] = '#';   
                            }
                            else {
                                maze[i][j] = '%';
                            }
                        }
                        else {
                            maze[i][j] = in.charAt(j);
                        }
                    }
                }
                else {
                    System.out.println("Maximum maze size exceeded: (" + MAX_WIDTH + " x " + MAX_HEIGHT + ")!");
                    System.exit(1);
                }
                i++;
            }
            width = in.length();
            height = i;
            System.out.println("("+width+" x "+height+ ") maze opened.");
            System.out.println();
            sc.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("File not found: " + e);
        }
    }
    
    /**
     * Removes white space from the supplied string and returns the trimmed String.
     * 
     * @param   s   String value to strip white space from.
     * @return  String stripped of white space.
     */
    public String trimWhitespace(String s) {
        String newString = "";
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i) != ' ') {
                newString += s.charAt(i);
            }
        }
        return newString;
    }
    
    /**
     * Returns the sprite facing the direction supplied.
     * 
     * @param   facing  String value containing 1 of 4 cardinal directions to make the sprite face.
     * @return  Image of the sprite facing the proper direction.
     */
    private Image printGuy(String facing) {
        if(facing.equals("south")) {  // draw sprite facing south
            if (step) {
                step = false;
                return south1.getImage();
             }
            else {
                step = true;
                return south2.getImage();
            }
        }
        else if(facing.equals("north")) {  // draw sprite facing north
            if (step) {
                step = false;
                return north1.getImage();
             }
            else {
                step = true;
                return north2.getImage();
            }
        }
        else if(facing.equals("east")) {  // draw sprite facing east
            if (step) {
                step = false;
                return east1.getImage();
            }
            else {
                step = true;
                return east2.getImage();
            }
        }
        else if(facing.equals("west")) {  // draw sprite facing west
            if (step) {
                step = false;
                return west1.getImage();
            }
            else {
                step = true;
                return west2.getImage();
            }
        }
        return null;
    }
    
    /**
     * Prints the Maze using sprites.
     * 
     * @return BufferedImage rendition of the maze.
     */
    public BufferedImage printMaze() {
        BufferedImage mi = new BufferedImage(width*SPRITE_WIDTH, height*SPRITE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = mi.createGraphics();
        
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                if (maze[i][j] == '#') {    // draw wall
                    g2.drawImage(wall1.getImage(), j*SPRITE_WIDTH, i*SPRITE_HEIGHT, null, null);
                }
                else if (maze[i][j] == '%') {   // draw wall
                    g2.drawImage(wall2.getImage(), j*SPRITE_WIDTH, i*SPRITE_HEIGHT, null, null);
                }
                else if (maze[i][j] == '.' || maze[i][j] == '-' || maze[i][j] == '+') {  // draw ground
                    g2.drawImage(ground.getImage(), j*SPRITE_WIDTH, i*SPRITE_HEIGHT, null, null);
                }
                else if (maze[i][j] == 'F') {   // draw finish
                    g2.drawImage(finish.getImage(), j*SPRITE_WIDTH, i*SPRITE_HEIGHT, null, null);
                }
            }
        }
         return mi;
    }

     public void closingMethod()
     {
            long endTime = currentTime - startTime;
            long finalTime = endTime / 100;
            System.out.println("Final Time = " + ((double)finalTime/(double)10));  
            System.exit(0);
      }
    /**
     * Handles the Timer, updates the boolean timerFired every time the Timer ticks.
     * Used to slow the animation down.
     */
    private class TimerHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            timerFired = true;
        }
    }
    
    /**
     * Catch the windowClosing event and exit gracefully.
     */
    private class WindowHandler extends WindowAdapter {
        public void windowClosing (WindowEvent e) {
            removeAll();
            closingMethod();
            System.exit(0);
        }        
    }  
    
    public void printTextMaze(){
        System.out.println();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(maze[i][j] + " ");
            }
            System.out.println();
        }
    }

}