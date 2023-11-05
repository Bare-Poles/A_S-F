package org.amazigh.foundry.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_MaliceStormAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check four to five times a second, to make it slightly random, but also RESPONSIVE
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.25f);
    
    private static final float RANGE = 1100f; // system range (slightly lower than the actual system range, so we only catch stuff that's 100% in range, and can't escape instantly
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        
    	// don't check if paused / can't use the system
    	if (engine.isPaused() || !AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }

        // don't check if timer not up
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }

        // setup variables
        Vector2f targetLocation = null;

        // assign our target location to whatever ship we are attacking
        if (target != null && target.getOwner() != ship.getOwner()) {
            targetLocation = target.getLocation();
        }
        
        // if we have a target that is within range, use system.
        if (targetLocation == null) {
        	return;
        } else if (MathUtils.isWithinRange(ship, target, ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE))) {
            ship.useSystem();
        }

    }
}
