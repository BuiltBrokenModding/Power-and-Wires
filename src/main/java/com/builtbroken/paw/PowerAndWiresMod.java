package com.builtbroken.paw;

import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.mod.ModCreativeTab;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.creativetab.CreativeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod focused towards power movement, conversion, and storage
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/30/2017.
 */
@Mod(modid = PowerAndWiresMod.DOMAIN, name = "Power and Wires", version = PowerAndWiresMod.VERSION)
public class PowerAndWiresMod extends AbstractMod
{
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

    public static final String DOMAIN = "powerandwires";

    public static final String PREFX = DOMAIN + ":";

    /** Information output thing */
    public static final Logger logger = LogManager.getLogger("Power and Wires");

    @SidedProxy(clientSide = "com.builtbroken.paw.client.ClientProxy", serverSide = "com.builtbroken.paw.CommonProxy")
    public static CommonProxy proxy;

    public static CreativeTabs creativeTab;

    @Mod.Instance(DOMAIN)
    public static PowerAndWiresMod instance;

    public PowerAndWiresMod()
    {
        super(PowerAndWiresMod.DOMAIN);
        creativeTab = new ModCreativeTab(PowerAndWiresMod.DOMAIN);
        getManager().setTab(creativeTab);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event)
    {
        super.loadComplete(event);
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }
}
