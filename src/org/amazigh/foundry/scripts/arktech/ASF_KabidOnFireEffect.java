package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_KabidOnFireEffect implements OnFireEffectPlugin {

	private static final Color COLOR_MUZZLE = new Color(240,210,255,100);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	Vector2f vel = ship.getVelocity();
    	
        engine.spawnExplosion(weapon.getFirePoint(0), vel, COLOR_MUZZLE, 35f, 0.5f);
        
        for (int i=0; i < 54; i++) {
            float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(-1f, 1f);
            Vector2f offsetVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(10f, 200f), angle);
            
            Vector2f point = MathUtils.getPointOnCircumference(weapon.getFirePoint(0), MathUtils.getRandomNumberInRange(2f, 40f), angle);
            
            Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point, 4f),
            		offsetVel,
            		MathUtils.getRandomNumberInRange(2f, 4f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.35f, 0.5f), //duration
            		new Color(150,255,240,200));
        }
    }
  }