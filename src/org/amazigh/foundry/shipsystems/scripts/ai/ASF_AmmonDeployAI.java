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

public class ASF_AmmonDeployAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check only one-two times a second, no need to be all that responsive.
    private IntervalUtil timer = new IntervalUtil(0.6f, 1f);
    
    private static final float RANGE = 1800f; // system range
    
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
        
        // if the target is in range, use system, a *really* simple system AI.
        if (targetLocation == null) {
        	return;
        } else if (MathUtils.isWithinRange(ship, target, RANGE)) {
            ship.useSystem();
        }
        
    }
}
