package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
//Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    // Start code for logging exercise
    private static final Logger logger;
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
        logger = Logger.getLogger(App.class.getName());
    }
    // End code for logging exercise
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
            logger.log(Level.INFO, "Wordle database created and connection checked successfully.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            logger.log(Level.SEVERE, "Not able to connect to the Wordle database.");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
            logger.log(Level.INFO, "Wordle table structures created successfully.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            logger.log(Level.SEVERE, "Not able to create Wordle table structures.");
            return;
        }

        // let's add some words to valid 4 letter words from the data.txt file

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                String word = line.trim();
                if (word.matches("^[a-z]{4}$")) {
                    wordleDatabaseConnection.addValidWord(i, word);
                    logger.log(Level.FINE, "Loaded valid word from file: {0}", word);
                    i++;
                } else {
                    logger.log(Level.SEVERE, "Invalid word found in data.txt: {0}. Expected a 4-letter lowercase word.", word);
                }
            }
            logger.log(Level.INFO, "Finished reading words from data.txt and added valid ones to the database.");
        } catch (IOException e) {
            System.out.println("Not able to load . Sorry!");
            logger.log(Level.WARNING, "Error reading data.txt file.", e);
            return;
        }

        // let's get them to enter a word

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

                while (!guess.equals("q")) {
                    if(guess.matches("^[a-z]{4}$")) {
                        System.out.println("You've guessed '" + guess+"'.");
                        logger.log(Level.INFO, "User guessed: {0}", guess);

                        if (wordleDatabaseConnection.isValidWord(guess)) { 
                            System.out.println("Success! It is in the the list.\n");
                            logger.log(Level.INFO, "Guess '{0}' is a valid word.", guess);
                        }else{
                            System.out.println("Sorry. This word is NOT in the the list.\n");
                            logger.log(Level.INFO, "Guess '{0}' is NOT in the valid word list.", guess);
                        }
                    } else {
                        System.out.println("Sorry. This is not a valid 4 letter word.\n");
                        logger.log(Level.WARNING, "Invalid guess format: '{0}'. Expected a 4-letter lowercase word.", guess);
                    }
                    System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                    guess = scanner.nextLine();
                    
                }
                logger.log(Level.INFO, "User quit the game.");
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error during user input.", e);
            System.out.println("An error occurred during input. Please see the log file for details."); 
    }
    
}
}