package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_MorosOnFireEffect implements OnFireEffectPlugin {
	
	private static final Color COLOR_P = new Color(180,225,100,180);
	private static final Color COLOR_FLSH = new Color(200,90,130,255);
    private static final Color COLOR_SMK = new Color(190,180,170,130); // 180,120,110,150
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		Vector2f loc = (Vector2f) (projectile.getLocation());
		
		Vector2f fxVel = new Vector2f();
		if (weapon.getShip() != null) {
			fxVel.set(weapon.getShip().getVelocity());
		}
		
		Global.getSoundPlayer().playSound("hurricane_mirv_split", 1.45f, 1.0f, loc, projectile.getVelocity());
		
		// "flash" particle
		engine.addHitParticle(loc,
                fxVel,
                30f,
                1.0f,
                0.15f,
                COLOR_FLSH);
		
		// some core smoke
		engine.addNebulaSmokeParticle(loc,
				fxVel,
        		18f, //size
        		2.2f, //end mult
        		0.5f, //ramp fraction
        		0.7f, //full bright fraction
        		1.2f, //duration
        		COLOR_SMK);
		
		for (int i=0; i < 4; i++) {
            
            for (int j=0; j < 2; j++) {
    			// core sparks
            	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(loc, 4f),
            			MathUtils.getRandomPointInCircle(fxVel, 12f),
            			MathUtils.getRandomNumberInRange(4f, 8f), //size
            			0.9f, //brightness
            			MathUtils.getRandomNumberInRange(0.7f, 1.0f), //duration
            			COLOR_P);
            	

                for (int j1=0; j1 < 2; j1++) {
                	// back sparks
                	Vector2f sparkRandomVelA = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(18f, 36f), projectile.getFacing() + MathUtils.getRandomNumberInRange(165f, 195f));
                																										//36, 73
                	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(loc, 4f),
                			sparkRandomVelA,
                			MathUtils.getRandomNumberInRange(3f, 6f), //size
                			1.0f, //brightness
                			MathUtils.getRandomNumberInRange(0.9f, 1.2f), //duration
                			COLOR_P);
                }
            	
                for (int jj=0; jj < 6; jj++) {
                	// front sparks
                	float sparkDist = (i * 12f) + (jj * 2f) + MathUtils.getRandomNumberInRange(0f, 11f); // 14-25 - 60-71
                	
                	Vector2f sparkRandomVelB = MathUtils.getPointOnCircumference(fxVel, sparkDist * 2.4f, projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f));
                	engine.addSmoothParticle(MathUtils.getRandomPointInCircle(loc, 4f),
                			sparkRandomVelB,
                			MathUtils.getRandomNumberInRange(3f, 6f), //size
                			1.0f, //brightness
                			MathUtils.getRandomNumberInRange(0.8f, 1.0f) + (sparkDist * 0.01f), //duration
                			COLOR_P);
                }
            }
            
            // frontal smoke jet/trail
            for (int k=0; k < 3; k++) {
            	float jetDist = (i * 12f) + (k * 5f) + MathUtils.getRandomNumberInRange(0f, 10f); // 17-27 - 63-73
            	int jetAlpha = 140 - ((int) jetDist);
            	Vector2f smokeRandomVelJ = MathUtils.getPointOnCircumference(fxVel, jetDist * 2.5f, projectile.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f));
            	engine.addNebulaSmokeParticle(loc,
            			smokeRandomVelJ,
            			MathUtils.getRandomNumberInRange(13f, 20f) - (i * 2), //size
            			2.0f, //end mult
            			0.5f, //ramp fraction
            			0.5f, //full bright fraction
            			1f + (jetDist * 0.01f), //duration
            			new Color(190,180,170,jetAlpha));
            }
		}
		
		
    }
}