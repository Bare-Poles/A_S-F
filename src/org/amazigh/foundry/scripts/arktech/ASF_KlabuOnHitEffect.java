package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_KlabuOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(255,190,100,255);
	private static final Color COLOR_X = new Color(175,90,50,255);
	private static final Color COLOR_U = new Color(170,50,30,200);
	private static final Color COLOR_D_C = new Color(155,155,155,255);
	private static final Color COLOR_D_F = new Color(200,140,80,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		float blastDamage = projectile.getDamageAmount() * 0.75f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                60f,
                30f,
                blastDamage,
                blastDamage * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                60,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(true);
        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
        blast.setUseDetailedExplosion(true);
        blast.setDetailedExplosionRadius(70f);
        blast.setDetailedExplosionFlashRadius(200f);
        blast.setDetailedExplosionFlashDuration(0.5f);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        engine.spawnExplosion(point, fxVel, COLOR_U, 80f, 1.3f);

		engine.addHitParticle(point, fxVel, 150f, 1f, 0.1f, COLOR_U);
        
		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 0.75f, 1.0f, point, fxVel); //"explosion_flak", 0.8f, 0.9f
	}
}
