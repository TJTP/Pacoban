package tracks.multiPlayer.myController.heuristicSearch;

import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Vector2d;
import tracks.multiPlayer.tools.heuristics.StateHeuristicMulti;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class MyHeuristic extends StateHeuristicMulti {
    private StateObservationMulti originStateObs;
    public static final double gridLen = 20.0;
    public enum Score {
        PELLET(200);

        private int val = 0;
        private Score(int value) {
            this.val = value;
        }

        public int getValue() {
            return val;
        }
    }

    public MyHeuristic(StateObservationMulti stateObs) {
        originStateObs = stateObs;
    }

    /**
     * @param stateObs
     * @param playerID
     * @return
     * getImmovablePositions()的第一个数组列表, itype=0, 是各个墙块的坐标
     *                          第二个数组列表, itype=2, 是地面的坐标
     *                          第三个数组列表, itype=4, 是水果的坐标, 吃掉后会减少, 一个水果得五分
     *                          第四个数组列表, itype=5, 是豆子的坐标, 吃掉后会减少, 一个豆子得一分
     *                          第五个数组列表, itype=6, 是宝石的坐标, 吃掉后会减少, 一个宝石得十分
     * getNPCPosition(): 得到幽灵的实时位置
     *                   粉色, itype=21, obsID=738
     *                   蓝色, itype=18, obsID=739
     *                   黄色, itype=24, obsID=740
     *                   红色, itype=15, obsID=741
     */
    public double evaluateState(StateObservationMulti stateObs, int playerID) {
        Vector2d avatarPos = stateObs.getAvatarPosition(playerID);
        ArrayList<Observation>[] ghostObss = stateObs.getNPCPositions(avatarPos);
        ArrayList<Observation>[] immovableObss = stateObs.getImmovablePositions(avatarPos);

        double value = 0.0;
        //与ghost的距离
        if (ghostObss.length == 1) {
            value += getManhattanDistance(avatarPos, ghostObss[0].get(0).position);
        } else if (ghostObss.length >= 2) {
            for (int i = 0; i < 2; i++) { //最近的两个ghost的曼哈顿距离之和
                value += getManhattanDistance(avatarPos, ghostObss[i].get(0).position);
            }
        }

        //豆子的距离
        if (originStateObs.getGameScore(playerID) == stateObs.getGameScore(playerID) - 1) {
            ;
        }

        return value;
    }

    private double getManhattanDistance(Vector2d x1, Vector2d x2) {
        return (Math.abs(x1.x-x2.x) + Math.abs(x1.y-x2.y)) / gridLen;
    }


}
