// Based on the MagicMissileAI script By Tartiflette.
package org.amazigh.foundry.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicTargeting;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_GangstalkerMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
	//////////////////////
	//     SETTINGS     //
	//////////////////////
	
	//Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
	//  Set to a negative value to disable
	private final float OVERSHOT_ANGLE=60;
	
	//Time to complete a wave in seconds.
	private final float WAVE_TIME=2;
	
	//Max angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
	private final float WAVE_AMPLITUDE=10;
	
	//Damping of the turn speed when closing on the desired aim. The smaller the snappier.
	private final float DAMPING=0.1f;
	
	//Target class priorities
	//set to 0 to ignore that class
	private final int fighters=0;
	private final int frigates=1;
	private final int destroyers=2;
	private final int cruisers=3;
	private final int capitals=4;

	//Arc to look for targets into
	//set to 360 or more to ignore
	private final int SEARCH_CONE=360;

	//range in which the missile seek a target in game units.
	private final int MAX_SEARCH_RANGE = 2000;

	//Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
	//   1: perfect leading with and without ECCM
	//   2: half precision without ECCM
	//   3: a third as precise without ECCM. Default
	//   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
	private float ECCM=2;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
	
	
	// how many missiles to fire
	private int AMMO = 30;
	
	// checks for whether the missile is:  engine use allowed / within range / firing / which side to strafe to / in final attack
	private boolean ENGINE = true;
	private boolean RANGE = false;
	private boolean FIRING = false;
	private boolean SIDE = false;
	private boolean FINAL = false;
	
	// square of range to switch zigs to outwards
	private float MIN_RANGE = 1000000; //1000^2
	
	// square of range to switch zigs to be inwards (and initial start range)
	private float IN_RANGE = 1322500; //1150^2
	
	// square of range to target at which to stop shooting and turn engine back on
	private float FAIL_RANGE = 1562500; //1250^2
	
	// how fast for the missile to accelerate when strafing
	private float STRAFE_ACCEL = 75f;
	
	// timers for:  "startup" / payload delay / flameout timer
	private IntervalUtil startInterval = new IntervalUtil(0.3f, 0.3f);
	private IntervalUtil payloadInterval = new IntervalUtil(0.3f, 0.3f);
	private IntervalUtil flameInterval = new IntervalUtil(10f, 10f);
	
	
	
	//////////////////////
	//    VARIABLES     //
	//////////////////////
	
	//max speed of the missile after modifiers.
	private final float MAX_SPEED;
	//Random starting offset for the waving.
	private final float OFFSET;
	private CombatEngineAPI engine;
	private final MissileAPI MISSILE;
	private CombatEntityAPI target;
	private Vector2f lead = new Vector2f();
	private boolean launch=true;
	private float timer=0, check=0f, strafeAngle=75f, shotTimer = 1f, sideTimer = 0.8f;
	
	//////////////////////
	//  DATA COLLECTING //
	//////////////////////
	
    public ASF_GangstalkerMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }
    	
        if (Math.random() > 0.5) {
			SIDE = true; // randomly pick whether we strafe to the left or right.
		} else {
			SIDE = false;
		}
        
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
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
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || ((target instanceof ShipAPI && !((ShipAPI) target).isAlive()) || !engine.isEntityInPlay(target))
                ){
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        MagicTargeting.targetSeeking.NO_RANDOM,
                        MAX_SEARCH_RANGE,
                        SEARCH_CONE,
                        fighters,
                        frigates, 
                        destroyers,
                        cruisers,
                        capitals, 
                        false
                )
            );
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time - fixed at 4x/sec because i say so
            check = 0.25f;
            
            //best intercepting point 
        	float leadVel = MAX_SPEED * ECCM; //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
        	Vector2f tagVel = target.getVelocity();
        	
        	lead = AIUtils.getBestInterceptPoint(
                    MISSILE.getLocation(),
                    leadVel,
                    target.getLocation(),
                    tagVel
            );
            //null pointer protection
            if (lead == null) {
                lead = target.getLocation(); 
            }
            
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        if(WAVE_AMPLITUDE>0){            
            //waving
            float multiplier=1;
            if(ECCM<=1){
                multiplier=0.3f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
        	if (ENGINE) {
        		MISSILE.giveCommand(ShipCommand.ACCELERATE);
        		
        		flameInterval.advance(amount);
            	if (flameInterval.intervalElapsed()) {
            		MISSILE.flameOut(); // manual handling of the flameout timer, as i only want it to tick up when the engine is firing.
            	}
        		
                if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) <= IN_RANGE) {
                	RANGE = true;
                	ENGINE = false;
                }
        	}
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        
        if (RANGE) {
        	
            if (!FIRING) {

                startInterval.advance(amount);
                
                Vector2f dampedVel = MISSILE.getVelocity();
                dampedVel.x = dampedVel.x * 0.9f; // was 0.92, but we lowered init time (0.5 > 0.3) so had to be made stronger to ~compensate
                dampedVel.y = dampedVel.y * 0.9f;
                
                MISSILE.getVelocity().set(dampedVel);
            	
            	if (startInterval.intervalElapsed()) {
            		FIRING = true;
            	}
            }
            
        }
        
        if (FIRING) {

    		if (AMMO <= 0) {
    			FIRING = false;
    			FINAL = true;
    		}

        	if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) > FAIL_RANGE) {
        		ENGINE = true;
    			FIRING = false;
    			RANGE = false;
    			// this is the engine reset if the target gets out of "range"
        	}
        	
        	shotTimer -= amount;
        	
        	if (target != null) {
        		if (target instanceof ShipAPI) {
        			if (((ShipAPI)target).getFluxTracker().isOverloadedOrVenting()) {
        				shotTimer -= (amount * 6.5f);
        				// shorter timer if target is a ship that is overloaded/venting 
        			}
        		}
        	}
        	
        	
        	float strafeDir = MISSILE.getFacing(); // strafe!
        	
        	// picking strafe angle orientation, alternating between slightly forwards/backwards, so it is more interesting
        	if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) >= IN_RANGE) {
            	strafeAngle = 80f;
        	}
        	if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) <= MIN_RANGE) {
        		strafeAngle = 100f;
        	}
        	
        	if (SIDE) {
        		strafeDir += strafeAngle;
        	} else {
        		strafeDir -= strafeAngle;
        	}
        	
        	Vector2f strafeVel = MathUtils.getPointOnCircumference(MISSILE.getVelocity(), STRAFE_ACCEL*amount, strafeDir);
            MISSILE.getVelocity().set(strafeVel); // strafe to the side, because it looks kinda cool, and for (minor) PD avoidance.
            sideTimer -= amount;
            if (sideTimer <= 0) {
            	SIDE = !SIDE;
            	sideTimer = 1.6f;
            }
            
        	if (shotTimer <= 0f) {
        		
        		shotTimer += 1f;
        		AMMO -= 3;
        		
        		Vector2f vel = MISSILE.getVelocity();
            	Vector2f loc = MISSILE.getLocation();
            	
            	for (int i=0; i < 3; i++) {
            		
            		float missileAngle = MISSILE.getFacing() + (-18 + (i * 18));
            		
            		Vector2f muzzleLoc = MathUtils.getPointOnCircumference(loc, 9f, missileAngle);
            		Vector2f missileVel = MathUtils.getRandomPointInCircle(vel, 50f);
            		
                	CombatEntityAPI missile = engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_omer_gang", muzzleLoc, missileAngle + MathUtils.getRandomNumberInRange(-7f, 7f), missileVel);
                	((DamagingProjectileAPI)missile).setFromMissile(true);
    				engine.addPlugin(new ASF_gangstalkerMicroHomingScript((MissileAPI)missile, target));
                	
                	Vector2f smokeVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(5f, 10f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-6f, 6f));
                	engine.addSmokeParticle(loc, smokeVel, MathUtils.getRandomNumberInRange(9f, 17f), 1.0f, 1.0f, new Color(185,175,160,95));
                	
            	}
				Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, loc, vel);
				
        	}
        }
        
        if (FINAL) {
        	payloadInterval.advance(amount);
        	
        	if (payloadInterval.intervalElapsed()) {
        		
    			Vector2f loc = (Vector2f) (MISSILE.getLocation());
    			float missileAngle = MISSILE.getFacing();
    					
        		CombatEntityAPI payload = engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_gangstalker_payload", loc, missileAngle, MISSILE.getVelocity());
            	((DamagingProjectileAPI)payload).setFromMissile(true);
            	engine.addPlugin(new ASF_gangstalkerRocketHomingScript((MissileAPI)payload, target));
            	
    			Vector2f fxVel = MISSILE.getVelocity();
    			fxVel.x = fxVel.x * 0.5f;
    			fxVel.y = fxVel.y * 0.5f;
    	        
    			engine.spawnDebrisSmall(loc, fxVel, 8, missileAngle + 90f, 150f, 18f, 28f, 700f);
    			engine.spawnDebrisSmall(loc, fxVel, 8, missileAngle - 90f, 150f, 18f, 28f, 700f);
    			engine.spawnDebrisMedium(loc, fxVel, 4, missileAngle, 120f, 6f, 25f, 350f);
				engine.spawnDebrisLarge(loc, fxVel, 1, missileAngle, 50f, 5f, 5f, 170f);
				
    			Global.getSoundPlayer().playSound("hurricane_mirv_split", 1.4f, 1.2f, loc, MISSILE.getVelocity());

				// some core smoke
    			engine.addNebulaSmokeParticle(loc,
    					MathUtils.getRandomPointOnCircumference(null, 1f),
                		18f, //size
                		2.5f, //end mult
                		0.5f, //ramp fraction
                		0.75f, //full bright fraction
                		1.0f, //duration
                		new Color(190,185,180,100));
    			for (int i=0; i < 4; i++) {
                    Vector2f puffRandomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(4f, 18f));
                	engine.addSmokeParticle(loc, puffRandomVel, MathUtils.getRandomNumberInRange(20f, 40f), 0.8f, 0.8f, new Color(110,100,90,100));
                	
                	// side + back sprays
                    for (int j=0; j < 2; j++) {
                    	// left smoke
                    	Vector2f smokeRandomVelL = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(29f, 41f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-120f, -60f));
                    	engine.addNebulaSmokeParticle(loc,
                        		smokeRandomVelL,
                        		MathUtils.getRandomNumberInRange(10f, 20f), //size
                        		2.2f, //end mult
                        		0.5f, //ramp fraction
                        		0.75f, //full bright fraction
                        		MathUtils.getRandomNumberInRange(0.9f, 1.3f), //duration
                        		new Color(190,185,180,120));
                    	// right smoke
                    	Vector2f smokeRandomVelR = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(29f, 41f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(120f, 60f));
                    	engine.addNebulaSmokeParticle(loc,
                        		smokeRandomVelR,
                        		MathUtils.getRandomNumberInRange(10f, 20f), //size
                        		2.2f, //end mult
                        		0.5f, //ramp fraction
                        		0.75f, //full bright fraction
                        		MathUtils.getRandomNumberInRange(0.9f, 1.3f), //duration
                        		new Color(190,185,180,120));

                		// frontal smoke jet/trail
                    	for (int k=0; k < 5; k++) {
                    		float jetDist = (i * 18f) + (k * 3f) + MathUtils.getRandomNumberInRange(0f, 15f); // 21-36 - 87-102
                    		int jetAlpha = 140 - ((int) jetDist);
                    		Vector2f smokeRandomVelJ = MathUtils.getPointOnCircumference(null, jetDist * 2.5f, MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f));
                        	engine.addNebulaSmokeParticle(loc,
                            		smokeRandomVelJ,
                            		MathUtils.getRandomNumberInRange(13f, 20f) - (i * 2), //size
                            		2.2f, //end mult
                            		0.5f, //ramp fraction
                            		0.5f, //full bright fraction
                            		0.25f + (jetDist * 0.02f), //duration
                            		new Color(190,185,180,jetAlpha));
                    	}
                    }
    			}

            	// left sparks
    			ASF_RadialEmitter emitterLeft = new ASF_RadialEmitter(null);
    			emitterLeft.location(loc);
    			emitterLeft.angle(MISSILE.getFacing() - 115f, 50f);
    			emitterLeft.life(1f, 1f);
    			emitterLeft.size(3f, 6f);
    			emitterLeft.velocity(30f, 25f);
    			emitterLeft.color(255,112,69,225);
    			emitterLeft.burst(16);
            	// right sparks
    			ASF_RadialEmitter emitterRight = new ASF_RadialEmitter(null);
    			emitterRight.location(loc);
    			emitterRight.angle(MISSILE.getFacing() + 65f, 50f);
    			emitterRight.life(1f, 1f);
    			emitterRight.size(3f, 6f);
    			emitterRight.velocity(30f, 25f);
    			emitterRight.color(255,112,69,225);
    			emitterRight.burst(16);
            	// back sparks
    			ASF_RadialEmitter emitterBack = new ASF_RadialEmitter(null);
    			emitterBack.location(loc);
    			emitterBack.angle(MISSILE.getFacing() + 170f, 20f);
    			emitterBack.life(1.2f, 1.2f);
    			emitterBack.size(3f, 8f);
    			emitterBack.velocity(23f, 50f);
    			emitterBack.color(255,112,69,255);
    			emitterBack.burst(19);
    			
    		
    			engine.removeEntity(MISSILE);
        	}
        }
        
		
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    //////////////////////
    //    TARGETING     //
    //////////////////////
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}