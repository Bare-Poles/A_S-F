package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_MaliceStormStats extends BaseShipSystemScript {

	private CombatEngineAPI engine;

	private static final float STORM_RANGE = 1200f;
	private static float SPD_MULT = 0.2f; // how much the target is to be slowed by
	
	private static float ARC_BLAST_SIZE = 45f;
	
	public static final float DAMAGE_REDUCTION = 0.5f;
	
	private IntervalUtil arcInterval1 = new IntervalUtil(0.2f,0.5f);
	private IntervalUtil arcInterval2 = new IntervalUtil(0.2f,0.5f);
	private IntervalUtil sparkInterval = new IntervalUtil(0.05f,0.05f);
	
	private IntervalUtil cloudInterval1 = new IntervalUtil(0.2f,0.3f);
	private IntervalUtil cloudInterval2 = new IntervalUtil(0.2f,0.3f);
	
	private boolean arcFired1 = false;
	private boolean arcFired2 = false;
	
	private static Map<HullSize, Float> arcRateMult = new HashMap<HullSize, Float>();
	static {
		arcRateMult.put(HullSize.FIGHTER, 0.15f);
		arcRateMult.put(HullSize.FRIGATE, 0.2f);
		arcRateMult.put(HullSize.DESTROYER, 0.25f);
		arcRateMult.put(HullSize.CRUISER, 0.35f);
		arcRateMult.put(HullSize.CAPITAL_SHIP, 0.45f);
		arcRateMult.put(HullSize.DEFAULT, 0.3f);
	}
	
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
		
		ShipAPI ship = (ShipAPI)stats.getEntity();
		float range = getMaxRange(ship);
		
		for (ShipAPI target_ship : engine.getShips()) {
			// check if the ship is a valid target
			if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
				continue;
			}
			
			// if the target ship is within range, slow it, otherwise clear slow
			if (MathUtils.isWithinRange(ship, target_ship, range)) {
				target_ship.getMutableStats().getMaxSpeed().modifyMult(id + ship.getId(), 1f - (effectLevel * SPD_MULT));
				target_ship.getMutableStats().getAcceleration().modifyMult(id + ship.getId(), 1f - (effectLevel * SPD_MULT));
				target_ship.getMutableStats().getDeceleration().modifyMult(id + ship.getId(), 1f - (effectLevel * SPD_MULT));	
			} else {
				target_ship.getMutableStats().getMaxSpeed().unmodify(id + ship.getId());
				target_ship.getMutableStats().getAcceleration().unmodify(id + ship.getId());
				target_ship.getMutableStats().getDeceleration().unmodify(id + ship.getId());
			}
		}
		
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
        
		
        // arc spawning
		
		float amount = engine.getElapsedInLastFrame();
		
		arcInterval1.advance(amount);
		if (arcInterval1.intervalElapsed()) {
			
			arcFired1 = false;
			
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
					continue;
				}
				
				// if the target ship is within range, do an arc
				if (MathUtils.isWithinRange(ship, target_ship, range)) {
					if (Math.random() < arcRateMult.get(target_ship.getHullSize())) {
						arcFired1 = true;
						
		                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);
		                
		                target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_ship,
		                        DamageType.ENERGY,
		                        120f,
		                        300f,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        11f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));
		                
		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 5f),
				        		ARC_BLAST_SIZE * 0.6f,
								1.6f,
								0.5f,
								0.7f,
								0.25f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 10f),
				        		ARC_BLAST_SIZE * 0.9f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.6f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 3; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 75f));
		    				Global.getCombatEngine().addSmoothParticle(loc,
		    						sparkVel,
		    						MathUtils.getRandomNumberInRange(4f, 9f), //size
		    						1.0f, //brightness
		    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
		    						new Color(255,52,84,255));
		            	}
		                
		                engine.removeEntity(dummy);
		                
					}
				}
			}
			
			for (MissileAPI target_missile : engine.getMissiles()) {
    			// check if the missile is a valid target
        		if (target_missile.getOwner() == ship.getOwner()) {
        			continue;
        		}

				// if the target missile is within range, do an arc
        		if (MathUtils.isWithinRange(ship, target_missile, range)) {
        			if (Math.random() < 0.1f) {
        				arcFired1 = true;
        				
        				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_missile,
		                        DamageType.ENERGY,
		                        120f,
		                        300f,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        10f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));
		                
		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.45f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 4f),
				        		ARC_BLAST_SIZE * 0.55f,
								1.6f,
								0.5f,
								0.7f,
								0.23f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 8f),
				        		ARC_BLAST_SIZE * 0.85f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.55f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 3; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 70f));
		    				Global.getCombatEngine().addSmoothParticle(loc,
		    						sparkVel,
		    						MathUtils.getRandomNumberInRange(4f, 8f), //size
		    						1.0f, //brightness
		    						MathUtils.getRandomNumberInRange(0.35f, 0.45f), //duration
		    						new Color(255,52,84,255));
		            	}
		                
		                engine.removeEntity(dummy);
        				
        			}
        		}
			}
			
			// if we didn't arc to anything, do a random visual arc
			if (!arcFired1) {
				
                Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), range * 0.8f));
                
                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);
                
                engine.spawnEmpArcVisual(loc, dummy,
                		MathUtils.getRandomPointOnCircumference(loc, MathUtils.getRandomNumberInRange(100f, 200f)),
                		dummy,
                		10f,
                        new Color(153,92,103,200),
						new Color(255,216,224,190));
                
                Global.getSoundPlayer().playSound("A_S-F_malice_arc_impact", 0.9f, 0.7f, loc, dummy.getVelocity());
                
                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
                
                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
		        		MathUtils.getRandomPointInCircle(null, 10f),
		        		ARC_BLAST_SIZE * 0.5f,
						1.6f,
						0.5f,
						0.7f,
						0.25f,
						new Color(255,216,224,95),
						false);
                
                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
		        		MathUtils.getRandomPointInCircle(null, 10f),
		        		ARC_BLAST_SIZE * 0.8f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.3f,
						0.6f,
						new Color(190,65,150,70),
						false);
                
                for (int i=0; i < 3; i++) {
            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 75f));
    				Global.getCombatEngine().addSmoothParticle(loc,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						1.0f, //brightness
    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
    						new Color(255,52,84,255));
            	}
                
                engine.removeEntity(dummy);
				
			}
		}
		
		arcInterval2.advance(amount);
		if (arcInterval2.intervalElapsed()) {
			
			arcFired2 = false;
			
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
					continue;
				}
				
				// if the target ship is within range, do an arc
				if (MathUtils.isWithinRange(ship, target_ship, range)) {
					if (Math.random() < arcRateMult.get(target_ship.getHullSize())) {
						arcFired2 = true;
						
		                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);

		                target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_ship,
		                        DamageType.ENERGY,
		                        120f,
		                        300f,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        11f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));
		                
		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 5f),
				        		ARC_BLAST_SIZE * 0.6f,
								1.6f,
								0.5f,
								0.7f,
								0.25f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 10f),
				        		ARC_BLAST_SIZE * 0.9f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.6f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 3; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 75f));
		    				Global.getCombatEngine().addSmoothParticle(loc,
		    						sparkVel,
		    						MathUtils.getRandomNumberInRange(4f, 9f), //size
		    						1.0f, //brightness
		    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
		    						new Color(255,52,84,255));
		            	}
		                
		                engine.removeEntity(dummy);
		                
					}
				}
			}
			
			for (MissileAPI target_missile : engine.getMissiles()) {
    			// check if the missile is a valid target
        		if (target_missile.getOwner() == ship.getOwner()) {
        			continue;
        		}

				// if the target missile is within range, do an arc
        		if (MathUtils.isWithinRange(ship, target_missile, range)) {
        			if (Math.random() < 0.1f) {
        				arcFired2 = true;
        				
        				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_missile,
		                        DamageType.ENERGY,
		                        120f,
		                        300f,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        10f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));
		                
		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.45f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 4f),
				        		ARC_BLAST_SIZE * 0.55f,
								1.6f,
								0.5f,
								0.7f,
								0.23f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 8f),
				        		ARC_BLAST_SIZE * 0.85f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.55f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 3; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 70f));
		    				Global.getCombatEngine().addSmoothParticle(loc,
		    						sparkVel,
		    						MathUtils.getRandomNumberInRange(4f, 8f), //size
		    						1.0f, //brightness
		    						MathUtils.getRandomNumberInRange(0.35f, 0.45f), //duration
		    						new Color(255,52,84,255));
		            	}
		                
		                engine.removeEntity(dummy);
        				
        			}
        		}
			}
			
			// if we didn't arc to anything, do a random visual arc
			if (!arcFired2) {
				
                Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), range * 0.8f));
                
                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, 0f, 0f);
                
                engine.spawnEmpArcVisual(loc, dummy,
                		MathUtils.getRandomPointOnCircumference(loc, MathUtils.getRandomNumberInRange(100f, 200f)),
                		dummy,
                		10f,
                        new Color(153,92,103,200),
						new Color(255,216,224,190));
                
                Global.getSoundPlayer().playSound("A_S-F_malice_arc_impact", 0.9f, 0.7f, loc, dummy.getVelocity());
                
                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
                
                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
		        		MathUtils.getRandomPointInCircle(null, 10f),
		        		ARC_BLAST_SIZE * 0.5f,
						1.6f,
						0.5f,
						0.7f,
						0.25f,
						new Color(255,216,224,95),
						false);
                
                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
		        		MathUtils.getRandomPointInCircle(null, 10f),
		        		ARC_BLAST_SIZE * 0.8f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.3f,
						0.6f,
						new Color(190,65,150,70),
						false);
                
                for (int i=0; i < 3; i++) {
            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 75f));
    				Global.getCombatEngine().addSmoothParticle(loc,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						1.0f, //brightness
    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
    						new Color(255,52,84,255));
            	}
                
                engine.removeEntity(dummy);
				
			}
		}
		
		
		// ship jitter
		float ALPHA = 20f + (40f * effectLevel);
		Color JITTER_UNDER_COLOR = new Color(128,26,42,(int)ALPHA);
		
		float jitterRangeBonus = 9f * (1f + effectLevel);
		float jitterLevel = (float) Math.sqrt(effectLevel);
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 18, 0f, 12f + jitterRangeBonus);
		
		
		// "local spark fx" + "radius marker"
		sparkInterval.advance(amount);
		if (sparkInterval.intervalElapsed()) {
			for (int i=0; i < 5; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(35f, 45f), angle);
				
				Vector2f sparkVelTemp = ship.getVelocity();
        		Vector2f sparkVel = MathUtils.getPointOnCircumference((Vector2f) sparkVelTemp.scale(0.5f), MathUtils.getRandomNumberInRange(25f, 35f), angle);
				Global.getCombatEngine().addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
						new Color(255,52,84,255));
        	}
			
			float offset = MathUtils.getRandomNumberInRange(0f, 10f);
			for (int i=0; i < 36; i++) {
				float angle = offset + (i * 10f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), range, angle + MathUtils.getRandomNumberInRange(-2f, 2f));
        		Vector2f sparkVel = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(-25f, -45f), angle);
				Global.getCombatEngine().addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(6f, 11f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.5f, 0.65f), //duration
						new Color(255,52,84,225));
			}
			
		}
		
		
        // general area visual stuff
		if (state != ShipSystemStatsScript.State.OUT) {
			// only spawn nebs when not in OUT, to prevent them "exceeding" sys duration *too* much
			
			// 1 - spawns generic clouds, and the "onship big nebs"
			// 2 - spawns generic clouds, and smaller "angry" clouds
			cloudInterval1.advance(amount);
			if (cloudInterval1.intervalElapsed()) {
				Vector2f cloudLoc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				
				for (int i=0; i < 3; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(110f, 140f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(190,65,150,70),
							false);
				}
				
				engine.addNebulaParticle(MathUtils.getRandomPointInCircle(ship.getLocation(), 40f),
						MathUtils.getRandomPointInCircle(null, 10f),
						MathUtils.getRandomNumberInRange(360f, 480f),
						MathUtils.getRandomNumberInRange(1.8f, 2.3f),
						0.7f,
						0.6f,
						MathUtils.getRandomNumberInRange(1.9f, 2.6f),
						new Color(210,55,140,50),
						false);
				
			}
			cloudInterval2.advance(amount);
			if (cloudInterval2.intervalElapsed()) {
				Vector2f cloudLoc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				
				for (int i=0; i < 3; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(110f, 140f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(190,65,150,70),
							false);
				}
				
				Vector2f cloudLoc2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				engine.addNebulaParticle(cloudLoc2,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						65f,
						1.6f,
						0.5f,
						0.5f,
						0.66f,
						new Color(255,104,168,123),
						false);
	            
	            engine.addNebulaParticle(cloudLoc2,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						130f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.6f,
						1.3f,
						new Color(190,65,150,70),
						false);
	            
	            for (int i=0; i < 3; i++) {
	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 70f));
					Global.getCombatEngine().addSmoothParticle(cloudLoc2,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.5f, 0.6f), //duration
							new Color(255,52,84,255));
	        	}
			}
		}
		
        
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
		
		ShipAPI ship = (ShipAPI) stats.getEntity();
        if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
        for (ShipAPI target_ship : engine.getShips()) {
            if (target_ship.isHulk() || target_ship.isFighter() || target_ship.getOwner() == ship.getOwner()) {
                continue;
            }
        	target_ship.getMutableStats().getMaxSpeed().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getAcceleration().unmodify(id + ship.getId());
        	target_ship.getMutableStats().getDeceleration().unmodify(id + ship.getId());
        }
        
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}
	
	public static float getMaxRange(ShipAPI ship) {
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(STORM_RANGE);
		//return RANGE;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
}
