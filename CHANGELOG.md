# Changelog

## [10.0.3]
- Fixed a bug where terraforming projects that require the Atmosphere Processor and/or Stellar Reflectors could be started if those buildings were still under construction. Thanks lchronosl for reporting this!
- Updated the art for certain buildings.
- Updated all AotD Vaults of Knowledge research project thumbnails to utilize current artwork.

## [10.0.2]
- Fixed a bug where the Atmosphere Processor couldn't be constructed on irradiated planets even when the LunaLib setting to enable the "remove radiation" terraforming project was toggled on. Thanks abramsxie and Astrefernal for reporting this!
- Added new artwork for several buildings. I intend to make further updates to the artwork. If you strongly like or dislike any of the artwork in TASC, please let me know why in this thread!
- Updated the instructions for setting up a local development environment for TASC in the GitHub readme file.
- Cleaned up some outdated or incorrect information in the LunaLib settings file.

## [10.0.1]
- Fixed a bug where constructing a third astropolis in orbit around a colony could cause a crash. Thanks Dadada for reporting this and taking the time to provide your log file!
- Fixed a bug where clicking a terraforming project could cause a crash. Thanks LeNainBerb and Daredevil for reporting this!

## [10.0.0]
- Added a new UI for terraforming projects. It shows the appearance and conditions a planet will have when terraforming is complete. Thanks Kaysaar for all your help with integrating the new UI with AotD!
- Terraforming projects now appear in the intel screen and notify the player when an event occurs, such as project completion.
- The terraforming menu can now be opened from the colony management screen by clicking on a TASC building related to terraforming and selecting the "Open terraforming menu" option.
  - This feature is unsupported when using TASC in conjunction with Ashes of the Domain. I'm working on adding support in a future patch.
- Added projects to terraform planets into certain Unknown Skies planet types.
- Added terraforming support for all remaining Unknown Skies planet types.
- Added support for Unknown Skies market conditions that are relevant to terraforming.
- Terraforming projects now become stalled if their requirements become unmet. Previously all progress would be reset.
- Added tooltips to certain terraforming requirements to clarify how they work. I intended to add additional tooltips or modify existing ones in the future depending on player feedback and questions.
- Fixed a bug where the LunaLib setting to make astropolis stations ignore orbital construction requirements did nothing.
- Fixed a bug where the Ouyang Optimizer could not be constructed. Thanks rime_reason for reporting this!
- Added LunaLib settings to control the number of days the Planet Cracker and Ouyang Optimizer take to apply their terraforming effects.
- Removed the non-default Magnetoshield building. The non-default radiation removal terraforming project can be used instead.
- Added support for planet types from JaydeePiracy. Thanks Dubdog for bringing this to my attention!
- Fixed a bug where building multiple astropolis stations on the same in-game day could result in the markets failing to function properly. Thanks Tukkan1 for reporting this, and including extra detail that made it easy to replicate and debug!

## [9.1.4]
- Fixed a bug where constructing the third astropolis station in orbit around a planet could sometimes cause a crash. Thanks SNIPERER_3413 for reporting this!

## [9.1.3]
- Fixed a bug where the production malus from the Limelight Network would not disappear if the building was deconstructed. Thanks SNIPERER_3413 for reporting this!
- Fixed a bug where the stability malus from the Limelight Network having a Domain-era artifact shortage would not disappear if the building was deconstructed.

## [9.1.2]
It's my belief that I fixed all the bugs and exploits related to all TASC abilities and stations in this patch. If you discover any bugs/issues/inconsistencies/bad grammar/etc please report it in this thread so I can fix it!

- Fixed a bug where colonizing an abandoned station could result in cargo and ships stored there by the player being lost. They are now added to the player fleet. Thanks Def16 for reporting this!
- Fixed a bug where station construction ability tooltips could fail to display correctly in nebula systems. Thanks NephilimNexus for reporting this!
- Fixed a bug where mousing over station construction abilities in certain custom star systems from other mods could cause a crash. Thanks J2Greene for reporting this!
- When the player creates a station colony via TASC, if it's their first colony, the player faction set up menu will now appear.
- Fixed a bug where remnant patrols from a TASC Remnant Station could have incorrect dialog options if the remnant patrol is not part of the player faction. Thanks sardis19 for reporting this!
- Fixed a bug where if the Luddic Church crisis event results in the player losing control of a planet with a TASC Remnant Station, the station could incorrectly remain part of the player faction. Now the station will be deconstructed instead. Thanks sardis19 for reporting this! (Note that this is a vanilla bug that also impacts non-TASC orbital stations. I fixed it for the Remnant Station but it will need to be fixed on the vanilla side as well. The cause is that the stationEntity is set to the Luddic Church faction, but the stationFleet entity does not get updated and remains part of the player faction.)
- Removed non-default feature to make stations have a delay between being built and when the market is founded. I removed this feature because there is a bug where the player must interact with a newly founded market immediately upon creation or the population will incorrectly grow very rapidly. It makes no sense for the player fleet to suddenly interact with the station market when construction finishes when the player fleet is physically located far away.
- Removed a setting to control the maximum number of astropolis stations per planet. The number is now fixed at the default three. I removed this feature because building large numbers of astropolis stations would lead to performance issues.
- Removed the non-default setting to enable destroying planets with a Planet-Killer Device. I removed this feature because it was buggy and had the potential to brick saves.

## [9.1.1]
- Fixed a bug where upon loading an existing save terraforming projects could fail to work correctly. Thanks PolkTech and SecFel for reporting this and helping with troubleshooting!

## [9.1.0]
It's my belief that I fixed all the bugs and exploits related to all TASC buildings in this patch. If you discover any bugs/issues/inconsistencies/bad grammar/etc please report it in this thread so I can fix it!

- Updated artwork for most of the default buildings. I intend to update the artwork for the non-default buildings in a future patch.

### Ashes of the Domain integration changes:
- Reworked the AotD tech tree. Non-default buildings are no longer part of the AotD tech tree and can be immediately constructed if enabled via the LunaLib settings. If AotD were to add support for conditionally populating the tech tree based on LunaLib settings I could add non-default buildings back to the tech tree.
- When Ashes of the Domain - Vaults of Knowledge is enabled, certain LunaLib settings are always locked to certain values. For these settings, I added a note to the setting description.

