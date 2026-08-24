# Migration Plan: MrCrayfish's Vehicle Mod to Minecraft 1.21.1

## Overview

This document outlines the implementation plan for migrating MrCrayfish's Vehicle Mod from **Minecraft 1.16.5 / Forge 36.2.20** to **Minecraft 1.21.1** with compatible Forge version.

### Current State
- **Minecraft Version**: 1.16.5
- **Forge Version**: 36.2.20
- **Mod Version**: 0.45.2-1.16.5
- **Java Version**: 8
- **Source Files**: 313 Java files
- **Dependencies**: 
  - curse.maven:obfuscate-289380:3336021
  - curse.maven:controllable-317269:3519536
  - curse.maven:configured-457570:3546348
  - curse.maven:catalogue-459701:3529457

---

## Phase 1: Environment Setup & Dependencies Update

### 1.1 Update Build System Configuration

**File**: `build.gradle`

**Changes Required**:
- [ ] Update Minecraft version from `1.16.5` to `1.21.1`
- [ ] Update Forge version from `1.16.5-36.2.20` to latest 1.21.1 (likely `1.21.1-50.x.x`)
- [ ] Update mappings channel from `"official", version: "1.16.5"` to `"official", version: "1.21.1"`
- [ ] Update `minecraft` dependency: `net.minecraftforge:forge:1.21.1-<latest>`
- [ ] Update ForgeGradle plugin version if needed (check compatibility)
- [ ] Update mod version string from `0.45.2-1.16.5` to `0.45.2-1.21.1` or higher

**Example Updated build.gradle snippet**:
```gradle
version = "0.45.2-1.21.1"

minecraft {
    mappings channel: "official", version: "1.21.1"
    // ... runs configuration
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.21.1-50.1.0' // Use latest stable
    // ... other dependencies
}
```

### 1.2 Update Mod Metadata

**File**: `src/main/resources/META-INF/mods.toml`

**Changes Required**:
- [ ] Update `loaderVersion` range from `[36,)` to appropriate 1.21.1 range (likely `[50,)`)
- [ ] Update `versionRange` for Minecraft dependency from `[1.16.5,1.17)` to `[1.21.1,1.22)`
- [ ] Update Forge dependency version range from `[36.2.20,)` to `[50.1.0,)`
- [ ] Consider updating mod version to reflect new MC version

**Example Updated mods.toml snippet**:
```toml
modLoader="javafml"
loaderVersion="[50,)"

[[dependencies.vehicle]]
    modId="forge"
    mandatory=true
    versionRange="[50.1.0,)"
    ordering="NONE"
    side="BOTH"
[[dependencies.vehicle]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.21.1,1.22)"
    ordering="NONE"
    side="BOTH"
```

### 1.3 Update Dependencies

**Action**: Check and update all mod dependencies to 1.21.1 compatible versions

**Dependencies to verify**:
- [ ] **Obfuscate** (curse.maven:obfuscate-289380): Check for 1.21.1 version
- [ ] **Controllable** (curse.maven:controllable-317269): Check for 1.21.1 version
- [ ] **Configured** (curse.maven:configured-457570): Check for 1.21.1 version
- [ ] **Catalogue** (curse.maven:catalogue-459701): Check for 1.21.1 version

**Note**: If any dependencies don't have 1.21.1 versions, alternatives must be found or features using them may need to be temporarily disabled.

---

## Phase 2: Java Version Compatibility

### 2.1 Assess Java Requirements

**Current**: Java 8
**Minecraft 1.21.1 Requirement**: Java 17+ (Minecraft 1.17+ requires Java 17)

**Actions**:
- [ ] Update `java.toolchain.languageVersion` in build.gradle from `8` to `17`
- [ ] Verify all code is compatible with Java 17 (1.16.5 code should generally be fine)
- [ ] Update IDE settings to use Java 17 JDK

---

## Phase 3: API Changes & Breaking Changes

### 3.1 Minecraft API Changes (1.16.5 → 1.21.1)

