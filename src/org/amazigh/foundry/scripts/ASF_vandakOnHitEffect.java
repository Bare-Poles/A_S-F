package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_vandakOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(final DamagingProjectileAPI projectile, CombatEntityAPI target,
					  final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {

		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 0.8f, 1.0f, point, new Vector2f(0f, 0f));
		
		engine.addNebulaParticle(point,
				new Vector2f(0f, 0f),
				MathUtils.getRandomNumberInRange(150f, 180f), //size
				1.5f, //endSizeMult
				0f, //rampUpFraction
				0.6f, //fullBrightnessFraction
				MathUtils.getRandomNumberInRange(0.67f, 0.9f), //dur
				new Color(255,110,95,67));
		
        for (int i = 0; i < 6; i++) {
        	engine.addNebulaParticle(point,
    				MathUtils.getRandomPointInCircle(null, 60f),
    				MathUtils.getRandomNumberInRange(120f, 200f), //size
    				2.0f, //endSizeMult
    				0.2f, //rampUpFraction
    				0.4f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1.23f, 1.8f), //dur
    				new Color(175,125,110,50));
        }
		
        
		engine.addPlugin(new EveryFrameCombatPlugin() {
			
            final int clusterCount = 10;
            int clustersFired = 0;
            float clusterDamage = projectile.getDamageAmount() * 0.1f;
            
            Vector2f initPoint = MathUtils.getPointOnCircumference(point, 40f, projectile.getFacing());
            
            boolean ready = false;
            
            final IntervalUtil clusterTimer = new IntervalUtil(0.05f, 0.09f);
            final IntervalUtil delayTimer = new IntervalUtil(0.3f, 0.3f);
            
            @Override
            public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
            	
            }
            
            @Override
            public void advance(float amount, List<InputEventAPI> events) {
                if (engine.isPaused()) return;
                
                delayTimer.advance(amount);
                if (delayTimer.intervalElapsed()) {
                	ready = true;
                }
                
                if (ready) {
                	clusterTimer.advance(amount);
                	if (clusterTimer.intervalElapsed()) {
    					
                    	Vector2f blastLoc = MathUtils.getRandomPointInCircle(initPoint, 135f);
                    	
                    	DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                                80f,
                                50f,
                                clusterDamage,
                                clusterDamage * 0.6f,
                                CollisionClass.PROJECTILE_NO_FF,
                                CollisionClass.PROJECTILE_FIGHTER,
                                4f,
                                4f,
                                0.8f,
                                40,
                                new Color(255,225,125,255),
                                new Color(155,125,75,255));
                        blast.setDamageType(DamageType.HIGH_EXPLOSIVE);
                        blast.setShowGraphic(true);
                        blast.setDetailedExplosionFlashColorCore(new Color(200,175,100,255));
                        blast.setDetailedExplosionFlashColorFringe(new Color(170,155,145,255));
                        blast.setUseDetailedExplosion(true);
                        blast.setDetailedExplosionRadius(90f);
                        blast.setDetailedExplosionFlashRadius(240f);
                        blast.setDetailedExplosionFlashDuration(0.5f);
                        
                        engine.spawnDamagingExplosion(blast,projectile.getSource(),blastLoc,false);
                        
                        Vector2f fxVel = new Vector2f();
                		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 1.2f, 0.92f, blastLoc, fxVel);
                		
                		clustersFired++;
                    }
                }
                
                if (clustersFired >= clusterCount) engine.removePlugin(this);
            }

            @Override
            public void renderInWorldCoords(ViewportAPI viewport) {

            }

            @Override
            public void renderInUICoords(ViewportAPI viewport) {

            }

            @Override
            public void init(CombatEngineAPI engine) {

            }
        });
		
	}
}
