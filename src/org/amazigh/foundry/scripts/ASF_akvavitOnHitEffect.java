package org.amazigh.foundry.scripts;

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

public class ASF_akvavitOnHitEffect implements OnHitEffectPlugin {
	
	private static Map<HullSize, Float> impulseMult = new HashMap<HullSize, Float>();
	static {
		impulseMult.put(HullSize.FIGHTER, 0.8f);
		impulseMult.put(HullSize.FRIGATE, 1.6f);
		impulseMult.put(HullSize.DESTROYER, 2.3f);
		impulseMult.put(HullSize.CRUISER, 3.0f);
		impulseMult.put(HullSize.CAPITAL_SHIP, 3.6f);
		impulseMult.put(HullSize.DEFAULT, 2.5f);
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		if (target instanceof ShipAPI) {
			target.getVelocity().scale(MathUtils.getRandomNumberInRange(0.9f, 0.95f)); // 5-10% slow applied onHit
			CombatUtils.applyForce(target, projectile.getFacing()+180f, projectile.getDamageAmount() * impulseMult.get(((ShipAPI) target).getHullSize()));
		}
		
		Global.getCombatEngine().addSmoothParticle(point,
				fxVel,
				40f, //size
				1.0f, //brightness
				0.15f, //duration
				new Color(70,180,250,255));
		
		// particle spray in the direction of the pull
		for (int i=0; i < 10; i++) {
			
			// base spray
			for (int j=0; j < 3; j++) {
				Vector2f sparkVel1 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(10f, 120f), projectile.getFacing() + MathUtils.getRandomNumberInRange(140f, 220f));
				
				Global.getCombatEngine().addSmoothParticle(point,
						sparkVel1,
						MathUtils.getRandomNumberInRange(3f, 8f), //size
						0.8f, //brightness
						MathUtils.getRandomNumberInRange(0.45f, 0.6f), //duration
						new Color(60,150,240,255));
			}
			
			// "jet"
			Vector2f sparkVel2 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(80f, 200f), projectile.getFacing() + MathUtils.getRandomNumberInRange(174f, 186f));
			
			Global.getCombatEngine().addSmoothParticle(point,
					sparkVel2,
					MathUtils.getRandomNumberInRange(2f, 9f), //size
					0.9f, //brightness
					MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
					new Color(80,160,255,255));
			
			// wide spread of "sparkles"
			Vector2f sparkVel3 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(50f, 360f), projectile.getFacing() + MathUtils.getRandomNumberInRange(160f, 200f));
			
			Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point, 5f),
					sparkVel3,
					MathUtils.getRandomNumberInRange(3f, 5f), //size
					1.0f, //brightness
					MathUtils.getRandomNumberInRange(0.25f, 0.4f), //duration
					new Color(20,240,255,255));
		}
	}
}