This is a **major version jump** spanning multiple Minecraft versions (1.17, 1.18, 1.19, 1.20, 1.21). Key areas to investigate:

#### 3.1.1 Core System Changes

- **Block & Item Registration**
  - [ ] Verify `DeferredRegister` system still works (Forge changed some internals)
  - [ ] Check for changes in `Block`, `Item`, `BlockItem` constructors
  - [ ] Review `BlockBehaviour.Properties` changes (was introduced in 1.16 but may have updates)

- **Entity System**
  - [ ] Major entity API overhaul in 1.19+ (EntityType.Builder changes)
  - [ ] Entity rendering system updates
  - [ ] Entity attributes system changes
  - [ ] Review `Entity` class hierarchy changes

- **Rendering**
  - [ ] Render system changes (especially with Fabric API influence in newer Forge)
  - [ ] Model loading and baking changes
  - [ ] Vertex buffer and mesh changes
  - [ ] Review `RenderType` changes

- **Networking**
  - [ ] Packet system changes in newer Forge versions
  - [ ] `SimpleChannel` may have API changes
  - [ ] Network direction handling

- **World & Level**
  - [ ] `Level` vs `World` class changes (Minecraft renamed World to Level in 1.18+)
  - [ ] Chunk system changes
  - [ ] Fluid handling updates

- **Inventory & Items**
  - [ ] Inventory API changes
  - [ ] ItemStack changes
  - [ ] Container/Menu system updates

- **Recipes**
  - [ ] Recipe system overhaul in 1.20+ (json recipe changes)
  - [ ] Ingredient system updates

- **Tags**
  - [ ] Tag system changes (1.18+ has new tag types)

- **Commands**
  - [ ] Command system updates (Brigadier changes)

#### 3.1.2 Forge-Specific Changes

- [ ] Forge registry system updates
- [ ] Forge event bus changes
- [ ] Forge capabilities system (replaced by components in 1.20.3+)
- [ ] Forge fluid system changes
- [ ] Forge energy system (if used)

### 3.2 Deprecated Method Replacements

Create a mapping of deprecated methods from 1.16.5 that need replacement:

| Old Method (1.16.5) | New Method (1.21.1) | Notes |
|---------------------|---------------------|-------|
| `World` class | `Level` class | Renamed in 1.18+ |
| `getMinecraft()` | `Minecraft.getInstance()` | Static access |
| `Entity#isAlive()` | `Entity#isAlive()` | May have different behavior |
| `BlockPos#up/down/north/south/east/west` | Same | Verify return types |
| `ItemStack#getTag()` | `ItemStack#getTag()` | May return Optional |
| `ItemStack#setTag()` | `ItemStack#setTag()` | Verify behavior |
| `ForgeRegistries` access | New registry system | Major changes in 1.20+ |

### 3.3 Specific Mod Areas to Review

#### 3.3.1 Entity System (High Priority)

The mod has extensive entity usage (`entity/` package with 22 classes). Key areas:

- [ ] **Vehicle Entities**: Custom entity types registration
- [ ] **Entity Properties**: Custom property system (`ExtendedProperties`, `VehicleProperties`, etc.)
- [ ] **Entity Movement**: Physics, collision handling
- [ ] **Entity Rendering**: Custom renderers for vehicles
- [ ] **Entity AI**: Any custom AI behaviors
- [ ] **Entity Interaction**: Player interaction with vehicles

**Files to review**:
- `com/mrcrayfish/vehicle/entity/` - All 22 files
- `com/mrcrayfish/vehicle/entity/properties/` - All property classes

#### 3.3.2 Block & TileEntity System

- [ ] **Block Registration**: Verify block registration works
- [ ] **TileEntity → BlockEntity**: Renamed in 1.18+
- [ ] **BlockEntityRenderer**: Changes in rendering system
- [ ] **Fluid Handling**: Fluid interaction with blocks

