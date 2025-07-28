package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;

//TODO!
//TODO!

	// THIS IS WIP CONTENT OK!!!

//TODO!
//TODO!

public class ASF_ExpiationWeaponScript implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin  {
	
	private int chargeCount = 0;
	
	private IntervalUtil muzzleInterval = new IntervalUtil(0.05f, 0.05f);
	
	private static final Color STAGE_1_COLOR = new Color(0,255,100,200);
	private static final Color STAGE_2_COLOR = new Color(0,175,255,200);
	private static final Color STAGE_3_COLOR = new Color(125,50,255,200);
	private static final Color STAGE_3B_COLOR = new Color(100,75,240,120);
	private static final Color STAGE_4_COLOR = new Color(255,0,150,200);
	private static final Color STAGE_4B_COLOR = new Color(225,0,175,120);
	
	public static Map<Integer, String> shotType = new HashMap<Integer, String>();
	static {
		shotType.put(9, "gigacannon");
		shotType.put(10, "gigacannon");
		shotType.put(11, "gigacannon");
		shotType.put(12, "gigacannon");
		shotType.put(13, "gigacannon");
		shotType.put(14, "gigacannon");
		shotType.put(15, "A_S-F_ionDriver");
		shotType.put(16, "A_S-F_ionDriver");
		shotType.put(17, "A_S-F_ionDriver");
		shotType.put(18, "A_S-F_ionDriver");
		shotType.put(19, "A_S-F_ionDriver");
		shotType.put(20, "A_S-F_ionDriver");
		shotType.put(21, "A_S-F_ionDriver");
		shotType.put(22, "A_S-F_ionDriver");
		shotType.put(23, "A_S-F_ionDriver");
		shotType.put(24, "A_S-F_ionDriver");
		shotType.put(25, "A_S-F_silaha");
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
    	engine.removeEntity(projectile);
    	
    	//TODO - play base charge sound
    	Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
		
    	
    	if (chargeCount == 2 || chargeCount == 8 || chargeCount == 14) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "lv1" sound
    	}
    	if (chargeCount == 3 || chargeCount == 9 || chargeCount == 15) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1.05f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "lv2" sound
    	}
    	if (chargeCount == 4 || chargeCount == 10 || chargeCount == 16) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1.1f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "lv3" sound
    	}
    	if (chargeCount == 5 || chargeCount == 11 || chargeCount == 17) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1.15f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "lv4" sound
    	}
    	if (chargeCount == 6 || chargeCount == 12 || chargeCount == 18) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1.25f, 1.1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "lv5!" sound
    	}
    	if (chargeCount == 24) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "final" sound
    	}
    	if (chargeCount > 31) {
//    		Global.getSoundPlayer().playSound("A_S-F_painter_loop", 1f, 1f, weapon.getFirePoint(0), weapon.getShip().getVelocity());
    		// play "warning" sound
    	}
    	//TODO - sounds!!
    	//TODO - sounds!!
    	//TODO - sounds!!
    	//TODO - sounds!!
    	
    	
    	if (chargeCount == 0) {
    		weapon.getGlowSpriteAPI().setColor(STAGE_1_COLOR);
    	}
    	if (chargeCount == 8) {
    		weapon.getGlowSpriteAPI().setColor(STAGE_2_COLOR);
    	}
    	if (chargeCount == 14) {
    		weapon.getGlowSpriteAPI().setColor(STAGE_3_COLOR);
    	}
    	if (chargeCount == 24) {
    		weapon.getGlowSpriteAPI().setColor(STAGE_4_COLOR);
    	}
    	
		chargeCount ++;
		
	}
		
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
		if (engine.isPaused()) {
			return;
		}
		
		if (!weapon.getShip().isAlive()) {
			chargeCount = 0;
			return;
		}
		
		if (weapon.getShip() == engine.getPlayerShip()) {
			engine.maintainStatusForPlayerShip("EXPITCHARGE", "graphics/icons/hullsys/entropy_amplifier.png", "Charge Count: " + chargeCount, "WepState: " + weapon.getChargeLevel(), false);
    	}
		
		
		//TODO
		//TODO
		//TODO - think about how i'm handling sounds!!
			// i guess just a pure-script based method (some horrible if/else chain in the onFire ???)
			// need to also think about the "startup" sound, and if i want to keep it ??
		//TODO
		//TODO
			// pulse some sort of visual on the "warning" charges!
		//TODO
		//TODO
		
		
		
		if (weapon.getChargeLevel() < 0.05) {
			weapon.getGlowSpriteAPI().setColor(STAGE_1_COLOR); // resetting glow color!
			chargeCount = 0;
		}
		
		if (chargeCount > 0) {
			ShipAPI ship = weapon.getShip();
			float baseAngle = weapon.getCurrAngle();
			
			double timeMult = (double) ship.getMutableStats().getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
    		int alpha = (int) Math.ceil(69 / timeMult);
    		alpha = Math.min(250, alpha);
    		
    		int alpha2 = (int) Math.ceil(43 / timeMult);
    		alpha2 = Math.min(250, alpha);
    		//TODO - decide if i need a second alpha var for the "pips"
			
			if (ship.getFluxTracker().isOverloaded()) {
				chargeCount = 0; // clear charges if you overload, so you should release and fire if at risk of overload, no autofiring!
			}
			
			
			if (weapon.getChargeLevel() < 0.9f) {
				// we fire when `chargeLevel` goes down, or: "how to tell that the trigger has been released"
				
				//TODO
				//TODO - real fire thing (variable projectiles / etc)
				//TODO - muzzle fx (flash, base particles, particle spray, nebs?, etc)
				//TODO

				ASF_RadialEmitter emitterMuzzle1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
				emitterMuzzle1.location(weapon.getFirePoint(0));
				emitterMuzzle1.life(0.18f, 0.25f);
				emitterMuzzle1.size(13f, 16f);
    			emitterMuzzle1.coreDispersion(5f);
				
				ASF_RadialEmitter emitterMuzzle2 = new ASF_RadialEmitter((CombatEntityAPI) ship);
				emitterMuzzle2.location(weapon.getFirePoint(0));
    			emitterMuzzle2.life(0.7f, 1.8f);
    			emitterMuzzle2.size(2f, 3.5f);
    	        emitterMuzzle2.velDistLinkage(false);
    	        emitterMuzzle2.lifeLinkage(true);
				
				if (chargeCount > 2) {
					if (chargeCount > 8) {
						CombatEntityAPI boltProjectile = engine.spawnProjectile(weapon.getShip(), weapon, "" + shotType.get(Math.min(chargeCount, 25)), weapon.getFirePoint(0), baseAngle, ship.getVelocity());
						//TODO - attach any everyframes that are needed for proj trails/etc
						
						if (chargeCount > 14) {
							if (chargeCount > 24) {
								//TODO - vortex muzzle!
							} else {
								//TODO - emp muzzle!
							}
						} else {
							//TODO - bolt muzzle!
						}
						
						
					} else {
						//shotgun shots!
						//TODO - shotgun muzzle!
						
						//TODO - neb(s)!
						
						emitterMuzzle1.angle(baseAngle -22f, 44f);
						emitterMuzzle1.velocity(0f, 15f);
						emitterMuzzle1.distance(0f, 30f);
						emitterMuzzle1.color(STAGE_1_COLOR.getRed(),STAGE_1_COLOR.getGreen(),STAGE_1_COLOR.getBlue(),STAGE_1_COLOR.getAlpha());
    	    			emitterMuzzle1.burst(35);
						
    	    			emitterMuzzle2.angle(baseAngle -15f, 30f);
    	    	        emitterMuzzle2.velocity(15f, 25f);
    	    	        emitterMuzzle2.distance(0f, 60f);
    	    	        emitterMuzzle2.color(STAGE_1_COLOR.getRed(),STAGE_1_COLOR.getGreen(),STAGE_1_COLOR.getBlue(),STAGE_1_COLOR.getAlpha());
    	    	        emitterMuzzle2.emissionOffset(-35f, 70f);
    	    	        emitterMuzzle2.burst(45);
    	    			
						
		            	for (int i=0; i < Math.min(8, (chargeCount+1)); i++) {
		            		float angle = baseAngle + MathUtils.getRandomNumberInRange(-(chargeCount + 6f), chargeCount + 6f);
		            		Vector2f vel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(-20f, 40f), angle);
		            		CombatEntityAPI shotgunProjectile = engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_expiation", weapon.getFirePoint(0), angle, vel);
		            		
		                	engine.addPlugin(new ASF_ExpiationProjScript((DamagingProjectileAPI) shotgunProjectile));
		            	}
					}
				}
				chargeCount = 0;
			}
			
			
			
			
			
			//TODO - proper charging muzzle particle stuff!
				//TODO - add nebs ?
				//TODO - test!
			
			muzzleInterval.advance(amount);
			if (muzzleInterval.intervalElapsed()) {
				
				ASF_RadialEmitter emitterFront = new ASF_RadialEmitter((CombatEntityAPI) ship);
		        emitterFront.location(weapon.getFirePoint(0));
				emitterFront.life(0.2f, 0.4f);
				emitterFront.size(2f, 3.5f);
				emitterFront.angle(baseAngle - 25f, 50f);
		        emitterFront.velDistLinkage(false);
		        
				if (chargeCount > 8) {
					
					ASF_RadialEmitter emitterJet = new ASF_RadialEmitter((CombatEntityAPI) ship);
			        emitterJet.location(weapon.getFirePoint(0));
					emitterJet.angle(baseAngle - 2f, 4f);
					emitterJet.life(0.35f, 0.5f);
					emitterJet.velDistLinkage(false);
					emitterJet.coreDispersion(2f);
					
					if (chargeCount > 14) {
						
						ASF_RadialEmitter emitterWide = new ASF_RadialEmitter((CombatEntityAPI) ship);
						emitterWide.location(weapon.getFirePoint(0));
						emitterWide.life(0.25f, 0.6f);
						emitterWide.size(5f, 7.5f);
						emitterWide.velDistLinkage(false);
						
						if (chargeCount > 24) {
							//vortex muzzle particle fx!
							emitterWide.angle(baseAngle - 55f, 110f);
							emitterWide.velocity(10f, 15f);
							emitterWide.distance(8f, 5f);
							emitterWide.color(STAGE_4B_COLOR.getRed(),STAGE_4B_COLOR.getGreen(),STAGE_4B_COLOR.getBlue(),STAGE_4B_COLOR.getAlpha());
							emitterWide.coreDispersion(8f);
							emitterWide.burst(3);
							
							emitterJet.size(4f, 5.5f);
							emitterJet.velocity(48f, 96f);
							emitterJet.color(STAGE_4_COLOR.getRed(),STAGE_4_COLOR.getGreen(),STAGE_4_COLOR.getBlue(),STAGE_4_COLOR.getAlpha());
							emitterJet.burst(6);
							
							emitterFront.velocity(20f, 25f);
							emitterFront.distance(5f, 15f);
							emitterFront.color(STAGE_4_COLOR.getRed(),STAGE_4_COLOR.getGreen(),STAGE_4_COLOR.getBlue(),STAGE_4_COLOR.getAlpha());
							emitterFront.coreDispersion(3f);
							emitterFront.burst(4);
						} else {
							//emp muzzle particle fx!
							emitterWide.angle(baseAngle - 50f, 100f);
							emitterWide.velocity(10f, 15f);
							emitterWide.distance(8f, 5f);
							emitterWide.color(STAGE_3B_COLOR.getRed(),STAGE_3B_COLOR.getGreen(),STAGE_3B_COLOR.getBlue(),STAGE_3B_COLOR.getAlpha());
							emitterWide.coreDispersion(8f);
							emitterWide.burst(2);

							emitterJet.size(3.5f, 5f);
							emitterJet.velocity(44f, 86f);
							emitterJet.color(STAGE_3_COLOR.getRed(),STAGE_3_COLOR.getGreen(),STAGE_3_COLOR.getBlue(),STAGE_3_COLOR.getAlpha());
							emitterJet.burst(6);
							
							emitterFront.velocity(20f, 25f);
							emitterFront.distance(5f, 15f);
							emitterFront.color(STAGE_3_COLOR.getRed(),STAGE_3_COLOR.getGreen(),STAGE_3_COLOR.getBlue(),STAGE_3_COLOR.getAlpha());
							emitterFront.coreDispersion(3f);
							emitterFront.burst(5);
						}
					} else {
						//bolt muzzle particle fx!
						emitterJet.size(4f, 5.5f);
						emitterJet.velocity(40f, 76f);
						emitterJet.color(STAGE_2_COLOR.getRed(),STAGE_2_COLOR.getGreen(),STAGE_2_COLOR.getBlue(),STAGE_2_COLOR.getAlpha());
						emitterJet.burst(6);
						
						emitterFront.velocity(20f, 25f);
						emitterFront.distance(5f, 15f);
						emitterFront.color(STAGE_2_COLOR.getRed(),STAGE_2_COLOR.getGreen(),STAGE_2_COLOR.getBlue(),STAGE_2_COLOR.getAlpha());
						emitterFront.coreDispersion(3f);
						emitterFront.burst(6);
					}
				} else {
					//shotgun muzzle particle fx!
					emitterFront.velocity(20f, 25f);
					emitterFront.distance(5f, 15f);
					emitterFront.color(STAGE_1_COLOR.getRed(),STAGE_1_COLOR.getGreen(),STAGE_1_COLOR.getBlue(),STAGE_1_COLOR.getAlpha());
					emitterFront.coreDispersion(5f);
					emitterFront.burst(7);
				}
			}
			
			
			//TODO - side muzzle glow thing! (starts on Lv3, "grows in")
			
			int glowR = 0;
			int glowG = 0;
			int glowB = 0;
			SpriteAPI chargeGlow = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
			
			if (chargeCount > 8) {
				if (chargeCount > 14) {
					if (chargeCount > 24) {
						glowR = STAGE_4_COLOR.getRed();
						glowG = STAGE_4_COLOR.getGreen();
						glowB = STAGE_4_COLOR.getBlue();
						
        				//TODO - vortex side muzzle glow thing!
						
						Vector2f spriteSize2 = new Vector2f(9, 60);
						Vector2f spriteSize3 = new Vector2f(12, 80);
						SpriteAPI muzzGlow1 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
						SpriteAPI muzzGlow2 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
						
		        		MagicRender.singleframe(muzzGlow1, weapon.getFirePoint(0), spriteSize2, baseAngle, new Color(glowR,glowG,glowB,alpha2), true);
		        		MagicRender.singleframe(muzzGlow2, weapon.getFirePoint(0), spriteSize3, baseAngle, new Color(glowR,glowG,glowB,alpha2), true);
						//TODO
						//TODO
						//TODO
						//TODO
					} else {
						glowR = STAGE_3_COLOR.getRed();
						glowG = STAGE_3_COLOR.getGreen();
						glowB = STAGE_3_COLOR.getBlue();
						
						Vector2f spriteSize2 = new Vector2f(9, 60);
						SpriteAPI muzzGlow1 = Global.getSettings().getSprite("campaignEntities", "fusion_lamp_glow");
						//TODO!
		        		MagicRender.singleframe(muzzGlow1, weapon.getFirePoint(0), spriteSize2, baseAngle, new Color(glowR,glowG,glowB,alpha2), true);
					}
				} else {
					glowR = STAGE_2_COLOR.getRed();
					glowG = STAGE_2_COLOR.getGreen();
					glowB = STAGE_2_COLOR.getBlue();
				}
			} else {
				glowR = STAGE_1_COLOR.getRed();
				glowG = STAGE_1_COLOR.getGreen();
				glowB = STAGE_1_COLOR.getBlue();
			}
			
			for (int i=0; i < 2; i++) {
				float sizeRandom = Math.min(40f, (20f + chargeCount)) + MathUtils.getRandomNumberInRange(0f, 3f);
    			Vector2f spriteSize1 = new Vector2f(sizeRandom, sizeRandom);
        		MagicRender.singleframe(chargeGlow, weapon.getFirePoint(0), spriteSize1, MathUtils.getRandomNumberInRange(0f, 90f), new Color(glowR,glowG,glowB,alpha), true);
        	}
			
        	
    		
			//TODO - charge indicators!
				//TODO - actual proper size/sprite/positioning (requires final sprites)
			
			Vector2f baseOffset = MathUtils.getPointOnCircumference(weapon.getLocation(), 20f, baseAngle + 90f);
			SpriteAPI chargePip1 = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
			SpriteAPI chargePip2 = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
			SpriteAPI chargePip3 = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
			SpriteAPI chargePip4 = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
			SpriteAPI chargePip5 = Global.getSettings().getSprite("fx", "A_S-F_arty_target_pip");
			Vector2f spriteSize = new Vector2f(4f, 8f);
			
