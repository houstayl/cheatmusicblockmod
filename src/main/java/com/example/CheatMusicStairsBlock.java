package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class CheatMusicStairsBlock extends StairsBlock {

    public static final IntProperty TYPE = IntProperty.of("type", 0, 34);

    private static final String[] TYPE_TO_LETTER = {
            "a", "b", "c", "d", "e", "f", "g",
            "a_double_flat", "b_double_flat", "c_double_flat", "d_double_flat", "e_double_flat", "f_double_flat", "g_double_flat",
            "a_flat", "b_flat", "c_flat", "d_flat", "e_flat", "f_flat", "g_flat",
            "a_sharp", "b_sharp", "c_sharp", "d_sharp", "e_sharp", "f_sharp", "g_sharp",
            "a_double_sharp", "b_double_sharp", "c_double_sharp", "d_double_sharp", "e_double_sharp", "f_double_sharp", "g_double_sharp"
    };



    public CheatMusicStairsBlock(Block baseBlock, Settings settings) {
        super(baseBlock.getDefaultState(), settings.luminance(state -> 15));
        setDefaultState(getStateManager().getDefaultState().with(TYPE, 0));
    }

    public CheatMusicStairsBlock(Block baseBlock, Settings settings, int type) {
        super(baseBlock.getDefaultState(), settings.luminance(state -> 15));
        setDefaultState(getStateManager().getDefaultState().with(TYPE, type));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder); // MUST be included to register FACING, HALF, SHAPE, WATERLOGGED
        builder.add(TYPE);               // Appends your custom property
    }

    /*
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            boolean hasPickaxe = player.getMainHandStack().isIn(
                    net.minecraft.registry.tag.ItemTags.PICKAXES
            );
            if (hasPickaxe) {
                Block.dropStack(world, pos, new ItemStack(CheatMusicBlock.CHEAT_MUSIC_BLOCK));
            } else {
                Block.dropStack(world, pos, new ItemStack(CheatMusicBlock.COLOR_SHARD, 9));
            }
        }
        super.onBreak(world, pos, state, player);
    }*/

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, net.minecraft.world.BlockView world, BlockPos pos) {
        boolean hasPickaxe = player.getMainHandStack().isIn(
                net.minecraft.registry.tag.ItemTags.PICKAXES
        );
        if (hasPickaxe) {
            return super.calcBlockBreakingDelta(state, player, world, pos) * 5.0f;
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }
    /*
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos,
                               Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            boolean powered = world.isReceivingRedstonePower(pos);
            if (powered) {
                nextType(state, world, pos, null);
            }
        }
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

     */
    /*
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            nextType(state, world, pos, player);
        }
        return ActionResult.SUCCESS;
    }*/

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        CheatMusicBlockMod.cheatMusicBlockCoordinates[state.get(CheatMusicStairsBlock.TYPE)].add(pos.toImmutable());
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        for(int i = 0; i < CheatMusicBlockMod.cheatMusicBlockCoordinates.length; i++){
            boolean removed = CheatMusicBlockMod.cheatMusicBlockCoordinates[i].remove(pos);
            //if (removed){
            //    System.out.println(CheatMusicBlockMod.cheatMusicBlockCoordinates[i].size());
            //}
        }

    }


    private void nextType(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        int newType = (state.get(CheatMusicStairsBlock.TYPE) + 1) % 35;
        world.setBlockState(pos, state.with(TYPE, newType));
        world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
                SoundCategory.BLOCKS, 1.0f, 0.5f + (newType * 0.05f));
        if (player != null) {
            player.sendMessage(
                    Text.literal("✦ Block changed to " + TYPE_TO_LETTER[newType] + "!"),
                    true
            );
        }
    }

    public void setType(BlockState state, World world, BlockPos pos, int type) {
        world.setBlockState(pos, state.with(TYPE, type));
    }
}