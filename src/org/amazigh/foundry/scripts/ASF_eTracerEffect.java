package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_eTracerEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
	}

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		
        float proj_facing = projectile.getFacing();
        Vector2f proj_location = projectile.getLocation();
        Vector2f ship_velocity = ship.getVelocity();
        
        engine.addHitParticle(proj_location, ship_velocity, 41f, 1f, 0.1f, new Color(130,90,230,222));
        
        ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
        emitter.location(proj_location);
        emitter.angle(proj_facing, 0f);
        emitter.life(0.7f, 0.8f);
        emitter.size(2f, 4f);
		emitter.velocity(2f, 58f);
		emitter.distance(1f, 8f);
		emitter.color(145,100,255,255);
		emitter.emissionOffset(-5, 10);
		emitter.coreDispersion(5f);
		emitter.burst(20);
        
		ASF_RadialEmitter emitterL = new ASF_RadialEmitter((CombatEntityAPI) ship);
		emitterL.location(proj_location);
		emitterL.angle(proj_facing, 0f);
		emitterL.life(0.4f, 0.8f);
		emitterL.size(2f, 4f);
		emitterL.velocity(33f, 6f);
		emitterL.distance(27f, -23f);
		emitterL.color(145,100,255,255);
		emitterL.emissionOffset(75, 20);
		emitterL.velDistLinkage(false);
		emitterL.lifeLinkage(true);
		emitterL.coreDispersion(3f);
		emitterL.burst(15);
		
		ASF_RadialEmitter emitterR = new ASF_RadialEmitter((CombatEntityAPI) ship);
		emitterR.location(proj_location);
		emitterR.angle(proj_facing, 0f);
		emitterR.life(0.4f, 0.8f);
		emitterR.size(2f, 4f);
		emitterR.velocity(33f, 6f);
		emitterR.distance(27f, -23f);
		emitterR.color(145,100,255,255);
		emitterR.emissionOffset(-95, 20);
		emitterR.velDistLinkage(false);
		emitterR.lifeLinkage(false);
		emitterR.coreDispersion(3f);
		emitterR.burst(15);
		
        for (int i=0; i < 2; i++) {
        	Vector2f neb_velocity = MathUtils.getPointOnCircumference(ship_velocity, 10f, proj_facing);
    		engine.addSwirlyNebulaParticle(proj_location,
    				neb_velocity,
    				12f * i,
    				1.9f,
    				0.34f,
    				0.5f,
    				MathUtils.getRandomNumberInRange(0.5f, 0.8f),
    				new Color(115,95,160,123),
    				true);
        }
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
			Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		float dir = Misc.getAngleInDegrees(target.getLocation(), point);
		
		if (target != null) {
			fxVel.set(target.getVelocity());
			
			if (target instanceof ShipAPI) {
				for (int i=0; i < 5; i++) {
					// done like this so i can force it to bring them (close) to overload, and not just waste the entire soft flux spike if near max flux
						// re: split into 5x 20 flux "chunks"
					((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.4f, false);
					// yes i could make it "real" damage on shield hits or w/e, but no i'm specifically doing it this way to make it shield efficiency agnositic!
				}
			}
			
			float distanceRandom1 = MathUtils.getRandomNumberInRange(10f, 16f);
			float angleRandom1 = dir - MathUtils.getRandomNumberInRange(65, 115);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float distanceRandom2 = MathUtils.getRandomNumberInRange(10f, 16f);
	        float angleRandom2 = dir + MathUtils.getRandomNumberInRange(65, 115);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(115,80,200,40),
					new Color(100,240,255,48));

	        for (int i=0; i < 3; i++) {
	        	Vector2f hitPoint = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(-20f, 20f), dir + MathUtils.getRandomNumberInRange(70, 110));
	        	
	        	engine.addHitParticle(hitPoint, fxVel, 9f, 1f, MathUtils.getRandomNumberInRange(0.2f, 0.6f), new Color(125,105,255,36));
	        }
			Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 0.25f, point, fxVel);	
		}
		
		ASF_RadialEmitter emitterCore = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterCore.location(point);
		emitterCore.angle(0f, 360f);
		emitterCore.life(0.25f, 0.35f);
		emitterCore.size(7f, 14f);
		emitterCore.velocity(5f, 17f);
		emitterCore.color(90,215,230,170);
		emitterCore.coreDispersion(10f);
		emitterCore.burst(7);
		
		
		Vector2f sparkBase = MathUtils.getPointOnCircumference(point, 7f, dir);
		
		ASF_RadialEmitter emitterSpark = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterSpark.location(sparkBase);
		emitterSpark.angle(dir - 55f, 110f);
		emitterSpark.life(0.3f, 0.57f);
		emitterSpark.size(2f, 4f);
		emitterSpark.velocity(9f, 37f);
		emitterSpark.color(145,100,255,255);
		emitterSpark.coreDispersion(8f);
		emitterSpark.angleSplit(true);
        emitterSpark.burst(19);
		
        for (int i=0; i < 2; i++) {
        	Vector2f neb_velocity = MathUtils.getPointOnCircumference(fxVel, 13f, dir);
    		engine.addSwirlyNebulaParticle(point,
    				neb_velocity,
    				14f * i,
    				1.8f,
    				0.34f,
    				0.6f,
    				MathUtils.getRandomNumberInRange(0.55f, 0.75f),
    				new Color(85,170,190,123),
    				true);
        }
        
	}
	
}