package org.amazigh.foundry.scripts.ai;

// A simple custom missile AI (homes in on targets last position if it dies), done as an everyframe to force a specific target (and also make it immune to flare distraction as a side effect?)

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import java.util.List;

public class ASF_vandakHomingScript extends BaseEveryFrameCombatPlugin {
	
	private MissileAPI projectile;
	private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private Vector2f lockPoint = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, ECCM=3;
    private final float MAX_SPEED;
    private final float OFFSET, OFFSET_2;
    private float PRECISION_RANGE=1000000; // 1000^2
    
    private float WAVE_TIME = 1.3f;
    private final float WAVE_AMPLITUDE = 5;
    
    private float WAVE_TIME_2 = 2.1f;
    private final float WAVE_AMPLITUDE_2 = 15;
    		
	public ASF_vandakHomingScript(@NotNull MissileAPI projectile, ShipAPI target) {
		this.projectile = projectile;
        this.target = target;
        
        lockPoint = target.getLocation();
        
		MAX_SPEED = projectile.getMaxSpeed();
		
		OFFSET=(float)(Math.random()*MathUtils.FPI*2);
		WAVE_TIME = MathUtils.getRandomNumberInRange(1.1f, 1.4f);
		
		OFFSET_2=(float)(Math.random()*MathUtils.FPI*2);
		WAVE_TIME_2 = MathUtils.getRandomNumberInRange(2.0f, 2.3f);
        // slight randomness to wave times, because
		
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
		
		
		timer+=amount;
        //finding lead point to aim to
        if(launch || timer>=check){
        	
        	if (target == null
    				|| ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                    		|| !engine.isEntityInPlay(target) )
    				){
    			

        		// NO TARGET, "dumbly" home in on targets last position
        		lead = lockPoint;
            } else {
            	// set a "backup" position of the target in case it dies
            	lockPoint = target.getLocation();
            	
            	launch=false;
                timer -=check;
                //set the next check time
                check = Math.min(
                        0.25f,
                        Math.max(
                                0.05f,
                                MathUtils.getDistanceSquared(projectile.getLocation(), target.getLocation())/PRECISION_RANGE)
                );
                
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
                
            }
        	
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
        correctAngle += multiplier*WAVE_AMPLITUDE_2*check*Math.cos(OFFSET_2+projectile.getElapsed()*(2*MathUtils.FPI/WAVE_TIME_2));
        
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( projectile.getFacing(), correctAngle);
        
        // accel if within 35 degrees of aimed at target, decel if over 67 degrees
        if (Math.abs(aimAngle) < 35){
    		projectile.giveCommand(ShipCommand.ACCELERATE);
    	} else if (Math.abs(aimAngle) > 67) {
    		projectile.giveCommand(ShipCommand.DECELERATE);
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
	
	
}