**Files to review**:
- `com/mrcrayfish/vehicle/block/` - All block classes
- `com/mrcrayfish/vehicle/tileentity/` - All tile entity classes (now BlockEntity)
- `com/mrcrayfish/vehicle/fluid/` - Fluid-related classes

#### 3.3.3 Networking

- [ ] **Packet System**: Review all packet handling
- [ ] **PacketHandler** class updates
- [ ] **Message serialization**: Verify serialization/deserialization
- [ ] **Network direction**: CLIENTBOUND vs SERVERBOUND handling

**Files to review**:
- `com/mrcrayfish/vehicle/network/` - All 4 files

#### 3.3.4 Rendering (Client-Side)

- [ ] **Model Loading**: Custom model loaders
- [ ] **Model Baking**: Changes in model baking pipeline
- [ ] **Rendering**: Vertex buffers, meshes, shaders
- [ ] **Textures**: Texture atlas changes
- [ ] **RenderType**: Changes in render type system

**Files to review**:
- `com/mrcrayfish/vehicle/client/` - All client classes
- `com/mrcrayfish/vehicle/client/model/` - Model-related classes

#### 3.3.5 Items & Crafting

- [ ] **Item Registration**: Verify item registration
- [ ] **Item Properties**: Custom item properties
- [ ] **Crafting Recipes**: Recipe system changes
- [ ] **Workstation**: Custom crafting system

**Files to review**:
- `com/mrcrayfish/vehicle/item/` - All item classes
- `com/mrcrayfish/vehicle/crafting/` - All crafting classes
- `com/mrcrayfish/vehicle/recipe/` - All recipe classes

#### 3.3.6 Configuration

- [ ] **Config System**: Forge config changes
- [ ] **Config Loading**: JSON vs TOML changes
- [ ] **Config Synchronization**: Server-client sync

**Files to review**:
- `com/mrcrayfish/vehicle/Config.java`

#### 3.3.7 Data Generation

- [ ] **Loot Tables**: Loot table generation changes
- [ ] **Recipes**: Recipe generation changes
- [ ] **Advancements**: Advancement generation (if any)

**Files to review**:
- `com/mrcrayfish/vehicle/datagen/` - All 3 files

#### 3.3.8 Commands

- [ ] **Command Registration**: Changes in command registration
- [ ] **Command Syntax**: Brigadier syntax changes
- [ ] **Command Arguments**: New argument types

**Files to review**:
- `com/mrcrayfish/vehicle/init/ModCommands.java`

#### 3.3.9 Events

- [ ] **Event Handling**: Review all event listeners
- [ ] **Event Bus**: Forge event bus changes
- [ ] **Custom Events**: Any custom events

**Files to review**:
- `com/mrcrayfish/vehicle/common/CommonEvents.java`

---

## Phase 4: Code Migration Strategy

### 4.1 Automated Migration Steps

1. **Create backup branch**: `git checkout -b backup/1.16.5-before-migration`
2. **Update build files first**: Update build.gradle, gradle.properties, mods.toml
3. **Run Gradle sync**: Test if basic build works
4. **Fix compilation errors**: Address immediate compilation issues
5. **Fix runtime errors**: Test in development environment

### 4.2 Manual Migration Steps

#### Step 1: Update Imports
- [ ] Run IDE's "Optimize Imports" to update all imports
- [ ] Search for deprecated imports and replace with new ones
- [ ] Update Forge imports to new versions

#### Step 2: Update Class References
- [ ] Replace all `World` with `Level` (with appropriate imports)
- [ ] Replace `TileEntity` with `BlockEntity`
- [ ] Replace other renamed classes

#### Step 3: Update Method Calls
- [ ] Replace deprecated method calls
- [ ] Update method signatures where parameters changed
- [ ] Update return type handling (Optional, etc.)

#### Step 4: Update Entity Registration
- [ ] Review `ModEntities.java` registration
- [ ] Update entity type builders
- [ ] Verify entity attributes

#### Step 5: Update Rendering Code
- [ ] Review all renderers in `client/` package
- [ ] Update model loading code
- [ ] Verify texture handling

