package org.amazigh.foundry.scripts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.Description;

import org.amazigh.foundry.scripts.ai.ASF_AlbatreosMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_FormiaMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LamiaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LernaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_LernaSubMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_MagicSwarmMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PersisMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PersisSwarmMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_PhiliaMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_RocketArtyMagicMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_TermiteMissileAI;
import org.amazigh.foundry.scripts.ai.ASF_WeaverDrunkRocketAI;
import org.amazigh.foundry.scripts.everyframe.ASF_arkTechSpawnPlugin;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;

import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import exerelin.utilities.NexFactionConfig.StartFleetSet;
import exerelin.utilities.NexFactionConfig.StartFleetType;

public class ASF_ModPlugin extends BaseModPlugin {
	
	public static final String LAMIA_MISSILE_ID = "A_S-F_lamia_main";
	public static final String LERNA_MISSILE_ID = "A_S-F_lerna_main";
	public static final String LERNA_SUB_MISSILE_ID = "A_S-F_lerna_sub";
	public static final String DESTRUCTOR_ROCKET_ID = "A_S-F_destructor_rocket";
	public static final String CONTRADICT_MISSILE_ID = "A_S-F_contradict_rocket";
	public static final String OMER_MISSILE_ID = "A_S-F_omer_srm";
	public static final String ASPINA_MISSILE_ID = "A_S-F_aspina_srm";
	public static final String AKVAVIT_MISSILE_ID = "A_S-F_akvavit_srm";
	public static final String FORMIA_MISSILE_ID = "A_S-F_formia_orb";
	public static final String FORMIA_SYS_MISSILE_ID = "A_S-F_formia_orb_sys";
	public static final String ASF_COPPERHEAD_MISSILE_ID = "A_S-F_copperhead_mssl";
	public static final String ASF_ALBATREOS_MISSILE_ID = "A_S-F_albatreos_missile";
	public static final String ASF_PHILIA_MISSILE_ID = "A_S-F_philia_srm";
	public static final String ASF_PHANTASMAGORIA_MICRO_MISSILE_ID = "A_S-F_phantasmagoria_micro_missile";
	public static final String ASF_TERMITE_MISSILE_ID = "A_S-F_termite_srm";
	public static final String ASF_PERSIS_MISSILE_ID = "A_S-F_persis_missile";
	public static final String ASF_PERSIS_SUB_MISSILE_ID = "A_S-F_persis_frag";
	public static final String ASF_WEAVER_ROCKET_ID = "A_S-F_weaver_rocket";

	public boolean HAS_GRAPHICSLIB = false;
    public boolean isExerelin = false;
    public boolean ratInfestation = false;
    
	public static String PHANTASMAGORIA_ALT_DESCRIPTION = "\"I am only satisfied if my spectators, shivering and shuddering, raise their hands or cover their eyes out of fear of ghosts and devils dashing towards them.\" - Found etched on primary flight control interface.";
    public static String TRANSPARENCE_ALT_DESCRIPTION = "The Transparence is a unique prototype ship featuring a wide variety of bleeding-edge technologies. The Photon Accelerator Core around which this vessel has been constructed stretches the bounds of what could be considered possible by conventional domain science to allow for the ship to deliver cruiser-grade levels of firepower while being nearly as agile as some frigates.";
    public static String RANGDA_ALT_DESCRIPTION = "A prototype testbed for a novel vectored thrust system, the Rangda is one of the most slippery vessels in the sector, able to get in and out of combat with ease.";
    public static String LAFIEL_ALT_DESCRIPTION = "Origins unknown, the designers clearly thought that it'd be sane to expose living crew to a rapidly fluctuating temporal gradient. Aftereffects of combat deployment mean that even with stringent psychological profiling, frequent cycling of crews is highly recommended.";
    public static String PERSENACHIA_ALT_DESCRIPTION = "A heretic, surrounded with the husks of the dead. In order to hide its form, it spreads a dense and violent storm. The glowing fog conceals it while it hunts its prey and then entraps them in a dance of death, creating more victims.";
    
    //New game stuff
    @Override
    public void onNewGameAfterProcGen() {
        //Spawning arkTech
    	ASF_arkTechSpawnPlugin.spawnArkTech(Global.getSector());
    }
    
    public void onApplicationLoad() throws Exception {

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            //TextureData.readTextureDataCSV((String)"data/config/asf_texture_data.csv");
            LightData.readLightDataCSV((String)"data/config/asf_lights_data.csv");
        }
    	
