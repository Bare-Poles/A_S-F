// Based on the MagicMissileAI script By Tartiflette.
// A wacky "drunk" missile AI, that can also randomly detonate in flight
package org.amazigh.foundry.scripts.ai;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_PinionDrunkRocketAI implements MissileAIPlugin {
	
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
    // Weave timer
    private IntervalUtil weaveInterval = new IntervalUtil(0.03f,0.06f);
    
    // how far off of the launch angle the rocket can get before it transitions into "back and forth" mode
    private float sweepAngle = 19f;
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //Random starting offset for the waving.
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private boolean init = false, turned = false, side = false;
    private float startAngle = 0f;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ASF_PinionDrunkRocketAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        MISSILE.giveCommand(ShipCommand.ACCELERATE);
        //forced acceleration always
        
        // add an initial startup delay!
        
        if (!init) {
        	init = true;
        	startAngle = MISSILE.getWeapon().getCurrAngle();
        }
        
        weaveInterval.advance(amount);
		if (weaveInterval.intervalElapsed()) {
			
			Vector2f loc = (Vector2f) (MISSILE.getLocation());
			
			Vector2f vel = new Vector2f();
			vel.x += MISSILE.getVelocity().x;
			vel.y += MISSILE.getVelocity().y;
			
			Vector2f puffRandomVel1 = MathUtils.getRandomPointOnCircumference((Vector2f) vel.scale(MathUtils.getRandomNumberInRange(0.4f, 0.6f)), MathUtils.getRandomNumberInRange(4f, 18f));
			Vector2f puffRandomVel2 = MathUtils.getRandomPointOnCircumference((Vector2f) vel.scale(MathUtils.getRandomNumberInRange(0.4f, 0.6f)), MathUtils.getRandomNumberInRange(4f, 18f));
			
			engine.addSmokeParticle(loc, puffRandomVel1, MathUtils.getRandomNumberInRange(7f, 13f), 0.7f, 0.3f, new Color(70,85,60,80));
            engine.addNebulaSmokeParticle(loc,
            		puffRandomVel2,
            		MathUtils.getRandomNumberInRange(7f, 13f), //size
            		1.9f, //end mult
            		0.5f, //ramp fraction
            		0.4f, //full bright fraction
            		0.33f, //duration
            		new Color(70,85,60,80));
			
			float facing = MISSILE.getFacing();
			facing += MathUtils.getRandomNumberInRange(-2f, 2f);
			MISSILE.setFacing(facing);
		}
        
		// if the rocket has weaved "really far" to one side, then transition into a "back and forth" sweep, otherwise just do some extra random weaving.
		float aimAngle = MathUtils.getShortestRotation(MISSILE.getFacing(), startAngle);
		if (aimAngle < -sweepAngle) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
            turned = true;
            side = true;
        }
		if (aimAngle > sweepAngle) {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
            turned = true;
            side = false;
        }
		if (!turned) {
	        if (Math.random() < 0.5f) {
	            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
	        } else {
	            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
	        }
		} else {
			if (side) {
	            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
	        } else {
	            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
	        }
		}
		
    }
   
    public void init(CombatEngineAPI engine) {}
}