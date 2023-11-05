package org.amazigh.foundry.scripts.supe;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_chillblainOnHitEffect implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fieldRandomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(1f, 5f));
		CombatEntityAPI proj = engine.spawnProjectile(projectile.getWeapon().getShip(), projectile.getWeapon(), "A_S-F_chillblain_field", point, projectile.getFacing(), fieldRandomVel);
		engine.addPlugin(new ASF_chillblainProjScript((DamagingProjectileAPI) proj));
		
		for (int i=0; i < 3; i++) {
			Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(0f, 45f));
            Vector2f point0 = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(0f, 25f));
            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(0f, 45f));
            Vector2f point01 = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(0f, 25f));
			
			engine.addNebulaParticle(point0,
					offsetVel0,
					MathUtils.getRandomNumberInRange(40f, 50f),
					MathUtils.getRandomNumberInRange(0.4f, 0.5f),
					0.4f,
					0.3f,
					MathUtils.getRandomNumberInRange(0.6f, 1.0f),
					new Color(40,140,250,225)); 
			engine.addSwirlyNebulaParticle(point01,
					offsetVel01,
					MathUtils.getRandomNumberInRange(40f, 50f),
					MathUtils.getRandomNumberInRange(0.4f, 0.5f),
					0.4f,
					0.3f,
					MathUtils.getRandomNumberInRange(0.6f, 1.0f),
					new Color(40,140,250,225), false);
		}
		
        Global.getSoundPlayer().playSound("hit_heavy_energy", 0.8f, 1.25f, point, target.getVelocity());
		
		for (int i=0; i < 3; i++) {
			Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(0f, 45f));
            Vector2f point0 = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(0f, 25f));
            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(0f, 45f));
            Vector2f point01 = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(0f, 25f));
			
			engine.addNebulaParticle(point0,
					offsetVel0,
					MathUtils.getRandomNumberInRange(40f, 50f),
					MathUtils.getRandomNumberInRange(0.4f, 0.5f),
					0.4f,
					0.3f,
					MathUtils.getRandomNumberInRange(0.6f, 1.0f),
					new Color(40,140,250,225)); 
			engine.addSwirlyNebulaParticle(point01,
					offsetVel01,
					MathUtils.getRandomNumberInRange(40f, 50f),
					MathUtils.getRandomNumberInRange(0.4f, 0.5f),
					0.4f,
					0.3f,
					MathUtils.getRandomNumberInRange(0.6f, 1.0f),
					new Color(40,140,250,225), false);
		}
		
	}
}
