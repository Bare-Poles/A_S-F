package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import org.amazigh.foundry.hullmods.ASF_ArtyMount.ShipSpecificData;
import org.amazigh.foundry.scripts.ai.ASF_vandakHomingScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_ArtyWepLowTech extends BaseHullMod {

	private float rechargeTime = 4f;
	// private float spoolTime = 3.5f; // portion of recharge time that the vfx is not spooling up - This weapon does this via a deco weapon as it's a "physical" missile and not a glow
	private float damage = 2000f;
	private float subDamage = 200f;
	private float health = 1000f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("ASF_ARTILLERY_DATA_KEY" + ship.getId());
        if (info == null) {
            return; // sanity check, just do nothing if the "main" hullmod hasn't set things up yet
        }
        
        if (info.READY) {
        	
    		if (info.LOCK) {
    			
    			Vector2f portLoc = new Vector2f();
    			float shotAngle = 0f;
    			for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isDecorative()) {
    					portLoc = port.computePosition(ship);
    					shotAngle = port.getAngle() + ship.getFacing();
    				}
    			}
    			Vector2f baseVel = ship.getVelocity();
    			
    			Global.getSoundPlayer().playSound("hurricane_mirv_fire", 0.78f, 1.2f, portLoc, baseVel);
    			
    			CombatEntityAPI projC = engine.spawnProjectile(ship, null, "A_S-F_vandak",
    					portLoc, // pos
    					shotAngle, //angle
    					baseVel);
    			
    			engine.addPlugin(new ASF_vandakHomingScript( (MissileAPI)projC, info.TARGET));
    			info.TARGET = null;
    			

        		for (int i=0; i < 5; i++) {
        			Vector2f smokeVel = MathUtils.getPointOnCircumference(baseVel, MathUtils.getRandomNumberInRange(15f, 30f), shotAngle + MathUtils.getRandomNumberInRange(-6f, 6f));
        			
                	engine.addSmokeParticle(portLoc, smokeVel, MathUtils.getRandomNumberInRange(18f, 40f), 1.0f, 1.3f, new Color(120,105,100,200));
        		}
    			

        		for (int i=0; i < 4; i++) {
        			Vector2f smokeVel = MathUtils.getPointOnCircumference(baseVel, MathUtils.getRandomNumberInRange(5f, 10f), shotAngle + MathUtils.getRandomNumberInRange(-30f, 30f));
        			Vector2f smokeLoc = MathUtils.getRandomPointInCircle(portLoc, 14f);
                	engine.addSmokeParticle(smokeLoc, smokeVel, MathUtils.getRandomNumberInRange(18f, 40f), 1.0f, 2.1f, new Color(120,105,100,200));
        		}
    			
    	        
        		info.READY = false;
        		info.LOCK = false;
        		info.TIMER = 0f;
        	}
        	
        	
        } else {
        	info.TIMER += amount;
        	if (info.TIMER >= rechargeTime) {
        		info.READY = true;
        	}
        }
        
		engine.getCustomData().put("ASF_ARTILLERY_DATA_KEY" + ship.getId(), info);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		// this tactical artillery system is designed as extendable by other mods!
		// they just need have a ship with the base hullmod, and an individual "weapon" hullmod that follows the same mechanical setup as this one.
		
		LabelAPI label = tooltip.addPara("An integrated Tactical Artillery weapon.", opad);
		
		label = tooltip.addPara("Fires an large guided missile that deals %s damage.", opad, h, (int)damage + " Fragmentation");
		label.setHighlight((int)damage + " Fragmentation");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("On impact the missile will additionally release %s clusters that each deal %s damage.", pad, h, "10", (int)subDamage + " High Explosive");
		label.setHighlight("10", (int)subDamage + " High Explosive");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("The missile has %s hitpoints.", opad, h, (int)health + "");
		label.setHighlight((int)health + "");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("This weapon takes %s seconds to reload after firing.", opad, h, "" + (int)rechargeTime);
		label.setHighlight("" + (int)rechargeTime);
		label.setHighlightColors(h);
		
	}
	
	@Override
    public int getDisplaySortOrder() {
        return 22213;
    }
	
}