#### Step 6: Update Networking
- [ ] Review `PacketHandler.java`
- [ ] Verify all packet message classes
- [ ] Test packet serialization

#### Step 7: Update Configuration
- [ ] Review `Config.java`
- [ ] Update config spec builders
- [ ] Verify config sync

### 4.3 Testing Strategy

#### 4.3.1 Unit Testing
- [ ] Create test cases for critical functionality
- [ ] Test entity registration
- [ ] Test item/block registration
- [ ] Test networking
- [ ] Test configuration

#### 4.3.2 Integration Testing
- [ ] Test mod loading in game
- [ ] Test vehicle spawning
- [ ] Test vehicle interaction
- [ ] Test crafting recipes
- [ ] Test multiplayer synchronization

#### 4.3.3 Compatibility Testing
- [ ] Test with other popular mods (if available for 1.21.1)
- [ ] Test on different OS (Windows, Linux, macOS)
- [ ] Test with different Forge versions

---

## Phase 5: Specific File-by-File Changes

### 5.1 Critical Files Requiring Attention

| File | Priority | Notes |
|------|----------|-------|
| `build.gradle` | HIGH | Update versions, dependencies |
| `mods.toml` | HIGH | Update version constraints |
| `VehicleMod.java` | HIGH | Main mod class, registration |
| `ModEntities.java` | HIGH | Entity registration, major API changes |
| `ModBlocks.java` | HIGH | Block registration |
| `ModItems.java` | HIGH | Item registration |
| `PacketHandler.java` | HIGH | Networking system |
| `Config.java` | HIGH | Configuration system |
| `ClientHandler.java` | HIGH | Client setup |
| All `entity/` classes | HIGH | Entity API changes |
| All `tileentity/` classes | HIGH | Renamed to BlockEntity |
| All `client/` classes | HIGH | Rendering changes |
| All `network/` classes | HIGH | Packet handling |

### 5.2 Medium Priority Files

| File | Priority | Notes |
|------|----------|-------|
| `ModTileEntities.java` | MEDIUM | BlockEntity registration |
| `ModContainers.java` | MEDIUM | Menu/Container system |
| `ModFluids.java` | MEDIUM | Fluid system changes |
| `CommonEvents.java` | MEDIUM | Event handling |
| `ModCommands.java` | MEDIUM | Command system |
| All `crafting/` classes | MEDIUM | Recipe system |
| All `block/` classes | MEDIUM | Block behavior |
| All `item/` classes | MEDIUM | Item behavior |

### 5.3 Low Priority Files

| File | Priority | Notes |
|------|----------|-------|
| Utility classes | LOW | Generally stable |
| Helper classes | LOW | Generally stable |
| Data generation | LOW | May need minor updates |

---

## Phase 6: Timeline & Milestones

### Week 1: Environment Setup
- [ ] Update build files
- [ ] Update dependencies
- [ ] Get project to compile
- [ ] Create development testing environment

### Week 2: Core System Migration
- [ ] Update entity registration
- [ ] Update block/item registration
- [ ] Update networking
- [ ] Update configuration

### Week 3: Rendering & Client Migration
- [ ] Update all client-side code
- [ ] Update model loading
- [ ] Update rendering
- [ ] Test client-side features

### Week 4: Testing & Bug Fixing
- [ ] Fix compilation errors
- [ ] Fix runtime errors
- [ ] Test all features
- [ ] Create test cases

### Week 5: Finalization
- [ ] Integration testing
- [ ] Compatibility testing
- [ ] Performance testing
- [ ] Documentation updates

---

## Phase 7: Risk Assessment

### High Risk Areas
1. **Entity System**: Major changes in Minecraft 1.19+ entity API
2. **Rendering**: Significant rendering engine changes across versions
3. **Networking**: Packet system changes
4. **Dependencies**: External mod dependencies may not have 1.21.1 versions

