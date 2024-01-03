package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_MaliceStormStats extends BaseShipSystemScript {

	private CombatEngineAPI engine;

	private static final float STORM_RANGE = 1000f;
	private static float SPD_MULT = 0.2f; // how much the target is to be slowed by
	
	private static float ARC_BLAST_SIZE = 50f;
	
	public static final float DAMAGE_REDUCTION = 0.25f; // you get damage resistance during sys use, not sure if this is really *needed* but w/e
	
	private IntervalUtil arcInterval1 = new IntervalUtil(0.25f,0.5f);
	private IntervalUtil arcInterval2 = new IntervalUtil(0.25f,0.5f);
	private IntervalUtil sparkInterval = new IntervalUtil(0.05f,0.05f);
	
	private IntervalUtil cloudInterval1 = new IntervalUtil(0.2f,0.3f);
	private IntervalUtil cloudInterval2 = new IntervalUtil(0.2f,0.3f);
	
	private boolean arcFired1 = false;
	private boolean arcFired2 = false;
	
	private static Map<HullSize, Float> arcRateMult = new HashMap<HullSize, Float>();
	static {
		arcRateMult.put(HullSize.FIGHTER, 0.1f);
		arcRateMult.put(HullSize.FRIGATE, 0.2f);
		arcRateMult.put(HullSize.DESTROYER, 0.25f);
		arcRateMult.put(HullSize.CRUISER, 0.35f);
		arcRateMult.put(HullSize.CAPITAL_SHIP, 0.45f);
		arcRateMult.put(HullSize.DEFAULT, 0.15f);
	}
	
	private static float ARC_DAM = 60f;
	private static float ARC_EMP = 400f;
	private static float PHASE_FLUX_SPIKE = 200f;
	
	private static float ARC_RATE_DECAY_MULT = 0.9f;
	
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
		
		
		ShipAPI ship = (ShipAPI)stats.getEntity();
		float range = getMaxRange(ship);
		
		ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
		
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
		
		arcInterval1.advance(amount * effectLevel);
		if (arcInterval1.intervalElapsed()) {
			
			arcFired1 = false;
			float arcMult = 1f; // scaling down arc chance if an arc has already happened, to prevent it being completely brainless op when in a big swarm
			float arcMultM = 1f; // seperate scalar for missiles
			
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner() || target_ship.isPhased()) {
					continue;
				}
				
				// if the target ship is within range, do an arc
				if (MathUtils.isWithinRange(ship, target_ship, range)) {
					if (Math.random() < (arcRateMult.get(target_ship.getHullSize()) * arcMult)) {						
						arcFired1 = true;
						arcMult *= ARC_RATE_DECAY_MULT;

						if (target_ship.isPhased()) {
	    					// if the enemy is phased, then we have them eat a chunk of soft flux, less "AI breaking" than hitting them with an arc after all
	    					target_ship.getFluxTracker().increaseFlux(PHASE_FLUX_SPIKE, false);
	    					
	    					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 5f),
	    			        		target_ship.getCollisionRadius(),
	    							1.6f,
	    							0.5f,
	    							0.7f,
	    							0.25f,
	    							new Color(255,216,224,95),
	    							false);
	    	                
	    	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 10f),
	    			        		target_ship.getCollisionRadius(),
	    							MathUtils.getRandomNumberInRange(1.5f, 1.8f),
	    							0.7f,
	    							0.3f,
	    							0.65f,
	    							new Color(190,65,150,70),
	    							false);
	    	                
	    	                for (int i=0; i < (target_ship.getCollisionRadius() * 0.2f); i++) {
	    	                	Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(target_ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 75f));
	    	                	
	    	            		engine.addSmoothParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), target_ship.getCollisionRadius() * 0.65f),
	    	    						sparkVel,
	    	    						MathUtils.getRandomNumberInRange(4f, 8f), //size
	    	    						1.0f, //brightness
	    	    						MathUtils.getRandomNumberInRange(0.35f, 0.5f), //duration
	    	    						new Color(255,52,84,255));
	    	                }
	    	                
	        			} else {
			                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
			                
			                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
			                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
			                
			                CombatEntityAPI dummy = new SimpleEntity(loc);

			                target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced
			                
			                engine.spawnEmpArc(
			                        ship,
			                        loc,
			                        dummy,
			                        target_ship,
			                        DamageType.ENERGY,
			                        ARC_DAM,
			                        ARC_EMP,
			                        10000f,
			                        "A_S-F_malice_arc_impact",
			                        11f,
			                        new Color(153,92,103,220),
									new Color(255,216,224,210));
			                
			                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
			                
			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
					        		MathUtils.getRandomPointInCircle(null, 5f),
					        		ARC_BLAST_SIZE * 1.2f,
									1.6f,
									0.5f,
									0.7f,
									0.25f,
									new Color(255,216,224,95),
									false);
			                
			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
					        		MathUtils.getRandomPointInCircle(null, 10f),
					        		ARC_BLAST_SIZE * 1.8f,
									MathUtils.getRandomNumberInRange(1.5f, 1.8f),
									0.7f,
									0.3f,
									0.6f,
									new Color(190,65,150,70),
									false);
			                
			                for (int i=0; i < 7; i++) {
			            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 85f));
			            		engine.addSmoothParticle(loc,
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
			}
			
			for (MissileAPI target_missile : engine.getMissiles()) {
    			// check if the missile is a valid target
        		if (target_missile.getOwner() == ship.getOwner()) {
        			continue;
        		}
        		
				// if the target missile is within range, do an arc
        		if (MathUtils.isWithinRange(ship, target_missile, range)) {
        			if (Math.random() < (0.1f *arcMultM)) {
        				arcFired1 = true;
        				arcMultM *= ARC_RATE_DECAY_MULT;
        				
        				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = new SimpleEntity(loc);
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_missile,
		                        DamageType.ENERGY,
		                        ARC_DAM,
		                        ARC_EMP,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        10f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));
		                
		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE * 0.9f, 0.45f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 4f),
				        		ARC_BLAST_SIZE * 1.1f,
								1.6f,
								0.5f,
								0.7f,
								0.23f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 8f),
				        		ARC_BLAST_SIZE * 1.7f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.55f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 6; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 80f));
		            		engine.addSmoothParticle(loc,
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
			
			// if we didn't arc to anything, spawn an extra "angry" cloud
			if (!arcFired1) {
				
                Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), range * 0.8f));
                
				engine.addNebulaParticle(loc,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						85f,
						1.6f,
						0.5f,
						0.5f,
						0.66f,
						new Color(255,104,150,123),
						false);
	            
	            engine.addNebulaParticle(loc,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						170f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.6f,
						1.3f,
						new Color(190,70,135,70),
						false);
	            
	            engine.addSmoothParticle(loc,
	            		MathUtils.getRandomPointInCircle(null, 1f),
						MathUtils.getRandomNumberInRange(35f, 45f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
						new Color(255,52,84,255));
	            
	            for (int i=0; i < 5; i++) {
	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
	        		engine.addSmoothParticle(loc,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
							new Color(255,52,84,255));
	        	}
                
			}
		}
		
		arcInterval2.advance(amount * effectLevel);
		if (arcInterval2.intervalElapsed()) {
			
			arcFired2 = false;
			float arcMult = 1f; // scaling down arc chance if an arc has already happened, to prevent it being completely brainless op when in a big swarm
			float arcMultM = 1f; // seperate scalar for missiles
			
			for (ShipAPI target_ship : engine.getShips()) {
				// check if the ship is a valid target
				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
					continue;
				}
				
				// if the target ship is within range, do an arc
				if (MathUtils.isWithinRange(ship, target_ship, range)) {
					
					if (Math.random() < (arcRateMult.get(target_ship.getHullSize()) * arcMult)) {						
						arcFired2 = true;
						arcMult *= ARC_RATE_DECAY_MULT;

						if (target_ship.isPhased()) {
	    					// if the enemy is phased, then we have them eat a chunk of soft flux, less "AI breaking" than hitting them with an arc after all
	    					target_ship.getFluxTracker().increaseFlux(PHASE_FLUX_SPIKE, false);
	    					
	    					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 5f),
	    			        		target_ship.getCollisionRadius(),
	    							1.6f,
	    							0.5f,
	    							0.7f,
	    							0.25f,
	    							new Color(255,216,224,95),
	    							false);
	    	                
	    	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 10f),
	    			        		target_ship.getCollisionRadius(),
	    							MathUtils.getRandomNumberInRange(1.5f, 1.8f),
	    							0.7f,
	    							0.3f,
	    							0.65f,
	    							new Color(190,65,150,70),
	    							false);
	    	                
	    	                for (int i=0; i < (target_ship.getCollisionRadius() * 0.2f); i++) {
	    	                	Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(target_ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 75f));
	    	                	
	    	            		engine.addSmoothParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), target_ship.getCollisionRadius() * 0.65f),
	    	    						sparkVel,
	    	    						MathUtils.getRandomNumberInRange(4f, 8f), //size
	    	    						1.0f, //brightness
	    	    						MathUtils.getRandomNumberInRange(0.35f, 0.5f), //duration
	    	    						new Color(255,52,84,255));
	    	                }
	    	                
	        			} else {
			                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
			                
			                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
			                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
			                
			                CombatEntityAPI dummy = new SimpleEntity(loc);

			                target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced
			                
			                engine.spawnEmpArc(
			                        ship,
			                        loc,
			                        dummy,
			                        target_ship,
			                        DamageType.ENERGY,
			                        ARC_DAM,
			                        ARC_EMP,
			                        10000f,
			                        "A_S-F_malice_arc_impact",
			                        11f,
			                        new Color(153,92,103,220),
									new Color(255,216,224,210));
			                
			                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
			                
			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
					        		MathUtils.getRandomPointInCircle(null, 5f),
					        		ARC_BLAST_SIZE * 1.2f,
									1.6f,
									0.5f,
									0.7f,
									0.25f,
									new Color(255,216,224,95),
									false);
			                
			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
					        		MathUtils.getRandomPointInCircle(null, 10f),
					        		ARC_BLAST_SIZE * 1.8f,
									MathUtils.getRandomNumberInRange(1.5f, 1.8f),
									0.7f,
									0.3f,
									0.6f,
									new Color(190,65,150,70),
									false);
			                
			                for (int i=0; i < 7; i++) {
			            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 85f));
			            		engine.addSmoothParticle(loc,
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
			}
			
			for (MissileAPI target_missile : engine.getMissiles()) {
    			// check if the missile is a valid target
        		if (target_missile.getOwner() == ship.getOwner()) {
        			continue;
        		}

				// if the target missile is within range, do an arc
        		if (MathUtils.isWithinRange(ship, target_missile, range)) {
        			if (Math.random() < (0.1f * arcMultM)) {
        				arcFired2 = true;
        				arcMultM *= ARC_RATE_DECAY_MULT;
        				
        				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
		                
		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
		                
		                CombatEntityAPI dummy = new SimpleEntity(loc);
		                
		                engine.spawnEmpArc(
		                        ship,
		                        loc,
		                        dummy,
		                        target_missile,
		                        DamageType.ENERGY,
		                        ARC_DAM,
		                        ARC_EMP,
		                        10000f,
		                        "A_S-F_malice_arc_impact",
		                        10f,
		                        new Color(153,92,103,220),
								new Color(255,216,224,210));

		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE * 0.9f, 0.45f);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 4f),
				        		ARC_BLAST_SIZE * 1.1f,
								1.6f,
								0.5f,
								0.7f,
								0.23f,
								new Color(255,216,224,95),
								false);
		                
		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
				        		MathUtils.getRandomPointInCircle(null, 8f),
				        		ARC_BLAST_SIZE * 1.7f,
								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
								0.7f,
								0.3f,
								0.55f,
								new Color(190,65,150,70),
								false);
		                
		                for (int i=0; i < 6; i++) {
		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 80f));
		            		engine.addSmoothParticle(loc,
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
			
			// if we didn't arc to anything, spawn an extra "angry" cloud
			if (!arcFired2) {
				
                Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), range * 0.8f));
                
				engine.addNebulaParticle(loc,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						85f,
						1.6f,
						0.5f,
						0.5f,
						0.66f,
						new Color(255,104,150,123),
						false);
	            
	            engine.addNebulaParticle(loc,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						170f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.6f,
						1.3f,
						new Color(190,70,135,70),
						false);
	            
	            engine.addSmoothParticle(loc,
	            		MathUtils.getRandomPointInCircle(null, 1f),
						MathUtils.getRandomNumberInRange(35f, 45f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
						new Color(255,52,84,255));
	            
	            for (int i=0; i < 5; i++) {
	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
	        		engine.addSmoothParticle(loc,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
							new Color(255,52,84,255));
	        	}
                
			}
		}
		
		
		// ship jitter
		float ALPHA = 25f + (45f * effectLevel);
		Color JITTER_UNDER_COLOR = new Color(128,26,42,(int)ALPHA);
		
		float jitterRangeBonus = 12f * (1f + effectLevel);
		float jitterLevel = (float) Math.sqrt(effectLevel);
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 23, 0f, 12f + jitterRangeBonus);
		
		
		// "local spark fx" + "radius marker"
		sparkInterval.advance(amount);
		if (sparkInterval.intervalElapsed()) {
			for (int i=0; i < 5; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(35f, 45f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
						new Color(255,52,84,255));
        	}
			
			float offset = MathUtils.getRandomNumberInRange(0f, 10f);
			for (int i=0; i < 24; i++) {
				float angle = offset + (i * 15f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), range, angle + MathUtils.getRandomNumberInRange(-4f, 4f));
        		Vector2f sparkVel = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(-25f, -55f), angle);
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
							MathUtils.getRandomNumberInRange(210f, 290f), // 110-140
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(160,60,185,70),
							false);
				}
				
				Vector2f cloudLoc2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.5f, 0.9f));
				for (int i=0; i < 2; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc2, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(220f, 300f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.8f, 2.5f),
							new Color(160,60,185,70),
							false);
				}
				
				engine.addNebulaParticle(MathUtils.getRandomPointInCircle(ship.getLocation(), 60f),
						MathUtils.getRandomPointInCircle(null, 10f),
						MathUtils.getRandomNumberInRange(360f, 480f),
						MathUtils.getRandomNumberInRange(1.8f, 2.3f),
						0.7f,
						0.6f,
						MathUtils.getRandomNumberInRange(1.9f, 2.6f),
						new Color(190,50,125,35), //210,55,140,50
						false);
				
			}
			cloudInterval2.advance(amount);
			if (cloudInterval2.intervalElapsed()) {
				Vector2f cloudLoc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				
				for (int i=0; i < 3; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(210f, 290f), // 110-140
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(150,65,190,70),
							false);
				}

				Vector2f cloudLoc2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.5f, 0.9f));
				for (int i=0; i < 2; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc2, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(220f, 300f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.8f, 2.5f),
							new Color(160,60,185,70),
							false);
				}
				
				Vector2f cloudLoc3 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), range * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				engine.addNebulaParticle(cloudLoc3,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						85f,
						1.6f,
						0.5f,
						0.5f,
						0.66f,
						new Color(255,104,158,123),
						false);
	            
	            engine.addNebulaParticle(cloudLoc3,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						170f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.6f,
						1.3f,
						new Color(190,65,140,70),
						false);
	            
	            engine.addSmoothParticle(cloudLoc3,
	            		MathUtils.getRandomPointInCircle(null, 1f),
						MathUtils.getRandomNumberInRange(35f, 45f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
						new Color(255,52,84,255));
	            
	            for (int i=0; i < 5; i++) {
	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
	        		engine.addSmoothParticle(cloudLoc3,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
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
