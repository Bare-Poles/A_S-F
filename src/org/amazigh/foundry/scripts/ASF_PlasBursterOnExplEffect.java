package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;

public class ASF_PlasBursterOnExplEffect implements ProximityExplosionEffect {

	private static final Color COLOR_P = new Color(140,90,255,220);
	private static final Color COLOR_X = new Color(215,0,130,50);
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
 
		engine.addHitParticle(
                point,
                explosion.getVelocity(),
                64f,
                0.8f,
                0.1f,
                COLOR_P);
		
        for (int i = 0; i < 4; i++) {
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 13f), // 11
    				MathUtils.getRandomNumberInRange(23f, 41f), //size  23,61
    				1.85f, //endSizeMult  1.75
    				0f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.6f), //dur
    				COLOR_X);

            for (int j = 0; j < 6; j++) {
            	
            	Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point, 35f),
            			MathUtils.getRandomPointInCircle(null, 4f),
            			MathUtils.getRandomNumberInRange(1f, 4f), //size
            			1.0f, //brightness
            			MathUtils.getRandomNumberInRange(0.9f, 1.3f), //duration
            			new Color(200,90,245,195));
            	
            }
        }
	}
}



