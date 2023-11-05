package org.amazigh.foundry.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ASF_CoralianWeaponScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	
	public static final int BARREL_COUNT = 6;
    private int shotCounter = 0;

    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<DamagingProjectileAPI>();

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	ShipAPI source = weapon.getShip();
    	
    	shotCounter++;
    	if (shotCounter >= BARREL_COUNT) {
    		shotCounter = 0;
    	     //yeah this bit is a hack, but it's a hack that WORKS (the onFire gets run six times because six barrels, and we have six barrels so the statcard matches what is fired)
    		for (WeaponSlotAPI port : source.getHullSpec().getAllWeaponSlotsCopy()) {
        		if (port.isDecorative()) {
        			if (port.isHidden()) {
        				engine.spawnProjectile(source, weapon, "A_S-F_coralian_sub", port.computePosition(source), port.getAngle() + source.getFacing(), source.getVelocity());
            			
            			engine.spawnExplosion(port.computePosition(source), source.getVelocity(), new Color(255,150,230,255), 5f, 0.5f);
            			
            			for (int i=0; i < 18; i++) {
            				float distanceRandom2 = MathUtils.getRandomNumberInRange(0.4f, 1.2f);
            	            Vector2f Dir2 = Misc.getUnitVectorAtDegreeAngle(port.computeMidArcAngle(source) + MathUtils.getRandomNumberInRange(-12f, 12f));
            	            Vector2f particleVel = new Vector2f(Dir2.x * 100 * distanceRandom2, Dir2.y * 100 * distanceRandom2);
            	            particleVel.x += source.getVelocity().x;
            	            particleVel.y += source.getVelocity().y;
            	            float randomSize1 = MathUtils.getRandomNumberInRange(5f, 11f);
            	            engine.addSmoothParticle(port.computePosition(source), particleVel, randomSize1, 1f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), new Color(255,110,240,255));	
            			}
        			} else {
                		Global.getSoundPlayer().playSound("A_S-F_coralian_fire", 1.0f, 0.8f, port.computePosition(source), source.getVelocity());
                			// so we get a couple extra instances of the fire sound, for more "punch" to it
        			}
    			}
        	}
    	}
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null ){
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {

            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
            //if (proj.getProjectileSpecId() == "A_S-F_coralian_bolt" && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
            	//so we look for projectiles by projectile ID, this could cause issues if you get more than one of this ship, but you shouldn't be able to.
            	if (target == null) {
                	engine.addPlugin(new ASF_CoralianDumbProjScript(proj, target));
                	alreadyRegisteredProjectiles.add(proj);
            	} else {
                	engine.addPlugin(new ASF_CoralianProjScript(proj, target));
                	alreadyRegisteredProjectiles.add(proj);
            	}
            }
        }

        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }

    }
}
