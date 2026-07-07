package org.amazigh.foundry.scripts.ai;

// A simple (missile spread) custom missile AI, done as an everyframe, idk it was the easiest way i could think of to pass missile target from a mirv.

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;

import java.util.List;

public class ASF_gangstalkerMicroHomingScript extends BaseEveryFrameCombatPlugin {
	
	private MissileAPI projectile;
	private CombatEntityAPI target;
    private Vector2f targetPoint = new Vector2f();
    private Vector2f lead = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, ECCM=3, swarmBase=0f, swarmTrue=0f;
    private final float MAX_SPEED;
    private final float OFFSET;
    private float SWARM_OFFSET = 25f;
    private float PRECISION_RANGE=90000; // 300^2
    
    private float WAVE_TIME=1.7f;
    private final float WAVE_AMPLITUDE=30;
    
    		
	public ASF_gangstalkerMicroHomingScript(@NotNull MissileAPI projectile, CombatEntityAPI target) {
		this.projectile = projectile;
        this.target = target;
        
		MAX_SPEED = projectile.getMaxSpeed();
		
		swarmBase = MathUtils.getRandomNumberInRange(-SWARM_OFFSET, SWARM_OFFSET);
		
		OFFSET=(float)(Math.random()*MathUtils.FPI*2);
		WAVE_TIME = MathUtils.getRandomNumberInRange(1.5f, 1.9f);
        // slight randomness to wave time, because
		
        if (projectile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }
	}
	
	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}
		
		//Checks if our script should be removed from the combat engine
		if (projectile == null || projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
			engine.removePlugin(this);
			return;
		}
				
		if (Global.getCombatEngine().isPaused() || projectile.isFading() || projectile.isFizzling()) {return;}
		
		if (target == null
				|| ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                		|| !engine.isEntityInPlay(target) || (target instanceof ShipAPI && ((ShipAPI) target).isPhased()))
				){
			target = MagicTargeting.pickTarget(
					projectile,
					MagicTargeting.targetSeeking.IGNORE_SOURCE,
					1600,	// MAX_SEARCH_RANGE
					360,
					1,
					5, 
					4,
					3,
					2, 
					false);
			
			applySwarmOffset();
            //forced acceleration if no target
            projectile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
		
		timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(projectile.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            
            swarmTrue = swarmBase *(Math.min(1f, MathUtils.getDistanceSquared(projectile.getLocation(), target.getLocation()) / PRECISION_RANGE));
            	// swarm offset scales down as the missile closes with the target (under 300 SU)
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
            		projectile.getLocation(),
            		MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
            		target.getLocation(),
            		target.getVelocity()
            		);
            //null pointer protection
            if (lead == null) {
            	lead = target.getLocation(); 
            }
            
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(lead, targetPointRotated, lead);
        }
		
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
        		projectile.getLocation(),
        		lead);
        
        //waving
        float multiplier=1;
        if(ECCM<=1){
            multiplier=0.6f;
        }
        correctAngle += multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+projectile.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( projectile.getFacing(), correctAngle) + swarmTrue;
        
        // accel if within 35 degrees of aimed at target
        if (Math.abs(aimAngle) < 35){
    		projectile.giveCommand(ShipCommand.ACCELERATE);
    	}
        
        
        if (aimAngle < 0) {
        	projectile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
        	projectile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(projectile.getAngularVelocity()) * 0.1f) {
        	projectile.setAngularVelocity(aimAngle / 0.1f);
        }		
		   
	}
	
	private void applySwarmOffset() {
 		int i = 10; //We don't want to take too much time, even if we get unlucky: only try 10 times
 		boolean success = false;
 		while (i > 0 && target != null) {
 			i--;

 			//Get a random position and check if its valid
 			Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
 			if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
 				//If the point is valid, convert it to an offset and store it
 				potPoint.x -= target.getLocation().x;
 				potPoint.y -= target.getLocation().y;
 				potPoint = VectorUtils.rotate(potPoint, -target.getFacing());
 				targetPoint = new Vector2f(potPoint);
 				success = true;
 				break;
 			}
 		}

 		//If we didn't find a point in 13 tries, just choose target center
 		if (!success) {
 			targetPoint = new Vector2f(Misc.ZERO);
 		}
 	}
	
}