package org.amazigh.foundry.scripts;

import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_amateusOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(final DamagingProjectileAPI projectile, CombatEntityAPI target,
					  final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
		engine.addNebulaParticle(point,
				new Vector2f(0f, 0f),
				MathUtils.getRandomNumberInRange(95f, 120f), //size
				1.5f, //endSizeMult
				0f, //rampUpFraction
				0.5f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.3f, 0.6f), //dur
				new Color(245,160,95,50));
		
        for (int i = 0; i < 6; i++) {
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 40f),
    				MathUtils.getRandomNumberInRange(72f, 123f), //size
    				1.9f, //endSizeMult
    				0.15f, //rampUpFraction
    				0.35f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1f, 1.5f), //dur
    				new Color(175,145,130,50));
        }
        
        CombatEntityAPI bomb = engine.spawnProjectile(projectile.getSource(),
                projectile.getWeapon(), "A_S-F_amateus_bomb",
                point,
                projectile.getFacing(),
                new Vector2f(0f, 0f));
		((DamagingProjectileAPI)bomb).setFromMissile(true);
		
	}
}
