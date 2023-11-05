package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASF_chillblainWeaponScript implements EveryFrameWeaponEffectPlugin {
	
	private static Map<HullSize, Float> hullDamMult = new HashMap<HullSize, Float>();
	static {
		hullDamMult.put(HullSize.FIGHTER, 0.8f);
		hullDamMult.put(HullSize.FRIGATE, 0.9f);
		hullDamMult.put(HullSize.DESTROYER, 1f);
		hullDamMult.put(HullSize.CRUISER, 1.15f);
		hullDamMult.put(HullSize.CAPITAL_SHIP, 1.35f);
		hullDamMult.put(HullSize.DEFAULT, 1f);
	}

	private static Map<HullSize, Float> hullDamFlat = new HashMap<HullSize, Float>();
	static {
		hullDamFlat.put(HullSize.FIGHTER, 50f);
		hullDamFlat.put(HullSize.FRIGATE, 70f);
		hullDamFlat.put(HullSize.DESTROYER, 100f);
		hullDamFlat.put(HullSize.CRUISER, 150f);
		hullDamFlat.put(HullSize.CAPITAL_SHIP, 250f);
		hullDamFlat.put(HullSize.DEFAULT, 100f);
	}
	
    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<DamagingProjectileAPI>();
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    	
    	ShipAPI ship = weapon.getShip();
    	
    	//status display for player info on damage
    	if (ship.getShipTarget() != null && ship == engine.getPlayerShip()) {
    		
    		float tagMass = ship.getShipTarget().getMass();
    		float tagRad = ship.getShipTarget().getCollisionRadius();
    		
    		float FF_mult = 1f;
			if (ship.getShipTarget().getOwner() == ship.getOwner()) {
				FF_mult = 0.5f;
			}

			// "clown ships get clown damage"
			FF_mult *= (Math.max(5000f, tagMass) / 5000f);
    		
			
    		float coreDam = FF_mult * (((tagMass * 0.2f) + tagRad) * hullDamMult.get(ship.getShipTarget().getHullSize())) + hullDamFlat.get(ship.getShipTarget().getHullSize());
    		
    		if (ship.getShipTarget().isStationModule() || ship.getShipTarget().isShipWithModules()) {
    			coreDam *= 0.25f;
			}
    		
        	float arcDam = FF_mult * (tagMass / 10f) * hullDamMult.get(ship.getShipTarget().getHullSize());
        	
        	if (ship.isStationModule()) {
        		arcDam *= hullDamMult.get(ship.getParentStation().getHullSize());
			}
        	
        	int arcCount = 0;
        	float arcValue = (tagRad/2) + (tagMass/6);
			for (int i=0; i < arcValue; i += 30) {
				arcCount++;
			}
        	
			engine.maintainStatusForPlayerShip("CHILLDAM", "graphics/icons/hullsys/entropy_amplifier.png", "Approximate damage of final blast to current target", "Core Blast: " + (int)coreDam + " | " + arcCount + " * Arcs, each dealing: " + (int)arcDam, false);
    	}
    	
    	
    	
        for (MissileAPI proj : CombatUtils.getMissilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
            	engine.addPlugin(new ASF_chillblainDetonator(proj));
            	alreadyRegisteredProjectiles.add(proj);
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
