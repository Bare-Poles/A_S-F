package org.amazigh.foundry.scripts.arktech;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_sumakeOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	engine.addPlugin(new ASF_SumakeProjScript(projectile));
        	
    }
  }