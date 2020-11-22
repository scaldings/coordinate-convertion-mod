package com.scaldings.ccmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;


public class Main implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        registerCommands();
        System.out.print("Initializing CCMod finished!\n");
    }

    public void registerCommands()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("cc").executes(context -> {
            convertCoordinatesCommand();
            return 1;
        })));
    }

    public void convertCoordinatesCommand()
    {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null)
        {
            String dimension = getDimension(player);
            ArrayList<String> coordinates = translateCoordinates(player.getX(), player.getY(), player.getZ(), dimension);
            ArrayList<String> messages = formatMessage(coordinates, dimension);
            for (String message : messages) {sendMessage(message);}
        }
    }

    public String getDimension(ClientPlayerEntity player)
    {
        DimensionType dimension = player.getEntityWorld().getDimension();
        if (dimension.isBedWorking()) {return "Overworld";}
        else
        {
            if (dimension.hasEnderDragonFight()) {return "The End";}
            else {return "The Nether";}
        }
    }

    public ArrayList<String> translateCoordinates(double x, double y, double z, String dimension)
    {
        String regularToNether = round(x / 8, 2) + ":" + round(y, 2) + ":" + round(z / 8, 2) + ":" + "The Nether";
        String netherToRegular = round(x * 8, 2) + ":" + round(y, 2) + ":" + round(z * 8, 2);
        String noChange = round(x, 2) + ":" + round(y, 2) + ":" + round(z, 2);
        ArrayList<String> coordinates = new ArrayList<>();
        if (dimension.equals("Overworld"))
        {
            coordinates.add(noChange + ":" + dimension);
            coordinates.add(regularToNether);
            coordinates.add(noChange + ":" + "The End");
        }
        else if (dimension.equals("The Nether"))
        {
            coordinates.add(netherToRegular + ":" + "Overworld");
            coordinates.add(noChange + ":" + dimension);
            coordinates.add(netherToRegular + ":" + "The End");
        }
        else
        {
            coordinates.add(noChange + ":" + "Overworld");
            coordinates.add(regularToNether);
            coordinates.add(noChange + ":" + dimension);
        }
        return coordinates; // 0 = Overworld, 1 = The Nether, 2 = The End
    }

    public ArrayList<String> formatMessage(ArrayList<String> coordinates, String dimension)
    {
        String[] overworld = coordinates.get(0).split(":");
        String[] theNether = coordinates.get(1).split(":");
        String[] theEnd = coordinates.get(2).split(":");

        String overworldString = "§6§l" + overworld[3] + ": " + "§o§e" + overworld[0] + " / " + overworld[1] + " / "  + overworld[2];
        String theNetherString = "§6§l" + theNether[3] + ": " + "§o§e" + theNether[0] + " / " + theNether[1] + " / "  + theNether[2];
        String theEndString = "§6§l" + theEnd[3] + ": " + "§o§e" + theEnd[0] + " / " + theEnd[1] + " / "  + theEnd[2];

        if (dimension.equals("Overworld"))
        {
            overworldString = "§4§l" + overworld[3] + ": " + "§o§e" + overworld[0] + " / " + overworld[1] + " / "  + overworld[2];
        }
        else if (dimension.equals("The Nether"))
        {
            theNetherString = "§4§l" + theNether[3] + ": " + "§o§e" + theNether[0] + " / " + theNether[1] + " / "  + theNether[2];
        }
        else
        {
            theEndString = "§4§l" + theEnd[3] + ": " + "§o§e" + theEnd[0] + " / " + theEnd[1] + " / "  + theEnd[2];
        }

        ArrayList<String> messages = new ArrayList<>();
        messages.add(overworldString);
        messages.add(theNetherString);
        messages.add(theEndString);

        return messages;
    }

    public void sendMessage(String text)
    {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {player.sendMessage(Text.of(text), false);}
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}