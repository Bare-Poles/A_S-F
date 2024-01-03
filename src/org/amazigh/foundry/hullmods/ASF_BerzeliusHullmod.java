package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.Random;

import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_BerzeliusHullmod extends BaseHullMod {
	
	public static final float SUPPLY_COST = 80f;
	
	public static final float OVERLOAD_MULT = 0.9f;
	public static final float REVENGE_SHOT = 25f; // [CUSTOM CARTRIDGE: REVENGE SHOT]
		//these two are a bit weaker than on normal warburn, because the ship is good enough already
	
	public static final float RoF_PENALTY = 0.15f; // because the ship is too strong, give it an inherent reduction to RoF to balans it out
	
	public static final float BROAD_RADAR = 50f; // [CUSTOM CARTRIDGE: BROAD RADAR]
	
	public static final float REPAIR_MULT = 240f; // 1800 armour - we repair *twice*, but at a bit under half the power it ""should"" have (which would be 550ish) 
	public static final float BLAST_SIZE = 666f; // radius 226
	
	public static final float SHUNT = 0.05f;
	
    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);
    private final IntervalUtil interval_2 = new IntervalUtil(0.033f, 0.033f);
    private final Random rand = new Random();
    private static final Color SPARK_COLOR = new Color(130, 40, 140);
    
    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;
        return cellLoc;
    }
        
	public static final float TIMESCALE = 4f;
	public static final float TIME_SPEED = 0.66f;
	public static final float TIME_RoF = 0.6f;
	
	public Color ENGINE_COLOR = new Color(90,255,165,55);
    private static final Color COLOR_EX = new Color(90,255,165,155);
		
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyPercent(id, SUPPLY_COST);
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
        
		stats.getSightRadiusMod().modifyPercent(id, BROAD_RADAR);
        
		stats.getHardFluxDissipationFraction().modifyFlat(id, SHUNT); // 5% flux shunt ;)
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("WARBURN_B_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == engine.getPlayerShip();
		}
		
		// Stat setup section
		float DAMAGE = 1 - ship.getHullLevel();
		if (DAMAGE > 0.8f) {
			DAMAGE = 0.8f;
		}
		float HULL_RATIO = DAMAGE / 0.8f;
		// Stat setup section
		
		
		// Revenge Shot
		stats.getBallisticWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		stats.getEnergyWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		stats.getMissileWeaponFluxCostMod().modifyPercent(spec.getId(), -(REVENGE_SHOT * HULL_RATIO));
		// Revenge Shot
		
		// RoF Penalty
		stats.getBallisticRoFMult().modifyMult(spec.getId(), 1f - RoF_PENALTY);
		// RoF Penalty
		
		
		// Vent Repair Section
		if (ship.getFluxTracker().isVenting()) {
			interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
	        if (interval.intervalElapsed()) {
	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            
	            for (int i = 0; i < 2; i++) {
	            	int x = rand.nextInt(armorGrid.getGrid().length);
		            int y = rand.nextInt(armorGrid.getGrid()[0].length);
		            float newArmor = armorGrid.getArmorValue(x, y);
		            float cellSize = armorGrid.getCellSize();

		            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
		                newArmor += REPAIR_MULT * interval.getIntervalDuration() * (2f + ship.getFluxLevel());
		                
		                boolean REPAIR = false;
		                if (armorGrid.getArmorValue(x, y) < armorGrid.getMaxArmorInCell()) {
		                	REPAIR = true;
		                }
		                armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));
		                
		                Vector2f cellLoc = getCellLocation(ship, x, y);
		                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
		                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
		                engine.addHitParticle(cellLoc,
		                		ship.getVelocity(),
		                		(8f * (float) Math.random()) + 6f,
		                		0.9f,
		                		0.36f,
		                        SPARK_COLOR);
		                if (REPAIR) {
		                	engine.spawnExplosion(cellLoc, ship.getVelocity(), SPARK_COLOR, 22f, 0.21f);
		                }
		            }
	            }
	            
	        }
		}
		
		if (ship.getFluxTracker().isVenting() || info.ACTIVE) { // [CUSTOM CARTRIDGE: STUN REGAIN]
            
            if (DefenseUtils.hasArmorDamage(ship)) {

	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            
	        	final float[][] grid = armorGrid.getGrid();
		        final float max = armorGrid.getMaxArmorInCell();
		        
		        float repairAmount = 5f * amount;
		        	// so you get a flat 5/sec armour repair in all cells, during the "hull charge"
		        
		        if (info.ACTIVE) {
		        	repairAmount *= 0.5f;
		        	// halving repair during "hull charge" (balans)
		        }
		        
				for (int x = 0; x < grid.length; x++) {
		            for (int y = 0; y < grid[0].length; y++) {
		                if (grid[x][y] < max) {
		                    float regen = grid[x][y] + repairAmount;
		                    armorGrid.setArmorValue(x, y, regen);
		                }
		            }
		        }
				
        	}
            
            ship.syncWithArmorGridState(); // to remove gross damage when repairing, thanks ruddygreat
            ship.syncWeaponDecalsWithArmorDamage();
		}
		
		// 
		
		if (ship.getFluxTracker().isVenting()) {
			ship.setHitpoints(Math.min(ship.getHitpoints() + (100f * amount), ship.getMaxHitpoints())); // [CUSTOM CARTRIDGE: STUN REGAIN]
		}
		
		// Vent Repair Section
		
		
		// Fancy Damage Buff Section
		if (!info.SET) {
			info.THRESHOLD = ship.getHullLevel();
			info.SET = true;
		}
		
		if (ship.getHullLevel() < info.THRESHOLD) {
			info.TIMER += ((info.THRESHOLD - ship.getHullLevel()) * 100f);
			info.THRESHOLD = ship.getHullLevel();
		}
		
		if (info.TIMER > 8f) {
			info.ARMED = true;
		}
		
		if (info.TIMER > 0f && info.ARMED) {
			if (!info.ACTIVE) {
				// INITIAL EFFECT
				info.ACTIVE = true;
				engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), COLOR_EX, BLAST_SIZE, 0.25f);	
				Global.getSoundPlayer().playSound("system_temporalshell", 1f, 0.9f, ship.getLocation(), ship.getVelocity());
				// INITIAL EFFECT
			}
			
			interval_2.advance(engine.getElapsedInLastFrame());
			if (interval_2.intervalElapsed()) {
				info.TIMER -= interval_2.getIntervalDuration();
			}
			//TIMER -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();
			
			// BUFF
			float shipTimeMult = 1f + ((TIMESCALE - 1f) * (Math.min(info.TIMER/3f, 1f)));
			if (player) {
				stats.getTimeMult().modifyMult(spec.getId(), shipTimeMult);
				engine.getTimeMult().modifyMult(spec.getId(), 1f / shipTimeMult);
			} else {
				stats.getTimeMult().modifyMult(spec.getId(), shipTimeMult);
				engine.getTimeMult().unmodify(spec.getId());
			}
			stats.getMaxSpeed().modifyMult(spec.getId(), 1f - (TIME_SPEED * (Math.min(info.TIMER/3f, 1f))));
			stats.getBallisticRoFMult().modifyMult(spec.getId(), 1f - (TIME_RoF * (Math.min(info.TIMER/3f, 1f))));
			stats.getMissileRoFMult().modifyMult(spec.getId(), 1f - (TIME_RoF * (Math.min(info.TIMER/3f, 1f))));
			
			float hullBonus = Math.min(0.8f, 1 - ship.getHullLevel()) / 0.8f;
			float DAM_RES = 0.1f + (0.4f * hullBonus);
			stats.getHullDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			stats.getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			stats.getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - DAM_RES);
			// BUFF
			
			// Jitter
			float ALPHA_1 = (Math.min(info.TIMER, 3f) * 1.75f) + 15f;
			float ALPHA_2 = (Math.min(info.TIMER, 3f) * 2.6f) + 20f;
			Color JITTER_COLOR = new Color(90,255,165,(int)ALPHA_1);
			Color JITTER_UNDER_COLOR = new Color(90,255,165,(int)ALPHA_2);
			
			float jitterRangeBonus_1 = (Math.min(info.TIMER, 3f)) * 1.5f;
			float jitterRangeBonus_2 = (Math.min(info.TIMER, 3f)) * 3f;
			
			float jitterLevel = ( (float) Math.sqrt((Math.min(info.TIMER/3f, 1f))) * 0.35f ) + 0.65f;
			
			ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 10f + jitterRangeBonus_1);
			ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 15f + jitterRangeBonus_2);
			// Jitter

			// ENGINE FX
			ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(15,0,30,40), Math.min(info.TIMER/3f, 1f), 0.6f);
			ship.getEngineController().extendFlame(this, 0.2f, 0.2f, 0.2f);
			// ENGINE FX
			
		} else if (info.ACTIVE) {
			info.ACTIVE = false;
			info.SET = false;
			info.ARMED = false;
			stats.getTimeMult().unmodify(spec.getId());
			engine.getTimeMult().unmodify(spec.getId());
			stats.getMaxSpeed().unmodify(spec.getId());
			stats.getBallisticRoFMult().unmodify(spec.getId());
			stats.getMissileRoFMult().unmodify(spec.getId());
		}
		
		if (ship.getHullLevel() > info.THRESHOLD) { // here we check if the ship has regenerated hull and adjust our threshold appropriately
			info.THRESHOLD = ship.getHullLevel();
		}		
		// Fancy Damage Buff Section
		
		engine.getCustomData().put("WARBURN_B_DATA_KEY" + ship.getId(), info);
	}
		// So what this hullmod does is as follows:
		// - Increases monthly supply cost by 80%
		// - Reduces overload duration by 15%
		// - grants a 5% hardflux dissipation bonus
		// - Reduces all weapons flux cost as the ship takes hull damage
		// - Repairs armour while venting, the amount repaired scales up based on current flux level
		// - Also repairs hull while venting, albeit very slowly.
		// -- On taking Hull damage:
		// --- Increment a "timer" gaining 10 seconds of time for each 10% hull damage taken.
		// --- If timer is over 8, start decaying the timer and gain a timeflow + damage resistance buff until the timer runs out.
		// --- Speed and RoF are reduced while this buff is active, to stop you becoming a psycho demon, and to make it more of a "free vent" than a pure buff.
		// ---- also applies a slow flat armour repair to all cells during this "boost"
	
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
		
		LabelAPI label = tooltip.addPara("The internal systems of this ship have been constructed using unknown advanced methods that only bear a passing resemblance to Domain technology.", opad);
		label = tooltip.addPara("What limited documentation could be found on internal systems classifies this special configuration as %s", pad, h, "\"Warburn Systems\"");
		label.setHighlight("\"Warburn Systems\"");
		label.setHighlightColors(h);
		label = tooltip.addPara("The one confirmed feature of these systems is that inner armour layers feature integrated repair nanites that will activate and repair a small amount of damaged armour while venting.", pad);
		
		label = tooltip.addPara("Other more esoteric behaviours are suggested in the limited recovered documentation, but limited initial testing has been unable to fully determine their exact nature.", opad);
		label = tooltip.addPara("Due to only possessing partial documentation and understanding of the internal systems, the maintenance cost for this ship is increased by %s.", pad, bad, "" + (int)SUPPLY_COST + "%");
		label.setHighlight("" + (int)SUPPLY_COST + "%");
		label.setHighlightColors(bad);		
	}

    private class ShipSpecificData {
        private boolean SET = false;
        private float THRESHOLD = 1.0f;
        private float TIMER = 0f;
        private boolean ARMED = false;
        private boolean ACTIVE = false;
    }
}
