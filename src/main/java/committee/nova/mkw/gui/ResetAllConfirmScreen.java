package committee.nova.mkw.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ResetAllConfirmScreen extends ConfirmScreen {
    public ResetAllConfirmScreen(BooleanConsumer callback) {
        super(callback, new TranslatableText("gui.confirm.resetAll"), new LiteralText(""));
    }
}
