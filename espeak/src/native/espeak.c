#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdbool.h>
#include <jni.h>
#include <string.h>
#include <espeak/speak_lib.h>

bool isInitialized = false;

#define BUFFER_SIZE_IN_MILLISECONDS 1000

JNIEXPORT void JNICALL Java_ch_epfl_scala_accessible_espeak_Espeak_nativeSynthesize
  (JNIEnv *env, jobject object, jstring text) {

  if (!isInitialized) {
    espeak_Initialize(AUDIO_OUTPUT_PLAYBACK, BUFFER_SIZE_IN_MILLISECONDS, NULL, 0);
    isInitialized = true;
  }

  const char *c_text = text ? (*env)->GetStringUTFChars(env, text, NULL) : NULL;
  unsigned int unique_identifier;
  espeak_Synth(
      c_text,
      strlen(c_text),
      0,
      POS_CHARACTER,
      0,
      espeakCHARS_UTF8,
      &unique_identifier,
      NULL
  );
  espeak_Synchronize();
  return;
}

JNIEXPORT void JNICALL Java_ch_epfl_scala_accessible_espeak_Espeak_nativeStop
  (JNIEnv *env, jobject object) {
  espeak_Cancel();
  return;
}