### Genelab changes:
- Decreased Genelab Domain-era artifact demand quantity.
- Genelab now demands organics if the colony does not have the habitable condition.
- Reworked Genelab Mesozoic Park income bonus amount calculations.
- Genelab can now be improved to increase Mesozoic Park income multiplier.
- Genelab can now install AI cores to increase Mesozoic Park income multiplier.
- Genelab no longer displays any information about the Mesozoic Park income increase if the Mesozoic Park has been disabled via the LunaLib settings.
- Genelab can no longer be improved or install AI cores if Mesozoic Park is disabled via the LunaLib settings.
- A bug was reported with the Genelab where lobster seeding did not progress. I was unable to replicate this issue on my system. If anyone encounters it, please post in this thread.
- A bug was reported by Cat-in-the-Hat where the Fishing Harbor from Ashes of the Domain fails to produce lobsters. I was able to replicate this issue but it will have to be fixed from the AotD side. TASC simply adds the Volturnian Lobster pens condition to the market - it is not responsible for how other mods interact with that condition.
- A bug was reported where there is an incompatibility between TASC-created water planets and the Lobster Proliferation mod. I see that mod does not appear to have been updated for 0.98. I will not investigate this unless a player reports it as an ongoing issue after TASC 9.0.15 is released.

### Limelight Network changes:
- Limelight Network can now be built by the player by default.
- Limelight Network DEA shortages now cause a proportional reduction in income, rather than an upkeep malus.
- Limelight Network DEA shortages now reduce colony stability.
- Limelight Network now reduces commodity production at all buildings on the market by 1.
- Limelight Network negative effects (including the production malus and stability malus) now only apply to player-owned markets (i.e. not Fikenhild).

### Mesozoic Park changes:
- Mesozoic Park income multiplier from AI cores and improvements increased.
- Mesozoic Park income is now modified by market stability in addition to market accessibility.

### Kletka Simulator changes:
- Adjusted Kletka Simulator tooltips to make it more clear how the industry works.
- Fixed a bug where the LunaLib settings option to make suppressing conditions remove the Kletka Simulator temperature-based upkeep adjustment did nothing.
- Adjusted Kletka Simulator AI core drop percentages.
- Kletka Simulator now writes a log each time it rolls for an AI core containing the roll information. If you feel there's a bug with the drop percentages, please check the logs before posting in this thread!

### CHAMELEON changes:
- Fixed a bug where CHAMELEON could apply the improvement bonus to stability even if it was disrupted or had a commodity shortage.

### Cryosanctum changes:
- Removed a setting to control whether Cryosanctums demand Domain-era artifacts. They now use the same logic as all other buildings - both the Domain-tech Content and Domain Archaeology settings must be enabled for buildings to demand DEA.
- Fixed a bug where Cryosanctums could demand Domain-era artifacts even if the settings were configured to remove DEA demand. Thanks JamesTripleQ for reporting this!
- Fixed a bug where Cryosanctums could be buildable by the player even if the setting to enable this was toggled off.
- Fixed a bug where Cryosanctums could incorrectly produce no organs.
- Fixed a bug where the Cryosanctum on Nomios could be replaced with the TASC version even if the setting to enable this was toggled off.
- Cryosanctums no longer demand organics if the colony has the habitable condition.
- Cryosanctums can now be improved to increase organ production.

### Hydroponics changes:
- Fixed a bug where improving Hydroponics did not increase food supply. Thanks JenkoRun for reporting this!
- Hydroponics now demands organics if the colony does not have the habitable condition.
- Increased Hydroponics heavy machinery demand quantity.
- Rebalanced Hydroponics build time and upkeep cost.

### Cloning changes:
- Fixed a bug where improving Cloning did not increase organ production. Thanks leon123442 for reporting this!
- Cloning now demands organics if the colony does not have the habitable condition.
- Increased Cloning Domain-era artifacts demand quantity.

### GPA (Galatian Particle Accelerator) changes:
- Fixed a bug where the GPA could be buildable by the player.
- Fixed a bug where the GPA could fail to demand Domain-era artifacts regardless of the LunaLib settings configuration.

### Magnetoshield changes:
- Magnetoshield now removes the Irradiated condition instead of suppressing it. This will allow for terraforming of irradiated planets. If the magnetoshield is deconstructed the condition will return.

### Miscellaneous:
- Fixed several typos and grammatical issues in the LunaLib settings.
- Updated industries.csv descriptions for several buildings.
- There was some discussion about the Planetary Shield and an interaction with IndEvo. TASC does not modify or interact with the Planetary Shield so any issues with this building should be reported in the IndEvo thread. TASC previously modified the Planetary Shield to make it remove the Meteor Impacts condition, but that functionality was implemented in vanilla so I deleted it from TASC. I'm not planning to implement any features in TASC related to the Planetary Shield at this time, including anything related to Domed Cities or making the Planetary Shield demand Domain-era artifacts.

## [9.0.15]
- Conditions suppressed by Stellar Reflector Array can now be configured via CSV file. The player can use this to make it suppress very_hot and very_cold if desired.
- Fixed a bug where Domed Cities provided the population growth bonus even when disabled or under construction. Thanks sardis19 for reporting this!
- Fixed a bug where mousing over the Perihelion Project icon in the AotD research menu could cause a crash. Thanks Ptikobj for reporting this!

## [9.0.14]
- Fixed a bug where building a Stellar Reflector Array in a nebula could cause a crash on save. Thanks Stretop for reporting this!
- Fixed a bug where constructing a coronal hypershunt using the Perihelion Project while Ashes of the Domain - Vaults of Knowledge is enabled would result in the newly constructed hypershunt being unusable. Thanks zrx1000 for reporting this!
- Adjusted rules for where the Perihelion Project can be constructed. Previously it could only be constructed at systems with a blue star, now it can also be constructed at trinary star systems (e.g. systems where there are three stars located right next to each other at the center of the map) and there are at least two star colors among the three stars. I am not planning to implement a settings option to disable this requirement as the logic to place the hypershunt assumes certain star configurations.
- Implemented a check to prevent the player from constructing multiple copies of the Perihelion Project in the same system. Thanks Strauss_hd for bringing this possibility to my attention!
- Adjusted values for cost and construction time of the Perihelion Project. These new values can be adjusted using the industries.csv file.
- Updated Perihelion Project artwork.
- If Ashes of the Domain - Vaults of Knowledge is enabled, the Perihelion Project does not demand any commodities during construction.

**Note:** The Perihelion Project is disabled by default (when AotD - VoK is not enabled). To enable it, use the Perihelion Project settings in the Domain-tech section of the LunaLib settings.

