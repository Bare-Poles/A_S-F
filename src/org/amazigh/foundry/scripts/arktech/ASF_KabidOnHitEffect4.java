package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_KabidOnHitEffect4 implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
    	if (!projectile.isFading()) {
    		
    		WeaponAPI weapon = projectile.getWeapon();
    		ShipAPI ship = projectile.getSource();

    		float randomArc = MathUtils.getRandomNumberInRange(-5f, 5f);
        	
        	Vector2f vel = (Vector2f) (ship.getVelocity());
        	Vector2f RandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(20f, 100f));
    		
			Global.getCombatEngine().spawnProjectile(ship,
	                weapon,
	                "A_S-F_kabid_sub4",
	                weapon.getFirePoint(0),
	                weapon.getCurrAngle() + randomArc,
	                RandomVel);
			
			for (int i=0; i < 26; i++) {
                float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(-5.6f, 5.6f);
                Vector2f offsetVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(6f, 140f), angle);
                
                Vector2f point2 = MathUtils.getPointOnCircumference(weapon.getFirePoint(0), MathUtils.getRandomNumberInRange(2f, 30f), angle);
                
                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point2, 4f),
                		offsetVel,
                		MathUtils.getRandomNumberInRange(2f, 4f), //size
                		1.0f, //brightness
                		MathUtils.getRandomNumberInRange(0.3f, 0.44f), //duration
                		new Color(150,255,240,160));
            }
    		
            engine.spawnEmpArcVisual(point, target, weapon.getFirePoint(0), ship, 10f,
    				new Color(120,85,130,25),
    				new Color(205,225,255,30));
    		
    		Global.getSoundPlayer().playSound("A_S-F_kabid_fire", 1.8f, 0.5f, ship.getLocation(), ship.getVelocity());
    		
    	}
		
	}
}