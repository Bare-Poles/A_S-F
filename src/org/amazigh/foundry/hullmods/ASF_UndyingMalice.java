package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
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
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicIncompatibleHullmods;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_UndyingMalice extends BaseHullMod {
	
	public static final float MAINT_MALUS = 100f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
	
	public static final float DAMAGE_PER_CHARGE = 100f;
	public static final int DECAY_TIMER = 4;
	
	public static float PHASE_DISSIPATION_MULT_1 = 1.4f; // regen // ship has a weaker phase anchor (full diss bonus, but reduced reload/regen bonuses)
	public static float PHASE_DISSIPATION_MULT_2 = 1.6f; // reload
	public static float PHASE_DISSIPATION_MULT_3 = 2f; // diss
	public static float FLUX_THRESHOLD_INCREASE_PERCENT = 75f; // and super adaptive phase coils
	
	public static final Color PARTICLE_COLOR = new Color(255,52,84,255);
	public static final Color BLAST_COLOR = new Color(210,55,140,255);
	
	private IntervalUtil ventInterval1 = new IntervalUtil(0.3f,0.45f);
	private IntervalUtil ventInterval2 = new IntervalUtil(0.3f,0.45f);
	
	//TODO
	
	//timescale not having bullettime (fixed?)
	
	// revive is BROKEN (WHY!)
	 // doesn't seem to *do*
	 // also stops the ship from doing hull damage?????????
	
	//  vent vfx (could be a bit *more* (maybe also scale with charge?)
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		
		stats.getDynamic().getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyPercent(id, FLUX_THRESHOLD_INCREASE_PERCENT);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

		//ship.addListener(new ASF_UndyingDiveScript(ship));
		//ship.addListener(new ASF_maliceDamageListener(ship));

        MutableShipStatsAPI stats = ship.getMutableStats();
		if(stats.getVariant().getHullMods().contains("phase_anchor")){
			//if someone tries to install phase anchor, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"phase_anchor",
					"A_S-F_UndyingMalice"
					);	
		}
		if(stats.getVariant().getHullMods().contains("adaptive_coils")){
			//if someone tries to install adaptive phase coils, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"adaptive_coils",
					"A_S-F_UndyingMalice"
					);	
		}
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		

		ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("UNDYING_MALICE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }

        CombatEngineAPI engine = Global.getCombatEngine();
        
        if (info.doOnce) {
        	engine.getListenerManager().addListener(new ASF_UndyingDiveScript(ship));
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
						new Color(153,92,103,35),
						new Color(255,216,224,40));
		        
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
		
        
        // the damage listener and (standard) charge gain/decay - [start]
        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
        
        float currDamage = 0f;
        
        if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
            currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
        }
        
        if (customCombatData.get("ASF_undyingHullmodCharge" + ship.getId()) instanceof Float) {
        	info.charge = (float) customCombatData.get("ASF_undyingHullmodCharge" + ship.getId()); // updating charge if it was changed by the damage taken listener
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
        
        customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
        // the damage listener and (standard) charge gain/decay - [end]
        
        
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
        

        // vent section - [start]
        stats.getVentRateMult().modifyPercent(spec.getId(), info.charge); // boost vent rate by an amount proportional to current charge
        
		if (ship.getFluxTracker().isVenting()) {
			info.charge = Math.max(0f, info.charge - (Math.max(5f, info.charge * 0.1f) * amount)); // while venting, charge decays by the higher of   5% /sec   or   1/10th of current charge /sec
			
			ventInterval1.advance(amount);
            if (ventInterval1.intervalElapsed()) {
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.55f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		65f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.4f,
						0.7f,
						new Color(140,70,130,70),
						false);
            }
            
            ventInterval2.advance(amount);
            if (ventInterval2.intervalElapsed()) {
            	
            	for (int i=0; i < 3; i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f));
    				Global.getCombatEngine().addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.55f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
						65f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.4f,
						0.7f,
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
        if (info.charge >= 100f) {
        	if (info.fadeIn < 1f) {
            	info.fadeIn = Math.min(1f, info.fadeIn - (amount * 1.5f));
        	}
        }
    	if (info.fadeIn > 0f) {
        	SpriteAPI GlowPipe = Global.getSettings().getSprite("fx", "A_S-F_persenachia_pipe_glow");
        	MagicRender.singleframe(GlowPipe, spritePos, spriteSize, ship.getFacing() - 90f, new Color(80,255,175,(int) 180 * alphaMult * info.fadeIn), true);
        	
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
        Global.getCombatEngine().maintainStatusForPlayerShip("MALICEDEBUG1", "graphics/icons/hullsys/phase_cloak.png", "DEBUG INFO", "Charge: " + info.charge, false);
        Global.getCombatEngine().maintainStatusForPlayerShip("MALICEDEBUG2", "graphics/icons/hullsys/phase_cloak.png", "Decay: " + info.decay, "currDamage: " + currDamage, false);
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
		
		LabelAPI label = tooltip.addPara("This vessel features a unique system called the Malice Resonator, this improves the ships performance after dealing damage and even allows it to cheat death.", pad);
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The rate of in-combat CR decay after peak performance time runs out is increased by %s.", pad, bad, "" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlight("" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("The resonator generates one charge for every %s damage the ship deals.", opad, h, "" + (int) DAMAGE_PER_CHARGE);
		label.setHighlight("" + (int) DAMAGE_PER_CHARGE);
		label.setHighlightColors(h);
		label = tooltip.addPara("The ship recieves bonuses to Weapon Performance against Armour, Timescale and Damage Resistance based on the number of stored charges.", pad);
		label = tooltip.addPara("Active vent rate is increased based on the number of stored charges, but charges will also decay while actively venting.", pad);
		label = tooltip.addPara("If the ship does not generate any new charges for %s then stored charges will start to decay.", pad, bad, "" + DECAY_TIMER + " seconds");
		label.setHighlight("" + DECAY_TIMER + " seconds");
		label.setHighlightColors(bad);
		
		
		label = tooltip.addPara("When suffering critical damage that would otherwise destroy the ship, if there is at least %s available then the ship will initiate an emergency dive and engage a self-repair system, this consumes %s but restores the ship to full combat functionality.", opad, h, "100 Charge", "All Charges");
		label.setHighlight("100 Charge", "All Charges");
		label.setHighlightColors(h, bad);
		
		label = tooltip.addPara("%s or %s are incompatible with the Malice Resonator and cannot be installed on this vessel.", opad, bad, "Phase Anchor", "Adaptive Phase Coils");
		label.setHighlight("Phase Anchor", "Adaptive Phase Coils");
		label.setHighlightColors(bad, bad);
		
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
    
    
    // revive listener / anchor [start]
    public static class ASF_UndyingDiveScript implements AdvanceableListener, HullDamageAboutToBeTakenListener {
		public ShipAPI ship;
		public boolean emergencyDive = false;
		public float diveProgress = 0f;
		
		public static final Color DIVE_TEXT_COLOR = new Color(185,100,205,255);
		
		public static final Color UNDYING_TEXT_COLOR = new Color(210,175,121,255);
		
	    private float diveDuration = 5f; // how long the ship will "dive" for before reviving.
		
	    private final IntervalUtil repairSparkInterval = new IntervalUtil(0.05f, 0.05f);
	    
		
		public ASF_UndyingDiveScript(ShipAPI ship) {
			this.ship = ship;
		}
		
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			
				if (!emergencyDive) {
					boolean canDive = false;
					
			        Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
					float currCharge = 0f;
			        if (customCombatData.get("ASF_undyingHullmodCharge" + ship.getId()) instanceof Float) {
			            currCharge = (float) customCombatData.get("ASF_undyingHullmodCharge" + ship.getId());
			        }
					
					if (ship.getCurrentCR() > 0f && currCharge >= 100f) {
						canDive = true;
						// so we can only do this if we have some CR remaining
					}
					
					float hull = ship.getHitpoints();
					if (damageAmount >= hull && canDive) {
						
						// repair hull!
						ship.setHitpoints(ship.getMaxHitpoints());
						
						//full armour repair!
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
						
						emergencyDive = true;
						
						if (!ship.isPhased()) {
							Global.getSoundPlayer().playSound("system_phase_cloak_activate", 1f, 1f, ship.getLocation(), ship.getVelocity());

							Global.getSoundPlayer().playSound("ui_refit_slot_filled_energy_large", 1.2f, 1.0f, ship.getLocation(), ship.getVelocity());
							//TODO test sound
							
					        customCombatData.put("ASF_undyingHullmodCharge" + ship.getId(), 0f); // resetting charge!
						}
					}
				}
			
			if (emergencyDive) {
				return true;
			}
			
			return false;
		}

		public void advance(float amount) {
			String id = "undying_dive_modifier";
			
			if (emergencyDive) {
				
				if (diveProgress == 0f) {
					
					if (ship.getFluxTracker().showFloaty()) {
						float timeMult = ship.getMutableStats().getTimeMult().getModifiedValue();
						Global.getCombatEngine().addFloatingTextAlways(ship.getLocation(),
								"Emergency dive!",
								NeuralLinkScript.getFloatySize(ship), DIVE_TEXT_COLOR, ship, 16f * timeMult, 3.2f/timeMult, 1f/timeMult, 0f, 1f/timeMult,
								1f);
					}
				}
				
				// clear flux (also so we don't break this from phase cost!)
				ship.getFluxTracker().setHardFlux(0f);
				ship.getFluxTracker().setCurrFlux(0f);
				
				
				diveProgress += amount;
				
				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				
				float curr = ship.getExtraAlphaMult();
				ship.getPhaseCloak().forceState(SystemState.IN, Math.min(1f, Math.max(curr, diveProgress)));
				
				MutableShipStatsAPI stats = ship.getMutableStats();
				
				stats.getHullDamageTakenMult().modifyMult(id, 0f);
				
				stats.getCombatEngineRepairTimeMult().modifyMult(id, 0.1f); // we repair weppies/engines!
				stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.1f);
				
				stats.getEnergyRoFMult().modifyMult(id, 10f); // we also reload all weppies!
				stats.getBallisticRoFMult().modifyMult(id, 10f);
				stats.getMissileRoFMult().modifyMult(id, 10f);
				
				
				Vector2f spritePos = MathUtils.getPointOnCircumference(ship.getLocation(), 2f, ship.getFacing());
				Vector2f spriteSize = new Vector2f(98f, 104f);
				
				SpriteAPI GlowTatt1 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow1");
				int alpha1 = (int) (50f - (diveProgress * 10f));
				
				MagicRender.singleframe(GlowTatt1, spritePos, spriteSize, ship.getFacing() - 90f, new Color(125,255,135,alpha1), true);
				
				if (diveProgress < 2.5f) {
					SpriteAPI GlowTatt2 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow2");
					int alpha2 = (int) (50f - diveProgress * 20f);
					
					MagicRender.singleframe(GlowTatt2, MathUtils.getRandomPointInCircle(spritePos, 1f), spriteSize, ship.getFacing() - 90f, new Color(80,255,175,alpha2), true);
				}
				
				repairSparkInterval.advance(amount);
    	        if (repairSparkInterval.intervalElapsed()) {
    	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f);
    	        	Global.getCombatEngine().addHitParticle(sparkLoc, ship.getVelocity(),
    	        			MathUtils.getRandomNumberInRange(5f, 10f), //size
    	        			0.8f, //bright
    	        			0.4f, //dur
    	        			new Color(50, 240, 100));
    	        }
				
				
				if (diveProgress >= diveDuration) {
					emergencyDive = false;
					diveProgress = 0f;
					
					ship.getPhaseCloak().forceState(SystemState.OUT, 0f);
					
					stats.getHullDamageTakenMult().unmodify(id);
					
					stats.getCombatEngineRepairTimeMult().unmodify(id);
					stats.getCombatWeaponRepairTimeMult().unmodify(id);
					stats.getEnergyRoFMult().unmodify(id);
					stats.getBallisticRoFMult().unmodify(id);
					stats.getMissileRoFMult().unmodify(id);
					
				}
				
			}
			
			
			boolean phased = ship.isPhased();
			if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isChargedown()) {
				phased = false;
			}
			
			MutableShipStatsAPI stats = ship.getMutableStats();
			if (phased) {
				stats.getFluxDissipation().modifyMult(id, PHASE_DISSIPATION_MULT_3);
				stats.getBallisticRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT_2);
				stats.getEnergyRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT_2);
				stats.getMissileRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT_2);
				stats.getBallisticAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT_1);
				stats.getEnergyAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT_1);
				stats.getMissileAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT_1);
			} else {
				stats.getFluxDissipation().unmodifyMult(id);
				stats.getBallisticRoFMult().unmodifyMult(id);
				stats.getEnergyRoFMult().unmodifyMult(id);
				stats.getMissileRoFMult().unmodifyMult(id);
				stats.getBallisticAmmoRegenMult().unmodifyMult(id);
				stats.getEnergyAmmoRegenMult().unmodifyMult(id);
				stats.getMissileAmmoRegenMult().unmodifyMult(id);
			}
			
		}

	}
    // revive listener / anchor [end] 
    
	
    private class ShipSpecificData {
    	private float charge = 0f;
    	private float decay = -4f;
    	private boolean dead = false;
    	private boolean doOnce = true;
    	private float fadeIn = 0f;
    }

}
