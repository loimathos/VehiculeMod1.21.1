# Reprise du portage 1.16.5 → 1.21.1 — état au 27/07/2026

Branche : `test/1.21.1_beta` · Forge **1.21.1-52.1.15** · Java 21 · mappings **official**

Ce document remplace la lecture de tout l'historique : il dit ce qui marche, ce qui reste,
et les pièges déjà rencontrés (pour ne pas les repayer). Le plan d'origine détaillé est dans
`MIGRATION_PLAN_1.21.1.md` (obsolète en partie) et le plan d'exécution réel est résumé ici.

---

## Où on en est

**Le mod compile (0 erreur, 332 fichiers) et CHARGE en jeu** : menu principal OK, monde OK.
Vérifié en production dans PrismLauncher le 27/07 au soir.

Dernier build déployé : `build/libs/vehicle-mod-0.45.2-1.21.1.jar` copié dans
`C:\Users\loimathos\AppData\Roaming\PrismLauncher\instances\1.21.1\minecraft\mods\` (22:17).

**Dernier test en attente** (à faire au prochain lancement) :
1. Blocs/items texturés ? Véhicules texturés ? (fix atlas déplacé vers `assets/minecraft/atlases/blocks.json`)
2. Cliquer une caisse de véhicule en créatif ne kick plus ? (fix `BLOCK_ENTITY_DATA` + `id`)
3. Onglet créatif rempli, 4 keybinds dans Options → Contrôles ?

⚠️ **Si test sur serveur** : le serveur doit avoir LE MÊME jar. Le kick
`set_creative_mode_slot` du 27/07 est arrivé sur un serveur — le jar serveur n'a jamais été
mis à jour (chemin du serveur inconnu à ce jour).

---

## Fait (Stages 0–2 du plan, commit "Phase 7")

| Quoi | Où | Détail |
|---|---|---|
| Bloc `runs{}` client/server/data | `build.gradle` | `gradlew runClient / runServer / runData` existent enfin |
| Access transformer | `META-INF/accesstransformer.cfg` + `build.gradle` | Expose `Camera.setPosition/move/getMaxZoom`. **Piège : une ligne `#` nue fait planter le parseur AT** → tout commentaire doit avoir du texte |
| Crash reflection caméra (A1) | `client/CameraHelper.java` | Les 4 lookups SRG 1.16 supprimés, appels directs. `move`/`getMaxZoom` prennent des `float` en 1.21.1 (plus des `double`). `getLeftVector()` est public → la reflection sur `left` était inutile |
| Chargement resources module-safe (A2) | `entity/properties/VehicleProperties.java` | `getResourceAsStream` → `ModList.getModFileById(...).getFile().findResource(...)` (JPMS bloque l'ancien chemin en prod) |
| Atlas textures (B1) | `assets/minecraft/atlases/blocks.json` | **Piège : le fichier atlas va sous le namespace DE L'ATLAS (`minecraft`), pas du mod** — sous `assets/vehicle/` il est ignoré en silence. Stitche `model/`, `vehicle/`, `vehicles/` |
| Onglet créatif vide (B2) | `init/ModCreativeTab.java` (nouveau) | `BuildCreativeModeTabContentsEvent` ; itère `ModItems.REGISTER` (couvre aussi les BlockItems) + caisses |
| Caisses créatif / kick réseau | `block/VehicleCrateBlock.java` | 1.20.5 : `BlockEntityTag` n'existe plus → composant `BLOCK_ENTITY_DATA`, **obligatoirement via `BlockItem.setBlockEntityData`** (le codec exige un champ `id`, sinon kick `set_creative_mode_slot` en créatif) |
| Keybinds morts (B3) | `client/KeyBinds.java` | `@Mod.EventBusSubscriber(bus=MOD)` — l'event part AVANT `FMLClientSetupEvent`, impossible de l'enregistrer depuis `ClientHandler.setup()`. +2 clés lang |
| Stub `ItemBlockRenderTypesHelper` (B4) | supprimé | Le commentaire "removed in 1.21.1" était FAUX — `ItemBlockRenderTypes.setRenderLayer` existe toujours, appels directs restaurés (cutout/translucent) |
| Thread-safety setup (D) | `client/ClientHandler.java`, `VehicleMod.java` | `MenuScreens.register` + render layers sous `event.enqueueWork` ; couleurs d'items → `RegisterColorHandlersEvent.Item` |
| 5 modèles boost_pad | `models/block/boost_pad*.json` | `vehicle:blocks/…` → `vehicle:block/…` (typo, dossier inexistant) |

## Reste à faire (Stages 2 fin + 3 + 4)

Par ordre recommandé — détail complet dans le plan approuvé
(`C:\Users\loimathos\.claude\plans\hey-i-am-currently-tender-quilt.md`) :

- **B5** — 5 draws immediate-mode sans `RenderSystem.setShader` : `util/RenderUtil.java:50,74`,
  `util/FluidUtils.java:135`, `client/screen/EditVehicleScreen.java:107`,
  `client/render/EntityVehicleRenderer.java:112`. Jauges de fluides + preview Edit Vehicle vides sinon.
- **B6** — Compteur de vitesse : `client/handler/OverlayHandler.java:83` construit un `GuiGraphics`
  dans `RenderTickEvent` → jamais visible. Migrer vers `RenderGuiEvent`.
- **C3** — `cap == null` → `cap == ForgeCapabilities.ITEM_HANDLER` dans
  `FluidExtractorBlockEntity:452` et `FluidMixerBlockEntity:621` (hoppers cassés).
  ⚠️ Balayer TOUS les commentaires « removed in 1.21.1 » : les capabilities Forge existent
  toujours en 52.x (c'est NeoForge qui les a viré). Plusieurs stubs viennent de cette confusion.
- **C2** — **Le plus gros morceau** : les 3 serializers de recettes custom sont des coquilles vides
  (`crafting/FluidExtractorRecipeSerializer`, `FluidMixerRecipeSerializer`,
  `WorkstationRecipeSerializer`). Résultat hardcodé `null` / matériaux `emptyList()`, et
  `ItemStack.CODEC` ne parse pas le JSON `{"item": ...}` → utiliser `Ingredient.CODEC` + vrais
  `MapCodec`/`StreamCodec` lisant TOUS les champs.
- **C1/C9** — Données 1.16 : dossiers `recipes/`→`recipe/`, `loot_tables/`→`loot_table/`,
  `advancements/`→`advancement/` (rien ne charge actuellement). Réactiver `onGatherData`
  (`VehicleMod.java:101`), corriger smithing→`smithing_transform` dans `datagen/RecipeGen`,
  lancer `gradlew runData`, supprimer l'ancien `src/generated/resources`. Corriger à la main les
  7 recettes de roues dans `src/main/resources/data/vehicle/recipes/`
  (`"result": {"item": X}` → `{"id": X}`).
- **C4** — `com/mrcrayfish/obfuscate/.../SyncedPlayerData` est un stub no-op → pompe à essence et
  attelage de remorque morts en silence. Remplacer par `AttachmentType` Forge + packet de sync
  sur `PLAY_CHANNEL`. 2 clés : `GAS_PUMP` (`Optional<BlockPos>`), `TRAILER` (int).
- **C5** — `PlayerModelEvent`/`RenderItemEvent` (stubs obfuscate) ne sont JAMAIS postés → 4 handlers
  morts (poses assises, animation de portage, pistolet à essence). Ré-exprimer sur
  `RenderPlayerEvent`/`RenderHandEvent`, puis supprimer le package `obfuscate`.
- **C7** — Coremod mort : `META-INF/coremods.json` + `transformers/camera.js` ciblent des noms SRG
  1.16. Supprimer, ré-exprimer via `ViewportEvent.ComputeCameraAngles/ComputeFov` +
  `MovementInputUpdateEvent`. (`CameraHandler.setupVehicleCamera/onPlayerTurn` ne sont jamais appelés.)
- **C8** — Canal handshake vide : les 2 messages login (`HandshakeMessages`) ne sont plus enregistrés
  → pas de sync des propriétés véhicules serveur→client, et le canal vide rejette les clients vanilla.
- **C6** — `client/VehicleHelper.java:251` : vitesse hélico hardcodée `return 0F` ; `:214` mouvement
  clavier hélico commenté. **Décision actée : support manette ABANDONNÉ** (garder les guards
  `ModList.isLoaded("controllable")`, supprimer le package stub `controllable`).
- **D divers** — layer véhicule porté via `EntityRenderersEvent.AddLayers` (reflection + flag one-shot
  actuel se perd au resource reload) ; `.setNormal()` manquant sur `RenderType.LINES`
  (`EntityRayTracer:713,771`, crash si `renderOutlines` on) ; `getTextureLocation` null (NPE F3+B) ;
  `FluidMixerBlockEntity:99` hash d'identité fluide par `Object.hashCode()`.
- **Stage 4** — Déplacer `com/mrcrayfish/framework/OpenModel` (collision avec le vrai mod Framework),
  supprimer les packages stub `obfuscate`/`controllable`, `pack.mcmeta` → 34/48 (cosmétique),
  ajouter les tags `mineable/` (aucun bloc n'a d'outil attitré).

## Vérification (gates du plan)

1. Menu principal sans erreur ✅ (vérifié 27/07)
2. `grep "Missing textures in model vehicle:" logs/debug.log` → **0** (fix déployé, à re-vérifier)
3. Onglet créatif + textures + keybinds (à re-vérifier)
4. `/reload` sans erreurs de parsing recettes/loot (attendu KO tant que C1 pas fait)
5. Craft roue + moteur à la workstation (KO tant que C2 pas fait)
6. Boucle : caisse → rouler → plein → remorque → coffre (KO tant que C4 pas fait)
7. Hélico/avion volent, roll caméra (KO tant que C6/C7 pas faits)
8. Serveur dédié `runServer` (KO tant que C8 pas fait)
9. `build_and_deploy.ps1` + test dans Prism

## Comment builder

- **Toi (terminal normal)** : `.\gradlew build` ou `build_and_deploy.ps1` (build + copie dans Prism).
  Pas de reobf nécessaire — Forge 1.20.2+ tourne en mappings official, `gradlew jar` suffit.
- **⚠️ Session Claude Code : Gradle NE MARCHE PAS** (`Selector.open()` bloqué par l'environnement →
  « Unable to establish loopback connection », non contournable). Pipeline de secours utilisé et
  fiable : `javac` direct contre le jar ForgeGradle mappé
  (`~/.gradle/caches/forge_gradle/minecraft_user_repo/...52.1.15_mapped_official_1.21.1.jar`,
  patché AT localement via ASM) + libs Prism 1.21.1 dédupliquées, puis `jar cfm` avec
  `src/main/resources` + `src/generated/resources`. ⚠️ Exclure les jars 1.16.5 du classpath
  (forgespi 3.2.0, DFU 4.0.26 traînent dans les libraries Prism).

## Pièges déjà payés (ne pas refaire)

1. Ligne `#` nue dans un fichier AT → crash au boot, avant même le chargement des mods.
2. Fichier atlas sous le namespace du mod → ignoré en silence (aucun log). C'est `assets/minecraft/`.
3. `BLOCK_ENTITY_DATA` sans champ `id` → kick réseau en créatif (validation `validatedStreamCodec`).
4. Les commentaires « X removed in 1.21.1 » du portage initial sont souvent faux — vérifier au javap
   avant de croire un stub.
5. `RegisterKeyMappingsEvent` part pendant le constructeur de `Minecraft`, avant client setup.
6. Ne jamais utiliser `Class.getResourceAsStream("/data/...")` — JPMS le bloque en prod.
