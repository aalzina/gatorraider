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
        int safeDistance = 18 / game.getLivesRemaining();
        Defender closestDefender = game.getDefender(0);
        Defender secondClosestDefender = game.getDefender(1);
        int nodesToNextDefender = 999;
        int nodesToDefender = 999;
        List<Defender> defenders = game.getDefenders();
        Attacker me = game.getAttacker();
        Node nextNode = me.getLocation().getNeighbor(me.getDirection());
        for (Defender d : defenders) {
            int tempNodeCount = me.getLocation().getPathDistance(d.getLocation());

            if (tempNodeCount < nodesToDefender && tempNodeCount >= 0) {
                nodesToNextDefender = Integer.valueOf(nodesToDefender);
                nodesToDefender = tempNodeCount;
                secondClosestDefender = closestDefender;
                closestDefender = d;
            }
        }
        //if you run into a wall or reach a junction

        //if an enemy is too close for comfort
        if (closestDefender != null) {

            if (!closestDefender.isVulnerable() && nodesToDefender <= safeDistance ) {
                // System.out.println("Defender " + closestDefender.toString() + " is " + me.getLocation().getPathDistance(closestDefender.getLocation()) + " moves away!");
                int moveToPill = 0;
                if (game.getPowerPillList().size() > 0)
                    moveToPill = me.getNextDir(me.getTargetNode(game.getPowerPillList(), true), true);
                else
                    moveToPill = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);

                int moveToDefender = me.getNextDir(closestDefender.getLocation(), true);
                if (moveToPill != moveToDefender) {
                    action = moveToPill;
                } else {
                    action = me.getNextDir(closestDefender.getLocation(), false);
                }
                //need to make sure that the direction I'm going doesn't have anybody to stop me if I'm at a junction
                //this way I can make a better choice
                if (me.getLocation().isJunction() && nodesToNextDefender < 50 && nodesToNextDefender >= 0) {
                    for (Integer i : me.getPossibleDirs(true)) {
                        if (i == secondClosestDefender.getDirection() && i != me.getDirection() && i != me.getNextDir(closestDefender.getLocation(), false))
                            action = i;
                        else if (i == closestDefender.getDirection() && i != me.getDirection() && i != me.getNextDir(secondClosestDefender.getLocation(), false))
                            action = i;
                        else if (i != me.getNextDir(closestDefender.getLocation(), true) && i != me.getNextDir(secondClosestDefender.getLocation(), true))
                            action = i;
                    }
                }
                return action;
            } else {
                if (closestDefender.isVulnerable()) {
                    int chaseDistance = (int) Math.ceil(75 * ((double) closestDefender.getVulnerableTime() / Game.VULNERABLE_TIME));
                    if (nodesToDefender < chaseDistance) {
                        // System.out.println("Eat defender " + closestDefender.toString() + ". He's only " + nodesToDefender + " moves away!");
                        return me.getNextDir(closestDefender.getLocation(), true);
                    }
                } else if (secondClosestDefender.isVulnerable()) {
                    int chaseDistance = (int) Math.ceil(75 * ((double) secondClosestDefender.getVulnerableTime() / Game.VULNERABLE_TIME));
                    if (nodesToNextDefender < chaseDistance) {
                        // System.out.println("Eat defender " + closestDefender.toString() + ". He's only " + nodesToDefender + " moves away!");
                        return me.getNextDir(secondClosestDefender.getLocation(), true);
                    }
                }
            }
        }
        if (nextNode == null || me.getLocation().isJunction()) {
            return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
        }

        return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
    }
}