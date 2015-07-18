/**
 * Warlight AI Game Bot
 * <p>
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class BotParser {
    final Scanner scan;
    final Bot bot;
    private final PrintStream out;
    private final PrintStream err;
    BotState currentState;

    public BotParser(InputStream in, PrintStream out, PrintStream err, Bot bot) {
        this.out = out;
        this.err = err;
        this.scan = new Scanner(in);
        this.bot = bot;
        this.currentState = new BotState();
    }

    public void run() {
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim();
            if (line.length() == 0) {
                continue;
            }
            String[] parts = line.split(" ");
            if (parts[0].equals("pick_starting_region")) //pick which regions you want to start with
            {
                currentState.setPickableStartingRegions(parts);
                Region startingRegion = bot.getStartingRegion(currentState, Long.valueOf(parts[1]));

                System.out.println(startingRegion.getId());
            } else if (parts.length == 3 && parts[0].equals("go")) {
                //we need to do a move
                String output = "";
                if (parts[1].equals("place_armies")) {
                    //place armies
                    List<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
                    for (PlaceArmiesMove move : placeArmiesMoves)
                        output = output.concat(move.toString() + ",");
                } else if (parts[1].equals("attack/transfer")) {
                    //attack/transfer
                    List<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
                    for (AttackTransferMove move : attackTransferMoves)
                        output = output.concat(move.toString() + ",");
                }
                if (output.length() > 0)
                    out.println(output);
                else
                    out.println("No moves");
            } else if (parts[0].equals("settings")) {
                //update settings
                currentState.updateSettings(parts[1], parts);
            } else if (parts[0].equals("setup_map")) {
                //initial full map is given
                currentState.setupMap(parts);
            } else if (parts[0].equals("update_map")) {
                //all visible regions are given
                currentState.updateMap(parts);
            } else if (parts[0].equals("opponent_moves")) {
                //all visible opponent moves are given
                currentState.readOpponentMoves(parts);
            } else {
                err.printf("Unable to parse line \"%s\"\n", line);
            }
        }
    }

}