    	isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
    	if(isExerelin) {
    		if (Global.getSettings().getMissionScore("ASF_phantasmagoria_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetPhant = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> phantasmagoriaFleet = new ArrayList<>(1);
    			phantasmagoriaFleet.add("A_S-F_phantasmagoria_starter");
    			fleetSetPhant.addFleet(phantasmagoriaFleet);
    			Global.getSettings().getDescription("A_S-F_phantasmagoria", Description.Type.SHIP).setText2(PHANTASMAGORIA_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_transparence_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetTrans = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> transparenceFleet = new ArrayList<>(1);
    			transparenceFleet.add("A_S-F_transparence_starter");
    			fleetSetTrans.addFleet(transparenceFleet);
    			Global.getSettings().getDescription("A_S-F_transparence", Description.Type.SHIP).setText2(TRANSPARENCE_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_rangda_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetRangda = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> rangdaFleet = new ArrayList<>(1);
    			rangdaFleet.add("A_S-F_rangda_starter");
    			fleetSetRangda.addFleet(rangdaFleet);
    			Global.getSettings().getDescription("A_S-F_rangda", Description.Type.SHIP).setText2(RANGDA_ALT_DESCRIPTION);
    		}
    		if (Global.getSettings().getMissionScore("ASF_persenachia_mission") > 0) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetPers = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> persenachiaFleet = new ArrayList<>(1);
    			persenachiaFleet.add("A_S-F_persenachia_starter");
    			fleetSetPers.addFleet(persenachiaFleet);
    			Global.getSettings().getDescription("A_S-F_persenachia", Description.Type.SHIP).setText2(PERSENACHIA_ALT_DESCRIPTION);
    		}
    		
    		// so you *can* unlock the lafiel as a custom start, "just" beat all special missions with over 95% score (only 75% score is needed for the test mission tho!)
    		if (Global.getSettings().getMissionScore("ASF_phantasmagoria_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_transparence_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_rangda_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_persenachia_mission") > 0.95f && Global.getSettings().getMissionScore("ASF_arkDefenders") > 0.95f && Global.getSettings().getMissionScore("ASF_testbattle") > 0.75f) {
    			NexFactionConfig faction = NexConfig.getFactionConfig("player");
    			StartFleetSet fleetSetLafiel = faction.getStartFleetSet(StartFleetType.SUPER.name());
    			List<String> lafielFleet = new ArrayList<>(1);
    			lafielFleet.add("A_S-F_lafiel_starter");
    			fleetSetLafiel.addFleet(lafielFleet);
    			Global.getSettings().getDescription("A_S-F_lafiel", Description.Type.SHIP).setText2(LAFIEL_ALT_DESCRIPTION);
    		}
    		
    	}
    	
    	
    	// fucking with variants if we have rotcesrats installed
    	ratInfestation = Global.getSettings().getModManager().isModEnabled("rotcesrats");
    	if(ratInfestation) {
    		Global.getSettings().resetCached();
            if (Global.getSettings().getVariant("A_S-F_rinka_p_raider") != null) {
                ShipVariantAPI RaiderRinkaVariant = Global.getSettings().getVariant("A_S-F_rinka_p_raider");
                if (Global.getSettings().getWeaponSpec("rr_miser_coil") != null) {
                        RaiderRinkaVariant.setNumFluxCapacitors(RaiderRinkaVariant.getNumFluxCapacitors()-1); //8 to 7
                        RaiderRinkaVariant.clearSlot("WS0002");
                        RaiderRinkaVariant.addWeapon("WS0002","rr_miser_coil"); //from thumper
                        RaiderRinkaVariant.clearHullMods(); // to strip big mags
                        RaiderRinkaVariant.addMod("armoredweapons");
                        RaiderRinkaVariant.addMod("fluxdistributor");
                        RaiderRinkaVariant.addMod("frontemitter");
                        
                }
            }
    	}
    	
	}

	
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case LAMIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LamiaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case LERNA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LernaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case LERNA_SUB_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_LernaSubMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case DESTRUCTOR_ROCKET_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case CONTRADICT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case OMER_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASPINA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case AKVAVIT_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FORMIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_FormiaMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case FORMIA_SYS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_FormiaMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_COPPERHEAD_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_RocketArtyMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_ALBATREOS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_AlbatreosMagicMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PHILIA_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PhiliaMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PHANTASMAGORIA_MICRO_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_MagicSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_TERMITE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_TermiteMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PERSIS_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PersisMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_PERSIS_SUB_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_PersisSwarmMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case ASF_WEAVER_ROCKET_ID:
                return new PluginPick<MissileAIPlugin>(new ASF_WeaverDrunkRocketAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
                return null;
        }
    }
}