### Medium Risk Areas
1. **Recipes**: Recipe system overhaul
2. **Configuration**: Config system changes
3. **Commands**: Command system updates

### Low Risk Areas
1. **Utility Classes**: Generally stable APIs
2. **Data Generation**: Minor changes expected

### Mitigation Strategies
- **Frequent commits**: Commit after each major change
- **Backup branches**: Maintain backup branches
- **Incremental testing**: Test after each phase
- **Documentation**: Document all changes made

---

## Phase 8: Tools & Resources

### Recommended Tools
- **IntelliJ IDEA**: Best Java IDE with Minecraft support
- **Forge Gradle**: Use latest version
- **MCP Reborn**: For decompiled Minecraft code reference
- **Git**: Version control
- **JD-GUI**: For examining dependency JARs

### Useful Resources
- [Forge Documentation](https://mcforge.readthedocs.io/)
- [Minecraft Wiki](https://minecraft.fandom.com/)
- [Minecraft Forge Discord](https://discord.gg/UvedJ9m)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods) - For dependency versions
- [Modrinth](https://modrinth.com/) - Alternative mod hosting

### Command Reference
```bash
# Update Gradle wrapper
gradlew wrapper --gradle-version 8.6

# Clean build
gradlew clean

# Build mod
gradlew build

# Run client
gradlew runClient

# Run server
gradlew runServer

# Generate run configurations
gradlew genIntellijRuns

# Refresh dependencies
gradlew --refresh-dependencies
```

---

## Phase 9: Checklist Before Release

- [ ] All code compiles without errors
- [ ] All code compiles without warnings (or warnings are documented)
- [ ] Mod loads in singleplayer
- [ ] Mod loads in multiplayer (server and client)
- [ ] All vehicles can be crafted
- [ ] All vehicles can be spawned
- [ ] All vehicles can be driven/ridden
- [ ] All vehicle features work (fuels, upgrades, etc.)
- [ ] Configuration system works
- [ ] Commands work
- [ ] Networking synchronization works
- [ ] All dependencies are compatible
- [ ] Mod metadata is correct
- [ ] Version numbers are updated
- [ ] Changelog is updated
- [ ] README is updated

---

## Phase 10: Post-Migration Tasks

- [ ] Update mod description for 1.21.1
- [ ] Update screenshots/videos if needed
- [ ] Update issue tracker labels
- [ ] Notify users of version update
- [ ] Publish to CurseForge/Modrinth
- [ ] Monitor for bug reports
- [ ] Plan next feature updates

---

## Appendix A: Minecraft Version Changes Summary

### Major Changes Between Versions

| Version | Key Changes |
|---------|-------------|
| 1.17 | Java 17 requirement, Bundles, New cave generation |
| 1.18 | World height changes, Biome changes, `Level` replaces `World` |
| 1.19 | The Wild Update, Mangrove swamps, Deep Dark, Warden |
| 1.20 | Trails & Tales, Armadillos, Bamboo, Chiseling, Sniffer |
| 1.21 | Tricky Trials, Trial Chambers, Breeze, Bogged, Armadillo updates |

### Forge-Specific Changes

| Forge Version | Minecraft | Key Changes |
|---------------|-----------|-------------|
| 36.x | 1.16.5 | Current version |
| 40.x | 1.18.x | Major API updates |
| 43.x | 1.19.x | Entity API overhaul |
| 47.x | 1.20.x | More API stability |
| 50.x | 1.21.x | Latest stable |

---

## Appendix B: Quick Start Commands

```bash
# On branch test/1.21.1_beta

# 1. Update build files as per this plan
# 2. Run Gradle wrapper update
gradlew wrapper --gradle-version 8.6

# 3. Refresh and build
gradlew clean build --refresh-dependencies

# 4. If build succeeds, run client
gradlew runClient

# 5. Fix errors incrementally
```

---

*Document Version*: 1.0  
*Last Updated*: 2026-07-15  
*Author*: Migration Plan Generated for Vehicle Mod 1.21.1 Migration
