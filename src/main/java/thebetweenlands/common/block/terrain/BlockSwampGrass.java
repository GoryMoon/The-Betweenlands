package thebetweenlands.common.block.terrain;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.tab.BLCreativeTabs;
import thebetweenlands.common.block.BasicBlock;
import thebetweenlands.common.block.ITintedBlock;
import thebetweenlands.common.block.farming.BlockGenericDugSoil;
import thebetweenlands.common.registries.BlockRegistry;
import thebetweenlands.common.tile.TileEntityDugSoil;
import thebetweenlands.common.world.gen.biome.decorator.DecorationHelper;
import thebetweenlands.common.world.gen.biome.decorator.DecoratorPositionProvider;

public class BlockSwampGrass extends BasicBlock implements IGrowable, ITintedBlock {
	public BlockSwampGrass() {
		super(Material.GRASS);
		this.setTickRandomly(true);
		this.setSoundType(SoundType.PLANT);
		this.setHardness(0.5F);
		this.setHarvestLevel("shovel", 0);
		this.setCreativeTab(BLCreativeTabs.BLOCKS);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			updateGrass(worldIn, pos, rand);
		}
	}

	public static boolean updateGrass(World world, BlockPos pos, Random rand) {
		if(world.getBlockState(pos.up()).getLightOpacity(world, pos.up()) > 2) {
			revertToDirt(world, pos);
			return true;
		} else {
			for (int i = 0; i < 4; ++i) {
				BlockPos blockPos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);

				if (blockPos.getY() >= 0 && blockPos.getY() < 256 && !world.isBlockLoaded(blockPos)) {
					return false;
				}

				IBlockState blockStateAbove = world.getBlockState(blockPos.up());

				if(blockStateAbove.getLightOpacity(world, pos.up()) <= 2) {
					spreadGrassTo(world, blockPos);
					return true;
				}
			}
		}
		return false;
	}

	public static void revertToDirt(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		if(state.getBlock() == BlockRegistry.SWAMP_GRASS) {
			world.setBlockState(pos, BlockRegistry.SWAMP_DIRT.getDefaultState());
		}

		TileEntityDugSoil te = BlockGenericDugSoil.getTile(world, pos);
		if(te != null) {
			int compost = te.getCompost();
			int decay = te.getDecay();

			if(state.getBlock() == BlockRegistry.DUG_SWAMP_GRASS) {
				world.setBlockState(pos, BlockRegistry.DUG_SWAMP_DIRT.getDefaultState());
			}

			if(state.getBlock() == BlockRegistry.DUG_PURIFIED_SWAMP_GRASS) {
				world.setBlockState(pos, BlockRegistry.DUG_PURIFIED_SWAMP_DIRT.getDefaultState());
			}

			te = BlockGenericDugSoil.getTile(world, pos);
			if(te != null) {
				te.setCompost(compost);
				te.setDecay(decay);
			}
		}
	}

	public static void spreadGrassTo(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		if(state.getBlock() == BlockRegistry.SWAMP_DIRT) {
			world.setBlockState(pos, BlockRegistry.SWAMP_GRASS.getDefaultState());
		}

		TileEntityDugSoil te = BlockGenericDugSoil.getTile(world, pos);
		if(te != null) {
			int compost = te.getCompost();
			int decay = te.getDecay();

			if(state.getBlock() == BlockRegistry.DUG_SWAMP_DIRT) {
				world.setBlockState(pos, BlockRegistry.DUG_SWAMP_GRASS.getDefaultState());
			}

			if(state.getBlock() == BlockRegistry.DUG_PURIFIED_SWAMP_DIRT) {
				world.setBlockState(pos, BlockRegistry.DUG_PURIFIED_SWAMP_GRASS.getDefaultState());
			}

			te = BlockGenericDugSoil.getTile(world, pos);
			if(te != null) {
				te.setCompost(compost);
				te.setDecay(decay);
			}
		}
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return BlockRegistry.SWAMP_DIRT.getItemDropped(BlockRegistry.SWAMP_DIRT.getDefaultState(), rand, fortune);
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return true;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		DecoratorPositionProvider provider = new DecoratorPositionProvider();
		provider.init(worldIn, worldIn.getBiomeGenForCoords(pos), null, rand, pos.getX(), pos.getY() + 1, pos.getZ());
		provider.setOffsetXZ(-4, 4);
		provider.setOffsetY(-2, 2);

		for(int i = 0; i < 4; i++) {
			DecorationHelper.generateSwampDoubleTallgrass(provider);
			DecorationHelper.generateTallCattail(provider);
			DecorationHelper.generateSwampTallgrassCluster(provider);
			if(rand.nextInt(5) == 0) {
				DecorationHelper.generateCattailCluster(provider);
			}
			if(rand.nextInt(3) == 0) {
				DecorationHelper.generateShootsCluster(provider);
			}
		}
	}

	@Override
	public int getColorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
		return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}