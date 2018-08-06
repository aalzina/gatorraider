package edu.ufl.cise.cs1.controllers;

import game.controllers.AttackerController;
import game.models.Attacker;
import game.models.Defender;
import game.models.Game;
import game.models.Node;

import java.util.List;

public final class StudentAttackerController implements AttackerController {
    public void init(Game game) {
    }

    public void shutdown(Game game) {
    }

    public int update(Game game, long timeDue) {
        int action = -1;
        int SAFE_DISTANCE = 6;/// game.getLivesRemaining(); //calculating a variable safe distance from defenders.
        int nodesToNextDefender = 999;
        int nodesToDefender = 999;
        int moveToPill = 0;
        int LOOK_AHEAD_WHEN_RUNNING = 50;
        //50/35 -> 5783.3
        //60/40 -> 5415
        //30/20 5214
        //70/40 5735
        //40/40 5257
        //80/25 5682.4
        //40/20
        //70/70 -> 5490
        int LOOK_AHEAD_WHEN_SAFE = 50;
        int CHASE_THEM_DOWN = 75;
        Defender closestDefender = game.getDefender(0);
        Defender secondClosestDefender = game.getDefender(1);
        List<Defender> defenders = game.getDefenders();
        Attacker me = game.getAttacker();
        Node nextNode = me.getLocation().getNeighbor(me.getDirection());

        //figure out the closest two defenders
        for (Defender d : defenders) {
            int tempNodeCount = me.getLocation().getPathDistance(d.getLocation());
            if (tempNodeCount < nodesToDefender && tempNodeCount >= 0) {
                nodesToNextDefender = Integer.valueOf(nodesToDefender);
                nodesToDefender = tempNodeCount;
                secondClosestDefender = closestDefender;
                closestDefender = d;
            }
        }


        //if an enemy is too close for comfort - primary decision making
        if (closestDefender != null) {
            //can't eat them then run!
            if ((!closestDefender.isVulnerable() && !secondClosestDefender.isVulnerable()) && nodesToDefender <= SAFE_DISTANCE) {
                if (me.getLocation().isJunction()) {
                    if (me.getTargetNode(game.getPowerPillList(), true) != null)
                        moveToPill = me.getNextDir(me.getTargetNode(game.getPowerPillList(), true), true);
                    if (checkPath(LOOK_AHEAD_WHEN_RUNNING, me.getLocation(), moveToPill, defenders)) {
                        action = moveToPill;
                    } else {
                        if (me.getTargetNode(game.getPillList(), true) != null)
                            moveToPill = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
                        if (checkPath(LOOK_AHEAD_WHEN_RUNNING, me.getLocation(), moveToPill, defenders)) {
                            action = moveToPill;
                        } else {
                            for (Integer i : me.getPossibleDirs(true)) {
                                if (checkPath(LOOK_AHEAD_WHEN_RUNNING, me.getLocation(), i, defenders)) {
                                    action = i;
                                }
                            }
                        }
                    }
                } else {
                    if (game.getPowerPillList().size() > 0) //if being chased, run to closest power pill!
                        moveToPill = me.getNextDir(me.getTargetNode(game.getPowerPillList(), true), true);
                    else //if no more power pills, just start trying to finish the level
                        moveToPill = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
                    if (moveToPill != me.getNextDir(closestDefender.getLocation(), true)) {
                        action = moveToPill;
                    } else {
                        action = me.getNextDir(closestDefender.getLocation(), false);
                    }
                }
                //need to make sure that the direction I'm going doesn't have anybody to stop me if I'm at a junction
                //this way I can make a better choice
                return action;
            } else {
                if (closestDefender.isVulnerable()) {
                    int chaseDistance = (int) Math.ceil(CHASE_THEM_DOWN * ((double) closestDefender.getVulnerableTime() / Game.VULNERABLE_TIME));
                    if (nodesToDefender < chaseDistance) {
                        // System.out.println("Eat defender " + closestDefender.toString() + ". He's only " + nodesToDefender + " moves away!");
                        return me.getNextDir(closestDefender.getLocation(), true);
                    }
                } else {
                    int chaseDistance = (int) Math.ceil(CHASE_THEM_DOWN * ((double) secondClosestDefender.getVulnerableTime() / Game.VULNERABLE_TIME));
                    if (nodesToNextDefender < chaseDistance) {
                        // System.out.println("Eat defender " + closestDefender.toString() + ". He's only " + nodesToDefender + " moves away!");
                        return me.getNextDir(secondClosestDefender.getLocation(), true);
                    }
                }
            }
        }

        //if you run into a wall or reach a junction
        if (nextNode == null || me.getLocation().isJunction()) {
            if (me.getLocation().isJunction()) {
                int badPathCounter = 0;
                int backupDirection = 0;
                for (Integer i : me.getPossibleDirs(true)) {
                    if (!checkPath(LOOK_AHEAD_WHEN_SAFE, me.getLocation(), i, defenders)) {
                        badPathCounter++;
                    } else {
                        backupDirection = i;
                    }
                }
                if (badPathCounter >= 2) {
                    if (me.getTargetNode(game.getPowerPillList(), true) != null)
                        moveToPill = me.getNextDir(me.getTargetNode(game.getPowerPillList(), true), true);
                    if (checkPath(LOOK_AHEAD_WHEN_RUNNING, me.getLocation(), moveToPill, defenders)) {
                        action = moveToPill;
                    } else
                        action = backupDirection;
                }
                if (checkPath(LOOK_AHEAD_WHEN_SAFE, me.getLocation(), me.getNextDir(me.getTargetNode(game.getPillList(), true), true), defenders)) {
                    action = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
                } else {
                    for (Integer i : me.getPossibleDirs(true)) {
                        if (checkPath(LOOK_AHEAD_WHEN_SAFE, me.getLocation(), i, defenders)) {
                            action = i;
                        }
                    }
                }
            } else {
                if (checkPath(LOOK_AHEAD_WHEN_SAFE, me.getLocation(), me.getNextDir(me.getTargetNode(game.getPillList(), true), true), defenders))
                    action = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
                else
                    action = me.getNextDir(me.getTargetNode(game.getPillList(), true), false);
            }
            return action;
        }
        //default
        return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
    }

    private boolean checkPath(int moves, Node startingNode, int direction, List<Defender> defenders) {
        while (moves > 0) {
            for (Defender d : defenders) {
                if (startingNode == d.getLocation())
                    return false;
            }
            if (startingNode.getNeighbor(direction) == null) {
                List<Node> nodes = startingNode.getNeighbors();
                for (int i = 0; i < nodes.size(); i++) {
                    if (i != direction && nodes.get(i) != null) {
                        startingNode = nodes.get(i);
                    }
                }
            } else {
                startingNode = startingNode.getNeighbor(direction);
            }
            moves--;
        }
        //if I get here its a safe path
        return true;
    }
}