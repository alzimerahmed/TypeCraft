package com.alzimerahmed84.typecraft.voice;

import com.alzimerahmed84.typecraft.voice.IVoiceCallback;
import com.alzimerahmed84.typecraft.voice.VoiceEngineInfo;
import com.alzimerahmed84.typecraft.voice.ModelState;
import com.alzimerahmed84.typecraft.voice.ModelImportRequest;
import com.alzimerahmed84.typecraft.voice.VoiceSessionConfig;
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
