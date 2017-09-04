package com.builtbroken.paw.content.gen;

import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.tile.ConnectionType;
import com.builtbroken.mc.api.tile.ITileConnection;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.framework.block.imp.IActivationListener;
import com.builtbroken.mc.framework.energy.UniversalEnergySystem;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.energy.EnergyBufferWrapper;
import com.builtbroken.mc.prefab.tile.logic.TilePowerNode;
import com.builtbroken.paw.PowerAndWiresMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * Pressure plate power generator
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/1/2017.
 */
@TileWrapped(className = "TileWrapperPlateGen", wrappers = "Energy")
public class TileNodePlateGen extends TilePowerNode implements IEnergyBufferProvider, IActivationListener, ITileConnection
{
    public static int BUFFER_SIZE = 100;
    public static int POWER_GEN = 10;

    boolean wasSteppedOn = false;

    AxisAlignedBB stepBounds;

    public TileNodePlateGen()
    {
        super("gen.plate", PowerAndWiresMod.DOMAIN);
    }

    @Override
    public void update(long ticks)
    {
        super.update(ticks);
        if (isServer())
        {
            outputPower();
            checkForStep();
            //TODO generate power based on fall distance of entity allowing mob farms to generate lots of power
        }
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (isServer())
        {
            player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("text.ue.power.amount").replace("%1", "" + getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored()).replace("%2", "" + BUFFER_SIZE)));
        }
        return false;
    }

    @Override
    protected EnergyBufferWrapper createEnergySideWrapper()
    {
        return new EnergyBufferWrapper(getEnergyBuffer(ForgeDirection.UNKNOWN)).disableInput();
    }

    @Override
    public int getEnergyBufferSize()
    {
        return BUFFER_SIZE;
    }

    protected void outputPower()
    {
        if (getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored() > 0)
        {
            TileEntity tile = world().unwrap().getTileEntity(xi(), yi() - 1, zi());
            if (UniversalEnergySystem.isHandler(tile, ForgeDirection.UP))
            {
                //test fill on target
                int filled = (int) Math.ceil(UniversalEnergySystem.fill(tile, ForgeDirection.UP, getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored(), false));
                //Do remove based on test result
                int removed = getEnergyBuffer(ForgeDirection.UNKNOWN).removeEnergyFromStorage(filled, true);
                //Do actual fill based on how much was removed from battery
                double aFill = Math.ceil(UniversalEnergySystem.fill(tile, ForgeDirection.UP, removed, true));
                if (aFill != filled && Engine.runningAsDev)
                {
                    System.out.println("Error: final fill did not match test fill");
                }
            }
        }
    }

    protected void checkForStep()
    {
        if (stepBounds == null)
        {
            stepBounds = AxisAlignedBB.getBoundingBox(xi(), yi(), zi(), xi() + 1, yi() + 1, zi() + 1);
        }

        boolean somethingAbove = false;
        int count = 0;
        List<Entity> list = world().unwrap().getEntitiesWithinAABB(Entity.class, stepBounds);
        for (Entity entity : list)
        {
            if (entity != null && entity.isEntityAlive() && entity.onGround)
            {
                somethingAbove = true;
                count++;
            }
        }

        if (!wasSteppedOn && somethingAbove)
        {
            getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(Math.min(POWER_GEN * count, 100), true);
        }
        wasSteppedOn = somethingAbove;
    }

    @Override
    public boolean canConnect(TileEntity connection, ConnectionType type, ForgeDirection from)
    {
        return from == ForgeDirection.DOWN && super.canConnect(connection, type, from);
    }
}
