package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_UndyingMalice extends BaseHullMod {
	
	public static final float MAINT_MALUS = 100f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
	
	public static final float HIT_MOD = 30f; // +30% hitstrength "base"
	public static final float DEF_MOD = 0.1f; // -10% damage taken "base"
	public static final float TIME_MOD = 0.5f; // +50% timescale "base"
	
	public static final float DAMAGE_PER_CHARGE = 100f;
	public static final int DECAY_TIMER = 2;
	
	public static final Color PARTICLE_COLOR = new Color(255,52,84,255);
	public static final Color BLAST_COLOR = new Color(210,55,140,255);
	
	public static final float VENT_BONUS = 80f; // +80% active vent rate "base"
	private IntervalUtil ventInterval1 = new IntervalUtil(0.3f,0.45f);
	private IntervalUtil ventInterval2 = new IntervalUtil(0.3f,0.45f);
	
	private IntervalUtil sysInterval = new IntervalUtil(0.35f,0.5f); // a bit lower rate than system arcs, because it's 100% rate (and more powerful)
	private static final float STORM_RANGE = 750f; // shorter range than the actual system, because
	private static float ARC_DAM = 50f; // so these start out a bit weaker than the "aura" ones, with lower EMP, and scale up to be powerful at higher charge levels
	private static float ARC_EMP = 200f;
	private static float PHASE_FLUX_SPIKE = 100f;
	
	private static final float REPAIR_CD = 10f;
	private static final float REPAIR_THRESHOLD = 150f; // amount of charge needed before repairs are available
	private static final float REPAIR_SHOCK_RANGE = 600f;
	private static Map<HullSize, Float> repairImpulseMult = new HashMap<HullSize, Float>();
	static {
		repairImpulseMult.put(HullSize.FIGHTER, 75f);
		repairImpulseMult.put(HullSize.FRIGATE, 150f);
		repairImpulseMult.put(HullSize.DESTROYER, 300f);
		repairImpulseMult.put(HullSize.CRUISER, 700f);
		repairImpulseMult.put(HullSize.CAPITAL_SHIP, 800f);
		repairImpulseMult.put(HullSize.DEFAULT, 450f);
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		
	}
	
	/*
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		//ship.addListener(new ASF_maliceDamageListener(ship));
		
	}
	*/
	
	public void advanceInCombat(ShipAPI ship, float amount){
		
        CombatEngineAPI engine = Global.getCombatEngine();
		ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("UNDYING_MALICE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        if (info.doOnce) {
        	engine.getListenerManager().addListener(new ASF_maliceDamageListener(ship));
        	info.doOnce = false;
        }
        
		// death section - [start]
		if (!ship.isAlive() && !info.dead) {
			info.charge = 0f;
			
			stats.getTimeMult().unmodify(spec.getId());
        	engine.getTimeMult().unmodify(spec.getId());
        	
			for (int i=0; i < 9; i++) {
				
				float distanceRandom1 = MathUtils.getRandomNumberInRange(80f, 300f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(1f, 1.3f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 130);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
						new Color(153,92,103,135),
						new Color(255,216,224,140));
		        
				Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.9f, 0.5f, ship.getLocation(), ship.getVelocity());
				
			}
			
			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.6f,
	                350f,
	                210f,
	                1500f,
	                900f,
	                CollisionClass.PROJECTILE_FF,
	                CollisionClass.PROJECTILE_FIGHTER,
	                2f,
	                6f,
	                0.5f,
	                175,
	                PARTICLE_COLOR,
	                BLAST_COLOR);
	        blast.setDamageType(DamageType.ENERGY);
	        blast.setShowGraphic(true);
	        blast.setDetailedExplosionFlashColorCore(new Color(165,140,160,255));
	        blast.setDetailedExplosionFlashColorFringe(new Color(200,80,140,255));
	        blast.setUseDetailedExplosion(true);
	        blast.setDetailedExplosionRadius(400f);
	        blast.setDetailedExplosionFlashRadius(550f);
	        blast.setDetailedExplosionFlashDuration(0.5f);
	        
	        engine.spawnDamagingExplosion(blast,ship,ship.getLocation(),true);
	        
	        	// background smoke
	        for (int i=0; i < 5; i++) {
	        	engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(ship.getLocation(), 25f),
		        		MathUtils.getRandomPointOnCircumference(ship.getVelocity(), 10f),
		        		210f,
						MathUtils.getRandomNumberInRange(1.7f, 2.1f),
						0.9f,
						0.6f,
						MathUtils.getRandomNumberInRange(2.1f, 2.65f),
						new Color(140,40,120,60),
						false);
	        }
			
	        	// sub-blasts, main smoke
	        for (int i=0; i < 5; i++) {
	        	Vector2f blastPos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(70f, 200f));
	        	
		        engine.spawnExplosion(blastPos, ship.getVelocity(), BLAST_COLOR, 140f, 1.1f);
		        
		        for (int j=0; j < 6; j++) {
		        	float nebAngle = MathUtils.getRandomNumberInRange(0f, 360f);
		        	float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
					
			        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 350f * dist, nebAngle),
			        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), nebAngle),
							50f,
							MathUtils.getRandomNumberInRange(1.6f, 2.0f),
							0.7f,
							0.5f,
							MathUtils.getRandomNumberInRange(1.45f, 1.65f),
							new Color(190,65,150,75),
							false);
		        }
	        }
			
			info.dead = true;
		}
		// death section - [end]
		
		
		if (info.dead || ship.isPiece()) {
			return;
		}
		// Global.getCombatEngine().isPaused() ||
		
        
        // the damage listener, (standard) charge gain/decay, "scaling stat setup", [AND] system gimmick - [start]
        Map<String, Object> customCombatData = engine.getCustomData();
        
        float maxCharge = ship.getMaxFlux() * 0.1f; // we limit max charge, for balans
        
        float currDamage = 0f;
        
        if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
            currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
        }
        
        // can only generate charge when CR remains, no 0CR bullshit.
    	while (currDamage >= DAMAGE_PER_CHARGE && ship.getCurrentCR() > 0f) {
            currDamage -= DAMAGE_PER_CHARGE;
            info.charge = Math.min(maxCharge, info.charge + 1f);
            
            info.decay = -DECAY_TIMER;
            
//            // decay timer scales down once you exceed 10% of max charge (this means that once you hit 50% of max charge, there is no delay before charge starts decaying)
//            if (info.charge > (maxCharge * 0.1f)) {
//            	info.decay = - Math.max(0f, DECAY_TIMER * (1f - Math.min(1f, (info.charge - (maxCharge * 0.1f)) / (maxCharge * 0.4f)) ));
//            } else {
//    	        info.decay = -DECAY_TIMER;
//            }
	        
        }
        
        if (info.charge > 0f) {
        	info.decay += amount;
        	
        	if (ship.getCurrentCR() == 0f) {
        		info.decay += amount; // doubled decay when CR is out!
        	}
        }
        
        if (info.decay > 0f && info.charge > 0f) {
        	info.charge = Math.max(0f, info.charge - (info.decay * amount)); // reduce charge by current decay amount if it's in "decay mode"
        }
        
        float chargeScalarD = 1f; // damage resist/arc power scale slower, to make it less powerful
        float chargeScalar = 1f;
    	// simply:
    		// when charge is under 100 it's simply:  Scalar = (charge/100) 
    		// otherwise it becomes more complex, and you gain less and less from each point of charge over 100, (with "value thresholds" at each 100 extra charge)   
    	if (info.charge > 100f) {
    		float chargeTemp = info.charge - 100f;
    		int tempCount = 1;
    		
    		while (chargeTemp > 0f) {
    			if (chargeTemp > 100f) {
    				chargeScalar += Math.pow(0.75, tempCount);
    				chargeScalarD += Math.pow(0.6, tempCount);
    			} else {
    				chargeScalar += (chargeTemp * 0.01f) * Math.pow(0.75, tempCount);
    				chargeScalarD += (chargeTemp * 0.01f) * Math.pow(0.6, tempCount);
    			}
    			chargeTemp -= 100f;
    			tempCount += 1;
    		}
    		
    	} else {
    		chargeScalar = info.charge * 0.01f;
    		chargeScalarD = info.charge * 0.01f;
    	}
    	
    	// using isOn to match with when weapons are disabled from firing
        if (ship.getSystem().isOn()) {
        	
        	// decay charge by: ["scalardD"]% of current value/second
        	info.charge -= (info.charge * 0.01f * chargeScalarD * amount);
        	
    		sysInterval.advance(amount);
        	if(sysInterval.intervalElapsed()) {
        		
        		ShipAPI target_ship = AIUtils.getNearestEnemy(ship);
        		
        		if (target_ship != null) {
        			if (MathUtils.isWithinRange(ship, target_ship, ship.getMutableStats().getSystemRangeBonus().computeEffective(STORM_RANGE))) {
        				if (target_ship.isPhased()) {
        					// if the nearest enemy is phased, then we have them eat a chunk of soft flux, less "AI breaking" than hitting them with an arc after all
        					target_ship.getFluxTracker().increaseFlux(PHASE_FLUX_SPIKE * chargeScalarD, false);
        					
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
        	    						MathUtils.getRandomNumberInRange(0.35f, 0.6f), //duration
        	    						new Color(255,52,84,255));
        	                }
        	                
            			} else {
            				Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f));
                			
                			target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced

                			info.charge = Math.max(0f, info.charge - 0.1f); // you lose 0.1 charge for each of these ""bonus"" arcs fired (lose charge against very tough armour/strong shields)
                				// with a sanity check to prevent it going into negative charge!
                			
                			// scales up to 50 damage at 100 charge, then scales to an "infinite" bonus, with each point of charge being worth a bit less
                						// each 100 charge is worth 60% of the previous 100, so:
                						// 50 + 30 + 18 + 10.8 + 6.48 + 3.888 + 2.3328 + 1.39968	// etc
                			
                			engine.spawnEmpArc(
        	                        ship,
        	                        loc,
        	                        ship,
        	                        target_ship,
        	                        DamageType.ENERGY,
        	                        ARC_DAM + chargeScalarD * 50f,
        	                        ARC_EMP + (chargeScalarD * 100f),
        	                        10000f,
        	                        "A_S-F_malice_arc_impact",
        	                        11f + (chargeScalarD), // thiccer arcs to scale with bonus
        	                        new Color(153,92,103,220),
        							new Color(255,216,224,210));
        	                
        	                engine.spawnExplosion(loc, ship.getVelocity(), new Color(210,55,140,255), 50f + (chargeScalarD * 2f), 0.5f);
        	                
        	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
        			        		MathUtils.getRandomPointInCircle(null, 5f),
        			        		60f + (chargeScalarD * 2.4f),
        							1.6f,
        							0.5f,
        							0.7f,
        							0.25f,
        							new Color(255,216,224,95),
        							false);
        	                
        	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
        			        		MathUtils.getRandomPointInCircle(null, 10f),
        			        		90f + (chargeScalarD * 3.6f),
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
            			}
        				
        			} else {
            	        if (Math.random() < 0.5f) {
            	        	// if no target, then maybe fire a random arc
            	        	float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
            	        	float angle2 = angle1 + MathUtils.getRandomNumberInRange(65f, 85f);
            	        	
            	        	Vector2f loc1 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f), angle1);
            	        	Vector2f loc2 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f), angle2);
            	        	
            	        	engine.spawnEmpArcVisual(loc1, ship, loc2, ship, 11f, new Color(153,92,103,220), new Color(255,216,224,210));
            	        	
            	        }
            			
            		}
        		} else {
        			
        	        if (Math.random() < 0.5f) {
        	        	
        	        	float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
        	        	float angle2 = angle1 + MathUtils.getRandomNumberInRange(65f, 85f);
        	        	
        	        	Vector2f loc1 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f), angle1);
        	        	Vector2f loc2 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f), angle2);
        	        	
        	        	engine.spawnEmpArcVisual(loc1, ship, loc2, ship, 11f, new Color(153,92,103,220), new Color(255,216,224,210));
        	        	
        	        }
        			
        		}
        		
        	}
    	}
        
        // a sanity check, because something was causing this to drop to a MASSIVE negative value, for unknown reasons
        if (currDamage < 0f) {
        	currDamage = 0f;
        }
        
        customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
        // the damage listener and (standard) charge gain/decay [AND] system gimmick - [end]
        
                
        // buff section - [start]
    	if (info.charge > 0f) {
        	
        	float defMod = Math.min(0.4f, DEF_MOD * chargeScalarD); // "sanity check" to prevent you getting more than 40% dam res.
        	
        	stats.getHitStrengthBonus().modifyPercent(spec.getId(), HIT_MOD * chargeScalar);
        	
        	stats.getHullDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	stats.getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	stats.getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	
        	boolean player = ship == engine.getPlayerShip();
            
        	float TIME_MULT = 1f + (TIME_MOD * chargeScalar);
        	
    		if (player) {
    			stats.getTimeMult().modifyMult(spec.getId(), TIME_MULT);
    			engine.getTimeMult().modifyMult(spec.getId(), 1f / TIME_MULT);
    		} else {
    			stats.getTimeMult().modifyMult(spec.getId(), TIME_MULT);
    			engine.getTimeMult().unmodify(spec.getId());
    		}
    		
        } else {
        	stats.getHitStrengthBonus().unmodify(spec.getId());
        	stats.getHullDamageTakenMult().unmodify(spec.getId());
        	stats.getArmorDamageTakenMult().unmodify(spec.getId());
        	stats.getEmpDamageTakenMult().unmodify(spec.getId());
        	
        	stats.getTimeMult().unmodify(spec.getId());
        	engine.getTimeMult().unmodify(spec.getId());
        }
        // buff section - [end]
        
		// repair section - [start]
		if (info.repairCooldown < REPAIR_CD) {
			if (ship.isPhased()) {
				info.repairCooldown += amount * 0.3f; // repair cools down a lot slower when phased (balancing act!)
			} else {
				info.repairCooldown += amount; // increment the repair cooldown.
			}
		}
		
		// only repair if we have:
			// CR remaining (no zero CR zombie memes)
			// at least 100 charge
			// under 50% hull
			// repair is not cooling down (the delay is to make this at least a *bit* balanced)
		if (ship.getCurrentCR() > 0f && info.charge >= REPAIR_THRESHOLD && ship.getHullLevel() < 0.5f && info.repairCooldown >= REPAIR_CD) {
			
			info.charge *= 0.5f; // you really don't want to be forced into having a repair, as it eats a *lot* of charge, even/especially at higher charge levels.
			info.repairCooldown = 0f;
			
			ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() * 0.5f); // halving current flux, a lil helping hand!
			ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() * 0.5f); // we do both both hard+soft, and hard first because of how hard/soft are handled.
			
			float hull = ship.getHitpoints();
			ship.setHitpoints(Math.min(ship.getMaxHitpoints(), hull + (ship.getMaxHitpoints() * 0.5f))); // +50% hull (with a sanity check just in case)
			
			ArmorGridAPI armorGrid = ship.getArmorGrid();
	        final float[][] grid = armorGrid.getGrid();
	        final float max = armorGrid.getMaxArmorInCell();
	        
	        float repairAmount = armorGrid.getMaxArmorInCell();
	        
			for (int x = 0; x < grid.length; x++) {
	            for (int y = 0; y < grid[0].length; y++) {
	                if (grid[x][y] < max) {
	                    float regen = grid[x][y] + repairAmount;
	                    armorGrid.setArmorValue(x, y, regen);
	                }
	            }
	        }
			
			ship.clearDamageDecals();
			ship.syncWithArmorGridState();
	        ship.syncWeaponDecalsWithArmorDamage();
			
	        for (int i=0; i < 40; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(35f, 60f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.6f, 0.75f), //duration
						new Color(50,240,100,255));
        	}
	        
	        for (int i=0; i < 30; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(15f, 45f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.85f, 1.1f), //duration
						new Color(50,240,100,255));
        	}

	        for (int i=0; i < 28; i++) {
	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 1.2f);
	        	engine.addHitParticle(sparkLoc, ship.getVelocity(),
	        			MathUtils.getRandomNumberInRange(5f, 10f), //size
	        			0.8f, //bright
	        			0.4f, //dur
	        			new Color(50, 240, 100));
	        }
	        
	        for (int i=0; i < 3; i++) {
	        	
				float distanceRandom1 = MathUtils.getRandomNumberInRange(65f, 110f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(1f, 1.3f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 130);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
						new Color(92,193,130,135),
						new Color(190,255,220,140));
		        
		        for (int j=0; j < 2; j++) {
		        	
		        	float distanceRandom3 = distanceRandom1 * MathUtils.getRandomNumberInRange(1.9f, 2.1f);
					float angleRandom3 = MathUtils.getRandomNumberInRange(0, 360);
			        Vector2f arcPoint3 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom3, angleRandom3);
			        
			        float distanceRandom4 = distanceRandom3 * MathUtils.getRandomNumberInRange(1f, 1.3f);
			        float angleRandom4 = angleRandom3 + MathUtils.getRandomNumberInRange(45, 75);
			        Vector2f arcPoint4 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom4, angleRandom4);
			        
			        engine.spawnEmpArcVisual(arcPoint3, ship, arcPoint4, ship, 8f,
							new Color(92,193,130,140),
							new Color(190,255,220,145));
		        }
			}
	        
	        for (int i=0; i < 12; i++) {
	        	float angle = (i * 30f) + MathUtils.getRandomNumberInRange(-4f, 4f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius(), angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * dist, angle),
		        		ship.getCollisionRadius(),
						MathUtils.getRandomNumberInRange(1.8f, 2.1f),
						0.5f,
						0.7f,
						1.2f,
						new Color(80,255,175,70),
						false);
	        }
	        
	        
	        engine.addFloatingTextAlways(ship.getLocation(),
					"Emergency repairs!",
					NeuralLinkScript.getFloatySize(ship) * 1.5f, new Color(80,255,175,255), ship,
					15f, // flashFrequency
					3f, // flashDuration
					0.5f, // durInPlace
					1f, // durFloatingUp
					1.5f, // durFadingOut
					1f); // baseAlpha
			
			engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), new Color(80,255,175,70), 400f, 1f);
			
			for (ShipAPI target_ship : engine.getShips()) {
				if (MathUtils.isWithinRange(ship, target_ship, REPAIR_SHOCK_RANGE)) {
					
					float impulseForce = Math.min(repairImpulseMult.get(((ShipAPI) target_ship).getHullSize()) * 3f, (target_ship.getMass() * 0.4f) + repairImpulseMult.get(((ShipAPI) target_ship).getHullSize()));
					// force is whatever is lower of:
						// 3x hullsize scalar
						// hullsize scalar + 40% of target mass
							// so we get a respectable push, but it's not SILLY on stuff like the invictus
					
					CombatUtils.applyForce(target_ship, VectorUtils.getDirectionalVector(ship.getLocation(), target_ship.getLocation()), impulseForce); // knockback!
					
				}
			}
			
			Global.getSoundPlayer().playSound("ui_refit_slot_filled_energy_large", 1.2f, 1.75f, ship.getLocation(), ship.getVelocity());
			
		}
        // repair section - [end]
		
		
        // vent section - [start]
        stats.getVentRateMult().modifyPercent(spec.getId(), (VENT_BONUS * chargeScalar)); // boost vent rate by an amount proportional to current charge
        
		if (ship.getFluxTracker().isVenting()) {
			info.charge = Math.max(0f, info.charge - ((info.charge * 0.05f) * amount)); // while venting, charge decays by 1/20th of current charge /sec
				// you get this reasonably significant charge decay, because you get a (potentially) *insane* boost to vent rate.
			
			// vent fx rate scales up as charge goes up, at the same rate as the vent speed bonus goes up.
			ventInterval1.advance(amount * chargeScalar);
            if (ventInterval1.intervalElapsed()) {
            	
            	float mult = 1f + ship.getFluxLevel();
            	
            	for (int i=0; i < (4 * mult); i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * mult);
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				engine.addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.65f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		80f * mult, // 65
						MathUtils.getRandomNumberInRange(1.6f, 2.2f),
						0.8f,
						0.5f,
						0.8f,
						new Color(140,70,130,70),
						false);
            }
            
            ventInterval2.advance(amount *chargeScalar);
            if (ventInterval2.intervalElapsed()) {

            	float mult = 1f + ship.getFluxLevel();
            	
            	for (int i=0; i < (4 * mult); i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * mult);
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				engine.addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.65f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		80f * mult, // 65
						MathUtils.getRandomNumberInRange(1.6f, 2.2f),
						0.8f,
						0.5f,
						0.8f,
						new Color(140,70,130,70),
						false);
            }
			
		}
		// storing charge here, as we have to do it after the vent decay for that to actually work!
        customCombatData.put("ASF_undyingHullmodCharge" + ship.getId(), info.charge);
        // vent section - [end]
		
		
        // sprite rendering section - [start]
		Vector2f spritePos = MathUtils.getPointOnCircumference(ship.getLocation(), 2f, ship.getFacing());
		Vector2f spriteSize = new Vector2f(98f, 104f);
		float alphaMult = 1f;
		
		if (ship.isPhased()) {
			alphaMult = 0.3f;
		}
		
        if (info.charge > 0f) {
        	SpriteAPI GlowTatt1 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow1");
        	int alpha1 = (int) (Math.min(info.charge, 150) * alphaMult);
        	
    		double alphaTemp = alpha1;
    		double timeMult = (double) stats.getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
    		alpha1 = (int) Math.ceil(alphaTemp / timeMult);
    		
        	MagicRender.singleframe(GlowTatt1, spritePos, spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha1), true);
        	
        	if (info.charge > 100f) {
        		SpriteAPI GlowTatt2 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow2");
            	int alpha2 = (int) (Math.min((info.charge - 100f) * 0.75f, 255f) * alphaMult);
            	
        		double alphaTemp2 = alpha2;
        		alpha2 = (int) Math.ceil(alphaTemp2 / timeMult);
            	
            	MagicRender.singleframe(GlowTatt2, MathUtils.getRandomPointInCircle(spritePos, 1f), spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha2), true);
        	}
        }
		
		if (info.repairCooldown < REPAIR_CD) {
			// repair is not ready, lower alpha mult.
			alphaMult *= 0.3f;
		}
		
		// the pipe glow is set up like this, so it fades in/out, rather than popping in instantly (and is done after the tattoo so it gets an extra alphaMult when repairs aren't ready)
        if (info.charge >= REPAIR_THRESHOLD) {
        	if (info.fadeIn < 1f) {
            	info.fadeIn = Math.min(1f, info.fadeIn + (amount * 1.5f));
        	}
        }
    	if (info.fadeIn > 0f) {
    		
    		int pipeAlpha = (int) Math.min(180, Math.max(0, (int) (180 * alphaMult * info.fadeIn)) );
    		
    		double alphaTemp = pipeAlpha;
    		double timeMult = (double) stats.getTimeMult().modified;
    		pipeAlpha = (int) Math.ceil(alphaTemp / timeMult);
    		
        	SpriteAPI GlowPipe = Global.getSettings().getSprite("fx", "A_S-F_persenachia_pipe_glow");
        	MagicRender.singleframe(GlowPipe, spritePos, spriteSize, ship.getFacing() - 90f, new Color(80,255,175,pipeAlpha), true);
        	
        	if (info.charge < REPAIR_THRESHOLD) {
            	info.fadeIn = Math.max(0f, info.fadeIn - (amount * 1.5f));
        	}
        }
        // sprite rendering section - [end]
        
    	
        // ui info display section - [start]
        if (ship == engine.getPlayerShip()) {
        	
        	float chargeFill = Math.max(0f, Math.min(1f, info.charge / maxCharge)); // a double sanity check because i'm paranoid lol!
        	
        	if (info.charge >= REPAIR_THRESHOLD) {
        		if (info.repairCooldown < REPAIR_CD) {
            		MagicUI.drawHUDStatusBar(ship,
            				chargeFill,
    						new	Color(205,98,22,255), // old col: 80,205,125,255
    						null,
    						chargeFill * (Math.min(1f, info.repairCooldown * 0.1f)), // the repair "marker" moves up as the repair recharges
    						"CHARGE: " + (int) info.charge,
    						"",
    						false);
        		} else {
        			// when repair is ready the color matches the pipes!
            		MagicUI.drawHUDStatusBar(ship,
            				chargeFill,
    						new	Color(105,255,155,255),
    						null,
    						chargeFill,
    						"CHARGE: " + (int) info.charge,
    						"",
    						false);
        		}
        	} else {
        		MagicUI.drawHUDStatusBar(ship,
        				chargeFill,
        				new	Color(205,42,68,255), // was:  "textFriendColor").darker().darker()
						null,
						0,
						"CHARGE: " + (int) info.charge,
						"",
						false);
        	}
        }
        // ui info display section - [end]
        
        
        // debug display section - [start]
        // engine.maintainStatusForPlayerShip("MALICEDEBUG4", "graphics/icons/hullsys/phase_cloak.png",  "chargeScalar: " + chargeScalar, "chargeScalarD: " + chargeScalarD, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG3", "graphics/icons/hullsys/phase_cloak.png",  "Repair CD: " + info.repairCooldown, "Max Charge: " + maxCharge, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG2", "graphics/icons/hullsys/phase_cloak.png", "Decay: " + info.decay, "currDamage: " + currDamage, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG1", "graphics/icons/hullsys/phase_cloak.png", "DEBUG INFO", "Charge: " + info.charge, false);
        // debug display section - [end]
        
        
        engine.getCustomData().put("UNDYING_MALICE_DATA_KEY" + ship.getId(), info);
        	
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
		float dpad = 6f;
		float opad = 10f;
		float tpad = 10f;
		float hpad = 12f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color grey = Misc.getGrayColor();
		
		Color banner = Misc.getDarkHighlightColor();
		Color repBanner = new Color(65,160,100,255);
		
		// UNDYING \\ MALICE
		
		// A heretic, clad in the husks of the dead. In order to hide her figure, she spreads a thick, dark mist. The black fog conceals her as she hunts her next victim.
		
		LabelAPI label = tooltip.addPara("This vessel features a unique system called the Malice Resonator, this improves the ships performance after dealing damage and can use the accumulated energy to repair itself.", pad);
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The rate of in-combat CR decay after peak performance time runs out is increased by %s.", pad, bad, "" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlight("" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlightColors(bad);
		
		tooltip.addSectionHeading("Malice Resonator", h, banner, Alignment.MID, hpad);
		label = tooltip.addPara("The resonator generates one charge for every %s damage the ship deals.", tpad, h, "" + (int)DAMAGE_PER_CHARGE);
		label.setHighlight("" + (int)DAMAGE_PER_CHARGE);
		label.setHighlightColors(h);
		label = tooltip.addPara("The ship recieves bonuses to %s, %s, %s and %s based on the current charge level.", pad, h, "Weapon Hitstrength", "Timescale", "Damage Resistance", "Active vent rate");
		label.setHighlight("Weapon Hitstrength", "Timescale", "Damage Resistance", "Active vent rate");
		label.setHighlightColors(h, h, h, h);
		label = tooltip.addPara("Charges will %s while actively venting.", pad, bad, "Decay");
		label.setHighlight("Decay");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The resonator can only store a limited charge level, equivalent to %s of the vessels flux capacity.", dpad, h, "10%");
		label.setHighlight("10%");
		label.setHighlightColors(h);
		label = tooltip.addPara("%s %s", pad, grey, "Current charge capacity is:", "" + (int) ((ship.getMaxFlux() * 0.1f)));
		label.setHighlight("Current charge capacity is:", "" + (int) ((ship.getMaxFlux() * 0.1f)));
		label.setHighlightColors(grey, h);
		
		label = tooltip.addPara("Resonator charge can only remain stable for a limited duration before %s.", opad, bad, "Decaying");
		label.setHighlight("Decaying");
		label.setHighlightColors(bad);
		label = tooltip.addPara("There is a %s pause after generating charge before decay starts.", pad, h, DECAY_TIMER + " second");
		label.setHighlight(DECAY_TIMER + " second");
		label.setHighlightColors(h);
		
