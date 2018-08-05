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
        Defender closestDefender = game.getDefender(0);
        int nodesToDefender = 999;
        List<Defender> defenders = game.getDefenders();
        Attacker me = game.getAttacker();
        Node nextNode = me.getLocation().getNeighbor(me.getDirection());
        for (Defender d : defenders) {
            int tempNodeCount = me.getLocation().getPathDistance(d.getLocation());

            if (tempNodeCount < nodesToDefender && tempNodeCount >= 0) {
                nodesToDefender = tempNodeCount;
                closestDefender = d;

            }
        }
        //if you run into a wall or reach a junction

        //if an enemy is too close for comfort
        if (closestDefender != null) {
            if (!closestDefender.isVulnerable() && nodesToDefender < 6 && nodesToDefender >= 0) {
                // System.out.println("Defender " + closestDefender.toString() + " is " + me.getLocation().getPathDistance(closestDefender.getLocation()) + " moves away!");
                int moveToPill = 0;
                if (game.getPowerPillList().size() > 0)
                    moveToPill = me.getNextDir(me.getTargetNode(game.getPowerPillList(), true), true);
                else
                    moveToPill = me.getNextDir(me.getTargetNode(game.getPillList(), true), true);

                int moveToDefender = me.getNextDir(closestDefender.getLocation(), true);
                if (moveToPill != moveToDefender) {
                    boolean canMoveToPill = false;
                    for (Integer i : me.getPossibleDirs(false)) {
                        if (i == moveToPill) {
                            canMoveToPill = true;
                        }
                    }
                    if (canMoveToPill)
                        return moveToPill;
                    else {
                        for (Integer i : me.getPossibleDirs(false)) {
                            if (i != moveToDefender) {
                                return i;
                            }
                        }
                    }
                } else
                    return me.getNextDir(closestDefender.getLocation(), false);
            }
            if (closestDefender.isVulnerable() && nodesToDefender < 25 && nodesToDefender >= 0) {
                // System.out.println("Eat defender " + closestDefender.toString() + ". He's only " + nodesToDefender + " moves away!");
                return me.getNextDir(closestDefender.getLocation(), true);
            }
        }
        if (nextNode == null || me.getLocation().isJunction()) {
            return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
        }

        return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
    }
}