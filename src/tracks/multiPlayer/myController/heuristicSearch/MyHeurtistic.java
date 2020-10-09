package tracks.multiPlayer.myController.heuristicSearch;

import core.game.Observation;
import core.game.StateObservationMulti;
import tools.Vector2d;
import tracks.multiPlayer.tools.heuristics.StateHeuristicMulti;

import java.util.ArrayList;
import java.util.HashMap;

public class MyHeurtistic extends StateHeuristicMulti {
    public MyHeurtistic(StateObservationMulti stateObs) {

    }

    public double evaluateState(StateObservationMulti stateObs, int playerID) {
        //Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
        //ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
        //ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
        //HashMap<Integer, Integer> resources = stateObs.getAvatarResources(playerID);
        return 0;
    }

}
