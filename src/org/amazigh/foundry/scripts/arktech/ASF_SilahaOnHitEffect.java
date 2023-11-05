package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_SilahaOnHitEffect implements OnHitEffectPlugin {
	
	private static Map<HullSize, Float> impulseMult = new HashMap<HullSize, Float>();
	static {
		impulseMult.put(HullSize.FIGHTER, 0.2f);
		impulseMult.put(HullSize.FRIGATE, 0.45f);
		impulseMult.put(HullSize.DESTROYER, 0.55f);
		impulseMult.put(HullSize.CRUISER, 0.75f);
		impulseMult.put(HullSize.CAPITAL_SHIP, 0.9f);
		impulseMult.put(HullSize.DEFAULT, 0.6f);
	}
	
    private static final Color SPARK_COLOR = new Color(255,175,255,210);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(0.85f); // 15% slow applied onHit
			
			CombatUtils.applyForce(target, projectile.getFacing(), projectile.getEmpAmount() * impulseMult.get(((ShipAPI) target).getHullSize())); // knockback!
			
			for (int i=0; i < 20; i++) {
				// done like this so i can force it to bring them (close) to overload, and not just waste the entire soft flux spike if near max flux
				((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.1f, false);
					// 20 x 40 = 800	But also scales with stat mods to wep damage :) 
			}
		}
		
        engine.spawnExplosion(point, fxVel, SPARK_COLOR, 33f, 0.7f);
        
        // arcs
		for (int i=0; i < 5; i++) {
	        Vector2f arcPoint = MathUtils.getPointOnCircumference(point, 30f + (i * 35f), MathUtils.getRandomNumberInRange(projectile.getFacing() -24f, projectile.getFacing() +24f));
	        
	        engine.spawnEmpArcVisual(point, target, arcPoint, target, 13f,
					new Color(90,175,100,125),
					new Color(255,175,255,140));
		}
		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.8f, 1.1f, point, fxVel);
        
		// frontal particles
        for (int i=0; i < 80; i++) {
        	Vector2f spawnLoc = MathUtils.getRandomPointInCone(point, 210f, projectile.getFacing() - 25f, projectile.getFacing() + 25f);
        	
            engine.addSmoothParticle(spawnLoc,
            		MathUtils.getRandomPointOnCircumference(fxVel, 4f),
            		MathUtils.getRandomNumberInRange(4f, 10f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.6f, 1.2f), //duration
            		SPARK_COLOR.darker());
        }
        
        // side particles
        for (int i=0; i < 20; i++) {
        	float sparkAngle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(79f, 89f);
        	float sparkAngle2 = projectile.getFacing() - MathUtils.getRandomNumberInRange(79f, 89f);
        	
			Vector2f sparkVel1 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(30f, 175f), sparkAngle1);
			Vector2f sparkVel2 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(30f, 175f), sparkAngle2);
			
			Global.getCombatEngine().addSmoothParticle(point,
					sparkVel1,
					MathUtils.getRandomNumberInRange(2f, 9f), //size
					0.9f, //brightness
					MathUtils.getRandomNumberInRange(0.6f, 1f), //duration
					SPARK_COLOR.darker());
			Global.getCombatEngine().addSmoothParticle(point,
					sparkVel2,
					MathUtils.getRandomNumberInRange(2f, 9f), //size
					0.9f, //brightness
					MathUtils.getRandomNumberInRange(0.6f, 1f), //duration
					SPARK_COLOR.darker());
        }
        
        // rear "jet" particles
		for (int i=0; i < 20; i++) {
			for (int j=0; j < 4; j++) {
				Vector2f sparkVel1 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(10f, 120f), projectile.getFacing() + MathUtils.getRandomNumberInRange(125f, 235f));
				
				engine.addSmoothParticle(point,
						sparkVel1,
						MathUtils.getRandomNumberInRange(3f, 8f), //size
						0.8f, //brightness
						MathUtils.getRandomNumberInRange(0.55f, 0.7f), //duration
						SPARK_COLOR.darker());
			}
			
			Vector2f sparkVel2 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(80f, 280f), projectile.getFacing() + MathUtils.getRandomNumberInRange(177f, 183f));
			
			engine.addSmoothParticle(point,
					sparkVel2,
					MathUtils.getRandomNumberInRange(2f, 9f), //size
					0.9f, //brightness
					MathUtils.getRandomNumberInRange(0.6f, 0.8f), //duration
					SPARK_COLOR);
        }
        
	}
}
