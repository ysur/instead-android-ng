/*
    SDL_android_main.c, placed in the public domain by Sam Lantinga  3/13/14
    Converted to cpp by Anton P. Kolosov because of compilation errors otherwise
*/
/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>
#include <unistd.h>

/* Called before SDL_main() to initialize JNI bindings in SDL library */
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);

/* Start up the SDL app */
extern "C" void Java_com_silentlexx_instead_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jstring jpath, jstring jres, jstring jgame, jstring jidf)
{
    /* This interface could expand with ABI negotiation, calbacks, etc. */
    SDL_Android_Init(env, cls);

    SDL_SetMainReady();

    /* Run the application code! */
    int status;
    char *argv[6];
    int n = 1;
    if (jpath != NULL) {
        const char *path = env->GetStringUTFChars(jpath, 0);
        chdir(SDL_strdup(path));
        env->ReleaseStringUTFChars(jpath, path);
    }


    argv[0] = SDL_strdup("sdl-instead");

    if (jres != NULL) {
        const char *res = env->GetStringUTFChars(jres, 0);
        n = 3;
        argv[1] = SDL_strdup("-mode");
        argv[2] = SDL_strdup(res);
        env->ReleaseStringUTFChars(jres, res);

        if (jidf != NULL) {
            const char *idf = env->GetStringUTFChars(jidf, 0);
            n = 4;
            argv[3] = SDL_strdup(idf);
            argv[4] = NULL;
            env->ReleaseStringUTFChars(jidf, idf);
        } else if (jgame != NULL) {
            const char *game = env->GetStringUTFChars(jgame, 0);
            n = 5;
            argv[3] = SDL_strdup("-game");
            argv[4] = SDL_strdup(game);
            argv[5] = NULL;
            env->ReleaseStringUTFChars(jgame, game);
        } else {
            argv[3] = NULL;
        }
    } else {
        argv[1] = NULL;
    }

    status = SDL_main(n, argv);

/*
No freeing in initial SDL_android_main.c
    int i;
    for (i = 0; i < n; ++i) {
        free(argv[i]);
    }
*/

    /* Do not issue an exit or the whole application will terminate instead of just the SDL thread */
    /* exit(status); */
}

/* vi: set ts=4 sw=4 expandtab: */
