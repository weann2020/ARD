package com.mwl.util;

import com.mwl.characters.Player;
import com.mwl.characters.PlayerFactory;
import com.mwl.environment.RoomMap;
import com.mwl.util.commands.Commands;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mwl.util.ExitGame.exit;

public class ConsoleManager {
     private static final Scanner scanner = new Scanner(System.in);
     private static MenuTrieNode menu = read_xml();

     public ConsoleManager(){}

     private static String gameTitle() {
         String result;
         try {
             result = Files.lines(Path.of("resources/title_art/title_slanted.txt")).collect(Collectors.joining("\n"));
         } catch (IOException e) {
             result = "A. R. D.\n\n";
         }

         return result;
     }

     public static void gameIntro() {
         System.out.println(gameTitle());
         System.out.println("Welcome to ARD, the game where you get Another Random Destiny every time you play!");
         System.out.println("To learn about the game, type \"help me\".");
         System.out.println();
     }

     private static int getInput(List<List<String>> options) {
          options.add(List.of("b", "back"));
          options.add(List.of("q", "quit"));
          boolean doLoop = true;
          int choice = -1;
          while (doLoop){
               options.forEach(System.out::println); // print out the options
               String input = scanner.nextLine().strip(); // get the choice from console

               // map each item in options to be boolean, true if input is inside sublist
               choice = options.stream().map(e -> e.contains(input)).collect(Collectors.toList()).indexOf(true);

               if (choice != -1) {
                    doLoop = false;
               } else {
                    System.out.println("I didn't understand that option, please try again.");
               }
          }

          return (choice == options.size() - 1 ? -1 : choice == options.size() - 2 ? -2 : choice); // -> -1 or -2 or some number in list
     }

     public static void gameExplanation() {

          boolean navigateMenu = true;
          MenuTrieNode curr = menu;
          while (navigateMenu) {
               System.out.println("<" + curr.getTitle() + ">");
               System.out.println(curr.getDescription());
               MenuTrieNode finalCurr = curr;
               int choice = getInput(IntStream.range(0, curr.getChildren().size())
                       .mapToObj(e -> List.of("" + e, finalCurr.getChild(e).getTitle()))
                       .collect(Collectors.toList())); // -> {{"0", "Story details"}, {"1", "Game controls"}} etc.

               if (choice == -1) {
                    navigateMenu = false;
               } else if (choice == -2) {
                    curr = curr.getParent();
               } else {
                    curr = curr.getChild(choice);
               }
          }
     }

     public static Player choosePlayer(RoomMap map) {
         String[] instructions = {
                 "Please just type in the letter 'A' or 'B' to choose the type of player you want to play with.",
                 "A: [Wolverine] has special ability of health boost;\n" +
                         "B: [Iron Man] has special ability to randomly generate one item that's already in inventory.",
                 "Wrong input!\n"+"Enter A or B: ",
         };

         System.out.println(instructions[0]);
         System.out.println(instructions[1]);
         String playerChoice = scanner.nextLine();
         exit(playerChoice);
         while (!playerChoice.toUpperCase().strip().equals(Character.toString('A')) &&
                 !playerChoice.toUpperCase().strip().equals(Character.toString('B'))) {
             System.out.println(instructions[2]);
             System.out.println(instructions[0]);
             System.out.println(instructions[1]);
             playerChoice = scanner.nextLine();
             exit(playerChoice);
         }
         String playName = (playerChoice.toUpperCase().strip().equals(Character.toString('A')))? "Wolverine":"Iron Man";
             System.out.println("Player type: ["+ playName + "] has been chosen.");

         return  PlayerFactory.createPlayer(map.getStart(), new ArrayList<>(), playerChoice);
     }

      static MenuTrieNode recursiveHelper(Node current) {
          List<MenuTrieNode> result = new ArrayList<>();
          NodeList children = current.getChildNodes();

          for (int i = 0; i < children.getLength(); i++) {
               if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element temp = (Element) children.item(i);
                    if (temp.getParentNode().equals(current)) {
                         result.add(recursiveHelper(children.item(i)));
                    }
               }
          }

          Element temp = (Element) current;
          MenuTrieNode returning = new MenuTrieNode(temp.getAttribute("title"), temp.getAttribute("description"));
          result.forEach(menuTrieNode -> {
               returning.addChild(menuTrieNode);
               menuTrieNode.setParent(returning);
          });

          return returning;
     }

      static MenuTrieNode read_xml() {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          NodeList menuNodeList = null;

          try {
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document doc = builder.parse("resources/menu/help_menu.xml");
               menuNodeList = doc.getElementsByTagName("menu");

               MenuTrieNode menu = recursiveHelper(menuNodeList.item(0));
               menu.setParent(menu);
               return menu;
          } catch (ParserConfigurationException | SAXException | IOException  e) {
               e.printStackTrace();
          }

          MenuTrieNode failure = new MenuTrieNode("Help Menu", "There was an error reading the help menu," +
                  " please restart the game to try again.");
          failure.setParent(failure);

          return failure;
     }


    /**
     * scanInput gets user input and validates input
     * @param commands
     * @param str
     * @return Array of valid words
     */
    public static String[] scanInput(Map<String, Commands> commands, String str) {
        String playerInput = null;
        boolean valid = false;
        String[] words;
        int counter = 0;
        do {
            if (counter == 0) {
                playerInput = str.strip().toLowerCase();
                counter++;
            } else {
                playerInput = scanner().nextLine().strip().toLowerCase();
            }
            // check if input text is "exit." We need to do this on every input scanner.
            exit(playerInput);
            words = playerInput.split("\\W+");
            if (words.length != 2) {
                System.out.println("Not a valid action. Try again!");

            } else {
                if (commands.containsKey(words[0])) {
                    try {
                        words[1] = words[1].substring(0, 1).toUpperCase() + words[1].substring(1);
                        commands.get(words[0]).do_command(words[1]);
                        valid = true;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Not a valid action. Try again!");
                    }
                } else {
                    System.out.println("Invalid command. Try again!");
                }
            }
        } while (!valid);
        return words;
    }

    /**
     * Scanner class enable used of the same scanner outside class.
     * @return scanner
     */
    public static Scanner scanner() {
        return scanner;
    }

}
