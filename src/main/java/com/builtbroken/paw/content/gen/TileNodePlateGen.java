package com.builtbroken.paw.content.gen;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.codegen.annotations.EnergyWrapped;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.mc.lib.energy.UniversalEnergySystem;
import com.builtbroken.mc.prefab.energy.EnergyBuffer;
import com.builtbroken.paw.PowerAndWiresMod;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * Pressure plate power generator
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/1/2017.
 */
@TileWrapped(className = "TileWrapperPlateGen")
@EnergyWrapped()
public class TileNodePlateGen extends TileNode implements IEnergyBufferProvider
{
    public static int BUFFER_SIZE = 100;
    public static int POWER_GEN = 10;

    protected IEnergyBuffer buffer;

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

    protected void outputPower()
    {
        TileEntity tile = world().getTileEntity(xi(), yi() - 1, zi());
        if (UniversalEnergySystem.isHandler(tile, ForgeDirection.UP))
        {
            //Test remove, or actual remove call if infinite
            double removed = UniversalEnergySystem.fill(tile, ForgeDirection.UP, getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored(), false);
            //Second remove call removes the actual energy to ensure buffer drained correctly
            UniversalEnergySystem.fill(tile, ForgeDirection.UP, buffer.removeEnergyFromStorage((int) Math.ceil(removed), true), true);
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
        List<Entity> list = world().getEntitiesWithinAABB(Entity.class, stepBounds);
        for (Entity entity : list)
        {
            if (entity != null && entity.isEntityAlive() && entity.onGround)
            {
                somethingAbove = true;
                count++;
            }
        }

        if (!wasSteppedOn)
        {
            getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(POWER_GEN, true);
        }
        wasSteppedOn = somethingAbove;
    }

    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (buffer == null)
        {
            buffer = new EnergyBuffer(BUFFER_SIZE);
        }
        return buffer;
    }
}
