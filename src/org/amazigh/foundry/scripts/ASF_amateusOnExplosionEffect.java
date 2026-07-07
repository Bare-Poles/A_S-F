package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class ASF_amateusOnExplosionEffect implements ProximityExplosionEffect {

	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {

		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f point = explosion.getLocation();
		Vector2f fxVel = explosion.getVelocity();
		
		ASF_RadialEmitter emitterBase = new ASF_RadialEmitter((CombatEntityAPI) explosion);
		emitterBase.location(point);
		emitterBase.life(0.6f, 1.4f);
		emitterBase.size(6f, 8f);
		emitterBase.distance(5f, 70f);
		emitterBase.velocity(20f, 120f);
		emitterBase.color(60,90,240,240);
		emitterBase.lifeLinkage(true);
		emitterBase.sizeScale(-4f);
		emitterBase.burst(180);
		
		engine.spawnExplosion(point, fxVel, new Color(85,160,240,150), 200f, 0.8f);
		
		for (int i=0; i < 5; i++) {
			engine.addSwirlyNebulaParticle(point,
					fxVel,
					30f * i,
					1.8f,
					0.1f,
					0.36f,
					MathUtils.getRandomNumberInRange(1.2f, 1.4f) + (i * 0.1f),
					new Color(60,75,120,99),
					true);
		}
		
		
		SpriteAPI markRing = Global.getSettings().getSprite("fx", "A_S-F_shockwave");
		Vector2f ringSize = new Vector2f(MathUtils.getRandomNumberInRange(70f, 85f),MathUtils.getRandomNumberInRange(70f, 85f));
		Vector2f ringGrowth = new Vector2f(MathUtils.getRandomNumberInRange(270f, 300f),MathUtils.getRandomNumberInRange(270f, 300f));
		
		int r = MathUtils.getRandomNumberInRange(70, 85);
		int g = MathUtils.getRandomNumberInRange(65, 75);
		int b = MathUtils.getRandomNumberInRange(230, 255);
		
		MagicRender.battlespace(markRing, point, fxVel,
				ringSize, //size
				ringGrowth, //growth
				MathUtils.getRandomNumberInRange(0f,360f), //angle
				MathUtils.getRandomNumberInRange(-10f,10f),  //spin
				new Color(r,g,b,150),
				true,
				0.05f, //fadein
				0.25f, //full
				0.7f); //fadeout
		
		SpriteAPI markRing2 = Global.getSettings().getSprite("fx", "A_S-F_shockwave");
		Vector2f ringSize2 = new Vector2f(MathUtils.getRandomNumberInRange(80f, 95f),MathUtils.getRandomNumberInRange(80f, 95f));
		Vector2f ringGrowth2 = new Vector2f(MathUtils.getRandomNumberInRange(300f, 330f),MathUtils.getRandomNumberInRange(300f, 330f));
		
		int r2 = MathUtils.getRandomNumberInRange(80, 95);
		int g2 = MathUtils.getRandomNumberInRange(55, 65);
		int b2 = MathUtils.getRandomNumberInRange(230, 255);
		
		MagicRender.battlespace(markRing2, point, fxVel,
				ringSize2, //size
				ringGrowth2, //growth
				MathUtils.getRandomNumberInRange(0f,360f), //angle
				MathUtils.getRandomNumberInRange(-10f,10f),  //spin
				new Color(r2,g2,b2,150),
				true,
				0.05f, //fadein
				0.2f, //full
				0.75f); //fadeout
	
	}
}
