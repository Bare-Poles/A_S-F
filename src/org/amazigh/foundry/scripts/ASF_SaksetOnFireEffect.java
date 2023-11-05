package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_SaksetOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	float angle = projectile.getFacing();
        Vector2f ship_velocity = ship.getVelocity();
        Vector2f proj_location = projectile.getLocation();
    	
    	// explosion effect
    	engine.spawnExplosion(proj_location, ship_velocity, new Color(210,200,175,100), 40f, 0.9f); // 185,225,175
    	
    	for (int h=0; h < 16; h++) {
    		// left/right smoke spray
    		float angleL1 = angle + MathUtils.getRandomNumberInRange(-60f, -50f);
    		Vector2f smokeVelL = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 22f), angleL1);
    		Vector2f pointL1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 24f), angleL1);
    		engine.addNebulaSmokeParticle(pointL1,
    				smokeVelL,
    				10f, //size
    				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
    				0.6f, //ramp fraction
    				0.5f, //full bright fraction
    				MathUtils.getRandomNumberInRange(0.6f, 1.1f), //duration
    				new Color(120,105,90,75));
    		float angleR1 = angle + MathUtils.getRandomNumberInRange(50f, 60f);
    		Vector2f smokeVelR = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 22f), angleR1);
    		Vector2f pointR1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 24f), angleR1);
    		engine.addNebulaSmokeParticle(pointR1,
    				smokeVelR,
    				10f, //size
    				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
    				0.6f, //ramp fraction
    				0.5f, //full bright fraction
    				MathUtils.getRandomNumberInRange(0.6f, 1.1f), //duration
    				new Color(120,105,90,75));
    		
    		for (int i2=0; i2 <2; i2++) {
        		// left/right particle jets
            	float angleL2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-61f, -49f);
                Vector2f offsetVelL = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(4f, 60f), angleL2);
                Vector2f pointL2 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(2f, 20f), angleL2);
                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(pointL2, 3f),
                		offsetVelL,
                		MathUtils.getRandomNumberInRange(2f, 5f), //size
                		1.0f, //brightness
                		MathUtils.getRandomNumberInRange(0.7f, 1.0f), //duration
                		new Color(250,160,80,200));
            	float angleR2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(49f, 61f);
                Vector2f offsetVelR = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(4f, 60f), angleR2);
                Vector2f pointR2 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(2f, 20f), angleR2);
                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(pointR2, 3f),
                		offsetVelR,
                		MathUtils.getRandomNumberInRange(2f, 7f), //size
                		1.0f, //brightness
                		MathUtils.getRandomNumberInRange(0.7f, 1.0f), //duration
                		new Color(250,160,80,200));
    		}
    		
        	for (int i=0; i < 3; i++) {
        		// core smoke spray
        		float angle1 = angle + MathUtils.getRandomNumberInRange(-9f, 9f);
        		Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 67f), angle1);
        		Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-1f, 74f), angle1);
        		engine.addNebulaSmokeParticle(point1,
        				smokeVel,
        				10f, //size
        				MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
        				0.6f, //ramp fraction
        				0.5f, //full bright fraction
        				MathUtils.getRandomNumberInRange(0.7f, 1.25f), //duration
        				new Color(120,105,90,75));       		
        	}

    		for (int j=0; j < 5; j++) {
        		// core particle jet
            	float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-10f, 10f);
                Vector2f offsetVel1 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(5f, 90f), angle2);
                Vector2f point2 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(2f, 30f), angle2);
                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point2, 3f),
                		offsetVel1,
                		MathUtils.getRandomNumberInRange(2f, 6f), //size
                		1.0f, //brightness
                		MathUtils.getRandomNumberInRange(0.9f, 1.2f), //duration
                		new Color(250,160,80,200));
    		}
    	}
    	
    }
  }