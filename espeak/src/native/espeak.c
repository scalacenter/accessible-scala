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

  const espeak_ERROR result = 
    espeak_Synth(
      c_text,
      strlen(c_text) + 1,
      0,  // position
      POS_CHARACTER,
      0, // end position
      espeakCHARS_UTF8,
      &unique_identifier,
      object
    );

  if (c_text) (*env)->ReleaseStringUTFChars(env, text, c_text);

  switch (result) {
    case EE_OK:             break;
    case EE_INTERNAL_ERROR: printf("espeak_Synth: internal error.\n"); break;
    case EE_BUFFER_FULL:    printf("espeak_Synth: buffer full.\n"); break;
    case EE_NOT_FOUND:      printf("espeak_Synth: not found.\n"); break;
  }
  return;
}

JNIEXPORT void JNICALL Java_ch_epfl_scala_accessible_espeak_Espeak_nativeStop
  (JNIEnv *env, jobject object) {
  espeak_Cancel();
  return;
}
