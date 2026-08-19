package com.example.client;

import com.example.CheatMusicBlock;
import com.example.CheatMusicBlockMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

public class CheatMusicBlockModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		//make the leaves transparent
		Block[] leafs = CheatMusicBlockMod.CHEAT_MUSIC_BLOCKS.get("_leaf");
		for(int i = 0; i < leafs.length; i++){
			BlockRenderLayerMap.INSTANCE.putBlock(leafs[i], RenderLayer.getCutoutMipped());
		}
		//for(int i = 0; i < CheatMusicBlockMod.CHEAT_MUSIC_BLOCK_LEAFS.length; i++){
		//	BlockRenderLayerMap.INSTANCE.putBlock(CheatMusicBlockMod.CHEAT_MUSIC_BLOCK_LEAFS[i], RenderLayer.getCutoutMipped());
		//}

	}
}