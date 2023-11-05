package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_SaksetOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(225,195,175,180);
	private static final Color COLOR_X = new Color(95,165,55,160);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		float blastDamage = projectile.getDamageAmount() * 0.2f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.1f,
                69f,
                34f,
                blastDamage,
                blastDamage * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                4f,
                0.75f,
                35,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(true);
        blast.setUseDetailedExplosion(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		
		
		// rear shrapnel spawning
		for (int i=0; i < 8; i++) {
			
			// MathUtils.getRandomNumberInRange(120, 240)
			float arcRandom = projectile.getFacing() + 120f + (i * 15f) + MathUtils.getRandomNumberInRange(0, 15);
            Vector2f origin = MathUtils.getPointOnCircumference(point, 18f, arcRandom);
            Vector2f fragVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(-75f, 150f), arcRandom);
            engine.spawnProjectile(projectile.getSource(),
                        projectile.getWeapon(), "A_S-F_sakset_frag",
                         origin,
                         arcRandom,
                         fragVel);
            
    		for (int j=0; j < 9; j++) {
    			// some smoke particles
                float angle1 = arcRandom + MathUtils.getRandomNumberInRange(-6f, 6f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, j * 7f, angle1);
                Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), j + 2f, angle1);
                engine.addNebulaSmokeParticle(point1,
                		smokeVel,
                		7f, //size
                		MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
                		0.6f, //ramp fraction
                		0.5f, //full bright fraction
                		MathUtils.getRandomNumberInRange(0.65f, 0.8f) * (0.5f + (0.05f * j)), //duration
                		new Color(230,220,195,75));
    		}
    		
    		// also some "general" smoke
    		engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 15f),
    				MathUtils.getRandomNumberInRange(35f, 100f),
    				2f,
    				0.1f,
    				0.3f,
    				MathUtils.getRandomNumberInRange(0.85f, 1.2f),
    				new Color(23,22,19,120),
    				true);
          }
		
	}
}
