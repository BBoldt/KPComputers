package kpc.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.common.registry.GameRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kpc.api.fs.MountRegistry;
import kpc.common.blocks.BlockComputer;
import kpc.common.computer.fs.Ext9001ResourceMount;
import kpc.common.computer.fs.Ext9001UsrMount;
import kpc.common.core.ClientComputerRegistry;
import kpc.common.core.ServerComputerRegistry;
import kpc.common.net.KPCPacket;
import kpc.common.net.KPCPacketHandler;
import kpc.common.tile.TileEntityComputer;
import net.minecraft.block.Block;

import java.io.IOException;

@Mod(
            modid = KPComputers.modid,
            name = "KPComputers",
            version = "0.0.0.0"
)
public final class KPComputers{
    @Mod.Instance(KPComputers.modid)
    public static KPComputers instance;

    @SidedProxy(
                       clientSide = "kpc.client.ClientProxy",
                       serverSide = "kpc.common.CommonProxy"
    )
    public static CommonProxy proxy;
    public static FMLEventChannel channel;

    public static final String modid = "kpc";
    public static final Block blockComputer = new BlockComputer();
    public static final ServerComputerRegistry serverComputerRegistry = new ServerComputerRegistry();
    public static final ClientComputerRegistry clientComputerRegistry = new ClientComputerRegistry();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(KPComputers.modid);
        channel.register(new KPCPacketHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        GameRegistry.registerBlock(blockComputer, "blockComputer");
        GameRegistry.registerTileEntity(TileEntityComputer.class, "tileComputer");

        NetworkRegistry.INSTANCE.registerGuiHandler(KPComputers.instance, new KPGuiHandler());

        KPComputers.proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e){
        FMLCommonHandler.instance().bus().register(new KPCTickHandler());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent e){
        try {
            MountRegistry.mount("/", new Ext9001ResourceMount("root"));
            MountRegistry.mount("/include", new Ext9001ResourceMount("include"));
            MountRegistry.mount("/bin", new Ext9001ResourceMount("bin"));
            MountRegistry.mount("/usr", new Ext9001UsrMount());
        } catch(IOException e1){
            throw new RuntimeException("/usr couldn't be mounted", e1);
        }
    }

    public static FMLProxyPacket encode(KPCPacket packet){
        ByteBuf buffer = Unpooled.buffer();
        packet.toBytes(buffer);
        return new FMLProxyPacket(buffer, KPComputers.modid);
    }
}