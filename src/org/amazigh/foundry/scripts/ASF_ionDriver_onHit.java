package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_ionDriver_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}

		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.8f, 0.65f, point, fxVel);
		
		// particle burst
		for (int i=0; i < 37; i++) {
            float dist = MathUtils.getRandomNumberInRange(7f, 29f);
            float angle = MathUtils.getRandomNumberInRange(0f, 360f);
            Vector2f offsetLoc = MathUtils.getPointOnCircumference(point, dist, angle);
            Vector2f offsetVel = MathUtils.getPointOnCircumference(fxVel, (34f - dist) * 4.8f, angle);
            
            Global.getCombatEngine().addSmoothParticle(offsetLoc,
            		offsetVel,
            		MathUtils.getRandomNumberInRange(3f, 6f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.62f, 0.83f), //duration
            		new Color(29,200,230,255)); // 29,170,255
        }
		
		// inner arcs
		for (int i=0; i < 2; i++) {
	        float distanceRandom1 = MathUtils.getRandomNumberInRange(18f, 27f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(80, 130);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(25,130,155,98), // 25,110,165
					new Color(220,240,250,114)); // 230,255,114
		}
		
		// outer arcs
		for (int i=0; i < 2; i++) {
	        float distanceRandom1 = MathUtils.getRandomNumberInRange(33f, 41f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 120);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 9f,
					new Color(25,145,140,103), // 25,110,165
					new Color(220,240,250,121)); // 230,255,114
		}
		
	}
}
