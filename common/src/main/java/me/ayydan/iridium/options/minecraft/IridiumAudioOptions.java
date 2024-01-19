package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingListController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IridiumAudioOptions extends IridiumMinecraftOptions
{
    private final List<Option<?>> volumeOptions = new ArrayList<>();
    private final List<Option<?>> soundOptions = new ArrayList<>();

    private ConfigCategory audioOptionsCategory;

    public IridiumAudioOptions()
    {
        super(null);
    }

    @Override
    public void create()
    {
        this.createVolumeOptions();
        this.createSoundOptions();

        OptionGroup volumeOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.volume"))
                .options(this.volumeOptions)
                .build();

        OptionGroup soundOptionsGroup = OptionGroup.createBuilder()
                .name(Text.translatable("iridium.options.group.sound"))
                .options(this.soundOptions)
                .build();

        this.audioOptionsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("iridium.options.category.audio"))
                .groups(Lists.newArrayList(volumeOptionsGroup, soundOptionsGroup))
                .build();
    }

    @Override
    public ConfigCategory getYACLCategory()
    {
        return this.audioOptionsCategory;
    }

    private void createVolumeOptions()
    {
        Option<Double> masterVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.master"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.masterVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.MASTER)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();


        Option<Double> musicVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.music"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.musicVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.MUSIC)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> jukeboxAndNoteBlocksVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.record"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.recordVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.RECORDS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> weatherVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.weather"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.weatherVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.WEATHER)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> blocksVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.block"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.blockVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.BLOCKS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> hostileCreaturesVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.hostile"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.hostileCreaturesVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.HOSTILE)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> friendlyCreaturesVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.neutral"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.friendlyCreaturesVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.NEUTRAL)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> playersVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.player"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.playersVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.PLAYERS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> ambienceVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.ambient"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.ambienceVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.AMBIENT)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Option<Double> voiceVolumeOption = Option.<Double>createBuilder()
                .name(Text.translatable("soundCategory.voice"))
                .description(OptionDescription.of(Text.translatable("iridium.options.volume.voiceVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundVolumeOption(SoundCategory.VOICE)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, this::getPercentValueText))
                .build();

        Collections.addAll(this.volumeOptions, masterVolumeOption, musicVolumeOption, jukeboxAndNoteBlocksVolumeOption, weatherVolumeOption, blocksVolumeOption, hostileCreaturesVolumeOption, friendlyCreaturesVolumeOption, playersVolumeOption, ambienceVolumeOption, voiceVolumeOption);
    }

    private void createSoundOptions()
    {
        Option<String> audioOutputDeviceOption = Option.<String>createBuilder()
                .name(Text.translatable("iridium.options.sound.outputDevice"))
                .description(OptionDescription.of(Text.translatable("iridium.options.sound.outputDevice.description")))
                .binding(Binding.minecraft(this.client.options.getSoundDevice()))
                .customController(option -> new CyclingListController<>(option, this.client.getSoundManager().getAudioDevices(), audioDeviceName ->
                {
                    if (GameOptions.DEFAULT_AUDIO_DEVICE.equals(audioDeviceName))
                        return Text.translatable("options.audioDevice.default");

                    if (audioDeviceName.startsWith("OpenAL Soft on "))
                        return Text.literal(audioDeviceName.substring(SoundSystem.OPENAL_SOFT_ON_LENGTH));

                    return Text.literal(audioDeviceName);
                }))
                .build();

        Option<Boolean> directionAudioOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.directionalAudio"))
                .description(OptionDescription.of(Text.translatable("iridium.options.sound.directionalAudio.description")))
                .binding(Binding.minecraft(this.client.options.getDirectionalAudio()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> showSubtitlesOption = Option.<Boolean>createBuilder()
                .name(Text.translatable("options.showSubtitles"))
                .description(OptionDescription.of(Text.translatable("iridium.options.sound.showSubtitles.description")))
                .binding(Binding.minecraft(this.client.options.getShowSubtitles()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.soundOptions, audioOutputDeviceOption, directionAudioOption, showSubtitlesOption);
    }

    private Text getPercentValueText(double value)
    {
        return Text.literal((int) (value * 100.0d) + "%");
    }
}
