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

            if (tempNodeCount < nodesToDefender) {
                nodesToDefender = tempNodeCount;
                closestDefender = d;

            }
        }
        //if an enemy is too close for comfort
        if (closestDefender != null && !me.getLocation().isJunction()) {
            if (!closestDefender.isVulnerable() && me.getLocation().getPathDistance(closestDefender.getLocation()) < 9)
                return me.getNextDir(closestDefender.getLocation(), false);
        }
        //if you run into a wall or reach a junction
        if (nextNode == null || me.getLocation().isJunction()) {
            //get to closest pill unless defender is vulnerable
            if (me.getLocation().isJunction()) {
                if (closestDefender != null) {
                    if (closestDefender.isVulnerable() && me.getLocation().getPathDistance(closestDefender.getLocation()) < 20) {
                        return me.getNextDir(closestDefender.getLocation(), true);
                    }
                }
            }
            return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
        }
        return me.getNextDir(me.getTargetNode(game.getPillList(), true), true);
    }
}