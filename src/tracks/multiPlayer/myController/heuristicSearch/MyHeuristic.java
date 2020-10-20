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
    private int blockSize, width, height, maxDist;

    public static final int lookahead = 3;
    private static final boolean isDebug = false;
    private static final int alertGhostDist = 5, dangerGhostDist = 3, deadGhostDist = 1;

    private enum ExpectScore {
        PELLET(1), FRUIT(1.25), POWER(2.5), DBLOCK(-200),
        GETSCORE(10), GETPOWER(200),BACK(-30), NEARGHOST(-35),
        CLOSEGHOST(-50), INGHOST (-300), DPOWER(-50);

        private double val;

        ExpectScore(double value) {
            this.val = value;
        }
        public double getValue() {
            return val;
        }
    }

    public MyHeuristic(StateObservationMulti stateObs) {
        originStateObs = stateObs; //执行动作前的stateObs
        blockSize = stateObs.getBlockSize();
        width = stateObs.getWorldDimension().width;
        height = stateObs.getWorldDimension().height;
        maxDist = 2 * blockSize * blockSize;
    }

    /**
     * @param stateObs 执行动作后 (两个player都执行) 的stateObs
     * @param playerID 当前的player的ID
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
     * getAvatarType(): 得到吃豆人的状态
     *                  普通状态时为28, 31
     *                  无敌状态时为29, 32
     */
    public double evaluateState(StateObservationMulti stateObs, int playerID) {
        double value = 0.0; //当前状态的总估值

        Vector2d avatarPos = stateObs.getAvatarPosition(playerID);
        //如果遇到两层及以上的墙块的时候, 吃豆人的位置不会发生改变, 直接排除掉该动作
        if (avatarPos.equals(originStateObs.getAvatarPosition(playerID))) {
            value += ExpectScore.DBLOCK.getValue();
            return value;
        }

        //各个部分的估值
        double curActGain = 0.0, backPosGain = 0.0, ghostAlertGain = 0.0,
                ghostCloseGain = 0.0, pelletAroundGain = 0.0, pelletAheadGain = 0.0,
                fruitAroundGain = 0.0, fruitAheadGain = 0.0, powerAroundGain = 0.0,
                powerAheadGain = 0.0;

        double curActionValue = stateObs.getGameScore(playerID) - originStateObs.getGameScore(playerID); //获得当前动作取得的分数, 吃到豆子/宝石/水果
        if (curActionValue == 1.0 || curActionValue == 5.0) {
            //如果当前步能吃到豆子或者水果, 那么估值上加上GETSCORE
            curActGain += ExpectScore.GETSCORE.getValue();
        } else if (curActionValue == 10.0) {
            //如果能吃到宝石, 那么这步就是最优先的选择
            value += ExpectScore.GETPOWER.getValue();
            return value;
        }

        Vector2d lastOrientation = stateObs.getAvatarOrientation(playerID);
        if (isReverseAction(lastOrientation, originStateObs.getAvatarOrientation(playerID))) {
            //如果回到originStateObs之前的位置, 则减少估值
            backPosGain += ExpectScore.BACK.getValue();
        }
if (isDebug) {
//    System.out.printf("\tPlayerID: %d, Orientation: ", playerID);
//    System.out.print(stateObs.getAvatarOrientation(playerID));
//    System.out.print(" lastOrientation of originStateObs: ");
//    System.out.println(originStateObs.getAvatarOrientation(playerID));
}
        //与ghost的距离
        ArrayList<Observation>[] ghostObsListArr = stateObs.getNPCPositions(avatarPos); //取得幽灵的位置信息
        for (ArrayList<Observation> ghostObsList: ghostObsListArr) {
            if (ghostObsList.size() != 0) { //运行到一定时间getNPCPosition()会返回一些空数组列表
                double ghostDist = getManhattanDistance(avatarPos, ghostObsList.get(0).position);
                if (ghostDist <= deadGhostDist) {
                    value += ExpectScore.INGHOST.getValue();
                    return value;
                } else if (ghostDist <= dangerGhostDist) {
                    ghostCloseGain += ExpectScore.CLOSEGHOST.getValue();
                } else if (ghostDist <= alertGhostDist) {
                    ghostAlertGain += ExpectScore.NEARGHOST.getValue();
                }
            }
        }

        //搜索当前方向的lookahead个格子以及包围当前位置一周上的豆子, 水果, 宝石, 墙块数量
        ArrayList<Observation>[] immovableObsListArr = stateObs.getImmovablePositions(avatarPos);
        ArrayList<Observation> pelletObsList, fruitObsList, powerObsList, blockObsList;
        pelletObsList = fruitObsList = powerObsList = blockObsList =null;
        for (ArrayList<Observation> obsList: immovableObsListArr) {
            if (obsList.size() > 0) {
                int tp = obsList.get(0).itype;
                if (tp == Itype.PELLET.getValue()) {
                    pelletObsList = obsList;
                } else if (tp == Itype.FRUIT.getValue()) {
                    fruitObsList = obsList;
                } else if (tp == Itype.POWER.getValue()) {
                    powerObsList = obsList;
                } else if (tp == Itype.BLOCK.getValue()) {
                    blockObsList = obsList;
                }
            }
        }

        if (pelletObsList != null) {
            if (pelletObsList.get(0).sqDist <= maxDist || isOnOrientation(avatarPos, lastOrientation, pelletObsList.get(0).position)) {
                int orientationCnt = 0;
                for (Observation pelletObs: pelletObsList) {
                    if (pelletObs.sqDist <= maxDist) {
                        pelletAroundGain += ExpectScore.PELLET.getValue();
                    } else if (isOnOrientation(avatarPos, lastOrientation, pelletObs.position)) {
                        pelletAheadGain += ExpectScore.PELLET.getValue();
                        orientationCnt += 1;
                    }

                    if (orientationCnt == lookahead)  break;
                }
            }

        }

        if (fruitObsList != null) {
            if (fruitObsList.get(0).sqDist <= maxDist || isOnOrientation(avatarPos, lastOrientation, fruitObsList.get(0).position)) {
                int orientationCnt = 0;
                for (Observation fruitObs: fruitObsList) {
                    if (fruitObs.sqDist <= maxDist) {
                        fruitAroundGain += ExpectScore.FRUIT.getValue();
                    } else if (isOnOrientation(avatarPos, lastOrientation, fruitObs.position)) {
                        fruitAheadGain += ExpectScore.FRUIT.getValue();
                        orientationCnt += 1;
                    }

                    if (orientationCnt == lookahead) break;
                }
            }

        }

        if (powerObsList != null) {
            if (powerObsList.get(0).sqDist <= maxDist || isOnOrientation(avatarPos, lastOrientation, powerObsList.get(0).position)) {
                int orientationCnt = 0;
                for (Observation powerObs: powerObsList) {
                    if (powerObs.sqDist <= maxDist) {
                        powerAroundGain += ExpectScore.POWER.getValue();
                    } else if (isOnOrientation(avatarPos, lastOrientation, powerObs.position)) {
                        fruitAheadGain += ExpectScore.POWER.getValue();
                        orientationCnt += 1;
                    }

                    if (orientationCnt == lookahead) break;
                }
            }
        }

        /*if (blockObsList != null) {
            int roundCnt = 0;
            for (int i = 0; i < 4; i++) {
                if (blockObsList.get(i).sqDist == 400.0) roundCnt += 1;
            }

            if (roundCnt == 2) {
                value -= 5;
            } else  if (roundCnt == 3) {
                value -= 12;
            }
        }*/
if (isDebug) {
//    System.out.printf("\tcurActGain: %f, backPosGain: %f, ghostAlertGain: %f\n" +
//            "\tghostCloseGain: %f, pelletAroundGain: %f, pelletAheadGain: %f\n" +
//            "\tfruitAround: %f, fruitAheadGain: %f, powerAroundGain: %f\n" +
//            "\tpowerAheadGain: %f\n", curActGain, backPosGain, ghostAlertGain, ghostCloseGain,
//            pelletAroundGain, pelletAheadGain, fruitAroundGain, fruitAheadGain, powerAroundGain,
//            powerAheadGain);
}
        value = curActGain + backPosGain + ghostAlertGain + ghostCloseGain
                + pelletAroundGain + pelletAheadGain + fruitAroundGain + fruitAheadGain
                + powerAroundGain + powerAheadGain;
        return value;
    }

    public double evaluateStateAdvanced (Vector2d nearestGhost, StateObservationMulti stateObs, int playerID) {
        Vector2d avatarPos = stateObs.getAvatarPosition(playerID);
        double value = 0.0;
        double manDist = getManhattanDistance(avatarPos, nearestGhost);
        value -= manDist;

        StateObservationMulti stCopy = stateObs.copy();
        Types.ACTIONS[] actions = new Types.ACTIONS[]{Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL};
//        stCopy.advance(actions); //提前两个timestemp离开幽灵
//        stCopy.advance(actions); //提前三个timestemp离开幽灵

if (isDebug) {
    System.out.printf("\tManhattan dist: %f\n", manDist);
}
        if (stCopy.getAvatarType(playerID) == Itype.HUNGARY0.getValue()
            || stCopy.getAvatarType(playerID) == Itype.HUNGARY1.getValue()) {
if (isDebug) {
    System.out.println("\tGoing TO DIEEEEEEEEEEEEEEEE!");
}
            ArrayList<Observation>[] ghostObsListArr = stateObs.getNPCPositions(avatarPos); //取得幽灵的位置信息
            for (ArrayList<Observation> ghostObsList: ghostObsListArr) {
                if (ghostObsList.size() != 0) { //运行到一定时间getNPCPosition()会返回一些空数组列表
                    double ghostAvatarDist = getManhattanDistance(avatarPos, ghostObsList.get(0).position);
                    if (ghostAvatarDist <= deadGhostDist) {
                        value += ExpectScore.INGHOST.getValue();
                    } else if (ghostAvatarDist <= dangerGhostDist) {
                        value += ExpectScore.CLOSEGHOST.getValue();
                    } else if (ghostAvatarDist <= alertGhostDist) {
                        value += ExpectScore.NEARGHOST.getValue();
                    }
                }
            }
            return value;
        }

        //如果遇到两层及以上的墙块的时候, 吃豆人的位置不会发生改变, 直接排除掉该动作
        if (avatarPos.equals(originStateObs.getAvatarPosition(playerID))) {
            value += ExpectScore.DBLOCK.getValue();
            return value;
        }

        double curActionValue = stateObs.getGameScore(playerID) - originStateObs.getGameScore(playerID); //获得当前动作取得的分数, 吃到豆子/宝石/水果
        if (curActionValue == 10.0) {
            //如果能吃到宝石, 那么尽量不走这步, 使得能有下一次攻击幽灵
            value += ExpectScore.DPOWER.getValue();
        }

        ArrayList<Observation>[] immovableObsListArr = stateObs.getImmovablePositions(avatarPos);
        ArrayList<Observation> fruitObsList, powerObsList, blockObsList;
        fruitObsList = powerObsList = blockObsList =null;
        for (ArrayList<Observation> obsList: immovableObsListArr) {
            if (obsList.size() > 0) {
                int tp = obsList.get(0).itype;
                if (tp == Itype.FRUIT.getValue()) {
                    fruitObsList = obsList;
                } else if (tp == Itype.POWER.getValue()) {
                    powerObsList = obsList;
                } else if (tp == Itype.BLOCK.getValue()) {
                    blockObsList = obsList;
                }
            }
        }

        if (powerObsList != null) {
            if (powerObsList.get(0).sqDist <= maxDist) {
                value += ExpectScore.DPOWER.getValue();
            }
        }

        return value;
    }


    public double getManhattanDistance(Vector2d x1, Vector2d x2) {
        return (Math.abs(x1.x - x2.x) + Math.abs(x1.y - x2.y)) / blockSize;
    }

    private boolean isOnOrientation(Vector2d x0, Vector2d orientation, Vector2d x1) {
        Vector2d left = new Vector2d(-1.0, 0.0);
        Vector2d right = new Vector2d(1.0, 0.0);
        Vector2d down = new Vector2d(0.0, 1.0);
        Vector2d up = new Vector2d(0.0, -1.0);
        //由Agent确定了不会有ACTION_NIL
        if (orientation.equals(left) && x1.y == x0.y) {
            for (int i = 1; i <= lookahead; i++) {
                if (x1.x == (x0.x - (i + 1) * blockSize + width) % width)
                    return true;
            }
        } else if (orientation.equals(right) && x1.y == x0.y) {
            for (int i = 1; i <= lookahead; i++) {
                if (x1.x == (x0.x + (i + 1) * blockSize) % width)
                    return true;
            }
        } else if (orientation.equals(down) && x1.x == x0.x) {
            for (int i = 1; i <= lookahead; i++) {
                if (x1.y == (x0.y + (i + 1) * blockSize) % height)
                    return true;
            }
        } else if (orientation.equals(up) && x1.x == x0.x) {
            for (int i = 1; i <= lookahead; i++) {
                if (x1.y == (x0.y - (i + 1) * blockSize + height) % height)
                    return  true;
            }
        }
        return false;
    }
    /**
     * @param dir0 第一个方向
     * @param dir1 第二个方向
     * 经过测试发现, 通过getLastAction()得到stateObs的lastAction是对手的, 所以改用orientation比较
     * */
    private boolean isReverseAction (Vector2d dir0, Vector2d dir1) {
        Vector2d zero = new Vector2d(0.0, 0.0);
        if (dir0.add(dir1).equals(zero)) return true;
        return false;
    }




}
