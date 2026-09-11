package com.Alzimer Ahmed.TypeCraft.voice;

import com.Alzimer Ahmed.TypeCraft.voice.IVoiceCallback;
import com.Alzimer Ahmed.TypeCraft.voice.VoiceEngineInfo;
import com.Alzimer Ahmed.TypeCraft.voice.ModelState;
import com.Alzimer Ahmed.TypeCraft.voice.ModelImportRequest;
import com.Alzimer Ahmed.TypeCraft.voice.VoiceSessionConfig;
import android.os.ParcelFileDescriptor;

interface IVoiceEngine {
    VoiceEngineInfo getInfo();
    ModelState getModelState(String engineType);
    oneway void importModel(in ModelImportRequest request);
    void unloadModel(String engineType);
    void deleteModel(String engineType);
    void startSession(
        in VoiceSessionConfig config,
        in ParcelFileDescriptor audioInput,
        IVoiceCallback callback
    );
    void stopSession();
    void cancelSession();
    void release();
}
