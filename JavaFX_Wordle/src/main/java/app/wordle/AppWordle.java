package app.wordle;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.util.Random;

public class AppWordle extends Application {

    public static final Color wrongSpot = Color.YELLOW;
    public static final Color rightSpot = Color.BLUE;
    public static final int wordLength = 5;
    public static final int tries = 5;
    public static String solution = "";

    //how many attempts were already used
    public static int counter = 0;

    public static int lineCount = 0;

    public static final GridPane gridPane = new GridPane();

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setTitle("Wordle");

        countLines();
        createField();
        solution = getRandomWord();

        startGame();

        stage.show();
    }

    /*
      counts the ammount of lines in "words"
     */
    public void countLines(){
        //reading and counting all the lines in the words_alpha.txt file
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(AppWordle.class.getResourceAsStream("/words_alpha.txt")))){
            while(bufferedReader.readLine()!=null){lineCount++;}
        } catch (IOException e){
            System.out.println(e.getStackTrace());
        }
    }

    public void createField(){
        //creating the rectangles, one extra row for the answer
        for (int i = 0; i < tries+1; i++) {
            for (int j = 0; j < wordLength; j++) {
                Rectangle rectangle = new Rectangle(1000, 1000, Color.LIGHTGRAY);
                //rectangles bound to gridpane via fluent binding
                rectangle.widthProperty().bind(gridPane.widthProperty().divide(wordLength));
                rectangle.heightProperty().bind(gridPane.heightProperty().divide(tries+2));

                gridPane.add(rectangle ,j, i);
            }
        }
    }

    /*
      output: a string matching the criteria in validWord
     */
    public String getRandomWord(){
        //picking random word for the solution
        Random random = new Random();
        String randomWord  = "";
        while(!validWord(randomWord)){
            //caveman method, just try words until it works :)
            int chosenLine = random.nextInt(lineCount);

            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(AppWordle.class.getResourceAsStream("/words_alpha.txt")))){
                //reading lines until before chosen word
                for (int i = 0; i < chosenLine; i++) {
                    bufferedReader.readLine();
                }
                //reading next line with the chosen word
                randomWord = bufferedReader.readLine();
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return randomWord;
    }

    //################################ Game Logic #####################################
    public void startGame(){
        TextField inputField = new TextField();
        inputField.setPromptText("Enter your guess");
        inputField.setOnAction(event -> {
            handleUserInput(inputField.getText());
            inputField.setText("");
        });

        gridPane.add(inputField, 0, 6);
    }

    /*
      input: a string
      checks if the word is valid, and writes it into the current line, if not guesses left, writes the answer in the extra line
      returns false if the game should be stopped, but I don't want to do that
     */
    public boolean handleUserInput(String inputWord){
        if(!lost()){
            if(validWord(inputWord)){
                writeWordIntoRow(inputWord, counter);
                //player won1
                if(inputWord.equals(solution)){
                    System.out.println("YOU WIN");
                    return false;
                }
                counter++;
                //all guesses were used, player lost
                if(counter == tries){
                    writeWordIntoRow(solution, counter);
                    System.out.println("YOU LOST\nSolution was: "+solution);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /*
      input: a valid word that gets written into the nth row (decided by counter)
     */
    public void writeWordIntoRow(String inputWord, int row){
        char[] splitInputWord = inputWord.toCharArray();
        for (int i = 0; i < wordLength; i++) {
            Rectangle rectangle = (Rectangle) gridPane.getChildren().get(row*wordLength+i);
            int rectangleXCord = GridPane.getColumnIndex(rectangle);
            int rectangleYCord = GridPane.getRowIndex(rectangle);

            Label label = new Label(splitInputWord[i] + "");
            label.setAlignment(Pos.CENTER);
            label.setPrefWidth(rectangle.getWidth());
            label.setPrefHeight(rectangle.getHeight());
            gridPane.add(label, rectangleXCord, rectangleYCord);

            if(solution.contains(String.valueOf(splitInputWord[i]))){
                if(solution.toCharArray()[i] == splitInputWord[i]){
                    rectangle.setFill(rightSpot);
                } else {
                    rectangle.setFill(wrongSpot);
                }
            }
        }
    }

    /*
      input: a string
      checks if the string is the required length, does not contain any special characters or numbers and is present in the words_alpha.txt file
      output: if string matches requirements true, else false
     */
    public boolean validWord(String word){
        return(word.length()==wordLength && word.chars().allMatch(Character::isLetter) && fileContainsWord(word));
    }

    /*
      input: a string
      output: true if words_alpha.txt contains the string, else false
     */
    public boolean fileContainsWord(String word){
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(AppWordle.class.getResourceAsStream("/words_alpha.txt")))){
            for (int i = 0; i < lineCount; i++) {
                if(bufferedReader.readLine().equals(word)){
                    return true;
                }
            }
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    //checks if all attempts been used and returns true
    public boolean lost(){
        return counter == tries;
    }
}
