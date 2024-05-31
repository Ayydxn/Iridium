package me.ayydan.iridium.options.minecraft;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import dev.isxander.yacl3.gui.controllers.cycling.CyclingListController;
import dev.isxander.yacl3.gui.controllers.slider.DoubleSliderController;
import me.ayydan.iridium.options.util.OptionsUtil;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

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
                .name(Component.translatable("iridium.options.group.volume"))
                .options(this.volumeOptions)
                .build();

        OptionGroup soundOptionsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("iridium.options.group.sound"))
                .options(this.soundOptions)
                .build();

        this.audioOptionsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("iridium.options.category.audio"))
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
                .name(Component.translatable("soundCategory.master"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.masterVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.MASTER)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();


        Option<Double> musicVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.music"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.musicVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.MUSIC)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> jukeboxAndNoteBlocksVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.record"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.recordVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.RECORDS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> weatherVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.weather"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.weatherVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.WEATHER)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> blocksVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.block"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.blockVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.BLOCKS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> hostileCreaturesVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.hostile"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.hostileCreaturesVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.HOSTILE)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> friendlyCreaturesVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.neutral"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.friendlyCreaturesVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> playersVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.player"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.playersVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.PLAYERS)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> ambienceVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.ambient"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.ambienceVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.AMBIENT)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Option<Double> voiceVolumeOption = Option.<Double>createBuilder()
                .name(Component.translatable("soundCategory.voice"))
                .description(OptionDescription.of(Component.translatable("iridium.options.volume.voiceVolume.description")))
                .binding(Binding.minecraft(this.client.options.getSoundSourceOptionInstance(SoundSource.VOICE)))
                .customController(option -> new DoubleSliderController(option, 0.0d, 1.0d, 0.01d, OptionsUtil::getPercentValueText))
                .build();

        Collections.addAll(this.volumeOptions, masterVolumeOption, musicVolumeOption, jukeboxAndNoteBlocksVolumeOption, weatherVolumeOption, blocksVolumeOption, hostileCreaturesVolumeOption, friendlyCreaturesVolumeOption, playersVolumeOption, ambienceVolumeOption, voiceVolumeOption);
    }

    private void createSoundOptions()
    {
        Option<String> audioOutputDeviceOption = Option.<String>createBuilder()
                .name(Component.translatable("iridium.options.sound.outputDevice"))
                .description(OptionDescription.of(Component.translatable("iridium.options.sound.outputDevice.description")))
                .binding(Binding.minecraft(this.client.options.soundDevice()))
                .customController(option -> new CyclingListController<>(option, this.client.getSoundManager().getAvailableSoundDevices(), audioDeviceName ->
                {
                    if (Options.DEFAULT_SOUND_DEVICE.equals(audioDeviceName))
                        return Component.translatable("options.audioDevice.default");

                    if (audioDeviceName.startsWith("OpenAL Soft on "))
                        return Component.literal(audioDeviceName.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH));

                    return Component.literal(audioDeviceName);
                }))
                .build();

        Option<Boolean> directionAudioOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.directionalAudio"))
                .description(OptionDescription.of(Component.translatable("iridium.options.sound.directionalAudio.description")))
                .binding(Binding.minecraft(this.client.options.directionalAudio()))
                .customController(BooleanController::new)
                .build();

        Option<Boolean> showSubtitlesOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("options.showSubtitles"))
                .description(OptionDescription.of(Component.translatable("iridium.options.sound.showSubtitles.description")))
                .binding(Binding.minecraft(this.client.options.showSubtitles()))
                .customController(BooleanController::new)
                .build();

        Collections.addAll(this.soundOptions, audioOutputDeviceOption, directionAudioOption, showSubtitlesOption);
    }
}
