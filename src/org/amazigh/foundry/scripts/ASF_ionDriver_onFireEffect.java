package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_ionDriver_onFireEffect implements OnFireEffectPlugin {
	
	private static final Color FLASH_COLOR = new Color(90,185,230,240);
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        
        Vector2f proj_location = projectile.getLocation();
        engine.addHitParticle(proj_location, ship_velocity, 75f, 1f, 0.1f, FLASH_COLOR.brighter());
        
        for (int i=0; i < 40; i++) {
        	float arcPoint = MathUtils.getRandomNumberInRange(projectile.getFacing() - 3f, projectile.getFacing() + 3f);
        	
        	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(0f, 10f), MathUtils.getRandomNumberInRange(projectile.getFacing() - 85f, projectile.getFacing() + 85f));
        	
        	Vector2f spawnLocation = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(10f, 45f), arcPoint);
        	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
        	
        	engine.addSmoothParticle(spawnLocation,
        			velocity,
        			MathUtils.getRandomNumberInRange(2f, 3f),
        			1f,
        			MathUtils.getRandomNumberInRange(0.6f, 2.3f),
        			FLASH_COLOR);
        }
        
        for (int i=0; i < 16; i++) {
        	float arcPoint = MathUtils.getRandomNumberInRange(projectile.getFacing() - 2.5f, projectile.getFacing() + 2.5f);
        	
        	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(0f, 10f), MathUtils.getRandomNumberInRange(projectile.getFacing() - 85f, projectile.getFacing() + 85f));
        	
        	Vector2f spawnLocation = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(45f, 63f), arcPoint);
        	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 4f));
        	
        	engine.addSmoothParticle(spawnLocation,
        			velocity,
        			MathUtils.getRandomNumberInRange(2f, 3f),
        			1f,
        			MathUtils.getRandomNumberInRange(0.5f, 2.1f),
        			FLASH_COLOR);
        }
    }
}