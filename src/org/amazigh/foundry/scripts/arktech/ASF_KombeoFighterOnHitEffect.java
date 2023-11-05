package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_KombeoFighterOnHitEffect implements ProximityExplosionEffect {

	private static final Color COLOR_X = new Color(255,110,70,155);
    private static final int BLAST_COUNT = 4;
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float blastDamage = originalProjectile.getDamageAmount()*0.5f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                32f,
                16f,
                blastDamage,
                blastDamage/2f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                6f,
                0.7f,
                30,
                COLOR_X,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(false);

        Vector2f point = explosion.getLocation();
        
        for (int i = 0; i < BLAST_COUNT; i++) {
        	
        	Vector2f blastPoint = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(14.4f, 79.2f));
            
            engine.spawnDamagingExplosion(blast,explosion.getSource(),blastPoint,false);
            
            float timerRandom = MathUtils.getRandomNumberInRange(0.25f, 0.4f);
            engine.spawnExplosion(blastPoint, explosion.getVelocity(), COLOR_X, 27f, timerRandom);   
        }
        Global.getSoundPlayer().playSound("system_canister_flak_explosion", 1.4f, 0.5f, point, explosion.getVelocity()); //"explosion_flak", 0.8f, 0.9f
	}
}



