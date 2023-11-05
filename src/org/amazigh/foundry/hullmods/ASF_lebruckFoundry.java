package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ASF_lebruckFoundry extends BaseHullMod {

	public static final float AMMO_BONUS = 100f;
	public static final float RATE_BONUS = 30f;
	public static final float REGEN_BONUS = 45f;
	public static final float HEALTH_BONUS = 25f;
	
	private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
	
	public static final float ZWEIG_TIME = 6f;
	public static  int MISSILE_CAP = 12;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getMissileRoFMult().modifyPercent(id, RATE_BONUS);
		stats.getMissileAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
		stats.getMissileHealthBonus().modifyPercent(id, HEALTH_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("LEBRUCK_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
		MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();		
		
		if (ship.getVariant().getHullMods().contains("missleracks")) {
			MISSILE_CAP = 18;
		}
		
		// below here is the auto-firing missile power fantasy (done via hullmod mainly to avoid autofire AI having a skill issue and turning the weapons off, but also because i thought it'd be cool)
		
		interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
        if (interval.intervalElapsed()) {
        	
        	float zweigMult = 0f;
        	if (ship.getFluxTracker().isVenting() || ship.getSystem().isActive()) {
        		zweigMult = 2.5f * interval.getIntervalDuration(); // missile generation rate is boosted while venting / system is active :)
        	} else {
        		zweigMult = 1f * interval.getIntervalDuration();
        	}
        	info.TIMER += zweigMult;
        	
	        if (info.TIMER >= ZWEIG_TIME) {
	        	if (info.MISSILES < MISSILE_CAP) {
	            	info.MISSILES++;
	            	info.TIMER -= ZWEIG_TIME;
	        	}
	        }
	        
	        if (info.MISSILES > 0) {
	        	if (validTarget(ship)) {
		        	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
		        		if (weapon.isSystemSlot()) {
		        			
		        			float randomArc = MathUtils.getRandomNumberInRange(-15f, 15f);
		        			
		        			Global.getCombatEngine().spawnProjectile(ship,
		        					null,
		        					"A_S-F_zweig_srm",
		        					weapon.computePosition(ship),
		        					weapon.getAngle() + ship.getFacing() + randomArc,
		        					ship.getVelocity());
		        			Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
		        			
		        			float randomSize1 = MathUtils.getRandomNumberInRange(15f, 20f);
		        			float randomSize2 = MathUtils.getRandomNumberInRange(20f, 25f);
		        			engine.addSwirlyNebulaParticle(weapon.computePosition(ship),
		        					ship.getVelocity(),
		        					randomSize1, //size
		        					2.0f, //end mult
		        					0.5f, //ramp fraction
		        					0.35f, //full bright fraction
		        					0.9f, //duration
		        					new Color(115,105,100,70),
		        					true);
		        			engine.addNebulaParticle(weapon.computePosition(ship),
		        					ship.getVelocity(),
		        					randomSize2, //size
		        					2.2f, //end mult
		        					0.5f, //ramp fraction
		        					0.5f, //full bright fraction
		        					1.1f, //duration
		        					new Color(115,105,100,95),
		        					true);
		        		}
		        	}
				info.MISSILES--;
	        	}
	        }
        }
        
        if (ship == Global.getCombatEngine().getPlayerShip()) {
        	String reloadInfo = "";
        	if (info.MISSILES == MISSILE_CAP) {
        		reloadInfo = "Fully Loaded";
        	} else {
        		float zweigPercent = 100f * (info.TIMER / ZWEIG_TIME);
        		reloadInfo = "Reload Progress: " + (int) zweigPercent + "%";
        	}
			Global.getCombatEngine().maintainStatusForPlayerShip("LEBRUCKAMMO", "graphics/icons/hullsys/missile_racks.png",  (info.MISSILES * 2) + " Zweig SRMs Stored", reloadInfo, false);
		}
        
        Global.getCombatEngine().getCustomData().put("LEBRUCK_DATA_KEY" + ship.getId(), info);
	}
	
    private boolean validTarget(ShipAPI ship) {
        Vector2f curr = ship.getLocation();
        boolean target = false;

    	List<CombatEntityAPI> consider = CombatUtils.getEntitiesWithinRange(curr, 800f);
    	for (CombatEntityAPI test : consider) {
    		if (test instanceof ShipAPI) {
    			ShipAPI other = (ShipAPI) test;
    			if (other.getOwner() != ship.getOwner()) {
    				if (other.getOwner() != 100) {
    					target = true;
    				}
    			}
    		}
    	}
    	if (ship.getFluxTracker().isOverloadedOrVenting() || ship.getSystem().isActive()) {
    		target = false; // so missiles don't fire while venting / overloaded / system is active
    	}
        return target;
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
		Color drk = Misc.getDarkHighlightColor();
		
		//Unused generator capacity is re-routed to this specialised missile autoforge, offering the maximum output at 0 flux and scaling down to the minimum output at 80% or higher flux.
		// most of the below scaled with flux levels at one point, but uh, it was unecessary complication tbqh?
		
		//LabelAPI label = tooltip.addPara("Unused generator capacity is re-routed to this specialised missile autoforge.", opad);
		// label = tooltip.addPara("Applies the Following Fixed Bonuses:", pad);
		
		LabelAPI label = tooltip.addPara("Missile Weapon ammo capacity increased by %s.", pad, h, "" + (int)AMMO_BONUS + "%");
		label.setHighlight("" + (int)AMMO_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Missile Weapon Rate of Fire increased by %s.", pad, h, "" + (int)RATE_BONUS + "%");
		label.setHighlight("" + (int)RATE_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Missile Weapon ammo regeneration increased by %s.", pad, h, "" + (int)REGEN_BONUS + "%");
		label.setHighlight("" + (int)REGEN_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Missile hitpoints increased by %s.", pad, h, "" + (int)HEALTH_BONUS + "%");
		label.setHighlight("" + (int)HEALTH_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("The internal autofoundry fabricates a pair of enhanced Swarmer SRMs every %s seconds. ", opad, h, "" + (int)ZWEIG_TIME + "");
		label.setHighlight("" + (int)ZWEIG_TIME + "");
		label.setHighlightColors(h);
		label = tooltip.addPara("Up to %s missiles can be stored, missiles will automatically launch if there is an enemy within %s range", pad, h, "24", "800");
		label.setHighlight("24", "800");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("%s %s %s %s", opad, drk, "Installation of", "Expanded Missile Racks", "Increases maximium stored missile Count to", "36");
		label.setHighlight("Installation of", "Expanded Missile Racks", "Increases maximium stored missile Count to", "36");
		label.setHighlightColors(drk, h, drk, h);
	}

    private class ShipSpecificData {
        private int MISSILES = 12;
        private float TIMER = 0f;
    }

}
