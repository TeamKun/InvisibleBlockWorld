package net.kunmc.lab.invisibleblockworld;

import net.kunmc.lab.invisibleblockworld.packet.RevealedBlockPosMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(InvisibleBlockWorld.ModId)
public class InvisibleBlockWorld {
    public static final String ModId = "invisibleblockworld";
    public static final SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ModId, ModId + "_channel"))
            .clientAcceptedVersions(a -> true)
            .serverAcceptedVersions(a -> true)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static Set<BlockPos> revealedBlockPosSet = new HashSet<>();

    public InvisibleBlockWorld() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        channel.registerMessage(0, RevealedBlockPosMessage.class, RevealedBlockPosMessage::encodeMessage, RevealedBlockPosMessage::decodeMessage, RevealedBlockPosMessage::receiveMessage);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientPlayerUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntity() == null || !(e.getEntity() instanceof PlayerEntity)) {
            return;
        }

        Entity p = e.getEntity();
        BlockPos pos = p.getPosition();
        BlockPos downPos = pos.down();

        revealedBlockPosSet.add(pos);
        revealedBlockPosSet.add(downPos);

        World w = p.getEntityWorld();
        BlockState blockState = w.getBlockState(pos);
        BlockState downBlockState = w.getBlockState(downPos);
        w.notifyBlockUpdate(pos, blockState, blockState, 16);
        w.notifyBlockUpdate(downPos, downBlockState, downBlockState, 16);
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
        channel.send(PacketDistributor.ALL.noArg(), new RevealedBlockPosMessage(revealedBlockPosSet));
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public void onServerPlayerUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntity() == null || !(e.getEntity() instanceof PlayerEntity)) {
            return;
        }

        Entity p = e.getEntity();
        BlockPos pos = p.getPosition();
        BlockPos downPos = pos.down();

        revealedBlockPosSet.add(pos);
        revealedBlockPosSet.add(downPos);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("InvisibleBlockWorld", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
        }
    }
}