//		label = tooltip.addPara("If charge is under %s of capacity, there is a %s pause after generating charge before decay starts.", pad, h, "10%", "" + DECAY_TIMER + " second");
//		label.setHighlight("10%", "" + DECAY_TIMER + " second");
//		label.setHighlightColors(h, h);
//		label = tooltip.addPara("This pause shortens as charge level increases, once charge hits %s of capacity there is no pause to decay, but decay rate will still reset on dealing damage.", pad, h, "50%");
//		label.setHighlight("50%");
//		label.setHighlightColors(h, h);
		
		tooltip.addSectionHeading("Emergency Repair System", h, repBanner, Alignment.MID, hpad);
		label = tooltip.addPara("If the ship drops below %s hull and has at least %s charge stored, then the Resonator will consume %s of current charge to trigger an emergency repair.", tpad, bad, "50%", "" + (int) REPAIR_THRESHOLD, "50%");
		label.setHighlight("50%", "" + (int) REPAIR_THRESHOLD, "50%");
		label.setHighlightColors(bad, h, h);
		label = tooltip.addPara("An emergency repair will restore all damaged armour, and replenish %s of the ships maximum hull.", pad, h, "50%");
		label.setHighlight("50%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Emergency repairs can only be triggered once every %s seconds.", pad, h, "" + (int)REPAIR_CD);
		label.setHighlight("" + (int)REPAIR_CD);
		label.setHighlightColors(h);
		
	}
	
	// damage dealt listener [start]
		// "stolen" from the VIC stolas script ;)
    public static class ASF_maliceDamageListener implements DamageListener {
        ShipAPI ship;

        ASF_maliceDamageListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (source instanceof ShipAPI) {
                if (!source.equals(ship)) {
                    return;
                }
                if (!ship.isAlive()) {
                    Global.getCombatEngine().getListenerManager().removeListener(this);
                }
                
                if (target instanceof ShipAPI) {
                    if (!((ShipAPI) target).isAlive()) return;
                }
                
                float totalDamage = 0;
                totalDamage += result.getDamageToHull();
                totalDamage += (result.getDamageToShields());
                totalDamage += result.getTotalDamageToArmor();
                Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
                
                float currDamage = 0f;

                if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
                    currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
                }
                
                currDamage += totalDamage;
                
                customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
            }
        }
    }
	// damage dealt listener [end]
    
	
    private class ShipSpecificData {
    	private float charge = 0f;
    	private float decay = -2f;
    	private boolean dead = false;
    	private boolean doOnce = true;
    	private float fadeIn = 0f;
    	private float repairCooldown = 10f;
    }
}