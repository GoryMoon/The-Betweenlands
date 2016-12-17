package thebetweenlands.common.entity.mobs;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.client.render.particle.ParticleFactory.ParticleArgs;
import thebetweenlands.common.item.misc.ItemMisc.EnumItemMisc;
import thebetweenlands.common.registries.SoundRegistry;

public class EntityTarminion extends EntityTameable implements IEntityBL {
	public static final IAttribute MAX_TICKS_ATTRIB = (new RangedAttribute(null, "bl.maxAliveTicks", 7200.0D, 0, Integer.MAX_VALUE)).setDescription("Maximum ticks until the Tar Minion despawns");

	private int despawnTicks = 0;

	public EntityTarminion(World world) {
		super(world);
		this.setSize(0.3F, 0.5F);
		this.experienceValue = 0;
		this.isImmuneToFire = true;
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.7D, true));
		this.tasks.addTask(2, new EntityAIFollowOwner(this, 0.7D, 3.0F, 40.0F));
		this.tasks.addTask(3, new EntityAIWander(this, 0.5D));

		this.targetTasks.addTask(0, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(1, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.85D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9D);

		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.getAttributeMap().registerAttribute(MAX_TICKS_ATTRIB);
	}

	@Override
	public boolean canDespawn() {
		return false;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block state) {
		if(this.rand.nextInt(10) == 0) {
			this.playSound(SoundRegistry.TAR_BEAST_STEP, 0.8F, 1.5F);
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundRegistry.SQUISH;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.worldObj.isRemote) {
			this.despawnTicks++;
			if(this.despawnTicks > this.getEntityAttribute(MAX_TICKS_ATTRIB).getAttributeValue()) {
				this.attackEntityFrom(DamageSource.generic, this.getMaxHealth());
			}
		}

		if(this.worldObj.isRemote && this.ticksExisted % 20 == 0) {
			this.spawnParticles(this.worldObj, this.posX, this.posY, this.posZ, this.rand);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("despawnTicks", this.despawnTicks);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.despawnTicks = nbt.getInteger("despawnTicks");
	}

	@Override
	public void setDead() {
		if(!this.isDead) {
			if(this.getAttackTarget() != null) {
				if(this.worldObj.isRemote) {
					for(int i = 0; i < 200; i++) {
						Random rnd = this.worldObj.rand;
						float rx = rnd.nextFloat() * 1.0F - 0.5F;
						float ry = rnd.nextFloat() * 1.0F - 0.5F;
						float rz = rnd.nextFloat() * 1.0F - 0.5F;
						Vec3d vec = new Vec3d(rx, ry, rz);
						vec = vec.normalize();
						BLParticles.SPLASH_TAR.spawn(this.worldObj, this.posX + rx + 0.1F, this.posY + ry, this.posZ + rz + 0.1F, ParticleArgs.get().withMotion(vec.xCoord * 0.4F, vec.yCoord * 0.4F, vec.zCoord * 0.4F));
					}
				} else {
					for(int i = 0; i < 8; i++) {
						this.playSound(SoundRegistry.TAR_BEAST_STEP, 1F, (this.rand.nextFloat() * 0.4F + 0.8F) * 0.8F);
					}
					List<EntityCreature> affectedEntities = (List<EntityCreature>)this.worldObj.getEntitiesWithinAABB(EntityCreature.class, this.getEntityBoundingBox().expand(5.25F, 5.25F, 5.25F));
					for(EntityCreature e : affectedEntities) {
						if(e == this || e.getDistanceToEntity(this) > 5.25F || !e.canEntityBeSeen(this) || e instanceof EntityTarminion) continue;
						double dst = e.getDistanceToEntity(this);
						e.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 4);
						e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, (int)(20 + (1.0F - dst / 5.25F) * 150), 1));
					}
				}
			}

			if(!this.worldObj.isRemote) {
				this.entityDropItem(EnumItemMisc.INANIMATE_TARMINION.create(1), 0F);
			}

			this.playSound(SoundRegistry.TAR_BEAST_STEP, 2.5F, 0.5F);

			if(this.worldObj.isRemote) {
				for(int i = 0; i < 100; i++) {
					Random rnd = worldObj.rand;
					float rx = rnd.nextFloat() * 1.0F - 0.5F;
					float ry = rnd.nextFloat() * 1.0F - 0.5F;
					float rz = rnd.nextFloat() * 1.0F - 0.5F;
					Vec3d vec = new Vec3d(rx, ry, rz);
					vec = vec.normalize();
					BLParticles.SPLASH_TAR.spawn(this.worldObj, this.posX + rx + 0.1F, this.posY + ry, this.posZ + rz + 0.1F, ParticleArgs.get().withMotion(vec.xCoord * 0.2F, vec.yCoord * 0.2F, vec.zCoord * 0.2F));
				}
			}
		}
		super.setDead();
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		super.attackEntityAsMob(entity);
		return attack(entity);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(source.getEntity() instanceof EntityCreature) {
			this.attack(source.getEntity());
		}
		return super.attackEntityFrom(source, amount);
	}

	protected boolean attack(Entity entity) {
		if (!this.worldObj.isRemote) {
			if (this.onGround) {
				double dx = entity.posX - this.posX;
				double dz = entity.posZ - this.posZ;
				float dist = MathHelper.sqrt_double(dx * dx + dz * dz);
				this.motionX = dx / dist * 0.2D + this.motionX * 0.2D;
				this.motionZ = dz / dist * 0.2D + this.motionZ * 0.2D;
				this.motionY = 0.3D;
			}
			DamageSource damageSource;
			EntityLivingBase owner = this.getOwner();
			if(owner != null) {
				damageSource = new EntityDamageSourceIndirect("mob", this, owner);
			} else {
				damageSource = DamageSource.causeMobDamage(this);
			}
			entity.attackEntityFrom(damageSource, (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
			this.playSound(SoundRegistry.TAR_BEAST_STEP, 1.0F, 2.0F);
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, worldObj.getDifficulty().getDifficultyId() * 50, 0));
			return true;
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, double x, double y, double z, Random rand) {
		for (int count = 0; count < 3; ++count) {
			double a = Math.toRadians(renderYawOffset);
			double offSetX = -Math.sin(a) * 0D + rand.nextDouble() * 0.1D - rand.nextDouble() * 0.1D;
			double offSetZ = Math.cos(a) * 0D + rand.nextDouble() * 0.1D - rand.nextDouble() * 0.1D;
			BLParticles.TAR_BEAST_DRIP.spawn(world , x + offSetX, y + 0.1D, z + offSetZ);
		}
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entity) {
		return null;
	}
}