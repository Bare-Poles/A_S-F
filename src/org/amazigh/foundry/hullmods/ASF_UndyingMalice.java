package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
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
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_UndyingMalice extends BaseHullMod {
	
	public static final float MAINT_MALUS = 100f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
	
	public static final float DAMAGE_PER_CHARGE = 100f;
	public static final int DECAY_TIMER = 4;
	
	public static final Color PARTICLE_COLOR = new Color(255,52,84,255);
	public static final Color BLAST_COLOR = new Color(210,55,140,255);
	
	private IntervalUtil ventInterval1 = new IntervalUtil(0.3f,0.45f);
	private IntervalUtil ventInterval2 = new IntervalUtil(0.3f,0.45f);
	
	private IntervalUtil sysInterval = new IntervalUtil(0.35f,0.5f); //lower rate than in-system, because it's 100% chance
	private static final float STORM_RANGE = 800f; // shorter range than the actual system, because
	private static float ARC_DAM = 30f;
	private static float ARC_EMP = 60f; 
	
	private static final float REPAIR_CD = 10f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		//ship.addListener(new ASF_maliceDamageListener(ship));
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		

		ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("UNDYING_MALICE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        
        if (info.doOnce) {
        	engine.getListenerManager().addListener(new ASF_maliceDamageListener(ship));
        	info.doOnce = false;
        }
        
        
		// death section - [start]
		if (!ship.isAlive() && !info.dead) {
			info.charge = 0f;
			
			for (int i=0; i < 9; i++) {
				
				float distanceRandom1 = MathUtils.getRandomNumberInRange(60f, 240f);
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
		
        
        // the damage listener and (standard) charge gain/decay [AND] system gimmick - [start]
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        
        float currDamage = 0f;
        
        if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
            currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
        }
        
        if (customCombatData.get("ASF_undyingHullmodCharge" + ship.getId()) instanceof Float) {
        	info.charge = (float) customCombatData.get("ASF_undyingHullmodCharge" + ship.getId()); // updating charge if it was changed by the damage taken listener
        }
        
        // using isOn to match with when weapons are disabled from firing
        if (ship.getSystem().isOn()) {
        	
        	info.decay = Math.max(-DECAY_TIMER, info.decay - (amount * 0.5f)); // rather than resetting decay, we instead slowly lower it, because that's less abusable than a flat out reset
        	
        	while (currDamage >= DAMAGE_PER_CHARGE) {
                currDamage -= DAMAGE_PER_CHARGE;
                info.charge += 0.2f; // reduced charge gain, but at least you get some
                info.chargeSys += 1f; // charge is also used for "bonus arcs"
            }
        	
        } else {
        	if (!ship.getSystem().isActive()) {
            	info.chargeSys = 0f; // oops you lose all "system charge" once the system ends!
        	}
        	
        	while (currDamage >= DAMAGE_PER_CHARGE) {
                currDamage -= DAMAGE_PER_CHARGE;
                info.charge += 1f;
    	        info.decay = -DECAY_TIMER;
            }
            
            if (info.charge > 0f) {
                info.decay += amount;
            }
            
            if (info.decay > 0f && info.charge > 0f) {
            	info.charge = Math.max(0f, info.charge - (info.decay * amount));
            }
        }
        
        if (info.chargeSys > 0f) {
    		sysInterval.advance(amount);
        	if(sysInterval.intervalElapsed()) {
        		
        		ShipAPI target_ship = AIUtils.getNearestEnemy(ship);
        		
        		if (MathUtils.isWithinRange(ship, target_ship, ship.getMutableStats().getSystemRangeBonus().computeEffective(STORM_RANGE))) {
        			
        			Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f));
        			
        			target_ship.getVelocity().scale(0.95f); // slowing the target when they get arced
	                
        			float bonus = info.chargeSys * 10f; // bonus power added for each point of "system charge" - (you can in theory get some mad shit if you're in a big horde)
        			float sysChargeDecay = 1f;
        			float sysTemp = info.chargeSys;
        			
        			while(sysTemp > 5f) {
        				sysTemp -= 5f; // so when you have a "lot" of charge stored, you get STRONGer arcs (also to help burn through charge mostly)	
        				bonus *= 1.5f;
        				sysChargeDecay += 1f;
        			}
        			
        			bonus += (info.charge * 0.3f); // higher ("normal") charge? more powerful arcs!
        			
        			engine.spawnEmpArc(
	                        ship,
	                        loc,
	                        ship,
	                        target_ship,
	                        DamageType.ENERGY,
	                        ARC_DAM + bonus,
	                        ARC_EMP + (bonus * 1.5f),
	                        10000f,
	                        "A_S-F_malice_arc_impact",
	                        11f + (info.chargeSys * 0.5f) + (info.charge * 0.03f), // thiccer arcs to scale with "system charge"
	                        new Color(153,92,103,220),
							new Color(255,216,224,210));
	                
	                engine.spawnExplosion(loc, ship.getVelocity(), new Color(210,55,140,255), 50f + info.chargeSys, 0.5f);
	                
	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
			        		MathUtils.getRandomPointInCircle(null, 5f),
			        		60f + (info.chargeSys * 1.2f),
							1.6f,
							0.5f,
							0.7f,
							0.25f,
							new Color(255,216,224,95),
							false);
	                
	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
			        		MathUtils.getRandomPointInCircle(null, 10f),
			        		90f + (info.chargeSys * 1.8f),
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
	                info.chargeSys -= sysChargeDecay;	
        			
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
        
        
        
        customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
        // the damage listener and (standard) charge gain/decay [AND] system gimmick - [end]
        
                
        // buff section - [start]
        MutableShipStatsAPI stats = ship.getMutableStats();
        if (info.charge > 0f) {
        	
        	 // "base scaling rates"
        	float hitMod = 20f; // +20% hitstrength
        	float defMod = 0.1f; // -10% damage taken
        	float timeMod = 0.5f; // +50% timescale
        	
        	float chargeScalar = 1f;
        	
        	// simply:
        		// when charge is under 100 it's simply:  Scalar = 1 + (charge/100) 
        		// otherwise it becomes more complex, and you gain less and less from each point of charge over 100, (with "value thresholds" at each 100 extra charge)   
        	if (info.charge > 100f) {
        		float chargeTemp = info.charge - 100f;
        		int tempCount = 1;
        		
        		while (chargeTemp > 0f) {
        			if (chargeTemp > 100f) {
        				chargeScalar += Math.pow(0.7, tempCount);
        			} else {
        				chargeScalar += (chargeTemp * 0.01f) * Math.pow(0.7, tempCount);
        			}
        			chargeTemp -= 100f;
        			tempCount += 1;
        		}
        		
        	} else {
        		chargeScalar = info.charge * 0.01f;
        	}
        	
        	
        	hitMod *= chargeScalar;
        	defMod *= chargeScalar;
        	timeMod *= chargeScalar;
        	
        	stats.getHitStrengthBonus().modifyPercent(spec.getId(), hitMod);

        	stats.getHullDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	stats.getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	stats.getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - defMod);
        	
        	boolean player = ship == Global.getCombatEngine().getPlayerShip();
            
        	float TIME_MULT = 1f + timeMod;
        	
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
			info.repairCooldown += amount; // increment the repair cooldown.
		}
		
		// only repair if we have:
			// CR remaining (no zero CR zombie memes)
			// at least 100 charge
			// under 50% hull
			// repair is not cooling down (the delay is to make this at least a *bit* balanced)
		if (ship.getCurrentCR() > 0f && info.charge >= 100f && ship.getHullLevel() < 0.5f && info.repairCooldown >= REPAIR_CD) {
			
			info.charge -= 100f;
			info.repairCooldown = 0f;
			
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
			
			ship.syncWithArmorGridState();
	        ship.syncWeaponDecalsWithArmorDamage();
			
	        for (int i=0; i < 40; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(35f, 45f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
						new Color(50,240,100,255));
        	}
	        
	        for (int i=0; i < 30; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(15f, 35f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.65f, 0.85f), //duration
						new Color(50,240,100,255));
        	}

	        for (int i=0; i < 28; i++) {
	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f);
	        	Global.getCombatEngine().addHitParticle(sparkLoc, ship.getVelocity(),
	        			MathUtils.getRandomNumberInRange(5f, 10f), //size
	        			0.8f, //bright
	        			0.4f, //dur
	        			new Color(50, 240, 100));
	        }
	        
	        for (int i=0; i < 3; i++) {
	        	
				float distanceRandom1 = MathUtils.getRandomNumberInRange(60f, 110f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(1f, 1.3f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 130);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
						new Color(92,193,130,135),
						new Color(190,255,220,140));
			}
	        
			Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
					"Emergency repairs!",
					NeuralLinkScript.getFloatySize(ship), new Color(80,255,175,255), ship, 16f, 3.2f, 1f, 0f, 1f,
					1f);
			
			Global.getCombatEngine().spawnExplosion(ship.getLocation(), ship.getVelocity(), new Color(80,255,175,120), 120f, 0.75f);
			
			Global.getSoundPlayer().playSound("ui_refit_slot_filled_energy_large", 1.2f, 1.2f, ship.getLocation(), ship.getVelocity());
			//TODO test sound
	        
		}
        // repair section - [end]
		

        // vent section - [start]
        stats.getVentRateMult().modifyPercent(spec.getId(), info.charge); // boost vent rate by an amount proportional to current charge
        
		if (ship.getFluxTracker().isVenting()) {
			info.charge = Math.max(0f, info.charge - ((info.charge * 0.1f) * amount)); // while venting, charge decays by 1/10th of current charge /sec
				// you get this (potentially) huge charge decay, because you get a (potentially) *insane* boost to vent rate.
			
			float ventScalar = 1f;
			if (info.charge > 100f) {
        		float chargeTemp = info.charge - 100f;
        		int tempCount = 1;
        		ventScalar += 1f;
        		
        		while (chargeTemp > 0f) {
        			if (chargeTemp > 100f) {
        				ventScalar += Math.pow(0.5, tempCount);
        			} else {
        				ventScalar += (chargeTemp * 0.01f) * Math.pow(0.5, tempCount);
        			}
        			chargeTemp -= 100f;
        			tempCount += 1;
        		}
        		
        	} else {
        		ventScalar += info.charge * 0.01f;
        	}
			// vent fx rate scales up to 3x rate as charge goes up
			
			ventInterval1.advance(amount * ventScalar);
            if (ventInterval1.intervalElapsed()) {
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
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
		        		65f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.5f,
						0.8f,
						new Color(140,70,130,70),
						false);
            }
            
            ventInterval2.advance(amount * ventScalar);
            if (ventInterval2.intervalElapsed()) {
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
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
						65f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
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
		
		// the pipe glow is set up like this, so it fades in/out, rather than popping in instantly
		
		if (info.repairCooldown < REPAIR_CD) {
			// repair is not ready, lower alpha mult.
			alphaMult *= 0.5f;
		}
		
        if (info.charge >= 100f) {
        	if (info.fadeIn < 1f) {
            	info.fadeIn = Math.min(1f, info.fadeIn + (amount * 1.5f));
        	}
        }
    	if (info.fadeIn > 0f) {
    		
    		int pipeAlpha = (int) Math.min(120, Math.max(0, (int) (120 * alphaMult * info.fadeIn)) );
        	SpriteAPI GlowPipe = Global.getSettings().getSprite("fx", "A_S-F_persenachia_pipe_glow");
        	MagicRender.singleframe(GlowPipe, spritePos, spriteSize, ship.getFacing() - 90f, new Color(80,255,175,pipeAlpha), true);
        	
        	if (info.charge < 100f) {
            	info.fadeIn = Math.max(0f, info.fadeIn - (amount * 1.5f));
        	}
        }
        
        if (info.charge > 0f) {
        	SpriteAPI GlowTatt1 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow1");
        	int alpha1 = (int) (Math.min(info.charge, 100) * alphaMult);
        	
        	MagicRender.singleframe(GlowTatt1, spritePos, spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha1), true);
        	
        	if (info.charge > 100f) {
        		SpriteAPI GlowTatt2 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow2");
            	int alpha2 = (int) (Math.min(info.charge - 100f, 255f) * alphaMult);
            	
            	MagicRender.singleframe(GlowTatt2, MathUtils.getRandomPointInCircle(spritePos, 1f), spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha2), true);
        	}
        }
        // sprite rendering section - [end]
        
    	
        // ui info display section - [start]
        if (ship == Global.getCombatEngine().getPlayerShip()) {
        	
        	if (info.charge >= 100f) {
        		MagicUI.drawHUDStatusBar(ship,
						1f,
						new	Color(105,255,155,255),
						null,
						0,
						"CHARGE: " + (int) info.charge,
						"",
						false);
        	} else {
        		MagicUI.drawHUDStatusBar(ship,
        				Math.min(1f, info.charge * 0.01f),
        				Global.getSettings().getColor("textFriendColor").darker(),
						null,
						0,
						"CHARGE: " + (int) info.charge,
						"",
						false);
        	}
        }
        // ui info display section - [end]
        
        //TODO - comment this out for release!
        // debug display section - [start]
        Global.getCombatEngine().maintainStatusForPlayerShip("MALICEDEBUG3", "graphics/icons/hullsys/phase_cloak.png",  "Repair CD: " + info.repairCooldown, "Sys Charge: " + info.chargeSys, false);
        Global.getCombatEngine().maintainStatusForPlayerShip("MALICEDEBUG2", "graphics/icons/hullsys/phase_cloak.png", "Decay: " + info.decay, "currDamage: " + currDamage, false);
        Global.getCombatEngine().maintainStatusForPlayerShip("MALICEDEBUG1", "graphics/icons/hullsys/phase_cloak.png", "DEBUG INFO", "Charge: " + info.charge, false);
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
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		// UNDYING \\ MALICE
		
		// A heretic, clad in the husks of the dead. In order to hide her figure, she spreads a thick, dark mist. The black fog conceals her as she hunts her next victim.
		
		LabelAPI label = tooltip.addPara("This vessel features a unique system called the Malice Resonator, this improves the ships performance after dealing damage and can use the accumulated energy to repair itself.", pad);
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The rate of in-combat CR decay after peak performance time runs out is increased by %s.", pad, bad, "" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlight("" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("The resonator generates one charge for every %s damage the ship deals.", opad, h, "" + (int)DAMAGE_PER_CHARGE);
		label.setHighlight("" + (int)DAMAGE_PER_CHARGE);
		label.setHighlightColors(h);
		label = tooltip.addPara("The ship recieves bonuses to %s, %s and %s based on the number of stored charges.", pad, h, "Weapon Hitstrength", "Timescale", "Damage Resistance");
		label.setHighlight("Weapon Hitstrength", "Timescale", "Damage Resistance");
		label.setHighlightColors(h, h, h);
		label = tooltip.addPara("Active vent rate is increased based on the number of stored charges, but charges will also decay while actively venting.", pad);
		label = tooltip.addPara("If the ship does not generate any new charges for %s then stored charges will start to decay.", pad, bad, "" + DECAY_TIMER + " seconds");
		label.setHighlight("" + DECAY_TIMER + " seconds");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("If the ship drops below %s hull, and has at least %s charge stored, then the Resonator will consume %s charge to trigger an emergency repair.", pad, bad, "50%", "100", "100");
		label.setHighlight("50%", "100", "100");
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
            }
            if (target instanceof ShipAPI) {
                if (!((ShipAPI) target).isAlive()) return;
            }
            
            float totalDamage = 0;
            totalDamage += result.getDamageToHull();
            totalDamage += result.getDamageToShields();
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
	// damage dealt listener [end]
    
	
    private class ShipSpecificData {
    	private float charge = 0f;
    	private float chargeSys = 0f;
    	private float decay = -4f;
    	private boolean dead = false;
    	private boolean doOnce = true;
    	private float fadeIn = 0f;
    	private float repairCooldown = 5f;
    }

}