//        	for (int i=0; i < chargeCount; i++) {
//        		Vector2f pipLoc = MathUtils.getPointOnCircumference(baseOffset, i * 6f, baseAngle);
//        		
//        		Color temp = STAGE_1_COLOR;
//        		if (chargeCount > 6) {
//        			if (chargeCount > 12) {
//        				if (chargeCount > 18) {
//        					temp = STAGE_4_COLOR;
//        				} else {
//        					temp = STAGE_3_COLOR;
//        				}
//        			} else {
//        				temp = STAGE_2_COLOR;
//        			}
//        		}
//        		MagicRender.singleframe(chargePip, pipLoc, spriteSize, baseAngle, temp, true);
//        	}
        	
        	
        	// "bar" #1
        	if (chargeCount > 2) {
        		if (chargeCount > 8) {
    				if (chargeCount > 14) {
    					if (chargeCount > 24) {
    						MagicRender.singleframe(chargePip1, baseOffset, spriteSize, baseAngle, STAGE_4_COLOR, true);
    					} else {
    						MagicRender.singleframe(chargePip1, baseOffset, spriteSize, baseAngle, STAGE_3_COLOR, true);
    					}
    				} else {
    					MagicRender.singleframe(chargePip1, baseOffset, spriteSize, baseAngle, STAGE_2_COLOR, true);
    				}
    			} else {
    				MagicRender.singleframe(chargePip1, baseOffset, spriteSize, baseAngle, STAGE_1_COLOR, true);
    			}
        	}
			
        	// "bar" #2
        	if (chargeCount > 3) {
			Vector2f pipLoc = MathUtils.getPointOnCircumference(baseOffset, 6f, baseAngle);
    			if (chargeCount > 9) {
    				if (chargeCount > 15) {
    					if (chargeCount > 24) {
    	    				MagicRender.singleframe(chargePip2, pipLoc, spriteSize, baseAngle, STAGE_4_COLOR, true);
    					} else {
    	    				MagicRender.singleframe(chargePip2, pipLoc, spriteSize, baseAngle, STAGE_3_COLOR, true);
    					}
    				} else {
        				MagicRender.singleframe(chargePip2, pipLoc, spriteSize, baseAngle, STAGE_2_COLOR, true);
    				}
    			} else {
    				MagicRender.singleframe(chargePip2, pipLoc, spriteSize, baseAngle, STAGE_1_COLOR, true);
    			}
        	}
        	
        	// "bar" #3
        	if (chargeCount > 4) {
    			Vector2f pipLoc = MathUtils.getPointOnCircumference(baseOffset, 12f, baseAngle);
    			if (chargeCount > 10) {
    				if (chargeCount > 16) {
    					if (chargeCount > 24) {
    	    				MagicRender.singleframe(chargePip3, pipLoc, spriteSize, baseAngle, STAGE_4_COLOR, true);
    					} else {
    	    				MagicRender.singleframe(chargePip3, pipLoc, spriteSize, baseAngle, STAGE_3_COLOR, true);
    					}
    				} else {
        				MagicRender.singleframe(chargePip3, pipLoc, spriteSize, baseAngle, STAGE_2_COLOR, true);
    				}
    			} else {
    				MagicRender.singleframe(chargePip3, pipLoc, spriteSize, baseAngle, STAGE_1_COLOR, true);
    			}
        	}
        	
        	// "bar" #4
        	if (chargeCount > 5) {
    			Vector2f pipLoc = MathUtils.getPointOnCircumference(baseOffset, 18f, baseAngle);
    			if (chargeCount > 11) {
    				if (chargeCount > 17) {
    					if (chargeCount > 24) {
    	    				MagicRender.singleframe(chargePip4, pipLoc, spriteSize, baseAngle, STAGE_4_COLOR, true);
    					} else {
    	    				MagicRender.singleframe(chargePip4, pipLoc, spriteSize, baseAngle, STAGE_3_COLOR, true);
    					}
    				} else {
        				MagicRender.singleframe(chargePip4, pipLoc, spriteSize, baseAngle, STAGE_2_COLOR, true);
    				}
    			} else {
    				MagicRender.singleframe(chargePip4, pipLoc, spriteSize, baseAngle, STAGE_1_COLOR, true);
    			}
        	}
        	
        	// "bar" #5
        	if (chargeCount > 6) {
			Vector2f pipLoc = MathUtils.getPointOnCircumference(baseOffset, 24f, baseAngle);
    			if (chargeCount > 12) {
    				if (chargeCount > 18) {
    					if (chargeCount > 24) {
    	    				MagicRender.singleframe(chargePip5, pipLoc, spriteSize, baseAngle, STAGE_4_COLOR, true);
    					} else {
    	    				MagicRender.singleframe(chargePip5, pipLoc, spriteSize, baseAngle, STAGE_3_COLOR, true);
    					}
    				} else {
        				MagicRender.singleframe(chargePip5, pipLoc, spriteSize, baseAngle, STAGE_2_COLOR, true);
    				}
    			} else {
    				MagicRender.singleframe(chargePip5, pipLoc, spriteSize, baseAngle, STAGE_1_COLOR, true);
    			}
        	}
        	
        	
		}
	}
  }