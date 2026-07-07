package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.hullmods.ASF_ArtyMount.ShipSpecificData;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_vandakDecoScript implements EveryFrameWeaponEffectPlugin {

	private float spoolTime = 3.5f; // portion of recharge time that the vfx is not spooling up
	
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    	
		if (engine.isPaused()) {
			return;
		}
		
		if (!weapon.getShip().isAlive()) {
			return;
		}
		
		
    	ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("ASF_ARTILLERY_DATA_KEY" + weapon.getShip().getId());
        if (info == null) {
            return; // sanity check, just do nothing if the "main" hullmod hasn't set things up yet
        }
        
        if (info.TIMER > spoolTime) {
        	float spoolVal = info.TIMER - spoolTime;
        	int alpha = (int) Math.ceil((510 * spoolVal));
    		alpha = Math.min(255, alpha);
    		
        	weapon.getSprite().setColor(new Color(255,255,255,alpha));
        } else {
        	weapon.getSprite().setColor(new Color(255,255,255,0));
        }
        
    }
    
}