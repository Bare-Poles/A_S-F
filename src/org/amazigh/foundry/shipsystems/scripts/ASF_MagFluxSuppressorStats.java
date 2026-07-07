package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_MagFluxSuppressorStats extends BaseShipSystemScript {

	private CombatEngineAPI engine;
	
	private float pulseTimer = 0f;
	private float fxAngle = MathUtils.getRandomNumberInRange(0f, 360f);
	private float particleTimer = 0f;
	
	public static float SPEED_PENALTY = 0.2f;
	public static float FLUX_PENALTY = 0.1f;
	
	public static Color JITTER_COLOR = new Color(150,70,135,79);
	
	private static final float RANGE = 1500f;
	
	private IntervalUtil visInterval = new IntervalUtil(0.2f, 0.3f);
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		 if (engine != Global.getCombatEngine()) {
			 engine = Global.getCombatEngine();
		 }
		 
		 ShipAPI ship = (ShipAPI)stats.getEntity();
		 float range = getMaxRange(ship);
		 float timer = engine.getElapsedInLastFrame();
		 
		 if (effectLevel <= 0.01f) {
	        	return;
	        } else {
	        	
	        	visInterval.advance(timer);
	        	
	        	for (ShipAPI target_ship : engine.getShips()) {
	        			// check if the ship is a valid target
	                if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
	                    continue;
	                }
	                
	                	// if the target is in range, apply debuffs, otherwise clear debuffs
	                
	                float dist = MathUtils.getDistance(ship, target_ship);
	                if (dist <= range){
	                	target_ship.getMutableStats().getMaxSpeed().modifyMult(id + ship.getId(), 1f - (effectLevel * SPEED_PENALTY));
	                	target_ship.getMutableStats().getMaxTurnRate().modifyMult(id + ship.getId(), 1f - (effectLevel * SPEED_PENALTY * 2f));
	                	target_ship.getMutableStats().getAcceleration().modifyMult(id + ship.getId(), 1f - (effectLevel * SPEED_PENALTY * 2f));
	                	target_ship.getMutableStats().getTurnAcceleration().modifyMult(id + ship.getId(), 1f - (effectLevel * SPEED_PENALTY * 2f));
	                	target_ship.getMutableStats().getFluxDissipation().modifyMult(id + ship.getId(), 1f - (effectLevel * FLUX_PENALTY));
	                	
	                	// Glow!
            			float glowLength = target_ship.getCollisionRadius() * effectLevel * 2.8f;
            			
            			int alpha = (int) ((85 * effectLevel));  // 20 min value, then scales up to 105 at max effectLevel
            			if (dist > (0.5f * range)) {
            				alpha *= 1f - ((dist - (0.5f * range)) / (0.5f * range)); // lowering glow alpha if past 50% of range!
            			}
            	    	double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
            			alpha = (int) Math.ceil(alpha / timeMult);
            	    	alpha += 20f;
            	    	alpha = Math.min(alpha, 255);
            	    	int alpha2 = (int) (alpha * 0.6f); 
            	    	
            			Color glowColor1 = new Color(4, 108, 140,alpha);
            			Color glowColor2 = new Color(4, 108, 140,alpha2);
            			
            			SpriteAPI Glow1 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
            			SpriteAPI Glow2 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
            	    	Vector2f glowSize1 = new Vector2f(glowLength, glowLength);
            	    	Vector2f glowSize2 = new Vector2f(glowLength * 0.9f, glowLength * 0.9f);
            	    	
            	    	MagicRender.singleframe(Glow1, target_ship.getLocation(), glowSize1, ship.getFacing(), glowColor1, true, CombatEngineLayers.BELOW_SHIPS_LAYER);
            	    	MagicRender.singleframe(Glow2, target_ship.getLocation(), glowSize2, ship.getFacing(), glowColor2, true, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
            	    	// Glow!
	                	
	                	if (visInterval.intervalElapsed()) {
	                		ASF_RadialEmitter emitterPulse = new ASF_RadialEmitter((CombatEntityAPI) target_ship);
	    	    			emitterPulse.location(target_ship.getLocation());
	    	    			emitterPulse.life(0.47f, 0.6f);
	    	    			emitterPulse.size(3.5f, 7f);
	    	    			emitterPulse.velocity(20f, 40f);
	    	    			emitterPulse.distance(target_ship.getCollisionRadius() * 0.2f, target_ship.getCollisionRadius());
	    	    			emitterPulse.color(150,70,135,200);
	    	    			emitterPulse.velDistLinkage(false);
	    	    			emitterPulse.angleSplit(true);
	    	    			emitterPulse.burst((int)(target_ship.getCollisionRadius() * 0.1f));
	                	}
	                	
	                	target_ship.setJitterUnder(id + ship.getId(), JITTER_COLOR, Math.min(effectLevel, 0.8f), 3, 2f, 8f);
	                	
						if (target_ship == Global.getCombatEngine().getPlayerShip()) { 
							Global.getCombatEngine().maintainStatusForPlayerShip("ASF_MAG_FLUX_SUPP", 
									ship.getSystem().getSpecAPI().getIconSpriteName(),
									ship.getSystem().getDisplayName(), 
									"Speed and flux dissipation reduced", true);
						}
	                	
	                } else {
	                	target_ship.getMutableStats().getMaxSpeed().unmodify(id + ship.getId());
	                	target_ship.getMutableStats().getMaxTurnRate().unmodify(id + ship.getId());
	                	target_ship.getMutableStats().getAcceleration().unmodify(id + ship.getId());
	                	target_ship.getMutableStats().getTurnAcceleration().unmodify(id + ship.getId());
	                	target_ship.getMutableStats().getFluxDissipation().unmodify(id + ship.getId());
	                	
	                	target_ship.setJitter(id, null, 0f, 0, 0f, 0f);
	                }
	        	}
	        	
	        	
	        	// main glow and "ring pulses"
	        	SpriteAPI glowRing1 = Global.getSettings().getSprite("fx", "A_S-F_glow_ring");
	        	SpriteAPI glowRing2 = Global.getSettings().getSprite("fx", "A_S-F_glow_ring");
	        	SpriteAPI glowRing3 = Global.getSettings().getSprite("fx", "A_S-F_glow_ring_rough");
	        	SpriteAPI waveRing1 = Global.getSettings().getSprite("fx", "A_S-F_range_ring");
	        	SpriteAPI waveRing2 = Global.getSettings().getSprite("fx", "A_S-F_range_ring");
	        	
	           	int glowAlpha = (int) (effectLevel * 89f);
	        	double alphaTemp1 = glowAlpha;
	    		double timeMult = (double) stats.getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
	    		glowAlpha = Math.max(1, Math.min(255, (int) Math.ceil(alphaTemp1 / timeMult)));
	    		
	        	 // this makes the pulse wave fade in/out
	        	int waveAlpha = (int) (effectLevel * 69f);
	        	if (pulseTimer >= 1f) {
	        		waveAlpha *= (1f - (pulseTimer - 1f));
	        	} else {
	        		waveAlpha *= pulseTimer;
	        	}
	        	
	        	float roughMod = (float) (0.2f + (Math.sqrt(pulseTimer) * 0.8f)); // roughMod scales in curves, making "up" brighter and "down" dimmer
	        	if (pulseTimer > 1f) {
	        		roughMod = (float) (0.1f + (Math.pow((2f - pulseTimer), 2) * 0.9f));
	        	}
	        	fxAngle += (timer * MathUtils.getRandomNumberInRange(25f, 35f) * (1.2f - roughMod)); // "rough" fx rotates at 5-7deg/sec, but is up to ~5x faster when faded out
	        		// 0.2 to 1x mult scaling on roughMod (with it being 0.2 to 1.0)
	        		// also a 0.1 to 1.0 mult in the "down" state, idk complex!
	        	
	            Color glowColor = new Color(120, 50, 140, glowAlpha);
	            Color glowColor2 = new Color(4, 108, 140, (int) (glowAlpha * roughMod));
	            Color waveColor = new Color(4, 108, 140, waveAlpha);
	            
	        	Vector2f glowSize = new Vector2f(1895f, 1895f);
	        	MagicRender.singleframe(glowRing1, ship.getLocation(), glowSize, ship.getFacing() - (pulseTimer * 5f), glowColor, false);
	        	MagicRender.singleframe(glowRing2, ship.getLocation(), glowSize, ship.getFacing() + (pulseTimer * 5f), glowColor, false);
	        	MagicRender.singleframe(glowRing3, ship.getLocation(), glowSize, fxAngle, glowColor2, false);
	        	
	        	Vector2f waveSize1 = new Vector2f(getMaxRange(ship) * (2f - pulseTimer), getMaxRange(ship) * (2f - pulseTimer));
	        	Vector2f waveSize2 = new Vector2f((getMaxRange(ship) * (2f - pulseTimer)) +2f, (getMaxRange(ship) * (2f - pulseTimer)) +2f );
	        	MagicRender.singleframe(waveRing1, ship.getLocation(), waveSize1, ship.getFacing() - (pulseTimer * 5f), waveColor, false);
	        	MagicRender.singleframe(waveRing2, ship.getLocation(), waveSize2, ship.getFacing() + (pulseTimer * 5f), waveColor, false);
	            
	        	pulseTimer += timer;
	        	if (pulseTimer >= 2f) {
	        		pulseTimer = 0f;
	        	}
	        	
	        	particleTimer -= timer;
	    		while (particleTimer <= 0f) {
	    			particleTimer += 0.34f;
	    			
	    			ASF_RadialEmitter emitterPulse = new ASF_RadialEmitter((CombatEntityAPI) ship);
	    			emitterPulse.location(ship.getLocation());
	    			emitterPulse.life(0.58f, 0.69f);
	    			emitterPulse.size(4.5f, 10f);
	    			emitterPulse.velocity(30f, 60f);
	    			emitterPulse.distance(100f, getMaxRange(ship) - 120f);
	    			emitterPulse.color(150,70,135,255);
	    			emitterPulse.velDistLinkage(false);
	    			emitterPulse.angleSplit(true);
	    			emitterPulse.burst(30);
	    		}
	        	
			}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		pulseTimer = 0f;
		fxAngle = MathUtils.getRandomNumberInRange(0, 360f);
		
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
        for (ShipAPI target_ship : engine.getShips()) {
            if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
                continue;
            }
        	target_ship.getMutableStats().getMaxSpeed().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getMaxTurnRate().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getAcceleration().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getTurnAcceleration().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getFluxDissipation().unmodify(id + ship.getId());
        	
        	target_ship.setJitter(id + ship.getId(), null, 0f, 0, 0f, 0f);
        }
	}
	
	public static float getMaxRange(ShipAPI ship) {
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
		//return RANGE;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel > 0) {
			if (index == 0) {
				return new StatusData("Suppressing nearby hostiles", false);
			}
		}
		return null;
	}
}
