package committee.nova.mkw.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.Text;

public class ResetAllConfirmScreen extends ConfirmScreen {
    public ResetAllConfirmScreen(BooleanConsumer callback) {
        super(callback, Text.translatable("gui.confirm.resetAll"), Text.empty());
    }
}
