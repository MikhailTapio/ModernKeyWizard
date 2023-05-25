package committee.nova.mkw.gui;

import committee.nova.mkb.api.IKeyBinding;
import committee.nova.mkb.keybinding.KeyModifier;
import committee.nova.mkw.mixin.AccessorKeyBinding;
import committee.nova.mkw.util.KeyBindingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyBindingListWidget extends FreeFormListWidget<KeyBindingListWidget.BindingEntry> implements TickableElement {
    public KeyWizardScreen keyWizardScreen;
    private String currentFilterText = "";
    private String currentCategory = KeyBindingUtil.DYNAMIC_CATEGORY_ALL;

    public KeyBindingListWidget(KeyWizardScreen keyWizardScreen, int top, int left, int width, int height, int itemHeight) {
        super(MinecraftClient.getInstance(), top, left, width, height, itemHeight);
        this.keyWizardScreen = keyWizardScreen;

        for (KeyBinding k : this.client.options.allKeys) {
            this.addEntry(new BindingEntry(k));
        }
        this.setSelected(this.children().get(0));
    }

    @Nullable
    public KeyBinding getSelectedKeyBinding() {
        if (this.getSelectedOrNull() == null) {
            return null;
        }
        return ((BindingEntry) this.getSelectedOrNull()).keyBinding;
    }

    private void updateList() {
        boolean filterUpdate = !this.currentFilterText.equals(this.keyWizardScreen.getFilterText());
        boolean categoryUpdate = !this.currentCategory.equals(this.keyWizardScreen.getSelectedCategory());

        if (categoryUpdate || filterUpdate) {
            if (categoryUpdate) {
                this.currentCategory = this.keyWizardScreen.getSelectedCategory();
            }

            KeyBinding[] bindings = getBindingsByCategory(this.currentCategory);

            if (filterUpdate) {
                this.currentFilterText = this.keyWizardScreen.getFilterText();
                if (!this.currentFilterText.equals("")) {
                    bindings = filterBindings(bindings, this.currentFilterText);
                }
            }

            this.children().clear();
            if (bindings.length > 0) {
                for (KeyBinding k : bindings) {
                    this.addEntry(new BindingEntry(k));
                }
                this.setSelected(this.children().get(0));
            } else {
                this.setSelected(null);
            }
            this.setScrollAmount(0);
        }
    }

    private KeyBinding[] filterBindings(KeyBinding[] bindings, String filterText) {
        KeyBinding[] bindingsFiltered = bindings;
        String keyNameRegex = "<.*>";
        Matcher keyNameMatcher = Pattern.compile(keyNameRegex).matcher(filterText);


        if (keyNameMatcher.find()) {
            String keyNameWithBrackets = keyNameMatcher.group();
            String keyName = keyNameWithBrackets.replace("<", "").replace(">", "");
            filterText = filterText.replace(keyNameWithBrackets, "");
            bindingsFiltered = filterBindingsByKey(bindingsFiltered, keyName);
        }

        if (!filterText.equals("")) {
            bindingsFiltered = filterBindingsByName(bindingsFiltered, filterText);
        }

        return bindingsFiltered;
    }

    private KeyBinding[] filterBindingsByName(KeyBinding[] bindings, String bindingName) {
        String[] words = bindingName.split("\\s+");
        return Arrays.stream(bindings).filter(binding -> {
            boolean flag = true;
            for (String w : words) {
                flag = flag && I18n.translate(binding.getTranslationKey()).toLowerCase().contains(w.toLowerCase());
            }
            return flag;
        }).toArray(KeyBinding[]::new);
    }

    private KeyBinding[] filterBindingsByKey(KeyBinding[] bindings, String keyName) {
        return Arrays.stream(bindings).filter(b -> {
            Text t = ((AccessorKeyBinding) b).getBoundKey().getLocalizedText();
            if (t instanceof TranslatableTextContent) {
                return I18n.translate(((TranslatableTextContent) t).getKey()).equalsIgnoreCase(keyName);
            } else {
                return t.getString().equalsIgnoreCase(keyName);
            }
        }).toArray(KeyBinding[]::new);
    }

    private KeyBinding[] getBindingsByCategory(String category) {
        KeyBinding[] bindings = Arrays.copyOf(this.client.options.allKeys, this.client.options.allKeys.length);
        switch (category) {
            case KeyBindingUtil.DYNAMIC_CATEGORY_ALL:
                return bindings;
            case KeyBindingUtil.DYNAMIC_CATEGORY_CONFLICTS:
                Map<InputUtil.Key, Integer> bindingCounts = KeyBindingUtil.getBindingCountsByKey();
                return Arrays.stream(bindings).filter(b -> bindingCounts.get(((AccessorKeyBinding) b).getBoundKey()) > 1 && ((AccessorKeyBinding) b).getBoundKey().getCode() != -1).toArray(KeyBinding[]::new);
            case KeyBindingUtil.DYNAMIC_CATEGORY_UNBOUND:
                return Arrays.stream(bindings).filter(KeyBinding::isUnbound).toArray(KeyBinding[]::new);
            case KeyBindingUtil.DYNAMIC_CATEGORY_CTRL:
                return Arrays.stream(bindings).filter(k -> ((IKeyBinding) k).getKeyModifier().equals(KeyModifier.CONTROL)).toArray(KeyBinding[]::new);
            case KeyBindingUtil.DYNAMIC_CATEGORY_ALT:
                return Arrays.stream(bindings).filter(k -> ((IKeyBinding) k).getKeyModifier().equals(KeyModifier.ALT)).toArray(KeyBinding[]::new);
            case KeyBindingUtil.DYNAMIC_CATEGORY_SHIFT:
                return Arrays.stream(bindings).filter(k -> ((IKeyBinding) k).getKeyModifier().equals(KeyModifier.SHIFT)).toArray(KeyBinding[]::new);
            case KeyBindingUtil.DYNAMIC_CATEGORY_NONE:
                return Arrays.stream(bindings).filter(k -> ((IKeyBinding) k).getKeyModifier().equals(KeyModifier.NONE)).toArray(KeyBinding[]::new);
            default:
                return Arrays.stream(bindings).filter(b -> b.getCategory().equals(category)).toArray(KeyBinding[]::new);
        }
    }

    @Override
    public void tick() {
        updateList();
    }

    public class BindingEntry extends FreeFormListWidget<KeyBindingListWidget.BindingEntry>.Entry {

        private final KeyBinding keyBinding;

        public BindingEntry(KeyBinding keyBinding) {
            this.keyBinding = keyBinding;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            client.textRenderer.drawWithShadow(matrices, Text.translatable(this.keyBinding.getTranslationKey()), x, y, 0xFFFFFFFF);
            int color = 0xFF999999;
            client.textRenderer.drawWithShadow(matrices, this.keyBinding.getBoundKeyLocalizedText(), x, y + client.textRenderer.fontHeight + 5, color);
        }

    }

    @Override
    public void appendNarrations(NarrationMessageBuilder var1) {
        // TODO Auto-generated method stub

    }


}
