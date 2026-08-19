package com.example;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class CheatMusicBlockMod implements ModInitializer {
	//TODO add black block
	//TODO add recursive getblocks
	//todo handle pausing
	public static final String MOD_ID = "cheatmusicblockmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean showAccidentals = true;

	//todo maybe use list, but need to prevent duplicates
	//public static List<BlockPos>[] cheatMusicBlockCoordinates =  (List<BlockPos>[]) new ArrayList[7];
	public static Set<BlockPos>[] cheatMusicBlockCoordinates =  (Set<BlockPos>[]) new HashSet[7];

	private static Block[] createTierArrayBlock(Supplier<FabricBlockSettings> settingsSupplier) {
		return IntStream.range(0, 7)
				.mapToObj(tier -> new CheatMusicBlock(settingsSupplier.get(), tier))
				.toArray(Block[]::new);
	}




	public static final Map<String, Block[]> CHEAT_MUSIC_BLOCKS = Map.of(
			"base",         createTierArrayBlock(() -> FabricBlockSettings.create().strength(1.5f).requiresTool()),
			"_leaf",         createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque()),
			"_stone",        createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.STONE)),
			"_oak_plank",   createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)),
			"_stone_bricks", createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.STONE_BRICKS)),
			"_cobblestone", createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.COBBLESTONE)),
			"_dirt", createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.DIRT)),
			"_oak_log", createTierArrayBlock(() -> FabricBlockSettings.copyOf(Blocks.OAK_PLANKS))
	);






	// 1. Create the Identifier and SoundEvent

	public static final Identifier WET_HANDS_ID = new Identifier(MOD_ID, "wet_hands");
	public static final SoundEvent WET_HANDS_EVENT = SoundEvent.of(WET_HANDS_ID);
	public static final Identifier SWEDEN_ID = new Identifier(MOD_ID, "sweden");
	public static final SoundEvent SWEDEN_EVENT = SoundEvent.of(SWEDEN_ID);
	public static final Identifier SUBWOOFER_LULLABY_ID = new Identifier(MOD_ID, "subwoofer_lullaby");
	public static final SoundEvent SUBWOOFER_LULLABY_EVENT = SoundEvent.of(SUBWOOFER_LULLABY_ID);
	public static final Identifier DRY_HANDS_ID = new Identifier(MOD_ID, "dry_hands");
	public static final SoundEvent DRY_HANDS_EVENT = SoundEvent.of(DRY_HANDS_ID);
	//haggstrom
	public static final Identifier HAGGSTROM_ID = new Identifier(MOD_ID, "haggstrom");
	public static final SoundEvent HAGGSTROM_EVENT = SoundEvent.of(HAGGSTROM_ID);
	//living mice
	public static final Identifier LIVING_MICE_ID = new Identifier(MOD_ID, "living_mice");
	public static final SoundEvent LIVING_MICE_EVENT = SoundEvent.of(LIVING_MICE_ID);
	//danny
	public static final Identifier DANNY_ID = new Identifier(MOD_ID, "danny");
	public static final SoundEvent DANNY_EVENT = SoundEvent.of(DANNY_ID);
	//clark
	public static final Identifier CLARK_ID = new Identifier(MOD_ID, "clark");
	public static final SoundEvent CLARK_EVENT = SoundEvent.of(CLARK_ID);
	//moog city 2
	public static final Identifier MOOG_CITY_2_ID = new Identifier(MOD_ID, "moog_city_2");
	public static final SoundEvent MOOG_CITY_2_EVENT = SoundEvent.of(MOOG_CITY_2_ID);
	//minecraft
	public static final Identifier MINECRAFT_ID = new Identifier(MOD_ID, "minecraft");
	public static final SoundEvent MINECRAFT_EVENT = SoundEvent.of(MINECRAFT_ID);
	//mice on venus
	public static final Identifier MICE_ON_VENUS_ID = new Identifier(MOD_ID, "mice_on_venus");
	public static final SoundEvent MICE_ON_VENUS_EVENT = SoundEvent.of(MICE_ON_VENUS_ID);
	//bach prelude in c major
	public static final Identifier BACH_PRELUDE_C_MAJOR_ID = new Identifier(MOD_ID, "bach_prelude_c_major");
	public static final SoundEvent BACH_PRELUDE_C_MAJOR_EVENT = SoundEvent.of(BACH_PRELUDE_C_MAJOR_ID);
	//satie gymnopedie
	public static final Identifier SATIE_GYMNOPEDIE_ID = new Identifier(MOD_ID, "satie_gymnopedie");
	public static final SoundEvent SATIE_GYMNOPEDIE_EVENT = SoundEvent.of(SATIE_GYMNOPEDIE_ID);

	@Override
	public void onInitialize() {
		//initialize cheatmusicblockcoordinates
		//for(int i = 0; i < cheatMusicBlockCoordinates.length; i++){
		//	cheatMusicBlockCoordinates[i] = new ArrayList<>();
		//}
		for(int i = 0; i < cheatMusicBlockCoordinates.length; i++){
			cheatMusicBlockCoordinates[i] = new HashSet<>();
		}

		//register blocks
		for (Map.Entry<String, Block[]> entry : CHEAT_MUSIC_BLOCKS.entrySet()) {
			String variant = entry.getKey();
			Block[] blocks = entry.getValue();
			String path = "cheat_music_block";
			if (!variant.equals("base")){//if it is the cheatmusic block and not a variant of an existing block
				path += variant;
			}
			for (int i = 0; i < blocks.length; i++) {

				//Identifier id = new Identifier(MOD_ID, path);

				//register blocks
				Registry.register(Registries.BLOCK, new Identifier(MOD_ID, path + i), blocks[i]);
				Registry.register(Registries.ITEM, new Identifier(MOD_ID, path + i),
						new BlockItem(blocks[i], new FabricItemSettings()));

			}
		}




		//Add blocks to inventory menu
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
			for (Map.Entry<String, Block[]> entry : CHEAT_MUSIC_BLOCKS.entrySet()) {
				Block[] blocks = entry.getValue();
				for(int i = 0; i < blocks.length; i++){
					content.add(blocks[i]);
				}
			}

		});



		//Register the Sound Event to the game
		Registry.register(Registries.SOUND_EVENT, WET_HANDS_ID, WET_HANDS_EVENT);
		Registry.register(Registries.SOUND_EVENT, SWEDEN_ID, SWEDEN_EVENT);
		Registry.register(Registries.SOUND_EVENT, SUBWOOFER_LULLABY_ID, SUBWOOFER_LULLABY_EVENT);
		Registry.register(Registries.SOUND_EVENT, DRY_HANDS_ID, DRY_HANDS_EVENT);
		Registry.register(Registries.SOUND_EVENT, HAGGSTROM_ID, HAGGSTROM_EVENT);
		Registry.register(Registries.SOUND_EVENT, LIVING_MICE_ID, LIVING_MICE_EVENT);
		Registry.register(Registries.SOUND_EVENT, DANNY_ID, DANNY_EVENT);
		Registry.register(Registries.SOUND_EVENT, CLARK_ID, CLARK_EVENT);
		Registry.register(Registries.SOUND_EVENT, MOOG_CITY_2_ID, MOOG_CITY_2_EVENT);
		Registry.register(Registries.SOUND_EVENT, MICE_ON_VENUS_ID, MICE_ON_VENUS_EVENT);
		Registry.register(Registries.SOUND_EVENT, MINECRAFT_ID, MINECRAFT_EVENT);
		Registry.register(Registries.SOUND_EVENT, BACH_PRELUDE_C_MAJOR_ID, BACH_PRELUDE_C_MAJOR_EVENT);
		Registry.register(Registries.SOUND_EVENT, SATIE_GYMNOPEDIE_ID, SATIE_GYMNOPEDIE_EVENT);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("getblocks")
							.then(CommandManager.argument("radius", IntegerArgumentType.integer(1, 100))
									.executes(context -> {
										ServerPlayerEntity player = context.getSource().getPlayer();
										if (player == null) return 0; // Ensures command is run by a player
										int radius = IntegerArgumentType.getInteger(context, "radius");
										int numFound = getCheatMusicBlocksInRadius(context, player, radius);
										int total = 0;
										for (int i = 0; i < cheatMusicBlockCoordinates.length; i++){
											total += cheatMusicBlockCoordinates[i].size();
										}
										int total2 = total;
										context.getSource().sendFeedback(() -> Text.literal(
												"Found " + numFound + " CheatMusic blocks! Total blocks are " + total2
										), false);

										return 1;
									})
							)
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("setletter")
					.then(CommandManager.argument("block_number", IntegerArgumentType.integer(1, 7))
							.then(CommandManager.argument("letter", StringArgumentType.string())
											.executes(context -> {
												ServerPlayerEntity player = context.getSource().getPlayer();
												if (player == null) return 0; // Ensures command is run by a player
												int block_number = IntegerArgumentType.getInteger(context, "block_number");
												String letter = StringArgumentType.getString(context,"letter");
												String command = "/setletters ";// + "0 0 0 0 0 0 0" + " " + radius;
												for (int i = 1; i <= 7; i++){
													if (i == block_number){
														command += letter;
													}
													else{
														command += "0";
													}
													if (i < 7){//add a space after except for last one
														command += " ";
													}
												}
												// Get the command manager from the server instance
												player.getServer().getCommandManager().executeWithPrefix(
														player.getCommandSource(), command
												);


												return 1;
											})
							)
					)
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("setletters")
					.then(CommandManager.argument("letter1", StringArgumentType.string())
							.then(CommandManager.argument("letter2", StringArgumentType.string())
									.then(CommandManager.argument("letter3", StringArgumentType.string())
											.then(CommandManager.argument("letter4", StringArgumentType.string())
													.then(CommandManager.argument("letter5", StringArgumentType.string())
															.then(CommandManager.argument("letter6", StringArgumentType.string())
																	.then(CommandManager.argument("letter7", StringArgumentType.string())
																					.executes(context -> {
																						ServerPlayerEntity player = context.getSource().getPlayer();
																						if (player == null) return 0; // Ensures command is run by a player
																						int[] letters_to_numbers = new int[7];
																						//converting letters like "a", "b", "a2", to the number types -> 0, 1, 22
																						for(int i = 0; i < 7; i++){
																							String letters = StringArgumentType.getString(context,"letter" + (i + 1));
																							if (letters.equals("0")){
																								letters_to_numbers[i] = -1;
																								continue;
																							}
																							int compare = letters.substring(0, 1).compareToIgnoreCase("a");
																							letters_to_numbers[i] = compare;
																							if (letters.length() == 2 && showAccidentals == true){//convert accidental
																								if (letters.charAt(1) == '4'){//flat
																									letters_to_numbers[i] += 14;
																								}
																								if (letters.charAt(1) == '2'){//sharp
																									letters_to_numbers[i] += 21;
																								}
																								if (letters.charAt(1) == '1'){//double sharp
																									letters_to_numbers[i] += 28;
																								}
																								if (letters.charAt(1) == '5'){//double flat
																									letters_to_numbers[i] += 7;
																								}
																							}
																						}
																						int numSet = setCheatMusicBlocks(context, player, letters_to_numbers);
																						return numSet;
																					})
																			)
															)
													)
											)
									)
							)
					)
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("fillreplace")
					.then(CommandManager.argument("horizontal_distance", IntegerArgumentType.integer(0, 300))
							.then(CommandManager.argument("vertical_distance", IntegerArgumentType.integer(0, 300))
								.then(CommandManager.argument("fill_block", BlockStateArgumentType.blockState(registryAccess))
									.then(CommandManager.argument("block_to_replace", BlockStateArgumentType.blockState(registryAccess))
											.executes(context -> {
												ServerPlayerEntity player = context.getSource().getPlayer();
												ServerCommandSource source = context.getSource();
												if (player == null) return 0; // Ensures command is run by a player
												int hDist = IntegerArgumentType.getInteger(context, "horizontal_distance");
												int vDist = IntegerArgumentType.getInteger(context, "vertical_distance");
												// Extract BlockInput from context
												BlockStateArgument blockInput = BlockStateArgumentType.getBlockState(context, "fill_block");
												//Extract block identifier (e.g., "minecraft:stone" or "your_mod_id:custom_block")
												String blockId = Registries.BLOCK.getId(blockInput.getBlockState().getBlock()).toString();


												BlockStateArgument blockInput2 = BlockStateArgumentType.getBlockState(context, "block_to_replace");
												String blockId2 = Registries.BLOCK.getId(blockInput2.getBlockState().getBlock()).toString();
												// 2. Get current player position & horizontal facing vector (pitch = 0)
												Vec3d playerPos = source.getPosition();
												float yaw = source.getRotation().y;
												Vec3d horizontalDir = Vec3d.fromPolar(0, yaw); // Unit vector constrained to X/Z plane

												// 3. Calculate target position
												Vec3d targetVec = playerPos
														.add(horizontalDir.multiply(hDist)) // Forward/backward on X/Z plane
														.add(0, vDist, 0);                 // Up/down on Y axis

												BlockPos playerBlockPos = BlockPos.ofFloored(playerPos);
												BlockPos targetBlockPos = BlockPos.ofFloored(targetVec);

												String command = String.format("/fill %d %d %d %d %d %d %s replace %s", playerBlockPos.getX(), playerBlockPos.getY(), playerBlockPos.getZ(), targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ(), blockId, blockId2);
												//System.out.println(command);
												player.getServer().getCommandManager().executeWithPrefix(
														player.getCommandSource(), command
												);

												return 1;
											})
									)
								)
							)
					)
			);
		});




		// Initialize our tick-based scheduler
		//TaskScheduler.register();
		//CommandScheduler.register();
		//initialize time based scheduler
		RealTimeScheduler.register();
		double[] wethandsTimes = {0.01334, 0.443445, 0.839337, 1.246009, 1.671637, 2.064185, 2.461144, 2.902209, 3.303578, 3.700537, 4.104111, 4.527534, 4.928903, 5.31263, 6.548465, 6.926928, 7.330503, 7.778184, 8.159705, 8.559972, 8.971265, 9.363813, 9.787236, 10.188605, 10.606514, 11.015602, 11.412561, 11.822752, 13.020852, 13.422656, 13.831803, 14.228762, 14.663211, 15.033706, 15.435075, 15.873935, 16.270894, 16.663442, 17.078043, 17.514697, 17.885192, 18.306409, 18.703368, 19.091505, 19.506107, 19.916297, 20.346336, 20.752116, 21.149074, 21.537212, 21.938581, 22.33995, 22.789837, 23.171358, 23.596986, 23.993944, 24.423983, 24.785656, 25.200258, 25.628091, 26.014023, 26.452883, 26.821172, 27.244595, 27.641553, 28.038512, 28.435471, 28.858893, 29.24262, 29.670453, 30.074028, 30.468781, 30.896614, 32.497681, 32.940951, 33.329088, 33.726047, 34.125211, 34.524375, 34.936771, 35.37122, 35.779205, 36.180575, 36.581944, 36.99434, 37.413352, 38.174189, 39.003392, 39.424609, 39.823773, 40.233964, 40.639743, 41.087425, 41.473357, 41.876931, 42.295439, 42.664904, 43.112452, 43.516336, 43.92334, 44.331903, 44.684328, 45.12096, 47.138825, 47.552066, 47.965307, 48.352038, 48.748126, 49.583965, 49.98629, 50.421363, 50.811213, 51.213539, 51.639255, 52.054056, 52.858707, 53.24076, 53.65712, 54.085955, 54.472687, 54.885928, 55.29761, 55.71241, 56.889758, 59.353611, 59.740343, 60.13643, 60.571503, 61.017492, 61.426055, 61.842415, 62.210433, 62.626793, 63.021322, 63.422088, 63.836888, 64.195551, 64.654014, 65.025152, 65.404086, 65.885941, 66.662523, 67.06017, 67.470292, 67.863261, 68.301453, 68.691303, 69.085832, 69.525583, 69.898279, 70.303724, 70.734118, 71.117731, 71.527853, 71.934857, 72.34342, 72.741067, 73.160546, 73.558193, 73.958959, 74.367522, 74.847818, 76.405659, 76.789272, 77.222785, 77.634467, 78.028995};
		String[] wethandsCommands = {"a a 0 0 0 0 0", "c2 c2 0 0 0 0 0", "a a 0 0 0 0 0", "b b 0 0 0 0 0", "c2 c2 0 0 0 0 0", "b b 0 0 0 0 0", "a a 0 0 0 0 0", "e e 0 0 0 0 0", "d d 0 0 0 0 0", "f2 f2 0 0 0 0 0", "c2 c2 0 0 0 0 0", "e e 0 0 0 0 0", "c2 c2 0 0 0 0 0", "a a 0 0 0 0 0", "a a 0 0 0 0 0", "c2 c2 0 0 0 0 0", "a a 0 0 0 0 0", "b b 0 0 0 0 0", "c2 c2 0 0 0 0 0", "b b 0 0 0 0 0", "a a 0 0 0 0 0", "e e 0 0 0 0 0", "d d 0 0 0 0 0", "f2 f2 0 0 0 0 0", "c2 c2 0 0 0 0 0", "e e 0 0 0 0 0", "c2 c2 0 0 0 0 0", "a a 0 0 0 0 0", "a g2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "b 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "b 0 0 0 0 0 0", "a a 0 0 0 0 0", "e 0 0 0 0 0 0", "d f2 0 0 0 0 0", "f2 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 f 0 0 0 0 0", "a g2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "b 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "b 0 0 0 0 0 0", "a b 0 0 0 0 0", "e c2 0 0 0 0 0", "d 0 0 0 0 0 0", "f2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "g g 0 0 0 0 0", "b 0 0 0 0 0 0", "d 0 0 0 0 0 0", "f2 f2 0 0 0 0 0", "a d 0 0 0 0 0", "f2 0 0 0 0 0 0", "d a 0 0 0 0 0", "b b 0 0 0 0 0", "g 0 0 0 0 0 0", "b 0 0 0 0 0 0", "d 0 0 0 0 0 0", "f2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "g g 0 0 0 0 0", "b 0 0 0 0 0 0", "d f2 0 0 0 0 0", "f2 0 0 0 0 0 0", "a d 0 0 0 0 0", "f2 0 0 0 0 0 0", "d a 0 0 0 0 0", "b b 0 0 0 0 0", "g 0 0 0 0 0 0", "b 0 0 0 0 0 0", "d 0 0 0 0 0 0", "f2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "0 a 0 0 0 0 0", "a e 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "b 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "b 0 0 0 0 0 0", "a 0 0 0 0 0 0", "e 0 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "b 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "b f2 0 0 0 0 0", "d 0 0 0 0 0 0", "f2 0 0 0 0 0 0", "a c2 0 0 0 0 0", "c2 a 0 0 0 0 0", "0 e 0 0 0 0 0", "0 f2 0 0 0 0 0", "b 0 0 0 0 0 0", "d d 0 0 0 0 0", "f2 0 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "0 b 0 0 0 0 0", "0 c2 0 0 0 0 0", "g d 0 0 0 0 0", "b 0 0 0 0 0 0", "d c2 0 0 0 0 0", "f2 d 0 0 0 0 0", "a 0 0 0 0 0 0", "0 f2 0 0 0 0 0", "a e 0 0 0 0 0", "0 b 0 0 0 0 0", "0 a 0 0 0 0 0", "e 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "b 0 0 0 0 0 0", "e 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "b 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "b 0 0 0 0 0 0", "e 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "e 0 0 0 0 0 0", "a 0 0 0 0 0 0", "g g 0 0 0 0 0", "b f2 0 0 0 0 0", "d e 0 0 0 0 0", "f2 d 0 0 0 0 0", "a e 0 0 0 0 0", "f2 d 0 0 0 0 0", "d e 0 0 0 0 0", "b f2 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 e 0 0 0 0 0", "e 0 0 0 0 0 0", "a 0 0 0 0 0 0", "c2 a 0 0 0 0 0", "a 0 0 0 0 0 0", "e 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "e g2 0 0 0 0 0", "g2 e 0 0 0 0 0", "b b 0 0 0 0 0", "e g2 0 0 0 0 0", "g2 e 0 0 0 0 0", "e b 0 0 0 0 0", "g2 g2 0 0 0 0 0", "b e 0 0 0 0 0", "e b 0 0 0 0 0", "g2 0 0 0 0 0 0"};
		registerStartSongCommand("wethands", WET_HANDS_EVENT, wethandsTimes, wethandsCommands, "Playing Wet Hands by C418! (2 blocks)", .5);

		double[] swedenTimes = {0.056298, 1.437395, 2.810273, 4.19137, 5.527254, 6.902038, 8.241386, 11.017843, 12.337638, 13.706314, 15.065214, 16.429002, 17.797678, 19.176131, 21.89393, 23.25283, 24.626395, 25.999959, 27.349083, 28.722647, 30.086435, 32.799347, 34.168023, 34.842585, 35.526923, 36.890711, 37.570161, 37.91233, 38.259387, 39.623175, 40.297737, 40.649682, 40.977187, 43.709651, 44.398877, 45.078327, 45.752889, 46.427451, 47.805903, 48.495129, 48.812858, 49.174579, 50.538367, 51.208041, 51.55021, 51.902155, 54.624843, 55.988631, 56.663193, 57.347531, 58.716207, 59.390769, 59.708498, 60.079995, 61.434007, 62.128122, 62.450738, 62.817348, 65.540036, 66.9136, 67.578386, 68.257836, 69.611847, 70.325514, 70.653019, 70.980523, 72.334535, 73.033538, 73.375707, 73.712988, 76.440564, 77.770135, 78.498466, 79.177916, 80.536816, 81.259899, 81.562175, 81.893515, 83.230505, 83.919401, 84.286011, 84.62818, 87.34598, 88.719544, 89.398994, 90.068668, 91.442232, 92.199961, 92.513863, 92.816139, 94.169808, 94.829706, 95.196315, 95.533596, 98.256284, 100.314187, 100.636803, 101.003413, 102.357425, 103.031986, 103.711436, 105.764451, 106.096843, 106.434124, 109.176365, 111.224491, 111.551995, 111.894164, 113.262841, 113.937402, 114.631517, 115.975752, 116.664979, 116.997371, 117.349317};
		String[] swedenCommands = {"e e g 0 0 0 0", "f2 0 0 0 0 0 0", "g a c 0 0 0 0", "b 0 0 0 0 0 0", "a f2 a 0 0 0 0", "g 0 0 0 0 0 0", "d c2 e 0 0 0 0", "e g b 0 0 0 0", "f2 0 0 0 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "a f2 a c2 0 0 0", "g 0 0 0 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 0 0 0 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "a f2 a c 0 0 0", "g 0 0 0 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 a a a 0 0 0", "0 b b b 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "a f2 a c 0 0 0", "g 0 0 0 0 0 0", "0 f2 f2 f2 0 0 0", "0 a a a 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "0 d d d 0 0 0", "f2 b b b 0 0 0", "0 a a a 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "a f2 a c2 0 0 0", "g 0 0 0 0 0 0", "0 a a a 0 0 0", "0 f2 f2 f2 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 a a a 0 0 0", "0 b b b 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d f2 f2 0 0 0", "0 e e e 0 0 0", "a f2 a c2 0 0 0", "g 0 0 0 0 0 0", "0 f2 d d 0 0 0", "0 c2 c2 c2 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 b b b 0 0 0", "0 a a a 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "a f2 a c2 0 0 0", "g 0 0 0 0 0 0", "0 f2 f2 f2 0 0 0", "0 a a a 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 a a a 0 0 0", "0 b b b 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "a f2 a c2 0 0 0", "g 0 0 0 0 0 0", "0 f2 f2 f2 0 0 0", "0 a a a 0 0 0", "d a c2 e 0 0 0", "e e g b 0 0 0", "f2 a a a 0 0 0", "0 b b b 0 0 0", "g a d f2 0 0 0", "b 0 0 0 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "a f2 a c2 0 0 0", "g f2 f2 f2 0 0 0", "0 f2 f2 f2 0 0 0", "0 a a a 0 0 0", "d a c2 e 0 0 0", "b a d f2 0 0 0", "0 b b b 0 0 0", "0 a a a 0 0 0", "e b e g2 0 0 0", "0 e e e 0 0 0", "0 d d d 0 0 0", "a e a c2 0 0 0", "0 d d d 0 0 0", "0 e e e 0 0 0", "g f2 d b 0 0 0", "b d f2 a 0 0 0", "0 b b b 0 0 0", "0 a a a 0 0 0", "e b e g2 0 0 0", "0 e e e 0 0 0", "0 d e e 0 0 0", "a c2 e a 0 0 0", "0 d d d 0 0 0", "0 d d e 0 0 0", "0 e e e 0 0 0", "g b d b 0 0 0"};
		registerStartSongCommand("sweden", SWEDEN_EVENT, swedenTimes, swedenCommands, "Playing Sweden by C418! (4 blocks)", .1);

		double[] haggstromTimes = {0.092376, 0.612234, 1.153468, 1.696636, 2.233701, 2.758559, 3.295624, 3.829637, 4.366487, 4.913887, 5.439917, 5.981343, 6.504806, 7.585092, 8.129083, 8.660245, 9.195096, 9.734443, 10.267887, 10.794074, 11.332961, 11.877291, 12.412549, 12.938736, 13.477623, 14.011067, 15.085212, 15.618656, 16.159358, 16.685544, 17.22806, 17.774205, 18.313092, 18.857422, 19.392681, 19.927939, 20.461383, 21.00027, 21.535528, 22.606045, 23.139489, 23.703778, 24.219078, 24.747078, 25.295038, 25.844811, 26.370998, 26.897184, 27.443329, 27.974958, 28.506587, 29.045475, 30.11962, 30.653064, 31.184693, 31.723581, 32.258839, 32.790468, 33.351129, 33.866428, 34.398058, 34.93513, 35.468574, 36.029235, 36.549978, 37.61868, 38.152124, 38.689196, 39.226269, 39.75427, 40.294971, 40.911879, 41.367302, 41.909818, 42.437819, 42.980335, 43.517408, 44.083511, 45.117739, 45.656627, 46.193699, 46.727143, 47.26603, 47.803103, 48.338361, 48.866362, 49.405249, 49.931435, 50.481209, 51.012839, 51.548097, 52.61317, 53.154977, 53.670986, 54.226203, 54.763276, 55.300348, 55.830163, 56.359978, 56.90068, 57.434124, 57.978454, 58.50464, 59.027198, 61.179118, 61.723448, 62.275036, 62.797594, 63.331037, 63.871739, 64.397925, 64.942256, 65.482957, 66.030917, 66.557103, 67.083289, 67.609475, 68.694507, 69.238838, 69.786797, 70.298468, 70.846427, 71.372613, 71.906057, 72.44313, 72.994718, 73.524533, 74.054348, 74.59505, 75.135751, 76.188123, 76.721567, 77.276784, 77.806599, 78.343672, 78.891632, 79.417818, 79.947633, 80.488334, 81.021778, 81.558851, 82.077779, 82.614852, 83.696255, 84.236957, 84.777658, 85.321989, 85.837289, 86.36976, 86.903773, 87.453044, 87.987057, 88.536328, 89.055084, 89.598251, 90.120058, 93.328173, 93.875235, 94.401421, 94.945752, 95.482825, 96.019897, 96.55697, 97.086785, 97.620229, 98.153673, 98.694375, 99.231447, 99.775778, 100.842666, 101.372481, 101.916811, 102.439368, 102.972812, 103.513514, 104.050587, 104.591288, 105.124732, 105.650918, 106.177104, 106.725064, 107.269394};
		String[] haggstromCommands = {"g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "a a 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "a a 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c e 0 0 0 0 0", "0 a 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c c 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c e 0 0 0 0 0", "d a 0 0 0 0 0", "c e 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0", "c e 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0", "a b 0 0 0 0 0", "d a 0 0 0 0 0", "a e 0 0 0 0 0", "a b 0 0 0 0 0", "d a 0 0 0 0 0", "a g2 0 0 0 0 0", "f2 0 0 0 0 0 0", "a b 0 0 0 0 0", "d a 0 0 0 0 0", "a e 0 0 0 0 0", "a d 0 0 0 0 0", "0 c2 0 0 0 0 0", "0 a 0 0 0 0 0", "a b 0 0 0 0 0", "d a 0 0 0 0 0", "a c2 0 0 0 0 0", "a b 0 0 0 0 0", "d a 0 0 0 0 0", "a f2 0 0 0 0 0", "a f2 0 0 0 0 0", "a g2 0 0 0 0 0", "d a 0 0 0 0 0", "a e 0 0 0 0 0", "a g2 0 0 0 0 0", "d a 0 0 0 0 0", "a a 0 0 0 0 0", "f2 a 0 0 0 0 0", "b g2 0 0 0 0 0", "a e 0 0 0 0 0", "f2 a 0 0 0 0 0", "b g2 0 0 0 0 0", "a c2 0 0 0 0 0", "f2 0 0 0 0 0 0", "f2 a 0 0 0 0 0", "b g2 0 0 0 0 0", "a e 0 0 0 0 0", "f2 a 0 0 0 0 0", "b g2 0 0 0 0 0", "0 a 0 0 0 0 0", "f2 a 0 0 0 0 0", "b b 0 0 0 0 0", "a e 0 0 0 0 0", "f2 a 0 0 0 0 0", "b b 0 0 0 0 0", "a c2 0 0 0 0 0", "f2 0 0 0 0 0 0", "f2 g2 0 0 0 0 0", "b a 0 0 0 0 0", "a e 0 0 0 0 0", "f2 g2 0 0 0 0 0", "b a 0 0 0 0 0", "a e 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c e 0 0 0 0 0", "a a 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c f 0 0 0 0 0", "0 e 0 0 0 0 0", "0 c 0 0 0 0 0", "c g 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c d 0 0 0 0 0", "c a 0 0 0 0 0", "d g 0 0 0 0 0", "c e 0 0 0 0 0", "c g 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "0 d 0 0 0 0 0", "c a 0 0 0 0 0", "d g 0 0 0 0 0", "c e 0 0 0 0 0", "c g 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "c c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "c e 0 0 0 0 0", "d g 0 0 0 0 0", "c a 0 0 0 0 0", "e g 0 0 0 0 0", "c a 0 0 0 0 0", "d g 0 0 0 0 0", "c e 0 0 0 0 0", "c g 0 0 0 0 0", "d d 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "g c 0 0 0 0 0", "d b 0 0 0 0 0", "c e 0 0 0 0 0", "a 0 0 0 0 0 0", "g c 0 0 0 0 0", "d b 0 0 0 0 0", "c g 0 0 0 0 0", "g c 0 0 0 0 0", "d b 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0", "a a 0 0 0 0 0", "g c 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0", "g c 0 0 0 0 0", "d g 0 0 0 0 0", "c c 0 0 0 0 0"};
		registerStartSongCommand("haggstrom", HAGGSTROM_EVENT, haggstromTimes, haggstromCommands, "Playing Haggstrom by C418! (2 blocks)",-.1);

		double[] miceonvenusTimes = {21.497032, 24.127303, 26.549747, 29.033377, 31.455764, 33.819357, 36.206467, 38.581818, 40.921892, 43.188031, 45.441392, 47.678123, 48.796488, 49.885751, 51.544591, 52.080907, 54.271906, 54.345762, 55.92243, 56.475377, 58.134216, 58.678848, 58.755917, 60.865689, 63.065002, 64.166738, 65.231056, 67.426212, 68.515475, 69.621368, 70.723103, 71.804051, 71.860253, 73.965947, 76.161103, 78.356259, 80.518155, 81.640678, 82.709154, 84.879365, 85.985258, 87.062048, 88.155469, 89.265519, 91.431573, 93.610099, 94.68689, 95.767838, 96.844628, 97.655339, 97.925576, 99.027312, 99.555313, 100.09163, 102.278471, 104.321251, 106.604152, 108.775181, 109.837971, 110.914745, 112.596331, 113.075286, 114.974158, 116.852924, 117.452504, 119.034622, 119.509258, 121.710456, 123.850021, 124.930291, 126.007065, 128.150126, 129.476603, 130.604332, 132.177542, 134.00596, 137.820119};
		String[] miceonvenusCommands = {"c e g e 0 0 0", "c 0 0 0 0 0 0", "a g g a 0 0 0", "c e e c 0 0 0", "a c e c 0 0 0", "a4 c e c 0 0 0", "a c e g 0 0 0", "d f2 c c 0 0 0", "f a c c 0 0 0", "f a f a 0 0 0", "f a f e 0 0 0", "g g2 g2 g 0 0 0", "0 0 0 d 0 0 0", "c c e e 0 0 0", "c c e d 0 0 0", "c e g g 0 0 0", "f 0 0 0 0 0 0", "0 a c c 0 0 0", "f a c a 0 0 0", "c e g e 0 0 0", "c e g d 0 0 0", "a c 0 0 0 0 0", "0 0 e c 0 0 0", "f a4 c c 0 0 0", "c e g c 0 0 0", "c e g a 0 0 0", "f a c c 0 0 0", "f a c c 0 0 0", "f a c e 0 0 0", "f a c c 0 0 0", "f a c a 0 0 0", "g 0 0 0 0 0 0", "0 b d g 0 0 0", "g b g b 0 0 0", "a c e c 0 0 0", "f a4 c c 0 0 0", "c e g c 0 0 0", "c e g a 0 0 0", "f a c c 0 0 0", "d a d a 0 0 0", "d d c c 0 0 0", "g g b b 0 0 0", "g g g g 0 0 0", "d f d f 0 0 0", "f a f a 0 0 0", "c e g c 0 0 0", "c e g e 0 0 0", "f f c c 0 0 0", "f f f c 0 0 0", "f f f c 0 0 0", "a e g e 0 0 0", "a e g f 0 0 0", "a e g g 0 0 0", "f a c e 0 0 0", "c f g d 0 0 0", "a a e e 0 0 0", "c c g g 0 0 0", "g g d d 0 0 0", "e e e e 0 0 0", "c e g e 0 0 0", "c e g d 0 0 0", "c e g c 0 0 0", "f a c a 0 0 0", "f a c a 0 0 0", "c e g e 0 0 0", "c e g d 0 0 0", "a c e g 0 0 0", "d f a4 c 0 0 0", "g d f c 0 0 0", "g g a a 0 0 0", "f a c c 0 0 0", "d d a a 0 0 0", "d d c c 0 0 0", "g2 g2 e e 0 0 0", "g2 g2 g g 0 0 0", "c e g c 0 0 0", "c e g c 0 0 0"};
		registerStartSongCommand("miceonvenus", MICE_ON_VENUS_EVENT, miceonvenusTimes, miceonvenusCommands, "Playing Mice on Venus by C418! (4 blocks)", .36);

		double[] minecraftTimes = {0.0, 1.157395, 1.703943, 2.797039, 4.533132, 5.690528, 6.237075, 7.362321, 9.046105, 10.179499, 10.772108, 11.871303, 13.563107, 14.719651, 15.293143, 16.411455, 18.122375, 19.269361, 19.795062, 20.951606, 22.624294, 23.752163, 24.325656, 25.520433, 27.193121, 28.282757, 28.838437, 29.974561, 31.69504, 32.813351, 33.358169, 33.998569, 34.524271, 35.671257, 36.24475, 37.363061, 37.946112, 39.045307, 40.163618, 40.746669, 41.931888, 42.448031, 43.016531, 43.5759, 45.267704, 46.452923, 46.959508, 48.116052, 48.689545, 49.263038, 49.807856, 50.9644, 51.509219, 52.646646, 54.357567, 56.078045, 57.186798, 58.907277, 60.01603, 60.646872, 61.736508, 63.43787, 64.575298, 65.120116, 66.295777, 67.949348, 69.105892, 69.669826, 70.759463, 72.499058, 73.598253, 74.190862, 75.318731, 77.03921, 78.176637, 78.740572, 79.830209, 81.550687, 82.678557, 83.261608, 84.379919, 86.062164, 87.247383, 87.80176, 88.891396, 90.611875, 91.749302, 92.332353, 93.441106, 95.152027, 96.251221, 96.824714, 97.952583, 99.673062, 100.781815, 101.489123, 102.511852, 104.203656, 105.341083, 105.885902, 107.042446, 108.743808, 109.833444, 110.44517, 111.573039, 113.264843, 114.383154, 114.956647, 116.132308, 117.814553, 118.94936, 119.61509, 120.696478, 122.370457, 123.467571, 124.098512, 125.207683, 126.875458, 127.976591, 128.587438, 129.72474, 131.392515, 132.501686, 133.092439, 134.237779, 135.925647, 137.050893, 137.673796, 138.799042, 140.454761, 141.559912, 142.186835, 143.388437, 145.003968, 146.085007, 146.752117, 147.801007, 149.5371, 150.626177, 151.200856, 152.370307, 154.038082, 155.147253, 155.721932, 156.895402, 158.563177, 159.688422, 160.259083, 161.396384, 163.104347, 164.221555, 164.812309, 165.937554, 167.605329, 168.718518, 169.313291, 170.438536, 172.166592, 173.2838, 173.838386, 174.995781, 176.675612, 177.804876, 178.435818, 179.516857, 181.2208, 182.350065, 182.940819, 184.04597, 185.733839, 186.863103, 187.465913, 188.611253, 190.275009, 191.428385, 191.970914, 193.168497, 194.796084, 195.929368, 196.520121, 197.649386, 199.357348, 200.4625, 201.053254, 202.142331, 203.862349, 205.011707, 205.590405, 206.707613, 208.383425, 209.536802, 210.071293, 211.216632, 212.944688, 214.069934, 214.6205, 215.753783, 217.469783, 218.595028, 219.16167, 220.274859, 221.970765, 223.124142, 223.678727, 224.832104, 226.503897, 227.613068, 228.191766, 229.317011, 231.012917, 232.122088, 232.78116, 233.858181, 235.562124, 236.703445, 237.282143, 238.431501, 240.095256, 241.19639, 241.823312, 242.97267, 244.652501, 245.801859, 246.332332, 247.48169, 249.169558, 250.391254, 250.977989, 252.097911, 253.818389, 254.9367, 255.452844, 256.618946};
		String[] minecraftCommands = {"a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 0 0 0 0 0 0", "c2 0 0 0 0 0 0", "0 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "c2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "g2 c2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "a2 f2 0 0 0 0 0", "0 a2 0 0 0 0 0", "c2 c2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "c2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 a2 0 0 0 0 0", "a2 0 0 0 0 0 0", "g2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "a2 f2 0 0 0 0 0", "f2 c2 0 0 0 0 0", "c2 c2 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "g2 c2 0 0 0 0 0", "a2 0 0 0 0 0 0", "f2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "f2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "c2 0 0 0 0 0 0", "a2 c2 0 0 0 0 0", "g2 0 0 0 0 0 0", "d2 f2 0 0 0 0 0", "f2 a2 0 0 0 0 0"};
		registerStartSongCommand("minecraft", MINECRAFT_EVENT, minecraftTimes, minecraftCommands, "Playing Minecraft by C418! (2 blocks)", 0);

		double[] bachpreludecmajorTimes = {0.047289, 0.253005, 0.470209, 0.676381, 0.851707, 1.681883, 1.875069, 2.072431, 2.263877, 2.498136, 3.298029, 3.5121, 3.705981, 3.905084, 4.1383, 4.915219, 5.124765, 5.352759, 5.553602, 5.758274, 6.524751, 6.765624, 6.947671, 7.166963, 7.371636, 8.207729, 8.391865, 8.597233, 8.800861, 8.995091, 9.802642, 10.021238, 10.202937, 10.4365, 10.631774, 11.498499, 11.672192, 11.894964, 12.090239, 12.288645, 13.146668, 13.35517, 13.575506, 13.773564, 13.980673, 14.78892, 14.988023, 15.195132, 15.39702, 15.600996, 16.395668, 16.60765, 16.824853, 17.031962, 17.229325, 18.080386, 18.277053, 18.470238, 18.68779, 18.879583, 19.718461, 19.891806, 20.11075, 20.3377, 20.54272, 21.37951, 21.572695, 21.806955, 22.037037, 22.239273, 23.04926, 23.266812, 23.480534, 23.698434, 23.908675, 24.727017, 24.912196, 25.130444, 25.334072, 25.535264, 26.338986, 26.529735, 26.75738, 26.948826, 27.123215, 27.963137, 28.151102, 28.370394, 28.557314, 28.729963, 29.557006, 29.759938, 29.963566, 30.159536, 30.376044, 31.228149, 31.430037, 31.635405, 31.829287, 32.053104, 32.933752, 33.146778, 33.361893, 33.558212, 33.768453, 34.612553, 34.809567, 35.005189, 35.186888, 35.420452, 36.22487, 36.425365, 36.672852, 36.844456, 37.021978, 37.886266, 38.079452, 38.299788, 38.495758, 38.726189, 39.577946, 39.805592, 39.999822, 40.188482, 40.445367, 41.287726, 41.482652, 41.664351, 41.870416, 42.082746, 42.89691, 43.085919, 43.321223, 43.509187, 43.716992, 44.603725, 44.803718, 45.007082, 45.204971, 45.405814, 46.289426, 46.493222, 46.712772, 46.890971, 47.094275, 47.953766, 48.177253, 48.376127, 48.585831, 48.801934, 49.627459, 49.834701, 50.049328, 50.287583, 50.481042, 51.302136, 51.526608, 51.732866, 51.948477, 52.145874, 52.961554, 53.180118, 53.410005, 53.607402, 53.799877, 54.647061, 54.867102, 55.065976, 55.238761, 55.466678, 56.320262, 56.570823, 56.769205, 56.990231, 57.208796, 58.133757, 58.312449, 58.545781, 58.739732, 58.964697, 59.836001, 60.032414, 60.218489, 60.446899, 60.691062, 61.533323, 61.750903, 61.972914, 62.170803, 62.398721, 63.213908, 63.429519, 63.668758, 63.953286, 64.127547, 64.967839, 65.159822, 65.396108, 65.703772, 65.880494, 66.723248, 66.966426, 67.205173, 67.447366, 67.643779, 68.47718, 68.706082, 68.882312, 69.176685, 69.340609, 70.223235, 70.416202, 70.635259, 70.855301, 71.088141, 71.924495, 72.131738, 72.369008, 72.581666, 72.77414, 73.641999, 73.829059, 74.031871, 74.26865, 74.462109, 75.292556, 75.483554, 75.677998, 75.888194, 76.081653, 77.010553, 77.317232, 77.54072, 77.730733, 77.905978, 78.745779, 78.971727, 79.200137, 79.379321, 79.610684, 80.464267, 80.657234, 80.902873, 81.114054, 81.304068, 82.132546, 82.32502, 82.535216, 82.737536, 82.959055, 83.797378, 83.9805, 84.196111, 84.429935, 84.697234, 85.556232, 85.720648, 85.932813, 86.150886, 86.427537, 87.289489, 87.485902, 87.733018, 87.939276, 88.183438, 89.04539, 89.218666, 89.435754, 89.621337, 89.834487, 90.706284, 90.889406, 91.12963, 91.334411, 91.552483, 92.386376, 92.604941, 92.821044, 93.03124, 93.230115, 94.119141, 94.328845, 94.576453, 94.766466, 94.96091, 95.79628, 95.998107, 96.207319, 96.391918, 96.596699, 97.43896, 97.609283, 97.879535, 98.062165, 98.299435, 99.19683, 99.360261, 99.592117, 99.79936, 100.042537, 100.933533, 101.146682, 101.393798, 101.568059, 101.798438, 102.635285, 102.844496, 103.07586, 103.280149, 103.51545, 104.386755, 104.621072, 104.820931, 105.028173, 105.263475, 106.138718, 106.332669, 106.540404, 106.745185, 106.958335, 107.808965, 107.997009, 108.226896, 108.437092, 108.658118, 108.877667, 109.123799, 109.306428, 109.536807, 109.731251, 109.92471, 110.109801, 110.330827, 110.511979, 110.731036, 110.949601, 111.187856, 111.487644, 111.78054, 112.007473, 112.164013, 112.397345, 112.612464, 112.83792, 113.0619, 113.31443, 113.569915, 113.833275, 114.146847, 114.525889, 114.943327, 115.478909, 116.501831};
		String[] bachpreludecmajorCommands = {"c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 f 0 0", "c 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 f 0 0", "b 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 f 0 0", "b 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 f 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 e 0 0 0", "0 0 0 0 a 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 e 0 0 0", "0 0 0 0 a 0 0", "c 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 f2 0 0 0 0", "0 0 0 a 0 0 0", "0 0 0 0 d 0 0", "c 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 f2 0 0 0 0", "0 0 0 a 0 0 0", "0 0 0 0 d 0 0", "b 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 g 0 0", "b 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 d 0 0 0", "0 0 0 0 g 0 0", "b 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "b 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "a 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "a 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "d 0 0 0 0 0 0", "0 a 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 f2 0 0 0", "0 0 0 0 c 0 0", "d 0 0 0 0 0 0", "0 a 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 f2 0 0 0", "0 0 0 0 c 0 0", "g 0 0 0 0 0 0", "0 b 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 b 0 0", "g 0 0 0 0 0 0", "0 b 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 b 0 0", "g 0 0 0 0 0 0", "0 b4 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c2 0 0", "g 0 0 0 0 0 0", "0 b4 0 0 0 0 0", "0 0 e 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c2 0 0", "f 0 0 0 0 0 0", "0 a 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 a 0 0 0", "0 0 0 0 d 0 0", "f 0 0 0 0 0 0", "0 a 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 a 0 0 0", "0 0 0 0 d 0 0", "f 0 0 0 0 0 0", "0 a4 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 f 0 0 0", "0 0 0 0 b 0 0", "f 0 0 0 0 0 0", "0 a4 0 0 0 0 0", "0 0 d 0 0 0 0", "0 0 0 f 0 0 0", "0 0 0 0 b 0 0", "e 0 0 0 0 0 0", "0 g 0 0 0 0 0", "0 0 c 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "e 0 0 0 0 0 0", "0 g 0 0 0 0 0", "0 0 c 0 0 0 0", "0 0 0 g 0 0 0", "0 0 0 0 c 0 0", "e 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "e 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "d 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "d 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 f 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 g 0 0 0 0 0", "0 0 b4 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 g 0 0 0 0 0", "0 0 b4 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "f 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "f 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "f2 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e4 0 0", "f2 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e4 0 0", "g 0 0 0 0 0 0", "0 e4 0 0 0 0 0", "0 0 b 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e4 0 0", "g 0 0 0 0 0 0", "0 e4 0 0 0 0 0", "0 0 b 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e4 0 0", "a4 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 b 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 d 0 0", "a4 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 b 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 d 0 0", "g 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 d 0 0", "g 0 0 0 0 0 0", "0 f 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 d 0 0", "g 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "g 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 e 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 e4 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f2 0 0", "g 0 0 0 0 0 0", "0 e4 0 0 0 0 0", "0 0 a 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f2 0 0", "g 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 g 0 0", "g 0 0 0 0 0 0", "0 e 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 g 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "g 0 0 0 0 0 0", "0 d 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 c 0 0 0", "0 0 0 0 f 0 0", "c 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b4 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 g 0 0 0 0", "0 0 0 b4 0 0 0", "0 0 0 0 e 0 0", "c 0 0 0 0 0 0", "0 c 0 0 0 0 0", "0 0 f f f 0 0", "0 0 a a a 0 0", "0 0 c c c 0 0", "0 0 f f f 0 0", "0 0 c c c 0 0", "0 0 a a a 0 0", "c 0 c c c 0 0", "0 a a a a 0 0", "0 0 f f f 0 0", "0 0 a a a 0 0", "0 0 f f f 0 0", "0 0 d d d 0 0", "0 0 f f f 0 0", "0 0 d d d 0 0", "c 0 0 0 0 0 0", "0 b 0 0 0 0 0", "0 0 g g g 0 0", "0 0 b b b 0 0", "0 0 d d d 0 0", "0 0 f f f 0 0", "0 0 d d d 0 0", "0 0 b b b 0 0", "0 0 d d d 0 0", "0 0 b b b 0 0", "0 0 g g g 0 0", "0 0 b b b 0 0", "0 0 d d d 0 0", "0 0 f f f 0 0", "0 0 e e e 0 0", "0 0 d d d 0 0", "c c c c c 0 0"};
		registerStartSongCommand("bachpreludecmajor", BACH_PRELUDE_C_MAJOR_EVENT, bachpreludecmajorTimes, bachpreludecmajorCommands, "Playing Bach Prelude in C Major for 5 blocks(Andreas Pfaul)!", 0);

		double[] satiegymnopedieTimes = {0.024501, 0.973057, 2.667155, 3.622711, 5.355311, 6.296866, 7.987464, 8.985022, 10.644119, 11.491168, 12.271713, 13.048759, 13.867806, 14.567847, 15.31339, 16.107937, 16.884982, 17.70753, 18.684086, 20.392186, 21.309239, 22.852829, 23.787383, 25.537485, 26.426536, 28.239642, 29.142694, 31.253317, 32.096866, 32.85641, 33.626455, 34.329996, 34.946032, 35.572568, 36.272609, 36.962149, 37.802198, 38.743753, 40.276842, 41.165893, 42.810989, 43.777045, 45.226129, 46.136182, 47.557265, 48.568824, 50.101913, 51.204477, 52.650061, 53.451608, 54.155149, 54.86569, 55.56223, 56.227269, 56.969312, 57.756858, 58.460399, 59.261945, 60.354009, 61.820594, 62.730647, 63.479691, 64.179731, 64.879772, 65.53081, 66.230851, 67.070899, 67.76394, 68.442979, 69.188523, 69.951567, 70.837118, 71.936182, 73.528775, 74.449328, 75.310379, 76.377941, 77.305495, 78.77208, 79.710134, 81.299227, 82.104274, 82.741311, 83.476353, 84.200895, 84.862434, 85.600977, 86.430525, 87.141066, 88.06862, 88.940171, 89.937729, 91.253805, 94.123972, 96.847131, 97.827547, 99.599659, 100.540166, 102.119227, 103.064684, 104.891247, 105.807004, 107.391015, 108.207771, 108.885926, 109.50963, 110.237285, 110.96989, 111.583695, 112.32125, 112.959805, 113.895361, 115.033869, 116.840632, 117.795989, 119.409701, 120.310607, 122.07777, 123.018276, 124.849789, 125.770496, 127.75051, 128.161363, 128.884068, 129.547373, 130.275028, 130.893782, 131.537287, 132.210492, 132.908447, 133.749953, 134.631059, 136.259621, 137.140727, 138.596037, 139.571194, 140.986904, 141.967011, 143.392621, 144.328178, 145.98149, 147.065548, 148.723809, 149.515815, 150.215467, 150.931411, 151.655681, 152.342488, 153.003959, 153.79899, 154.544071, 155.414027, 156.471293, 158.011406, 158.885524, 159.830404, 160.621273, 161.362192, 161.986562, 162.644232, 163.435101, 164.063633, 164.737953, 165.532984, 166.223953, 167.164671, 168.321837, 169.995148, 170.956678, 171.847446, 172.892225, 173.870405, 175.164932, 176.097325, 177.616625, 178.324245, 179.044352, 179.793596, 180.513702, 181.192184, 181.924779, 182.786409, 183.552303, 184.717794, 185.662674, 186.811515, 188.131017, 191.760688};
		String[] satiegymnopedieCommands = {"g 0 0 0 g 0 0", "0 b d f2 0 0 0", "d 0 0 0 d 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 g 0 0", "0 b d f2 0 0 0", "d 0 0 0 d 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 0 0 0", "0 b d f2 f2 0 0", "0 0 0 0 a 0 0", "d 0 0 0 g 0 0", "0 a c2 f2 f2 0 0", "0 0 0 0 c2 0 0", "g 0 0 0 b 0 0", "0 b d f2 c2 0 0", "0 0 0 0 d 0 0", "d 0 0 0 a 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 f2 0 0", "0 b d f2 0 0 0", "d 0 0 0 0 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 0 0 0", "0 b d f2 0 0 0", "d 0 0 0 0 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 0 0 0", "0 b d f2 f2 0 0", "0 0 0 0 a 0 0", "d 0 0 0 g 0 0", "0 a c2 f2 f2 0 0", "0 0 0 0 c2 0 0", "g 0 0 0 b 0 0", "0 b d f2 c2 0 0", "0 0 0 0 d 0 0", "d 0 0 0 a 0 0", "0 a c2 f2 0 0 0", "f2 0 0 0 c2 0 0", "0 a c2 f2 0 0 0", "b 0 0 0 f2 0 0", "0 b d f2 0 0 0", "e 0 0 0 e 0 0", "0 e g b 0 0 0", "e 0 0 0 e 0 0", "0 b d g 0 0 0", "d 0 0 0 0 0 0", "0 f a d 0 0 0", "a 0 0 0 a 0 0", "0 a c e b 0 0", "0 0 0 0 c 0 0", "d 0 0 0 e 0 0", "0 g b e d 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 g b e c 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 c e a 0 0 0", "d 0 0 0 0 0 0", "0 c f2 a 0 0 0", "0 0 0 0 d 0 0", "d 0 0 0 e 0 0", "0 a c f f 0 0", "0 0 0 0 g 0 0", "d 0 0 0 a 0 0", "0 a c e c 0 0", "0 0 0 0 d 0 0", "d 0 0 0 e 0 0", "0 d g b d 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 c e a 0 0 0", "d 0 0 0 0 0 0", "0 c f2 a 0 0 0", "0 0 0 0 d 0 0", "e 0 0 0 g 0 0", "0 b e g 0 0 0", "f2 0 0 0 f2 0 0", "0 a c2 f2 0 0 0", "b 0 0 0 b 0 0", "0 b d f2 a 0 0", "0 0 0 0 b 0 0", "e 0 0 0 c2 0 0", "0 c2 e a d 0 0", "0 0 0 0 e 0 0", "e 0 0 0 c2 0 0", "0 a c2 f2 d 0 0", "0 0 0 0 e 0 0", "e 0 0 0 f2 0 0", "0 b a d 0 0 0", "0 b d g 0 0 0", "a a c e g 0 0", "d d f2 a d 0 0", "g 0 0 0 g 0 0", "0 b d f2 0 0 0", "d 0 0 0 d 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 g 0 0", "0 b d f2 0 0 0", "d 0 0 0 d 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 0 0 0", "0 b d f2 f2 0 0", "0 0 0 0 a 0 0", "d 0 0 0 g 0 0", "0 a c2 f2 f2 0 0", "0 0 0 0 c2 0 0", "g 0 0 0 b 0 0", "0 b d f2 c2 0 0", "0 0 0 0 d 0 0", "d 0 0 0 a 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 f2 0 0", "0 b d f2 0 0 0", "d 0 0 0 f2 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 f2 0 0", "0 b d f2 0 0 0", "d 0 0 0 f2 0 0", "0 a c2 f2 0 0 0", "g 0 0 0 0 0 0", "0 b d f2 f2 0 0", "0 0 0 0 a 0 0", "d 0 0 0 g 0 0", "0 a c2 f2 f2 0 0", "0 0 0 0 c2 0 0", "g 0 0 0 b 0 0", "0 b d f2 c2 0 0", "0 0 0 0 d 0 0", "d 0 0 0 a 0 0", "0 a c2 f2 0 0 0", "f2 0 0 0 c2 0 0", "0 a c2 f2 0 0 0", "b 0 0 0 f2 0 0", "0 b d f2 0 0 0", "e 0 0 0 e 0 0", "0 e g b 0 0 0", "e 0 0 0 0 0 0", "0 b d g 0 0 0", "d 0 0 0 0 0 0", "0 f a d 0 0 0", "a 0 0 0 a 0 0", "0 a c e b 0 0", "0 0 0 0 c 0 0", "d 0 0 0 e 0 0", "0 g b d d 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 g b e c 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 c e a 0 0 0", "d 0 0 0 0 0 0", "0 c f2 a 0 0 0", "0 0 0 0 d 0 0", "d 0 0 0 e 0 0", "0 a c f f 0 0", "0 0 0 0 g 0 0", "d 0 0 0 a 0 0", "0 a c e c 0 0", "0 0 0 0 d 0 0", "d 0 0 0 e 0 0", "0 g b e d 0 0", "0 0 0 0 b 0 0", "d 0 0 0 d 0 0", "0 c e a 0 0 0", "d 0 0 0 0 0 0", "0 c f2 a 0 0 0", "0 0 0 0 d 0 0", "e 0 0 0 g 0 0", "0 b e g 0 0 0", "e 0 0 0 f 0 0", "0 d f a 0 0 0", "e 0 0 0 b 0 0", "0 a c f c 0 0", "0 0 0 0 f 0 0", "e 0 0 0 e 0 0", "0 c e a d 0 0", "0 0 0 0 c 0 0", "e 0 0 0 e 0 0", "0 a c f d 0 0", "0 0 0 0 c 0 0", "e 0 0 0 f 0 0", "0 b a d 0 0 0", "0 b d g 0 0 0", "a a c e g 0 0", "d d f a d 0 0"};
		registerStartSongCommand("satiegymnopedie", SATIE_GYMNOPEDIE_EVENT, satiegymnopedieTimes, satiegymnopedieCommands, "Playing Satie Gymnopedie for 5 blocks(Andreas Pfaul)!", 0);


		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("setdefaulttypes")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) return 0;
								context.getSource().sendFeedback(() -> Text.literal("✦ Starting sequence..."), false);
								String command = "/setletters " + "a b c d e f g";
								runSetLettersCommand(player, command);
								return 1;
							})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("stopmusic")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						RealTimeScheduler.clearTasks();
						return 1;
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("clearblocks")
					.executes(context -> {
						for (int i = 0; i < cheatMusicBlockCoordinates.length; i++){
							cheatMusicBlockCoordinates[i].clear();
						}
						//int numBlocks = cheatMusicBlockCoordinates.size();
						//cheatMusicBlockCoordinates.clear();
						context.getSource().sendFeedback(() -> Text.literal(
								"Cheat Music Blocks cleared!"
						), false);
						return 1;
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("shifttiming")
					.then(CommandManager.argument("milliseconds", IntegerArgumentType.integer(-1000, 1000))
									.executes(context -> {
										ServerPlayerEntity player = context.getSource().getPlayer();
										if (player == null) return 0; // Ensures command is run by a player
										float delay = (float)IntegerArgumentType.getInteger(context, "milliseconds") / 1000;
										RealTimeScheduler.shiftAllTasks(delay);
										context.getSource().sendFeedback(() -> Text.literal(
												"Timing shifted by " + delay + " seconds"
										), false);

										return 1;
									})
							)
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("toggleaccidentals")
							.executes(context -> {
								showAccidentals = !showAccidentals;
								if (showAccidentals == true){
									context.getSource().sendFeedback(() -> Text.literal(
											"Showing accidentals"
									), false);
								}
								else{
									context.getSource().sendFeedback(() -> Text.literal(
											"Not showing accidentals"
									), false);
								}

								return 1;
							})
			);
		});
		//TODO these songs not implemented
		/*
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startsubwooferlullaby")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(SUBWOOFER_LULLABY_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Subwoofer Lullaby by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startdryhands")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(DRY_HANDS_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Dry Hands by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startlivingmice")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(LIVING_MICE_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Living Mice by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startdanny")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(DANNY_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Danny by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startclark")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(CLARK_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Clark by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("startmoogcity2")
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						// Play the sound specifically to the executing player
						player.playSound(MOOG_CITY_2_EVENT, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						context.getSource().sendFeedback(() -> Text.literal("Playing Moog City 2 by C418!"), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});
		 */



		LOGGER.info("CheatMusic Block Mod loaded!");
	}


	public int setCheatMusicBlocks(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, int[] type){
		ServerWorld world = context.getSource().getWorld();

		int changedCount = 0;

		// Iterate through cheatMusicBlockCoordinates
		for (int i = 0; i < type.length; i++){
			if (type[i] != -1){
				for (BlockPos pos : cheatMusicBlockCoordinates[i]) {
					BlockState state = world.getBlockState(pos);
					//iterate through all the block types
					for (Map.Entry<String, Block[]> entry : CHEAT_MUSIC_BLOCKS.entrySet()) {
						Block[] blocks = entry.getValue();
						if (state.isOf(blocks[i])) {
							((CheatMusicBlock) blocks[i]).setType(state, world, pos, type[i]);
							changedCount++;
						}
					}

				}

			}
		}
		return changedCount;

	}
	public int getCheatMusicBlocksInRadius(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, int radius){
		ServerWorld world = context.getSource().getWorld();
		BlockPos center = player.getBlockPos();
		int changedCount = 0;

		for (BlockPos pos : BlockPos.iterate(
				center.add(-radius, -radius, -radius),
				center.add(radius, radius, radius))) {

			BlockState state = world.getBlockState(pos);
			//If it is a cheat music block
			for (Map.Entry<String, Block[]> entry : CHEAT_MUSIC_BLOCKS.entrySet()) {
				Block[] blocks = entry.getValue();
				for (int i = 0; i < blocks.length; i++) {
					if (state.isOf(blocks[i])) {
						cheatMusicBlockCoordinates[i].add(pos.toImmutable());
						changedCount++;
						break;
					}
				}
			}





		}

		return changedCount;
	}

	public void runSetLettersCommand(ServerPlayerEntity player, String command){
		// Command shouldn't include the leading slash '/'
		//String formattedCommand = command.startsWith("/") ? command.substring(1) : command;

		// Get the command manager from the server instance
		player.getServer().getCommandManager().executeWithPrefix(
				player.getCommandSource(),
				command
		);
	}

	public void scheduleCheatMusicBlocks(MinecraftServer server, ServerPlayerEntity player, double[] times, String[] commands, double delay){
		int currentDelay = 0;
		for(int i = 0; i < times.length; i++){
			currentDelay += times[i];
			String command = "setletters " + commands[i];
			RealTimeScheduler.scheduleAction((float)(times[i] + delay), () -> {
						runSetLettersCommand(player, command);
					});

		}
	}

	public void registerStartSongCommand(String name, SoundEvent song, double[] times, String[] commands, String message, double delay){
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("start" + name)
					.executes(context -> {
						// Brigadier automatically throws a clean error to the console if a non-player executes this
						ServerPlayerEntity player = context.getSource().getPlayer();
						MinecraftServer server = context.getSource().getServer();
						player.networkHandler.sendPacket(
								new net.minecraft.network.packet.s2c.play.StopSoundS2CPacket(
										null, // Passing null as the Identifier tells it to stop ALL sounds in this category
										SoundCategory.MUSIC
								)
						);
						RealTimeScheduler.clearTasks();
						// Play the sound specifically to the executing player
						player.playSound(song, SoundCategory.MUSIC, 1.0f, 1.0f);
						// Send chat feedback
						scheduleCheatMusicBlocks(server, player, times, commands, delay);
						context.getSource().sendFeedback(() -> Text.literal(message), false);
						return 1; // Return 1 to indicate the command executed successfully
					})
			);
		});
	}


}