It's my belief that I fixed all the bugs and exploits related to the Perihelion Project with this patch. If you discover any additional issues please report them in this thread!

## [9.0.13]
- Fixed a bug where hovering over the Stellar Reflector icon in the VoK research tree could cause a crash. Thanks to the many players who reported this and my apologies for the delay in fixing it!
- Updated Illustrated Entities integration. Big thanks to SirHartley for providing the code for this change!

## [9.0.12]
- Updated TASC for Starsector 0.98a-RC7.
- Fixed a bug where hovering over the Seafloor Cities icon in the AotD research tree would cause a crash. Thanks to Aposkus for reporting this and providing a workaround!
- Fixed a bug where Domed Cities and several other TASC buildings were incorrectly unlocked even if not yet researched. Thanks ToeCutter23 for reporting this!
- Fixed a bug where the tooltip for Domed Cities could incorrectly state that it was increasing accessibility by 10%. Only the Sky Cities version increases accessibility.

## [9.0.11]
- Added the Sky Cities building, a version of Domed Cities buildable on gas giants.
  - Increases stability and accessibility and suppresses negative conditions, but reduces ground defenses and has high construction and upkeep costs.
  - All versions of Domed Cities now suppress additional conditions from Unknown Skies. The list of suppressed conditions can be viewed and edited using the domed_cities_suppressed_conditions.csv file at tasc/data/campaign/terraforming.
  - Planetary Agrav Field can now be constructed on gas giants if Sky Cities is present.
- Updated Ashes of the Domain TASC tech tree. Thanks to YourLocalMairaaboo for providing their updated tech options file!
- Fixed a bug where improving the Domain Archaeology building would not increase production by 1. Thanks to the many users who reported this!
- Fixed a bug where a NullPointerException could be thrown if the player has both the VRI mod and TASC enabled. Thanks to oleg for both reporting this and providing code to fix the problem!
- Fixed a bug where closing the game and reopening it could cause the CHAMELEON (with an alpha core installed) to fail to suppress pather cells. Thanks libertyordered for reporting this!
- Fixed a bug where dismantling a Remnant Station while outside the system could cause a crash when returning to the system. Thanks to hanyizzle and several other users for reporting this!
- Fixed a bug where the terraforming_colony_has_atmosphere terraforming requirement was incorrectly not inverted. Thanks to EnigmaniteZ for reporting this!
- Fixed a bug where taking certain actions, such as building a station, in a system with no planets could result in a crash to desktop. Thanks to TheShear for reporting this!
- Fixed a bug where the water level terraforming requirement could incorrectly not be met if one of the planets involved was governed by the player via Nexerelin. I think all the bugs related to the water level terraforming requirement are fixed at this time - please let me know if you encounter any going forward!
- I investigated a bug report where if the player has the Grand Colonies mod enabled, the Cramped Quarters condition allegedly always appeared on station markets even if it's disabled in the settings file. I was unable to replicate this bug on my system. If you encounter this going forward please let me know.
- I was unable to replicate a bug where governed or gifted stations (using the Nexerlin mod) could cause the hidden No Atmosphere condition to incorrectly apply an additional 50% hazard. I added additional checks to hopefully ensure this issue cannot occur even though I was unable to replicate it. If you encounter this problem going forward, please let me know. Thanks to Vendra and other for reporting this!
- A bug was reported where CHAMELEON stops working for colonies above size 6. I tested and was unable to replicate it. Please let me know if you encounter this going forward.
- I previously indicated I was going to make changes regarding stalling vs. restarting terraforming progress in this patch. I wasn't able to make those changes yet and they will be included in a future patch. Sorry for the delay!

## [9.0.10]
This patch focuses exclusively on the Remnant Station. I think I've addressed all outstanding bugs and feature requests. Please let me know if I missed anything!

**Edit 9/22/24:** 9.0.10 is confirmed not backwards compatible with 9.0.9 even if you never built a Remnant Station. Please start a new save after upgrading to 9.0.10!

