package tracks.multiPlayer.myController.heuristicSearch;

import core.game.Observation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import ontology.effects.unary.TurnAround;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractMultiPlayer {
    int oppID; //player ID of the opponent
    int id; //ID of this player
    int no_players; //number of players in the game
//    public static double epsilon = 1e-6;
//    public static Random m_rnd;

    /**
     * initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @param playerID ID if this agent
     */
    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID){
        id = playerID;
        no_players = stateObs.getNoPlayers();
        oppID = (id + 1) % stateObs.getNoPlayers();
        //测试部分
        //Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
        //ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition); //为空
        //ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
        //System.out.println(avatarPosition);
        //System.out.println(npcPositions);
        //System.out.println(portalPositions.toString());
    }

    /**
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        //可以返回一个动作, 或者在本方法内执行多步动作, 最后返回一个null动作
        Types.ACTIONS bestAction = null;
        MyHeurtistic heurtistic = new MyHeurtistic(stateObs);
        Types.ACTIONS oppAction = getOppNotLosingAction(stateObs, id, oppID);
        double maxGain = Double.NEGATIVE_INFINITY;

        Vector2d avatarPosition = stateObs.getAvatarPosition(id);
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
        System.out.println(npcPositions);

        for (Types.ACTIONS action: stateObs.getAvailableActions(id)) {
            StateObservationMulti stCopy = stateObs.copy();

            //需要提供两个players的动作来改变总状态
            Types.ACTIONS[] actions = new Types.ACTIONS[no_players];
            actions[id] = action;
            actions[oppID] = oppAction;

            stCopy.advance(actions);
            double curGain = heurtistic.evaluateState(stCopy, id);
            if (heurtistic.evaluateState(stCopy, id) > maxGain) {
                bestAction = action;
                maxGain = curGain;
            }
        }

        //return bestAction;
        return null;
    }

    private Types.ACTIONS getOppNotLosingAction (StateObservationMulti stateObs, int id, int oppID) {
        int no_players = stateObs.getNoPlayers();
        ArrayList<Types.ACTIONS> oppActions = stateObs.getAvailableActions(oppID);
        // Record actions which won't kill the opp
        ArrayList<Types.ACTIONS> nonDeathActions = new ArrayList<>();

        //Look for actions which won't kill the opp
        for (Types.ACTIONS action: oppActions) {
            Types.ACTIONS[] actEach = new Types.ACTIONS[no_players];
            actEach[id] = Types.ACTIONS.ACTION_NIL;
            actEach[oppID] = action;

            StateObservationMulti stCopy = stateObs.copy();
            stCopy.advance(actEach);

            if (stCopy.getMultiGameWinner()[oppID] != Types.WINNER.PLAYER_LOSES) {
                nonDeathActions.add(action);
            }
        }

        if (nonDeathActions.size() == 0) {
            //Every action kills the opp, just simply random
            return oppActions.get(new Random().nextInt(oppActions.size()));
        } else {
            //Randomly choose one non-death action
            return nonDeathActions.get(new Random().nextInt(nonDeathActions.size()));
        }
    }
}
