package workshop05code;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
//Import for logging exercise
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SQLiteConnectionManager {
    //Start code logging exercise
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());
    //End code logging exercise
    
    private String databaseURL = "";

    private static final String WORDLE_DROP_TABLE_STRING = "DROP TABLE IF EXISTS wordlist;";
    private static final String WORDLE_CREATE_STRING = "CREATE TABLE wordlist (\n"
            + " id integer PRIMARY KEY,\n"
            + " word text NOT NULL\n"
            + ");";

    private static final String VALID_WORDS_DROP_TABLE_STRING = "DROP TABLE IF EXISTS validWords;";
    private static final String VALID_WORDS_CREATE_STRING = "CREATE TABLE validWords (\n"
            + " id integer PRIMARY KEY,\n"
            + " word text NOT NULL\n"
            + ");";
    /**
     * Set the database file name in the sqlite project to use
     *
     * @param fileName the database file name
     */
    public SQLiteConnectionManager(String filename) {
        databaseURL = "jdbc:sqlite:sqlite/" + filename;
        logger.log(Level.INFO, "SQLiteConnectionManager initialised with database file: {0}", filename);

    }

    /**
     * Connect to a sample database
     *
     * @param fileName the database file name
     */
    public void createNewDatabase(String fileName) {

        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.log(Level.INFO,"The driver name is {0}", meta.getDriverName());
                logger.log(Level.INFO, "A new database has been created: {0}", databaseURL);

            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating database: " + databaseURL, e);
            //System.out.println("Error creating database, See log file for details.");

        }
    }

    /**
     * Check that the file has been cr3eated
     *
     * @return true if the file exists in the correct location, false otherwise. If
     *         no url defined, also false.
     */
    public boolean checkIfConnectionDefined() {
        if (databaseURL.equals("")) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL)) {
                if (conn != null) {
                    logger.log(Level.FINE, "Connection to database established: {0}", databaseURL);
                    return true;
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error checking database connection:" + databaseURL, e);
                return false;
            }
        }
        return false;
    }

    /**
     * Create the table structures (2 tables, wordle words and valid words)
     *
     * @return true if the table structures have been created.
     */
    public boolean createWordleTables() {
        if (databaseURL.equals("")) {
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL);
                    Statement stmt = conn.createStatement()) {
                stmt.execute(WORDLE_DROP_TABLE_STRING);
                logger.log(Level.FINE, "Dropped table: wordlist");
                stmt.execute(WORDLE_CREATE_STRING);
                logger.log(Level.FINE, "Created table: validWords");
                stmt.execute(VALID_WORDS_DROP_TABLE_STRING);
                logger.log(Level.FINE, "Dropped table: validWords");
                stmt.execute(VALID_WORDS_CREATE_STRING);
                logger.log(Level.FINE, "Created table: validWords");
                return true;

            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error creating wordle tables in database: " + databaseURL, e);
                //System.out.println("Error setting up game tables.See log file for detail.");
                return false;
            }
        }
    }

    /**
     * Take an id and a word and store the pair in the valid words
     * 
     * @param id   the unique id for the word
     * @param word the word to store
     */
    public void addValidWord(int id, String word) {

        String sql = "INSERT INTO validWords(id,word) VALUES(?,?);"; // ? for both parameters 

        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id); // set parameters 1 and 2
                    pstmt.setString(2,word);
            pstmt.executeUpdate();
            logger.log(Level.FINE, "Added valid word: {0} with id: {1}", new Object[] { word, id });
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error adding valid word: " + word + " to database:" + databaseURL, e);
            //System.out.println("Error adding a word. See log file for details.");
        }

    }

    /**
     * Possible weakness here?
     * 
     * @param guess the string to check if it is a valid word.
     * @return true if guess exists in the database, false otherwise
     */
    public boolean isValidWord(String guess) {
        String sql = "SELECT count(id) as total FROM validWords WHERE word = ?;"; //? for parameter 
        

        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1,guess); //set parameter value

            ResultSet resultRows = stmt.executeQuery();
            if (resultRows.next()) {
                int result = resultRows.getInt("total");
                if (result >= 1) {
                    logger.log(Level.FINE, "Checked if word '{0}' is valid. Result: true", guess);
                    return true;
                } else {
                    logger.log(Level.FINE, "Checked if word '{0}' is valid. Result: false", guess);
                    return false;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking if word '" + guess + "' is valid in database:" + databaseURL, e);
            //System.out.println("Error checking word validity. See log file for details.");
            return false;
        }

    }
 public void loadValidWordsFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                String word = line.trim();
                if (isValidWord(word)) {
                    addValidWord(id++, word);
                    logger.log(Level.FINE, "Loaded valid word from file: {0}", word);
                } else {
                    logger.log(Level.SEVERE, "Invalid word format in file: {0}", word);
                }
            }
            logger.log(Level.INFO, "Finished loading valid words from file: {0}", filePath);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading valid words from file: " + filePath, e);
            //System.out.println("Error reading word list file. See log file for details."); // Generic user message
        }
    }

 /**
     * Basic check if a word is in a valid format (e.g., only letters).
     * This can be expanded based on the game's requirements.
     *
     * @param word the word to check.
     * @return true if the word format is valid, false otherwise.
     */
    private boolean isValidWordFormat(String word) {
        return word != null && word.matches("[a-zA-Z]+");
    }

    // Hypothetical method to handle invalid guesses (assuming this class might receive guesses)
    public void logInvalidGuess(String guess) {
        logger.log(Level.INFO, "Invalid guess: {0}", guess);
        System.out.println("Invalid guess."); // Game-related info to console
    }


    public static void main(String[] args) {
        SQLiteConnectionManager manager = new SQLiteConnectionManager("wordle.db");
        manager.createNewDatabase("wordle.db");
        if (manager.checkIfConnectionDefined()) {
            manager.createWordleTables();
            // Load words from data.txt (assuming data.txt is in the project root or a specified path)
            manager.loadValidWordsFromFile("data.txt");
     
        }

}
}
