package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_rinkaHullmod_p extends BaseHullMod {

	public static final float RECOIL_MALUS = 15f;
	
	public static final float ROF_BONUS = 25f;
	public static final float FLUX_BONUS = 15f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		// lol, lmao.
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		MutableShipStatsAPI stats = ship.getMutableStats();
		
		stats.getBallisticRoFMult().modifyPercent(spec.getId(), ROF_BONUS);
		stats.getBallisticAmmoRegenMult().modifyPercent(spec.getId(), ROF_BONUS);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(spec.getId(), -FLUX_BONUS);
			
		stats.getMaxRecoilMult().modifyMult(spec.getId(), 1f + (0.01f * RECOIL_MALUS));
		stats.getRecoilPerShotMult().modifyMult(spec.getId(), 1f + (0.01f * RECOIL_MALUS));
		stats.getRecoilDecayMult().modifyMult(spec.getId(), 1f + (0.01f * RECOIL_MALUS));
		
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
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("What was once an Advanced array of hardware to enhance weapon performance has undergone pirate modifications and no longer operates to the original specfications, but the remaining functional features still provide the following effects:", pad);
		
		label = tooltip.addPara("Increases ballistic weapon rate of fire by %s.", opad, h, "" + (int)ROF_BONUS + "%");
		label.setHighlight("" + (int)ROF_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Reduces the flux cost of ballistic weapons by %s.", pad, h, "" + (int)FLUX_BONUS + "%");
		label.setHighlight("" + (int)FLUX_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Increases weapon recoil by %s.", pad, bad, "" + (int)RECOIL_MALUS + "%");
		label.setHighlight("" + (int)RECOIL_MALUS + "%");
		label.setHighlightColors(bad);
		
	}
}
