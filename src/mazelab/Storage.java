package mazelab;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Ryan Darnell
 */
public class Storage {
    int width = 0;
    int height = 0;
    int[][] array2D;
    
    public Storage() {
        //blank constructor
    }
    
    public char[][] load() {
        char[][] array = new char[255][255];
        char[][] arrayTrimmed;
        try {
            Random random = new Random();
            FileReader file = new FileReader("TruePath.txt");
            //Scanner checker = new Scanner(file);
            Scanner inFile = new Scanner(file);
            inFile.useDelimiter("\n");
            String in = "";
            
            int i = 0;
            
            //in case load file is empty
            if (!inFile.hasNextLine())
                return null;
            while (inFile.hasNextLine()) {
                in = inFile.nextLine();
                //check for maze content
                if (in.charAt(1) == ' ')
                    return null;
                if (in.length() <= 100 && i < 100) {
                    for (int j=0; j<in.length(); j++) {
                            array[i][j] = in.charAt(j);       
                    }
                }
                else {
                    System.out.println("Maximum maze size exceeded: (" + 255 + " x " + 255 + ")!");
                    System.exit(1);
                }
                i++;
            }
            //Trim the array down
            width = in.length();
            height = i;
            arrayTrimmed = new char[height][width];
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    arrayTrimmed[j][k] = array[j][k];
                }
            }
            System.out.println("("+width+" x "+height+ ") maze opened.");
            System.out.println();
            
            inFile.close();
            
        } catch (FileNotFoundException e) {
            array = null;
            System.out.println("No local solution found.");
            System.out.println("Deploying Right Hand Algorithm...");
        }
        return array;
    }
    
    public int width() {
        return width;
    }
    public int height(){
        return height;
    }

    public void save(char[][] toSave, int row, int column) {
        Scanner in = new Scanner(System.in);
        try {
            PrintWriter outFile = new PrintWriter("TruePath.txt");
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < column; j++) {
                    outFile.print(toSave[i][j]);
                }
                outFile.println();
            }
            outFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find that file");
        }
    }
}
