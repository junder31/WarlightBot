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

import java.util.List;

public interface Bot {

    public Region getStartingRegion(BotState state, Long timeOut);

    public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut);

    public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut);

}
