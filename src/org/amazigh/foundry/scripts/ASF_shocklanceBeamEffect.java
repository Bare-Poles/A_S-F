package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;

public class ASF_shocklanceBeamEffect implements BeamEffectPlugin {

    private boolean flash = false;
    
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

		Vector2f point = beam.getFrom();
        float facing = beam.getWeapon().getCurrAngle();
        
        if (!flash) {
       	 flash = true;
       	 
       	 DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                    100f,
                    60f,
                    200f, //maxDam
                    100f, //minDam
                    CollisionClass.PROJECTILE_NO_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    4f,
                    4f,
                    1f,
                    100,
                    new Color(255,100,80,25), //255 a
                    new Color(255,70,70,255));
            blast.setDamageType(DamageType.FRAGMENTATION);
            blast.setShowGraphic(true);
            blast.setUseDetailedExplosion(true);
            blast.setDetailedExplosionFlashColorCore(new Color(155,155,155,215));
            blast.setDetailedExplosionFlashColorFringe(new Color(255,10,25,215));
            blast.setDetailedExplosionRadius(90f);
            blast.setDetailedExplosionFlashRadius(222f);
            blast.setDetailedExplosionFlashDuration(1.2f);
            engine.spawnDamagingExplosion(blast,beam.getSource(),point,false);
            
    		for (int i = 0; i < 30; i++) {
    			float angleOffset = (float) Math.random();
    			if (angleOffset > 0.2f) {
    				angleOffset *= angleOffset;
    			}
    			float speedMult = 1f - angleOffset;
    			speedMult = 0.5f + speedMult * 0.5f;
    			angleOffset *= Math.signum((float) Math.random() - 0.5f);
    			angleOffset *= 22.5f;
    			float theta = (float) Math.toRadians(facing + angleOffset);
    			float r = (float) (Math.random() * Math.random() * 100);
    			float x = (float)Math.cos(theta) * r;
    			float y = (float)Math.sin(theta) * r;
    			Vector2f pLoc = new Vector2f(point.x + x, point.y + y);
    			
    			float speed = MathUtils.getRandomNumberInRange(20f, 110f);
    			speed *= speedMult;
    			
    			Vector2f pVel = Misc.getUnitVectorAtDegreeAngle((float) Math.toDegrees(theta));
    			pVel.scale(speed);
    			
    			float pSize = MathUtils.getRandomNumberInRange(20f, 40f);
    			float pDur = MathUtils.getRandomNumberInRange(0.6f, 1.0f);
    			float endSize = MathUtils.getRandomNumberInRange(1.0f, 1.8f);
    			
    			int rC = Math.min(255, 135 + (int) speedMult * 80);
    			int g = Math.max(10, 105 - (int) speedMult * 50);
    			int b = Math.max(5, 95 - (int) speedMult * 40);
    			
    			engine.addNebulaParticle(pLoc, pVel, pSize, endSize, 0.1f, 0.5f, pDur, new Color(rC,g,b,130)); //255,40,40,130
    		}
        }
        
    }
}