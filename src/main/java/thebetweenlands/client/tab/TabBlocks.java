package thebetweenlands.client.tab;

import java.util.Comparator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thebetweenlands.common.registries.BlockRegistry;

public class TabBlocks extends CreativeTabBetweenlands {
	Comparator<ItemStack> sortedBlocks;

	public TabBlocks() {
		super("thebetweenlands.block");
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(BlockRegistry.SWAMP_GRASS);
	}
}
