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
import java.util.Collections;
import java.util.Random;

public class Agent extends AbstractMultiPlayer {
    private int oppID; //Player ID of the opponent
    private int id; //ID of this player
    private int no_players; //Number of players in the game
    private static final boolean isDebug = false; //Debug switch

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
if (isDebug) {
//    Vector2d avatarPosition = stateObs.getAvatarPosition(this.id);
//    printGameInfo(stateObs.getImmovablePositions(avatarPosition));
//    System.out.println(stateObs.getBlockSize());
}
    }

    /**
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     * getAvailableActions()返回数组, 其中最后一个动作是ACTION_NIL
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
        //可以返回一个动作, 或者在本方法内执行多步动作, 最后返回一个null动作
        Types.ACTIONS bestAction = Types.ACTIONS.ACTION_NIL;
        MyHeuristic heuristic = new MyHeuristic(stateObs);
        Types.ACTIONS oppAction = getOppNotLosingAction(stateObs, this.id, this.oppID);
        double maxGain = Double.NEGATIVE_INFINITY;
        double sameGain = Double.NEGATIVE_INFINITY;
if (isDebug) {
//    Vector2d avatarPos = stateObs.getAvatarPosition(this.id);
//    printGameInfo(stateObs.getImmovablePositions()[3]);
//    System.out.println(stateObs.getImmovablePositions()[3].size());
//    System.out.println(stateObs.getAvailableActions(this.oppID));
//    System.out.println(stateObs.getAvatarHealthPoints(this.oppID));
//    System.out.println(stateObs.getGameScore(this.oppID));
//    ArrayList<Observation>[] ghostObss = stateObs.getNPCPositions(avatarPos);
//    if (ghostObss != null) {
//        System.out.println(ghostObss.length);
//    }
//    printGameInfo(ghostObss);
//    System.out.println(Types.ACTIONS.ACTION_LEFT.getKey());
}
if (isDebug) {
    System.out.println(String.join("", Collections.nCopies(20, "+")));
    System.out.print("Opp action: <");
    System.out.print(oppAction);
    System.out.println(">");
}
        ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions();
        ArrayList<Types.ACTIONS> sameBestActions = new ArrayList<>();
if (isDebug) {
//    for (Types.ACTIONS action: availableActions) {
//        System.out.print(action);
//        System.out.print(" ");
//    }
//    System.out.print("\n");
}
        for (int i = 0; i < availableActions.size(); i++) { //不考虑ACTION_NIL
            StateObservationMulti stCopy = stateObs.copy();
            Types.ACTIONS curAction = availableActions.get(i);

            //需要提供两个players的动作来改变总状态
            Types.ACTIONS[] actions = new Types.ACTIONS[no_players];
            actions[id] = curAction;
            actions[oppID] = oppAction;
            stCopy.advance(actions);
if (isDebug) {
    System.out.println(curAction);
}
            double curGain = heuristic.evaluateState(stCopy, this.id);
            if (curGain == maxGain) {
                if (sameBestActions.size() == 0) {
                    sameBestActions.add(bestAction);
                    sameBestActions.add(curAction);
                } else {
                    sameBestActions.add(curAction);
                }
            }
            if (curGain > maxGain) {
                bestAction = curAction;
                maxGain = curGain;
                if (sameBestActions.size() > 0) {
                    sameBestActions.clear();
                }
            }
if (isDebug) {
    System.out.printf("\tCurTotalGain: [%f]\n", curGain);
}
        }

if (isDebug) {
    System.out.printf("maxGain: %f, bestAction: ", maxGain);
    System.out.println(bestAction);
    System.out.println(String.join("", Collections.nCopies(20, "-")));
}

        if (sameBestActions.size() > 0) {
            bestAction = sameBestActions.get(new Random().nextInt(sameBestActions.size()));
        }
        return bestAction;
        //return null;
    }

    private void printGameInfo (ArrayList<Observation>[] obsListArr) {
        if (obsListArr == null) {
            System.out.println("EMPTY Array!");
        } else {
            System.out.println(String.join("", Collections.nCopies(20, "+")));
            for (ArrayList<Observation> obsList: obsListArr) {
                System.out.println(obsList);
            }
            System.out.println(String.join("", Collections.nCopies(20, "-")));
        }
    }

    private void printGameInfo (ArrayList<Observation>[] obsListArr, int idx) {
        if (obsListArr == null) {
            System.out.println("EMPTY Array!");
            return;
        }

        ArrayList<Observation> obsList = obsListArr[idx];
        if (obsList == null) {
            System.out.println("EMPTY ArrayList!");
        } else {
            System.out.println(String.join("", Collections.nCopies(20, "+")));
            for (Observation obs: obsList) {
                System.out.println(obs);
            }
            System.out.println(String.join("", Collections.nCopies(20, "-")));
        }
    }

    private void printGameInfo (Vector2d position) {
        System.out.println(position);
    }

    private Types.ACTIONS getOppNotLosingAction (StateObservationMulti stateObs, int id, int oppID) {
        int no_players = stateObs.getNoPlayers();
        ArrayList<Types.ACTIONS> oppActions = stateObs.getAvailableActions(oppID);
        oppActions.remove(Types.ACTIONS.ACTION_NIL); //假设另一个玩家始终保持运动

if (isDebug) {
//    System.out.println(oppActions);
}

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
