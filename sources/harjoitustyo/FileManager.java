package harjoitustyo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 */

/**
 * @author tkt
 *
 */
public class FileManager {
    public FileManager() {};

    /**
     *
     *
     * @param file
     * @return
     */
    public String readFile(File file) {
        BufferedReader input = null;
        StringBuilder sb = new StringBuilder();
        try {
            input = new BufferedReader(new FileReader(file));
            String line;
            while ((line = input.readLine()) != null) {
                sb.append(line+"\n");
            } 
        } catch (FileNotFoundException ex) {
            System.err.printf("To be read file: %s could not have been read.\n", file.getName());
        } catch (IOException e) {
            System.err.printf("Something went wrong with reading file: %s\n", file.getPath());
        } finally {
            try {
                if (input != null)
                    input.close();
                } catch (IOException e) {
                    System.err.printf("Something went wrong with closing file's '%s' stream.", file.getName());
                }
            }

        return sb.toString();
    }

    // Method for deleting file. Takes to be removed file as parameter.
    // Author: Teemu Tanninen
    // Added: 11.07.17
    public void deleteFile(File file) {
        boolean success = file.delete();
        // Print error message into error stream if file couldn't be removed.
        if (!success) 
            System.err.printf("To be removed file: '%s' could not be removed\n", file.getName());
    }



}
