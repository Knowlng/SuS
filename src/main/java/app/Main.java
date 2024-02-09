package app;

import app.components.ui.CommandUI;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    CommandUI ui = new CommandUI(scanner);
    try {
      ui.manageUserInput(args);
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      return;
    }
  }
}