- Fixed a bug where the Remnant Station could become indestructible or instantly repair itself. Thanks Seti and many others for reporting this!
- Fixed a bug where the Remnant station could start in an "undergoing repairs" state after initially being constructed.
- The AI core commander of the Remnant Station now matches the AI core installed in the building (except when there's no AI core installed, in which case it will be a gamma core commander).
- Fixed a bug where the improvement bonus for the Remnant Station did not work.
- Fixed a bug where the stability and ground defense bonuses from the Remnant Station were not impacted by commodity shortages.
- The Remnant Station now supports the modified Automated Ships skill from Second in Command. Thanks AdamLegend for bringing this to my attention and providing an easy way to add support!
- Fixed a bug where the Remnant Station could demand Domain-era artifacts even when the setting was disabled. I know there are several other buildings where this bug is not fixed and I will address those in the next couple patches. Thanks to all the players who reported this issue!
- The combat readiness formula for the Remnant Station has been adjusted. The formula is now 50% base combat readiness + 50% * the lower of the market ship quality modifier and supply shortage modifier.
- The Remnant Station now supports artillery stations from IndEvo (to the extent it didn't before. There were several bug reports about this but I was unable to replicate the issue on the latest version).
- Changed the artwork for the Remnant Station building in the colony management screen.
- Fixed some grammatical errors and inconsistencies in the tooltips for the Remnant Station.

## [9.0.9]
- Fixed a bug where after completing a terraforming project to improve a resource deposit, buildings would use the old deposit amount until a save was loaded.
- Added support for several Unknown Skies planet types. Thanks MahMeer for your code contribution!
- Added a gitignore file to remove unnecessary files on the GitHub repository. Thanks Wispborne for the suggestion!

## [9.0.8]
- Fixed a bug where Domed Cities could fail to suppress the Perpetual Dust Storms condition from the Unknown Skies mod. Thanks to zrx1000 for reporting this!
- Fixed a bug where if a planet with Seafloor Cities was terraformed, the -25% hazard bonus from Seafloor Cities would incorrectly remain in place.
- Fixed a bug where setting astroplis stations to always use a certain sprite variant would result in the wrong sprite being used. Thanks YourLocalMairaaboo for reporting this!
- Fixed a bug where Hydroponics could incorrectly be buildable even if it was disabled in the LunaLib settings if AotD is also enabled. Thanks Axisoflint for reporting this!
- Fixed a bug where the Genelab could incorrectly be buildable if AotD is enabled but the Genelab is not yet researched. Thanks Axisoflint for reporting this!

## [9.0.7]
- Fixed a bug where stations might not be buildable around purchased planets. Thanks YourLocalMairaaboo for reporting this!
- Fixed a bug where a crash related to installable items could occur in very specific circumstances. Thanks Soric for reporting this!
- Fixed a bug where the terraforming menu couldn't be opened to craft item if terraforming is disabled in the settings. Thanks MagnaSonic3000 for reporting this!
- Fixed a bug where the setting to use only a certain type of station sprite did not work as expected. Thanks YourLocalMairaaboo for reporting this!
- Fixed a bug where Domed Cities incorrectly could not be built on gas giants with the Floating Continents condition from Unknown Skies. Thanks YourLocalMairaaboo for reporting this!
- Fixed a bug where a crash could occur after using the Ouyang Optimizer to improve a gas giant. Thanks Niten for reporting this!

## [9.0.6]
- Made various improvements to the Ashes of the Domain integration. Thanks terminal, Baren, Erlkonig and others for reporting issues with this integration!
- Fixed a bug where a crash could occur if a coronal tap is installed on a station. Thanks Princess_of_Evil for reporting this!
- Fixed a bug where the terraforming menu couldn't be used on stations, thereby preventing Domain-tech crafting using those colonies. Thanks Princess_of_Evil for reporting this!

## [9.0.5]
- Fixed a bug where star types from Unknown Skies could fail to be detected by the Perihelion Project. Thanks Bangchow for reporting this!
- Fixed a bug where Seafloor Cities fails to properly stop suppressing the hazard from water surface when it's removed from the colony. Thanks Morgan Rue for reporting this!
- Fixed multiple bugs related to certain settings not being used in-game to alter features as intended. Thanks Arrean and Princess_of_Evil for reporting these!
- Fixed a bug where CHAMELEON progress could get reset on game load. Thanks obj188 for reporting this!
- Fixed a bug where the Colonize Abandoned Station tooltip could display incorrect text.
- Fixed a bug where stations could be placed incorrectly if they were given custom names.
- Made various improvements to the Ashes of the Domain integration.
- Added a setting to toggle whether suppressing temperature conditions removes the upkeep modifier on the Kletka Simulator.
- Added a feature to show the roll for the Kletka Simulator to aid with debugging if an anomalous series of drops is occurring.

## [9.0.4]
- Fixed a bug where the Remnant AI Battlestation could get stuck permanently in an "under repair" state. Thanks to Troika and Azina for reporting this!
- Fixed a bug related to input validation for planet types.
- Fixed a bug where changing the maximum number of mining stations per system in the LunaLib settings didn't actually change the construction limit. Thanks MRTL for reporting this!
- Fixed a bug where siphon stations could be incorrectly blocked from being constructed in orbit around a gas giant in certain circumstances. Thanks Chaotic Law for reporting this!
- Fixed a bug where stations built with TASC could incorrectly have a hazard rating of 150% due No Atmosphere not being suppressed properly. Thanks SaucyBagel for reporting this!

## [9.0.3]
- Fixed a bug where the Cloning, Hydroponics and Limelight Network industries could be missing the option to install an AI core or make improvements. Thanks n3xuiz for reporting this!
- Stations built using TASC now correctly set the $startingFactionId memory flag. Thanks to sawert42 for reporting this and Histidine for explaining how to fix it!
- Fixed multiple bugs with the Kletka Simulator that prevented it from working properly. Thanks Moon Spirit for reporting this!
- Fixed a bug with the Cryosanctum where installing an AI core could incorrectly reduce the supply of organs by one. Thanks WyldMann for reporting this!

## [9.0.2]
- Fixed a bug where not having LunaLib enabled could cause a crash when hovering over certain abilities. LunaLib is now a dependency for TASC. Thanks Arrean and andrro for reporting this!
- Fixed a bug where the Domain Archaeology, Remnant Station, and Cryosanctum industries could be missing the option to install an AI core or make improvements. Thanks Apocryphos and WyldMann for reporting this!

## [9.0.1]
- Fixed a bug where a crash could occur upon interacting with certain markets while Grand Colonies is enabled. Thanks to Apocryphos, Saevarna and several others for reporting this and assisting with troubleshooting, and thanks to SirHartley for providing a workaround until a fix could be implemented!
- Fixed a bug where a crash could occur if a new game is started, and then another new game is started in the same session. Thanks to Benizakura and several others for reporting this!

## [9.0.0]
- Added Terraforming Control Panel UI. It can be accessed via the ability bar. Thanks Evangel for your excellent work on this feature and the others below!
- Many features of TASC can now be modified via CSV files located at Starsector\mods\Terraforming and Station Construction\data\campaign\terraforming. See the documentation file for details.
- If Genelab or CHAMELEON is currently working on a project, it will be displayed with a progress bar under the building icon in the colony management screen.
- Many different tooltips were updated to be more descriptive.

## [8.4.6]
- Fixed an issue where the version file was pointed at the old GitHub repo instead of the new one. Thanks INH_Raider for reporting this!
- Added support for a Lost Sector planet with type nskr_ice_desert. Thanks OmegaInfinita, JimminyCrimbles, and several others for reporting this!
- Added compatibility with Crew Replacer (https://fractalsoftworks.com/forum/index.php?topic=24249.0). Thanks alaricdragon for your code contributions adding this!

## [8.4.5]
- Updated TASC to support compatibility with the 0.96a version of Illustrated Entities. Thanks Crimsteel for reporting a crash caused by TASC incompatibility with the latest version of IE!

## [8.4.4]
- Added an option to the terraforming control panel to cancel the current terraforming project. Thanks scorpico69 for making me aware of this oversight!

## [8.4.3]
- Fixed a bug where viewing a market in the terraforming control panel that the player governs but that is not part of the player faction could cause a crash. Big thanks to YourLocalMairaaboo for taking the time to upload their mods and saves folder to help me identify the cause of this crash!

## [8.4.2]
- Fixed a bug where the terraforming control panel could fail to list governed colonies (from Nex). Thanks Spacegoat for reporting this!
- Added an option in the terraforming control panel to list the current resources and conditions on a colony. Thanks MikroPik for the suggestion!

## [8.4.1]
- Fixed a bug where stations could appear on the terraforming menu (and cause a crash if clicked). Thanks to Hasufel and several others for reporting this!
- Fixed a bug where changing terraforming project duration settings via LunaLib was not reflected in-game. Thanks AERO for reporting this!
- A crash with Grand Colonies in the stack trace was reported. I did some testing and was unable to replicate this crash on my machine. I added some logic to fix what I suspect may have caused it, but if the crash persists please let me know. Thanks Malignantcookie and medKon for reporting this!

## [8.4.0]
- The terraforming menu is now opened via an ability instead of via an interaction option when docked at a planet.
  - The ability can be used anywhere - this allows the player to control terraforming projects remotely.
- Crafting costs for some special items is increased or decreased depending on the power level of the item.
  - The player-configured Domain-era artifact cost (default: 2000) is multiplied by two for more powerful items and divided by two for less powerful items.
- Added a non-default terraforming project to remove the atmosphere from a planet.

## [8.3.3]
- Fixed a bug where the colony name could be displayed incorrectly in terraforming dialogs. Thanks Spshamrocks3 for reporting this!
- Fixed a bug where the Planetary Agrav Field could fail to correctly suppress high or low gravity on gas giants. Thanks erik17 for reporting this!
- Added support for the terran_adapted planet type from the Volantian Reclamation Initiative mod. Thanks Lappers for letting me know about this!

## [8.3.2]
- Added compatibility with the Ashes of the Domain mod. If Ashes and TASC are both enabled many of the buildings in TASC are included as part of the research system in Ashes. Thanks Kaysaar for all your help with adding compatibility!
  - Please be sure to download the latest version of Ashes with the compatibility changes from https://fractalsoftworks.com/forum/index.php?topic=26307.0.
- Categorized the Ecumenopolis planet type from the Star Wars 2020 mod as barren for terraforming purposes. Thanks Jac90876 for letting me know about this!
- Added a note to the settings file to clarify that LunaLib settings override the settings file if LunaLib is enabled.

## [8.3.1]
- The artwork for several buildings has been updated. Big thanks to SirHartley for providing some very high quality images that match the art style of Starsector!
- Fixed a bug where building the Remnant Station could cause a crash if LunaLib is enabled. Thanks MonolithSF for reporting this!

## [8.3.0]
- Added support for LunaLib. Please be sure to report any bugs or grammatical problems related to the LunaLib settings!
  - LunaLib is not a dependency for TASC. If LunaLib is enabled, the settings in LunaLib will take effect. If LunaLib is not enabled, the settings in the settings file will be used instead.
- Updated CHAMELEON building for 0.96a. The alpha core bonus now reduces Pather interest on the colony to zero, instead of simply subtracting 1000. Please post in this thread if you encounter a situation where the Pather interest on a colony is a value other than zero while an alpha core is installed.
- The bug where farming/aquaculture could be the wrong version after terraforming is resolved. Thanks Alex for the API change!
- Fixed a bug where the third astropolis constructed at a single planet could be placed at the wrong orbital angle. Thanks Meeplet for providing the log that helped me identify the cause of the bug, and to several others who had previously reported this bug!
- Fixed a bug where the planet-killer device could show up as an option from the historian. Thanks Vendral for reporting this!
- Added a settings toggle to disable the Domed Cities defense malus.
- Added a settings toggle to enable a terraforming project to clean up radiation.
- Added a settings toggle to prevent the Terran and tundra type change projects from adding volatiles deposits.
  - I decided against creating non-default terraforming projects to remove beneficial conditions from planets (ex. atmosphere, transplutonics deposits, volatiles deposits).
- If the player has the Everybody Loves KoC mod enabled, the TechMining industry on Agreus will not be replaced with Domain Archaeology. Instead, the TechMining industry (on Agreus only) will now supply five domain-era artifacts. Thanks to Serenitis and others for making me aware that my previous method of handling this issue was not adequate.
- The planet-killer device nows adds the irradiated condition to the targeted planet.
- Planets that are very hot can no longer be terraformed into arid or jungle worlds, and cannot be given the habitable condition. Thanks e for the suggestion!
- Planets that are very cold can no longer be terraformed into tundra worlds, and cannot be given the habitable condition. Same here - thanks e!
- Fixed a bug where Stellar Reflector Arrays could still be automatically created on NPC colonies even if terraforming is disabled in the settings file. Thanks Edix for making me aware of this!
- Made changes to prevent a crash if a market lacks a primary entity, has no faction ID or has certain other bugged/uncommon circumstances. Thanks to Histidine, alaricdragon and several others for reporting these crashes to me!
- Fixed a bug where a station market transferred to another faction via Nexerelin could have an open market with no tariff. The bug was caused by TASC, not Nexerelin. Thanks styxhelix for reporting this!
- Fixed a bug where Perihelion Project could fail to detect a type of blue giant star from Unknown Skies. Thanks MenacingCaptain for reporting this!
- The non-default building Planetary Agrav Field can now be constructed on gas giants even if Domed Cities is not built on that colony.
- Modified the description of the non-default building Ouyang Optimizer to make it clear that the effects are permanent, even if the building is deconstructed.
- The planet Vena from Blackrock Drive Yards is now correctly categorized. Thanks not a luddic path member for bringing this to my attention!
- CHAMELEON can now remove the "decivilized" condition (as well as the "decivilized subpop" condition) just in case the "decivilized" condition somehow gets onto a player market. Thanks Kh0rnet for bringing this to my attention!
- The Auroran Dimensional Nanoforge from the United Aurora Federation mod is now recognized as a Pristine Nanoforge for purposes of determining whether a market meets the requirements to enable crafting. Thanks YourLocalMairaboo for bringing this to my attention!
- Thanks to Blackclaw, TalRaziid and evilsmoo for posting log files that helped me identify the cause of the "AI core mouseover" crash!
- I investigated an alleged compatibility issue with the Space Truckin' mod. I was unable to replicate the issue. If someone can provide steps to replicate the problem I will take another look.
- I previously indicated I would investigate allowing the player to configure which special items can be crafted. Adding this would be involved and require a lot of development work, so I've decided not to implement it because relatively few players would end up using it. Sorry!
- There were some compatibility issues with Illustrated Entities reported. Once that mod updates for 0.96a I will investigate this.
- I understand the Remnant Station is still not compatible with the artillery station from IndEvo. Once IndEvo updates to 0.96a I will investigate this.

## [8.2.1]
- Fixed a bug where the Perihelion Project could incorrectly display 1% construction progress in the UI even if the actual construction progress was higher. Thanks Nérévar42 for reporting this!
- Fixed a bug where the station population growth malus from Cramped Quarters could be calculated incorrectly. Thanks taerkar for reporting this!
- Markets with an Autonomous AI Battlestation no longer receive the MARKET_MILITARY memory flag. Thanks e for pointing this out!
- I was unable to replicate a bug where building multiple astropolis stations around a planet could cause two of the stations overlap in the same place instead of being placed at 120 degree intervals around the planet. I added logging statements to the astropolis construction code - if anyone experiences this bug in the future, please let me know and post your log file so I can identify the cause of the bug. Thanks Aran1 for reporting this!

## [8.2.0]
- Coronal Hypershunts can now be constructed by the player. Enable the non-default building Perihelion Project using the settings file to build them.
- Fixed a bug where using the Planet-Killer with the Nexerlin mod enabled could cause a crash. Thanks for your help Histidine!
- Fixed multiple bugs caused by compatibility issues with the Illustrated Entities mod. Thanks YourLocalMairaaboo for reporting this!
- Fixed a bug where astropolis stations could not be constructed around colonies granted autonomy using the Nexerelin mod. Thanks Reshy for reporting this!
- The Remnant Station is now compatible with the Artillery Station from the Industrial.Evolution mod. Thanks SirHartley for providing instructions on how to do this!
- Domed Cities can now be built on planets with Extreme Tectonic Activity if a Harmonic Damper is present. Note that the Harmonic Damper is non-default content and will have to be enabled via the settings file.
- Added a settings option to control the amount by which Cramped Quarters reduces population growth.
- Added terraforming support for the planet Charkha from the ScalarTech Solutions mod. Thanks e for reporting this!
- Fixed a compatibility problem with the Everbody Loves KoC mod caused by replacing the Tech-mining industry on Agreus with Domain Archaeology. Now the replacement won't happen if the player has Everybody Loves KoC enabled. Thanks bodeshmoun for reporting this!

## [8.1.6]
- Removed version checker and analytics reporting. I considered creating a popup to allow players to opt-in, but decided against this for a number of reasons, some of which I mentioned previously in posts in this thread.
- Friendly patrols from Remnant battlestations now have a unique dialogue message if the player interacts with them (instead of using the default friendly patrol dialogue).
- Added an option in the settings file to make all astropoli use low tech, midline or high tech sprites instead of alternating between them.
- Fixed a bug where if the player set a custom interaction image for a TASC station in the custom_entities.json file, the change would not show up in-game. Thanks Thoutzan for reporting this!
- Fixed a bug where the player could activate the Construct Astropolis ability even if the target planet already had the maximum number of astropoli in orbit.
- Added options in the settings file to modify the costs for building all Domain-tech stable location structures.
- Added a settings option to control reputation impact for upgrading a stable location structure.
- Made changes to the logic that determines the water level of a planet for terraforming purposes. Previously some Unknown Skies planets that visually are mostly covered in water did have not an appropriate water level. Thanks Grotez for bringing this issue to my attention!
- Players have reported a bug where a planet recently terraformed to or from a water world could incorrectly have the Farming or Aquaculture industry available to build. I checked with Alex and it's my understanding this bug cannot be fixed in the current version of the game, but an upcoming API change in the next vanilla release will address it. Until then, a workaround is to save and then reload the save after terraforming is complete. Once you reload the save, Farming and Aquaculture will behave correctly on that planet. Thanks Stormy Fairweather and AcaMetis for reporting this!
- Domed Cities can now be constructed on gas giants with the Floating Continent condition (from the Unknown Skies mod). Thanks Serenitis for the suggestion!
- Planetary Agrav Field (non-default building) can now only be constructed if the planet has high or low gravity. Thanks Mcgrolox for reporting this!
- Magnetoshield (non-default building) now suppresses radiation instead of removing it.
- Fixed various bugs and miscellaneous problems with the magnetoshield.
- Multiple players reported that they were unable to build mining stations because asteriod belts and fields were not being detected. I installed all the mods on their modlists and tried to replicate the issue but was unable to do so. If anyone encounters this problem in the future, please let me know.
- I previously indicated in a post in this thread I would add a settings option to control the base cost for station expansion. This is unnecessary as the cost for the Expand Station building can already be modified using industries.csv. My apologies for the confusion.
- ListenerUtil.reportSaturationBombardmentFinished(null, market, null) is now called when the player uses a planet-killer device.

## [8.1.5]
- Fixed a bug where mousing over the Deploy Planet Killer ability in a system with no planets could cause a crash. Thanks TheHZDev (and several others) for reporting this!
- Fixed a bug where the Remnant battlestation could fail to repair over time after being destroyed in a battle. Thanks TheHZDev for reporting this!
- Fixed a bug where the terraforming menu and crafting menu options could appear during dialogs with characters when using the comm directory.
- Fixed a bug where the Kletka Simulator could fail to detect that it's located on a station market and set upkeep appropriately. Thanks TheGodUncle for reporting this!
- Fixed a bug where terraforming progress could get frozen on planets where the player granted autonomy (via Nexerelin). Thanks Hexxod for reporting this!
- Added support for prv Starworks. Thanks J2Greene for bringing this incompatibility to my attention!

## [8.1.4]
- Remnant battlestations built by the player now spawn their own patrol fleets to guard the colony. The Remant battlestation patrols are separate from and in addition to any regular colony patrols.
- Added the Planet-Killer Device as a special item. I made this a non-default feature due to what I feel are significant gameplay problems associated with it.
    - Once enabled in the settings file, the Planet-Killer Device can be obtained through Domain-tech crafting under the Domain Restricted category.
    - The Planet-Killer Device is activated via an ability that can be added to the ability bar.
- By default all Cryosanctum buildings in the Sector will now demand Domain-era artifacts, not just the one on Nomios.

## [8.1.3]
- Fixed a bug where a crash could occur if the player is using randomized core worlds. Thanks Whisena for reporting this!

## [8.1.2]
- Added Limelight Network, a building unique to Fikenhild that demands Domain-era artifacts. There's a settings file option to allow the player to build it.
- By default, the Cryosanctum on Nomios will demand Domain-era artifacts.
- Added a settings option to allow the player build the Cryosanctum.
- Added GPA (Galatian Particle Accelerator), a building unique to Ancyra that demands Domain-era artifacts.
- By default, Volturn will start with a Genelab, which adds demand for Domain-era artifacts.
- Added an option to the terraforming menu to cancel the current project.
- Fixed a bug where the Genelab could remediate pollution even if an Orbital Works was currently generating pollution. Thanks TheHZDev for reporting this!
- Fixed a bug where a research station from the Arma Armatura mod could block construction of a siphon station at the gas giant Raven in the Nekki system. Thanks Aran1 for reporting this!
- Attempted to fix a bug involving a crash where the stack trace indicated the problem is related to data.scripts.terrain.MagicAsteroidFieldTerrainPlugin. I couldn't reproduce the crash on my system, but I think I fixed the cause, so let me know if it's still happening. Thanks DrTechman42 for reporting this!

## [8.1.1]
- Fixed a bug where Domain-tech crafting didn't work on station markets. Thanks Noobishnoob for reporting this!
- Crafting Domain-tech items now costs two story points by default. This amount can be changed using the settings file, and setting boggledDomainTechCraftingStoryPointCost to zero will eliminate the story point cost.

## [8.1.0]
- Implemented crafting of special items using Domain-era artifacts. This can be disabled using the settings file.
- Added Seafloor Cities building. It can only be built on water planets and has similar but not identical functionality to Domed Cities.
- Gates can only be built if there is a player-controlled colony of at least size 5 in the system. This can be modified using the settings file.
- Disabled gate construction in systems where a gate already exists.
- Fixed a visual bug where the amount of Domain-era artifacts the player has could be obscured when building Domain-tech stable location objectives.
- Reduced Stellar Reflector Array pather interest.
- When checking whether conditions for terraforming projects are met, a planet will be considered to have a Stellar Reflector Array based on whether it has the Orbital Reflector Array condition.

## [8.0.2]
- Fixed a bug where Domed Cities couldn't be built on Water planets even if the appropriate setting was enabled.
- Fixed a bug where the Kletka Simulator could have an incorrect upkeep on station markets with a Fusion Lamp installed.
- Stations can no longer be constructed in systems without any jump points.
- Rebalanced the number of asteroid belts required for certain ore resource levels on mining stations if boggledMiningStationLinkToResourceBelts is set to true.
- Added options in the settings file to control the number of asteroid belts required for certain ore resource levels on mining stations.

## [8.0.1]
- Fixed a bug where resource levels on Arid planets couldn't be improved via terraforming.
- Fixed a bug where gas giants could be terraformed.
- Reverted the change that removed the Domain-era artifact demand from stellar reflectors.
- The Escape key is now bound to going back in the terraforming menu.
- The terraforming menu will now appear below the ship repair menu in the colony interaction dialog.
- Added an option to the settings file to allow Domed Cities to be built on water worlds. If this proves popular, I may add unique effects and a special sprite for Domed Cities on water worlds.
- Added support for the built-in Version Checker in Nexerelin.
- Created my own update checker API to replace using Pastebin, which had caused issues for some players.

## [8.0.0]
- Completely reworked the terraforming system (again). Terraforming is now controlled via a menu when you interact with your colony, not the Genelab. Please be sure to leave feedback regarding the options available and requirements for same!
- E.U.T.E.C.K. removed.
- Added an option in the settings file to enable/disable inactive gate construction.
- Added an option in the settings file to require completion of the main questline before inactive gates can be constructed.
- Added the Magnetoshield, a non-default structure that will remove radiation so long as it's active.
- Updated the tooltip on the Kletka Simulator to clarify that installing AI cores will improve drop chances.
- CHAMELEON can now remove the Rogue AI Core condition.
- Fixed a bug where CHAMELEON wouldn't inform the player that it was inactive if it became disrupted.
- Removed several AI core and improvement bonuses from structures to reduce complexity.
- Updated descriptions for several structures.
- Mining stations can no longer be constructed on top of jump points.

## [7.1.3]
- Fixed a bug where terraforming a planet into a jungle world would incorrectly make it a paradise world instead. Thanks SenSayed for reporting this!

## [7.1.2]
- Stellar Reflector Arrays now provide a ground defense bonus if improved.
- Added the Planetary Agrav Field building. This is a new non-default structure that suppresses high/low gravity if the colony also has the Domed Cities building. Thanks to ozemandea for the inspiration to create this!
- Harmonic Damper no longer suppresses high/low gravity.

## [7.1.1]
- Fixed a bug where the open market from the Commerce industry would incorrectly have no tariffs on player-built stations. Thanks Vendral for reporting this!

## [7.1.0]
- Added the Harmonic Damper building. Suppresses tectonic activity and high/low gravity, and adds some ground defense if improved and/or an AI core is installed. This is a non-default option for now, although I think a version that only suppresses tectonic activity might become default in the future.
- The Atmosphere Processor is back. It will permanently remove no/thin/dense/toxic atmosphere from the planet it's built on. It's a non-default option.
- The Terraforming Platform is back. It will permanently add Habitable and Mild Climate to the planet if it doesn't have no/thin/dense/toxic atmosphere. It's a non-default option.
- Added the Planet Cracker building. It can only be built on an astropolis station. It will improve the ore and rare ore resources by one level on the host planet of the astropolis, but add tectonic activity. It's a non-default option.
- Added the Ouyang Optimizer building. It can only be built on an astropolis station or siphon station orbiting a gas giant. It will improve the volatiles resources by one level on the host planet of the station, but add extreme weather. It's a non-default option.
- If a host gas giant has the volatiles resource improved using the Ouyang Optimizer, any orbiting siphon stations will also have their volatiles improved by the same amount.
- The terraforming menu now has a tooltip which states the number of days remaining until terraforming is complete.
- The conditions section of the terraforming menu has been removed because the functions have been replaced with the new non-default terraforming buildings listed above. If the player wants this menu back, simply uncomment the line in rules.csv that adds the condition section.

## [7.0.1]
- Fixed multiple bugs relating to the Genelab/EUTECK and terraforming progress. Thanks to chrizeren, CrimsonPhalanx and Goldendragonfinn for reporting these issues!
- Fixed a bug where the farming/aquaculture script could set the wrong building type on Archipelago planets. Thanks Farya!
- The Remnant Battlestation will now be visible in the build menu if the player lacks the Automated Ships skill.
- Added a tooltip to CHAMELEON if there is a DEA shortage that clarifies the structure provides no benefits during the shortage.

## [7.0.0]
- The E.U.T.E.C.K. has returned! It's a special item that can be installed in the Genelab to begin terraforming. By default, the E.U.T.E.C.K. is single-use and will create a Paradise world, which is essentially a Terran planet with bountiful farmland and mild climate. Terraforming takes 400 days to complete, which can be adjusted in the settings file.
- For those who want more options for terraforming (despite the lore and balance problems I feel are associated with them), the "boggledEnableAllTerraformingProjects" setting can be switched to true to enable the "old-style" terraforming. It's now controlled via an option on the colony interaction menu, and there are planet type change options for both vanilla planet types and Unknown Skies types. There are also miscellaneous projects to alter resources and conditions on the planet. Please let me know if there are any projects you want but aren't implemented - I will add more based on popular demand!
- Genelabs now seed lobsters on planets with a water-covered surface. This process takes 200 days.
- "Crustacean Job" quest removed.
- Genelabs can now install AI cores and be improved. The Alpha Core bonus and improvement bonus increase income from the Mesozoic Park.
- Inactive Gates can now be built at a stable location. After playing the main quest line, I'm sure the player will understand why Astral gates were removed and this feature was added.
- Remnant Station updated for 0.95a and is now a default option.
- Fixed a bug where the wrong variant was being used for the Remnant Station.
- The Remnant Station is now only buildable if the player has the "Automated Ships" skill unlocked.
- Modified the Mining Station resource deposits if the "boggledMiningStationLinkToResourceBelts" setting is enabled. Rich and Ultrarich ore deposits are now possible in cases where there is an extreme number of asteroid belts in the system.

## [6.0.0]
- Terraforming and Domain-tech content has been re-enabled by default.
- Planet type transformations have been removed temporarily. Between the new 0.95a special items and the hazard-suppressing buildings in this mod, the old-style planet type transformations are largely unnecessary. Planet type transformations (and the EUTECK) will be back in a future update once I've reworked the system to be worthwhile in 0.95a. Players who don't like the new 0.95a suppression mechanic and prefer the old-style system are encouraged to check out the DIY Planets mod.
- Wildlife Exploitation replaced by Mesozoic Park industry. See the mod guide for details on how this industry works. It is a default option for now, but may be converted to non-default based on player feedback.
- Stellar Reflector Array structure updated for 0.95a. Functionality has changed somewhat. See the mod guide for more details.
- Domed Cities structure added. This building suppresses hazardous conditions related to the atmosphere, weather and biosphere. See the mod guide for more details.
- Military Police Headquarters replaced by CHAMELEON. Can still remove Decivilized Subpop, but now also counters Luddic Path cells. See the mod guide for more details.
- Domain Archaeology production changed from static amount to market size minus two. Ruins now add or subtracts production from Domain Archaeology like other resource deposits. Added improvement bonus.
- Minor changes made to Kletka Simulator industry. See the mod guide for more details.
- Eisen Division renamed to Genelab and function has been reworked. See the mod guide for more details.
- The Planetary Shield structure now suppresses the Meteor Impacts condition.
- All Astral gate content has been removed from the mod due to new features present in 0.95a.
- In order to enable various features in this mod, several vanilla market conditions have been overwritten. I'm not aware of any conflicts with other mods, but it may occur in the future if another mod also overwrites the same conditions. Disabling features from this mod will not eliminate the potential for a conflict to occur.
- Arcology worlds and the unique buildings for that planet type have been removed.
- Spice Harvesting has been removed.
- Atmosphere Processor has been removed.
- Terraforming Platform has been removed.
- Skyhook Anchor has been removed due to the addition of the Fullerene Spool in vanila.
- Cloning (non-default industry) can now be improved.
- Hydroponics (non-default industry) can now be improved.
- Hydroponics can now be built on both station markets and planet markets.
- Fixed a bug where building multiple astropoli around one planet would cause the orbital path to be set incorrectly. Thanks bragonfly1 for reporting this!
- Fixed a bug where the second and third astropolis built around a single planet would not use the construction timer (if enabled).
- I understand there have been requests to allow for turning gas giants into stars as part of terraforming. This will not be implemented because the fusion lamp makes this largely unnecessary in 0.95a, and because of lore compatibility problems.

## [5.5.0]
- All Terraforming, Domain-Tech, Astral Gate and Miscellaneous features have been disabled by default while I update them for 0.95a. Astral gates will likely be removed from the mod in 6.0.0 due to new content in vanilla.
- The settings file has been reorganized by content type.
- Added a "check for updates" feature to this mod. I copied a bunch of code from Version Checker, so thanks LazyWizard! If Version Checker is updated for 0.95a I will remove this feature. It can also be disabled using the settings file.
- When a station is created, the spaceport and mining industry (if applicable) are queued up for construction rather than being already built.
- Added a non-default setting to cause stations to be "under construction" for a certain amount of time before they become a market. The amount of construction time required is configurable by the player.
- Station sprite change adjusted to occur at lower market sizes to account for 0.95a restrictions to maximum market size.
- Astropolis stations are now built using an ability (like mining and siphon stations) rather than using a colony building.
- Astropolis stations can now be built in orbit around uncolonized planets.
- The "clear orbital path" and planet size requirements for astropolis stations can now be toggled off using the settings file.
- AI Mining Drones (non-default station-exclusive building) now has a story point improvement option.
- Added separate enable/disable toggles in the settings file for astropolis stations, mining stations, siphon stations and the recolonization ability.
- Added separate resource cost options in the settings file for astropolis stations, mining stations, siphon stations and the recolonization ability.
- Added settings to control how mining station resources are assigned. Default is moderate ore and rare ore, but this can be changed to any flat amount, or it can be based off the number of asteroid belts in the system.
- Added settings to control how siphon station resources are assigned. Default is to have the same richness as the gas giant the station orbits, but alternatively it can be set to a flat amount.
- Station settings (ex. base hazard and base accessibility modifications) are now applied to all stations in the sector by default, rather than just stations created using this mod. This can be toggled off in the settings file.
- Added sound effects when stations are constructed.
- Astropolis stations and siphon stations cannot be constructed in orbit around planets with four or more moons. This restriction was implemented to prevent a visual bug from occurring. Unfortunately I cannot fix the visual bug because it's hardcoded in vanilla. Thanks TiberQ for reporting this!
- Fixed various misspellings and grammatical problems. Thanks to Outlander for catching one of them!
- I have been unable to reproduce several bugs related to Nexerelin outposts and Lights Out moons after making updates to the astropolis construction logic. Please be sure to let me know if the bugs persist. Thanks Serenitis and wanderer3421 for reporting these issues!
- Fixed a bug where Ismara's Sling and Asteroid Breaking colony buildings could show up in the build menu even if they were disabled in the settings file. Thanks TiberQ for reporting this!
- It has been suggested that mining stations should be constructible in accretion disks around black holes. After reviewing the in-game description of ring systems, it appears that mining these ring systems would not make sense from a lore standpoint, so I have not made this change.
