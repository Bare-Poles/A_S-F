package org.amazigh.foundry.shipsystems.scripts;

import java.util.List;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_EwarStats extends BaseShipSystemScript {
	public static Object KEY_SHIP = new Object();
	public static Object KEY_TARGET = new Object();
	
	public static float RANGE_PENALTY = 0.1f;
	public static float RoF_PENALTY = 0.1f;
	public static float TURN_PENALTY = 0.3f;
	public static float PD_PENALTY = 0.2f;
	
	protected static float RANGE = 1500f;
	
	public static Color TEXT_COLOR = new Color(255,55,55,255);
	
	public static Color JITTER_COLOR = new Color(74,153,40,34);
	public static Color JITTER_UNDER_COLOR = new Color(101,212,55,79);

	
	public static class TargetData {
		public ShipAPI ship;
		public ShipAPI target;
		public EveryFrameCombatPlugin targetEffectPlugin;
		public float currRangeMult;
		public float currRoFMult;
		public float currTurnMult;
		public float currPDMult;
		public float elaspedAfterInState;
		public TargetData(ShipAPI ship, ShipAPI target) {
			this.ship = ship;
			this.target = target;
		}
	}
	
	
	public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		final String targetDataKey = ship.getId() + "_ewar_target_data";
		
		Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey); 
		if (state == State.IN && targetDataObj == null) {
			ShipAPI target = findTarget(ship);
			Global.getCombatEngine().getCustomData().put(targetDataKey, new TargetData(ship, target));
			if (target != null) {
				if (target.getFluxTracker().showFloaty() || 
						ship == Global.getCombatEngine().getPlayerShip() ||
						target == Global.getCombatEngine().getPlayerShip()) {
					target.getFluxTracker().showOverloadFloatyIfNeeded("EWAR Jamming!", TEXT_COLOR, 4f, true);
				}
			}
		} else if (state == State.IDLE && targetDataObj != null) {
			Global.getCombatEngine().getCustomData().remove(targetDataKey);
			((TargetData)targetDataObj).currRangeMult = 1f;
			((TargetData)targetDataObj).currRoFMult = 1f;
			((TargetData)targetDataObj).currTurnMult = 1f;
			((TargetData)targetDataObj).currPDMult = 1f;
			targetDataObj = null;
		}
		if (targetDataObj == null || ((TargetData) targetDataObj).target == null) return;
		
		final TargetData targetData = (TargetData) targetDataObj;
		targetData.currRangeMult = 1f - (RANGE_PENALTY * effectLevel);
		targetData.currRoFMult = 1f - (RoF_PENALTY * effectLevel);
		targetData.currTurnMult = 1f - (TURN_PENALTY * effectLevel);
		targetData.currPDMult = 1f - (PD_PENALTY * effectLevel);
		
		if (targetData.targetEffectPlugin == null) {
			targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
				@Override
				public void advance(float amount, List<InputEventAPI> events) {
					if (Global.getCombatEngine().isPaused()) return;
					if (targetData.target == Global.getCombatEngine().getPlayerShip()) { 
						Global.getCombatEngine().maintainStatusForPlayerShip(KEY_TARGET, 
								targetData.ship.getSystem().getSpecAPI().getIconSpriteName(),
								targetData.ship.getSystem().getDisplayName(), 
								"System performance degraded", true);
					}
					
					if (targetData.currRangeMult >= 1f || !targetData.ship.isAlive()) {
						targetData.target.getMutableStats().getBallisticWeaponRangeBonus().unmodify(id);
						targetData.target.getMutableStats().getEnergyWeaponRangeBonus().unmodify(id);
						
						targetData.target.getMutableStats().getBallisticRoFMult().unmodify(id);
						targetData.target.getMutableStats().getEnergyRoFMult().unmodify(id);
						targetData.target.getMutableStats().getMissileRoFMult().unmodify(id);
						
						targetData.target.getMutableStats().getWeaponTurnRateBonus().unmodify(id);
						
						targetData.target.getMutableStats().getDamageToFighters().unmodify(id);
						targetData.target.getMutableStats().getDamageToMissiles().unmodify(id);
						
						Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
					} else {
						targetData.target.getMutableStats().getBallisticWeaponRangeBonus().modifyMult(id, targetData.currRangeMult);
						targetData.target.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(id, targetData.currRangeMult);
						
						targetData.target.getMutableStats().getBallisticRoFMult().modifyMult(id, targetData.currRoFMult);
						targetData.target.getMutableStats().getEnergyRoFMult().modifyMult(id, targetData.currRoFMult);
						targetData.target.getMutableStats().getMissileRoFMult().modifyMult(id, targetData.currRoFMult);
						
						targetData.target.getMutableStats().getWeaponTurnRateBonus().modifyMult(id, targetData.currTurnMult);
						
						targetData.target.getMutableStats().getDamageToFighters().modifyMult(id, targetData.currPDMult);
						targetData.target.getMutableStats().getDamageToMissiles().modifyMult(id, targetData.currPDMult);
					}
				}
			};
			Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
		}
		
		
		if (effectLevel > 0) {
			if (state != State.IN) {
				targetData.elaspedAfterInState += Global.getCombatEngine().getElapsedInLastFrame();
			}
			float shipJitterLevel = 0;
			if (state == State.IN) {
				shipJitterLevel = effectLevel;
			} else {
				float durOut = 0.5f;
				shipJitterLevel = Math.max(0, durOut - targetData.elaspedAfterInState) / durOut;
			}
			float targetJitterLevel = Math.min(0.8f, effectLevel);
			
			float maxRangeBonus = 50f;
			float jitterRangeBonus = shipJitterLevel * maxRangeBonus;
			
			if (shipJitterLevel > 0) {
				ship.setJitterUnder(KEY_SHIP, JITTER_UNDER_COLOR, shipJitterLevel, 9, 0f, 3f + jitterRangeBonus);
				ship.setJitter(KEY_SHIP, JITTER_COLOR, shipJitterLevel, 3, 0f, 0 + jitterRangeBonus * 1f);
			}
			
			if (targetJitterLevel > 0) {
				targetData.target.setJitterUnder(KEY_TARGET, JITTER_UNDER_COLOR, targetJitterLevel, 4, 0f, 15f);
				targetData.target.setJitter(KEY_TARGET, JITTER_COLOR, targetJitterLevel, 2, 0f, 4f);
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	

	protected ShipAPI findTarget(ShipAPI ship) {
		float range = getMaxRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();
		
		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)){
			target = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM);
			if (target != null && target.getOriginalOwner() == ship.getOriginalOwner()) target = null;
		}
		
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true);
				} else {
					Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
					if (test instanceof ShipAPI) {
						target = (ShipAPI) test;
						float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
						float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
						if (dist > range + radSum || target.isFighter()) target = null;
						if (target != null && target.getOriginalOwner() == ship.getOriginalOwner()) target = null;
					}
				}
			}
		}
		
		if (target != null && target.isFighter()) target = null;
		if (target == null) {
			target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true);
		}
		
		return target;
	}
	
	
	public static float getMaxRange(ShipAPI ship) {
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}

	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel > 0) {
			if (index == 0) {
				return new StatusData("EWAR Jamming - Target system performance degraded", false);
			}
		}
		return null;
	}


	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		//if (true) return true;
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
	}

}
