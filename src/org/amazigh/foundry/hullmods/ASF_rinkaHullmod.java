package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_rinkaHullmod extends BaseHullMod {

	public static final float RECOIL_BONUS = 25f;
	public static final float VELOCITY_BONUS = 20f;
	
	public static final float ROF_BONUS = 60f;
	public static final float FLUX_BONUS = 40f;
	
	public static final float DAMAGE_BONUS = 30f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		// lol, lmao.
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("RINKA_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();	
		
		if (ship.getSystem().isActive()) {
			info.BOOST_MULT = Math.min(6f, info.BOOST_MULT + (amount*3f)); // capping timer at 6s
		} else {
			info.BOOST_MULT = Math.max(0f, info.BOOST_MULT - amount);
		}
		
		float boost_value = (Math.min(info.BOOST_MULT * 0.4f, 1f));
		
		stats.getBallisticRoFMult().modifyPercent(spec.getId(), ROF_BONUS * boost_value);
		stats.getBallisticAmmoRegenMult().modifyPercent(spec.getId(), ROF_BONUS * boost_value);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(spec.getId(), -(FLUX_BONUS * boost_value));
		stats.getEnergyWeaponDamageMult().modifyPercent(spec.getId(), DAMAGE_BONUS * boost_value);
		
		stats.getBallisticProjectileSpeedMult().modifyPercent(spec.getId(), VELOCITY_BONUS);
		stats.getEnergyProjectileSpeedMult().modifyPercent(spec.getId(), VELOCITY_BONUS);
		stats.getMaxRecoilMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilPerShotMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilDecayMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
		
		if (ship == Global.getCombatEngine().getPlayerShip()) {
        	Global.getCombatEngine().maintainStatusForPlayerShip("RINKABOOST", "graphics/icons/hullsys/ammo_feeder.png",  "Secondary Bonus Charge Level:", ((int)(boost_value * 100f)) + "%", false);
		}
		
        engine.getCustomData().put("RINKA_DATA_KEY" + ship.getId(), info);
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
		float bpad = 20f;
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("An Advanced array of hardware that enhances the performance of weapons has been installed on this vessel.", pad);
		label = tooltip.addPara("Provides the following effects passively:", pad);

		label = tooltip.addPara("Reduces weapon recoil by %s.", opad, h, "" + (int)RECOIL_BONUS + "%");
		label.setHighlight("" + (int)RECOIL_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Increases ballistic and energy projectile velocity by %s.", pad, h, "" + (int)VELOCITY_BONUS + "%");
		label.setHighlight("" + (int)VELOCITY_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Activating the ships %s system will generate charge for a secondary set of bonuses that lasts for up to %s:", bpad, h, "Quick Boost", "6 seconds");
		label.setHighlight("Quick Boost", "6 seconds");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("Increases ballistic weapon rate of fire and ammo regeneration rate by %s.", opad, h, "" + (int)ROF_BONUS + "%");
		label.setHighlight("" + (int)ROF_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Reduces the flux cost of ballistic weapons by %s.", pad, h, "" + (int)FLUX_BONUS + "%");
		label.setHighlight("" + (int)FLUX_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Increases energy weapon damage by %s.", pad, h, "" + (int)DAMAGE_BONUS + "%");
		label.setHighlight("" + (int)DAMAGE_BONUS + "%");
		label.setHighlightColors(h);
		
	}

    private class ShipSpecificData {
        private float BOOST_MULT = 0f;
    }
	
}
