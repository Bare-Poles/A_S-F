package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
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

public class ASF_LilinOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(50,255,25,255);
    private static final Color COLOR_X = new Color(100,240,50,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                20f,
                10f,
                projectile.getDamageAmount(),
                projectile.getDamageAmount()/2f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                6f,
                0.7f,
                32,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        engine.spawnExplosion(point, fxVel, COLOR_X, 33f, 0.35f);
        
        for (int i=0; i < 72; i++) {
            float dist = MathUtils.getRandomNumberInRange(1f, 35f);
            Vector2f offsetLoc = MathUtils.getPointOnCircumference(point, dist, i * 5f);
            Vector2f offsetVel = MathUtils.getPointOnCircumference(fxVel, (36f - dist) * 10f, i * 5f);
            
            Global.getCombatEngine().addSmoothParticle(offsetLoc,
            		offsetVel,
            		MathUtils.getRandomNumberInRange(3f, 5f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.3f, 0.4f), //duration
            		COLOR_P);
        }
        
	}
}
